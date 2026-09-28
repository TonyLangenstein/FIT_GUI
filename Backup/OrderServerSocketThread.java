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
public class OrderServerSocketThread implements Runnable {  

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
//   private static int longMessageCount = 0;
   private static int maxBufferSize = 40000;  // 50000 caused exception.  Going with 40k.
   
   public OrderServerSocketThread( javax.swing.JTextArea cw ){
      clientWindow=cw;
   }
   
  public void run() { 
    Thread.currentThread().setName("ServerSocketThread");
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
           clientWindow.append("Exception in Run method in Socket Thread!");
        }
    }
    CloseSocket();
  }    

      
  synchronized private static void ReadSocket() {
     try {
         if ( is.available() > 0 ) {
            String data = is.readUTF();
            LogData.LogThis(Level.INFO, "Got msg ... " + data + " ...\n" );
            if ( waiting4Ack && ( data.contains( TT_PESGUIData.ClientAck ) ) ) {
                TT_PESGUIData.UnlockOrderSocketData();
                waiting4Ack = false;
            }
            if ( data.contains( TT_PESGUIData.OrderStatus ) ) {
                // Since only thing dealing with I will process the data right here instead of in a separate thread.
                String[] message = data.split(TT_PESGUIData.SocketStringSeparator);
                if (message.length > 2) {
                    // We have a multipart message as it is too long for Socket Buffer
                    multiPartMessage = true;
                    longMessage.add(message[1]);
                } else {
                    JSONArray AllOrders = null;
                    if (multiPartMessage) {
                        longMessage.add(message[1]);
                        String fullMessage = "";
                        for (int i = 0; i < longMessage.size(); i++) {
                            fullMessage = fullMessage + longMessage.get(i);
                        }
                        AllOrders = new JSONArray( fullMessage );
                        multiPartMessage = false;
                        longMessage.clear();
                    } else {
                        AllOrders = new JSONArray( message[1] );
                    }
                    TT_PESGUIData.setAllAcctsOrders( AllOrders );
                    if ( !data.contains( TT_PESGUIData.ClientAck ) ) {
// Removed to keep window less busy                        clientWindow.append("Got msg  ... " + data + " ...\n");
                        msgCnt++;
                        if (msgCnt > 60) {
                            clientWindow.append(".\n");
                            msgCnt = 0;                        
                        } else {
                            clientWindow.append(".");
                        }
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
            // The issue is to know when to send a message over.  Possible message are:
            //  StartProcesing, StopProcessing, TokenUpdate, BuyOrder, SellOrder, OrderStatus, Heartbeat
            // So need data that is shared with other threads looking for a queue to send data over.
            //      Such as:
            //          Boolean DataToSend = false; When true then take what is in the message String and send
            //          String Message = "";  This is the data to send over.  It should be reset to "" once data is sent.
            //          Boolean OkToModify = true;  This is the semaphore, you must set this to false if you can then set the other two variables and leave this as false;
            //                                      The sendMessage routine below should reset this to true after resetting the other two variables.
            // We can try to use Wait(); here or a sleep and check the variables every so often.
         if ( TT_PESGUIData.OrdersDataReadyToSend() ) {
             String Msg = TT_PESGUIData.GetOrdersSocketDataToSend();
             if ( Msg.length() >= maxBufferSize ) {
                 String putBack = TT_PESGUIData.SymbolData + TT_PESGUIData.SocketStringSeparator + Msg.substring(maxBufferSize-1);
                 Msg = Msg.substring(0,maxBufferSize-1) + TT_PESGUIData.SocketStringSeparator + "MORE TO COME";
                 TT_PESGUIData.SetOrdersSocketData(putBack);
             } else {
                 TT_PESGUIData.ClearOrdersSocketData();
             }
             waiting4Ack = true;
             os.writeUTF(Msg);
             LogData.LogThis(Level.INFO, "Message sent ... " + Msg + " ...\n" );
         }
     } catch (Exception e) {
         LogData.LogThis(Level.ERROR, "Write Exception:  " + e );
         clientWindow.append("Exception in WriteSocket in Socket Thread!");
     }
 }
  
  private static void InitSocket() {
    // Try to open a server socket on port Config.QuotesSocketNumber 
    // Note that we can't choose a port less than 1023 if we are not
    // privileged users (root)
        try {
           echoServer = new java.net.ServerSocket(Config.SchwabOrdersSocketNumber);
           clientWindow.append("This program is waiting for the client side socket to start ..." + "\n");
           LogData.LogThis(Level.INFO, "This program is waiting for the client side socket to start ..." + "\n" );
           serverClientSocket = echoServer.accept();
           is = new DataInputStream(serverClientSocket.getInputStream());
           os = new DataOutputStream(serverClientSocket.getOutputStream());
           connected = true;
           LogData.LogThis(Level.INFO, "Connected to client side socket!" + "\n" );
           clientWindow.append("Connected to client side socket!" + "\n");
        }
        catch (Exception e) {
           LogData.LogThis(Level.ERROR, "Init Exception:  " + e );
           clientWindow.append("Exception in InitSocket in Socket Thread!");
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
        clientWindow.append("The send socket connection is closed!" + "\n");
        LogData.LogThis(Level.INFO, "The send socket connection is closed!" + "\n" );
        os.close();
        is.close();
        connected = false;
      } catch (UnknownHostException e) {
         LogData.LogThis(Level.ERROR, "Trying to connect to unknown host: " + e );
         clientWindow.append("Exception in CloseSocket in Socket Thread!");
      } catch (IOException e) {
         LogData.LogThis(Level.ERROR, "C;pse IO Exception:  " + e );
         clientWindow.append("Exception in CloseSocket in Socket Thread!");
      }
  }
}
