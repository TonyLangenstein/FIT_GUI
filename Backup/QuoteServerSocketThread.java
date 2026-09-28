/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PESGUI;

import org.apache.logging.log4j.Level;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.json.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author thela
 */
@ServerEndpoint("/PES_ServerSocket")
public class QuoteServerSocketThread implements Runnable {  

   private static javax.swing.JTextArea clientWindow;   
   private static java.net.ServerSocket echoServer = null;
   private static DataInputStream is;
   private static DataOutputStream os;
   private static Socket serverClientSocket = null;
   private static Boolean connected = false;
   private static Boolean waiting4Ack = false;
   private static Log4J2AsyncLogger LogData =new Log4J2AsyncLogger();
   private static List<String> longMessage = new ArrayList<>();
   private static boolean multiPartMessage = false;
   private static int msgCnt = 0;
   private static boolean sendAck = false;
   
//   private static int longMessageCount = 0;
   private static int maxBufferSize = 100;  // 50000 caused exception.  Going with 40k.
   
   public QuoteServerSocketThread( javax.swing.JTextArea cw ){
      clientWindow=cw;
   }
   
  public void run() { 
    Thread.currentThread().setName("QuoteServerSocketThread");
    Thread.currentThread().setPriority(4);
    InitSocket();
    while (connected) {
        try {
            WriteSocket();
            ReadSocket();
            Utility.hybridPrecisionWait(100);
            if (TT_PESGUIData.getShutDown() == true) {
                break;
            }
        } catch (Exception e) {
           LogData.LogThis(Level.ERROR, "Run Exception:  " + e );
           clientWindow.append("Exception in Run method in Quote Socket Thread!");
        }
    }
    CloseSocket();
  }    

   // Tested 5/20/24
   synchronized private static  void SymbolUpdate(String message) {
       String[] data = message.split(TT_PESGUIData.DataSeparator);
       try {
            LogData.LogThis(Level.INFO, "Here is data " + data[0]);
            JSONArray Symbols = new JSONArray( data[0].toString() );
            TT_StreamingQuotesData.SetSymbols( Symbols );
            JSONArray SymbolStats = new JSONArray( data[1].toString() );
            TT_StreamingQuotesData.SetSymbolStats( SymbolStats );
       } catch (Exception e) {
            LogData.LogThis(Level.ERROR, "Exception in SymbolUpdate: " + e + "Here is data " + data[1]);  // 39980
            String x = data[1];
            LogData.LogThis(Level.ERROR, "Exception in SymbolUpdate: " + e + "Here is data " + x.substring(39981)); 
            LogData.LogThis(Level.ERROR, "Exception in SymbolUpdate: " + e + "Here is data " + x.substring(39980)); 
            LogData.LogThis(Level.ERROR, "Exception in SymbolUpdate: " + e + "Here is data " + x.substring(39979)); 
            LogData.LogThis(Level.ERROR, "Exception in SymbolUpdate: " + e + "Here is message " + message);
            clientWindow.append("\n" + "Exception in SymbolUpdate." + "Here is data " + data[1] );
            if (e.toString().contains("character ")) {
            // Format here is:  "TokenUpdate,<refresh token>,<access token>"
              String[] SplitData = e.toString().split("character ");
              String[] SplitSplitData = SplitData[1].split(" ");
              int theChar = Integer.parseInt(SplitSplitData[0]) - 1;
              LogData.LogThis(Level.ERROR, "                Exception in data starts at: " + SplitSplitData[0] + "; Here is data from that character to end " + data[1].substring(theChar));
            }
       }
   }
      
  synchronized private static void ReadSocket() {
     try {
         String fullMessage = "";
         if ( is.available() > 0 ) {
            String data = is.readUTF();
            LogData.LogThis(Level.INFO, "Got msg ... " + data + " ...\n" );
            if ( waiting4Ack && ( data.contains( TT_PESGUIData.ClientAck ) ) ) {
                TT_PESGUIData.UnlockQuoteSocketData();
                waiting4Ack = false;
            }
            // Incoming messages will be the Symbols and SymbolStats data
            if (data.contains(TT_PESGUIData.QuotesReady)) {
                TT_PESGUIData.setQuotesSetup(true);
                os.writeUTF( TT_PESGUIData.ServerAck );
            }
            if ( data.contains( TT_PESGUIData.SymbolData ) ) {
                // Since only thing dealing with I will process the data right here instead of in a separate thread.
                String[] message = data.split(TT_PESGUIData.SocketStringSeparator);
                if (data.contains("MORE TO COME")) {
//                if (message.length > 2) {
                    // We have a multipart message as it is too long for Socket Buffer
                    multiPartMessage = true;
                    longMessage.add(message[2]);
                } else {
                    if (multiPartMessage) {
                        longMessage.add(message[2]);
                        for (int i = 0; i < longMessage.size(); i++) {
                            fullMessage = fullMessage + longMessage.get(i);
                        }
                        SymbolUpdate(fullMessage);
                        multiPartMessage = false;
                        longMessage.clear();
                    } else {
                        SymbolUpdate(message[2]);
                    }
                }
                os.writeUTF( TT_PESGUIData.ServerAck );
            } 
         }
     } catch (Exception e) {
         LogData.LogThis(Level.ERROR, "Read Exception:  " + e );
         clientWindow.append("Exception in ReadSocket in Socket Thread!");
     }
 }

  synchronized private static void WriteSocket() {
     try {
         if ( TT_PESGUIData.QuotesDataReadyToSend() ) {
             String Msg = TT_PESGUIData.GetQuotesSocketDataToSend();
             if ( Msg.length() >= maxBufferSize ) {
                 String putBack = TT_PESGUIData.SymbolData + TT_PESGUIData.SocketStringSeparator + Msg.substring(maxBufferSize-1);
                 Msg = Msg.substring(0,maxBufferSize-1) + TT_PESGUIData.SocketStringSeparator + "MORE TO COME";
                 TT_PESGUIData.SetQuotesSocketData(putBack);
             } else {
                 TT_PESGUIData.ClearQuotesSocketData();
             }
             waiting4Ack = true;
             os.writeUTF(Msg);
             LogData.LogThis(Level.INFO, "Message sent ... " + Msg + " ...\n" );
         }
     } catch (Exception e) {
         LogData.LogThis(Level.ERROR, "Write Exception:  " + e );
         clientWindow.append("Exception in WriteSocket in Quote Socket Thread!");
     }
 }
  
  private static void InitSocket() {
    // Try to open a server socket on port 9999
    // Note that we can't choose a port less than 1023 if we are not
    // privileged users (root)
        try {
           echoServer = new java.net.ServerSocket(Config.QuotesSocketNumber );
           clientWindow.append("This program is waiting for the Quote client socket to start ..." + "\n");
           LogData.LogThis(Level.INFO, "This program is waiting for the Quote client socket to start ..." + "\n" );
           serverClientSocket = echoServer.accept();
           is = new DataInputStream(serverClientSocket.getInputStream());
           os = new DataOutputStream(serverClientSocket.getOutputStream());
           connected = true;
           LogData.LogThis(Level.INFO, "Connected to Quote client socket!" + "\n" );
           clientWindow.append("Connected to Quote client socket!" + "\n");
        }
        catch (Exception e) {
           LogData.LogThis(Level.ERROR, "Init Exception:  " + e );
           clientWindow.append("Exception in InitSocket in Quote Socket Thread!");
        }   
    // Create a socket object from the ServerSocket to listen and accept 
    // connections.
    // Open input and output streams
   }

  private static void CloseSocket() {
      try {
        // clean up:
        // close the output stream
        // close the input stream
        // close the socket
        os.writeUTF( TT_PESGUIData.Close );
        clientWindow.append("The send Quote socket connection is closed!" + "\n");
        LogData.LogThis(Level.INFO, "The send Quote socket connection is closed!" + "\n" );
        os.close();
        is.close();
        connected = false;
      } catch (UnknownHostException e) {
         LogData.LogThis(Level.ERROR, "Trying to connect to unknown host: " + e );
         clientWindow.append("Exception in CloseSocket in Quote Socket Thread!");
      } catch (IOException e) {
         LogData.LogThis(Level.ERROR, "C;pse IO Exception:  " + e );
         clientWindow.append("Exception in CloseSocket in Quote Socket Thread!");
      }
  }
}
