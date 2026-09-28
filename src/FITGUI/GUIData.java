/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package FITGUI;

import FITGUI.Utility;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;
import java.util.concurrent.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

/**

 @author thela
 */
public class GUIData
   {
   public static final int accountNameColumn = 0;
   public static final int accountNumberColumn = 1;
   public static final int maxTradesColumn = 2;
   public static final int maxFundsColumn = 3;
   public static final int brokerNameColumn = 4;
   public static final String allBrokers = "ALL";
   public static final String tastyTrade = "TASTY";
   public static final String charlesSchwab = "SCHWAB";
   
   public static final int symbolColumn = 0;
   public static final int maxPriceColumn = 1;
   public static final int buyWiggleColumn = 2;
   public static final int minMoveColumn = 3;
   public static final int aveMoveColumn = 4;
   public static final int EOWMinColumn = 5;
   public static final int EOWAveColumn = 6;
   public static final int percentGoalColumn = 7;
   public static final int chaseWaitTimeColumn = 8;
   public static final int chaseAmountColumn = 9;
   public static final int tradeAmount1Column = 10;
   public static final int tradeAmount2Column = 11;
   public static final int tradeAmount3Column = 12;
   public static final int tradeAmount4Column = 13;
   public static final int tradeAmount5Column = 14;
   public static final int minutesToRunColumn = 15;
   public static final int justGetInColumn = 16;
   public static final int blueLineColumn = 17;
   public static final int numberOfStrikesColumn = 18;

   private static boolean Streaming = false;
   private static boolean orderProcessing = false;
   private static boolean acceptingQuotes = false;
   private static int[] tradesSent;
   private static double[] fundsUsed;
   private static boolean debug = false;
   private static String quoteSpeed = "5";
   private static int numberStrikes = 9;
   private static String orderProcessingSpeed = "3";
   private static boolean logMoreData = false;
   private static boolean shutDown = false;
   private static boolean ordersSocketInitDone = false;
   private static boolean quotesSocketInitDone = false;
   private static int defaultRunTime = 4;
   private static String googleSpreadsheetID = "";
   
   private static int count = -1;  // Debug only
   private static JSONArray TestData = new JSONArray();
   private static JSONArray AllAcctsOrders = new JSONArray();

   private static String[][] accounts;
   private static String OpenTime = "00:30";
   private static boolean MarketOpen = false;

   public static OrderServerSocketThread SSST; 
   public static final String SocketStringSeparator = ",,,,&&,,,,";
   public static final String DataSeparator = "&&&&&&&&&&";
   
  //  This Arraylist is used for inserting a Symbol that had a trade placed, chased, and didn't fill back into the loop that tries to place trades.
   //       In effect, try to place the trade again.  Likely at a different strike.  Only do this when it is clear the trade isn't going to happen.
   private static ArrayList<String> ReturnSymbols = new ArrayList<String>();
 
   // These variables are used by the socket to write data over the socket.
   private static Boolean OrdersDataToSend = false; // When true then take what is in the message String and send
   private static Boolean QuotesDataToSend = false; // When true then take what is in the message String and send
   private static String OrdersMessage = "";  // This is the data to send over.  It should be reset to "" once data is sent.
   private static String QuotesMessage = "";  // This is the data to send over.  It should be reset to "" once data is sent.
   private static Boolean OkToModifyOrder = true;  // This is the semaphore, you must set this to false if you can then set the other two variables and leave this as false;
   private static Boolean OkToModifyQuote = true;  // This is the semaphore, you must set this to false if you can then set the other two variables and leave this as false;

   // These are used to cause updates on the client side of the socket.
   private static Boolean UpdatePrefs = false;
   private static Boolean UpdateTable = false;
   private static Boolean QuotesSetup = false;
   
   // All the possible socket messages   
   public static final String StartProcessing = "StartProcessing";
   public static final String StartGUIOnlyProcessing = "StartGUIOnlyProcessing";
   public static final String StartQuotesOnlyProcessing = "StartQuotesOnlyProcessing";
   public static final String StopProcessing = "StopProcessing";
   public static final String QuoteOnlyProcessing = "QuoteOnlyProcessing";
   public static final String GUIOnlyProcessing = "GUIOnlyProcessing";
   public static final String TokenUpdate = "TokenUpdate";
   public static final String PlaceBuyOrder = "PlaceBuyOrder";
   public static final String PlaceSellOrder = "PlaceSellOrder";
   public static final String OrderStatus = "OrderStatus";
   public static final String PositionStatus = "PositionStatus";
   public static final String QuoteUpdates = "QuoteUpdates";
   public static final String Heartbeat = "Heartbeat";
   public static final String Close = "Close";
   public static final String LoadPrefs = "LoadPrefs";
   public static final String LoadTable = "LoadTable";
   public static final String KillOpenStraddleOrders = "KillOpenStraddleOrders";
   public static final String KillOpenStraddleOrdersButtonPress = "KillOpenStraddleOrdersButtonPress";
   public static final String PlaceMissingSellOrders = "PlaceMissingSellOrders";
   public static final String ClientAck = "ClientAck";
   public static final String ServerAck = "ServerAck";
   public static final String SymbolData = "SymbolData";
   public static final String SetupQuotes = "SetupQuotes";
   public static final String QuotesReady = "QuotesReady";
   
   // [Acctnum String][Stock Symbol String][Strike String]
   public static JSONArray positionsBeforeRunning = null;  

 // This is a copy of what the main program uses.  It should be updated when ever the main program updates it.
   private static javax.swing.JTable Symbol_Data_Table;
   
   synchronized public static int getDefaultRunTime() {
       return defaultRunTime;
   }

   synchronized public static void setDefaultRunTime(int newValue) {
       defaultRunTime = newValue;
   }
   
   private static JSONArray schwabSymbolQuotes = new JSONArray();
   
   synchronized public static double getSymbolOpenPrice(String symbol) {
       double returnValue = 0.0;
       try {
        for (int i = 0; i < schwabSymbolQuotes.length(); i++) {
            JSONObject quoteData = new JSONObject( schwabSymbolQuotes.getJSONObject(i).toString() );
            if (quoteData.getString("symbol").equalsIgnoreCase(symbol)) {
                JSONObject quote = quoteData.getJSONObject("quote");
                returnValue = quote.getDouble("openPrice");
                break;
            }
        }
       } catch (Exception ex) {
          Utility.dumpExceptionInfo(ex);
          ex.printStackTrace();
          returnValue = 0.0;
       }
       return returnValue;
   }
        
   synchronized public static JSONObject getSchwabSymbolQuote(String Symbol) {
        JSONObject returnValue = new JSONObject();
        try {
            for (int i = 0; i < schwabSymbolQuotes.length(); i++) {
                JSONObject thisIt = new JSONObject( schwabSymbolQuotes.get(i).toString() );
                if (thisIt.toString().contains(Symbol)) {
                    returnValue = thisIt;
                    break;
                }
            }
        }  catch (Exception ex)         {
            Utility.dumpExceptionInfo(ex);
            ex.printStackTrace();
        }
           return returnValue;
   }

   synchronized public static void setSchwabSymbolQuotes(JSONArray newQuotes) {
       schwabSymbolQuotes = newQuotes;
   }

   synchronized public static JSONArray getSchwabSymbolQuotes() {
       return schwabSymbolQuotes;
   }

   synchronized public static boolean getQuotesSetup() {
       return QuotesSetup;
   }

   synchronized public static void setQuotesSetup(boolean newValue) {
       QuotesSetup = newValue;
   }

   synchronized public static void setPositionsBeforeRunning( JSONArray pBR ) {
       positionsBeforeRunning = pBR;
   }

   synchronized public static boolean anyConflictingOrders(String symbol, String strike, String accountNum) {
       boolean returnValue = false;
       JSONArray JApBR = positionsBeforeRunning;
       // TBD in the future when doing TT Orders - this will be different.
       // Convert the strike to Schwab format
       strike = String.format("%09.3f", Double.valueOf(strike));
       strike = strike.substring(0, 5) + strike.substring(6);  // Remove the "."
       try {
          if ( JApBR != null ) {
             // See if Symbol in the array
             String pBR = JApBR.toString();
             if ( pBR.contains(symbol) && pBR.contains(strike) && pBR.contains(accountNum) ) {
                 // We likely have a match, let's see if we can find it.
                for (int a = 0; a < JApBR.length(); a++ ) {
                    JSONObject aPosition = new JSONObject( JApBR.getJSONObject(a).toString() );
                    String aP = aPosition.toString();
                    if ( aP.contains(symbol) && aP.contains(strike) && aP.contains(accountNum) ) {
                      // We found it.
                      returnValue = true;
                      break;
                    }
                }
             }
          }
      } catch (Exception ex) {
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
      }
      return returnValue;
   }

   synchronized public static void StartServerSockets( javax.swing.JTextArea Results_Text_Area ) {
        SSST = new OrderServerSocketThread( Results_Text_Area );
        Thread.ofVirtual().start(SSST);
   }

   synchronized public static void SetSymbolTable( javax.swing.JTable table ) {
       Symbol_Data_Table = table;
   }

   synchronized public static javax.swing.JTable GetSymbolTable( ) {
       return Symbol_Data_Table;
   }

   synchronized public static void SetUpdatePrefs( Boolean value ) {
       UpdatePrefs = value;
   }

   synchronized public static Boolean GetUpdatePrefs( ) {
       return UpdatePrefs;
   }

   synchronized public static void SetUpdateTable( Boolean value ) {
       UpdateTable = value;
   }

   synchronized public static Boolean GetUpdateTable( ) {
       return UpdateTable;
   }
      
   synchronized public static JSONArray getAllAcctsOrders( ) {
       return AllAcctsOrders;
      }   
   
   synchronized public static void setAllAcctsOrders( JSONArray AllOfIt ) {
       AllAcctsOrders = AllOfIt;
      }   
   
   // Returns true if Lock was successful
   synchronized public static boolean LockOrderSocketData( ) {
       boolean result = false;
       if ((OkToModifyOrder == true) && (OrdersDataToSend == false)) {
          OkToModifyOrder = false;
          result = true;
       }
       return result;
   }

   // Returns true if UnLock was successful
   synchronized public static boolean UnlockOrderSocketData( ) {
       boolean result = false;
       if (!OkToModifyOrder) {
          OkToModifyOrder = true;
          result = true;
       }
       return result;
   }
   
   // Returns true if Lock was successful
   synchronized public static boolean LockQuoteSocketData( ) {
       boolean result = false;
       if ((OkToModifyQuote == true) && (QuotesDataToSend == false)) {
          OkToModifyQuote = false;
          result = true;
       }
       return result;
   }

   // Returns true if UnLock was successful
   synchronized public static boolean UnlockQuoteSocketData( ) {
       boolean result = false;
       if (!OkToModifyQuote) {
          OkToModifyQuote = true;
          result = true;
       }
       return result;
   }

   // Is there data to send?
   synchronized public static boolean OrdersDataReadyToSend( ) {
       return OrdersDataToSend;
   }

   // Is there data to send?
   synchronized public static boolean QuotesDataReadyToSend( ) {
       return QuotesDataToSend;
   }

   // Get the data
   synchronized public static String GetOrdersSocketDataToSend( ) {
       return OrdersMessage;
   }

   // Set the data to be sent
   synchronized public static void SetOrdersSocketData( String Data ) {
       OrdersMessage = Data;
       OrdersDataToSend = true;
   }

   // ReSet the data to be sent
   synchronized public static void ClearOrdersSocketData() {
       OrdersMessage = "";
       OrdersDataToSend = false;
   }

   // Get the data
   synchronized public static String GetQuotesSocketDataToSend( ) {
       return QuotesMessage;
   }

   // Set the data to be sent
   synchronized public static void SetQuotesSocketData( String Data ) {
       QuotesMessage = Data;
       QuotesDataToSend = true;
   }

   // ReSet the data to be sent
   synchronized public static void ClearQuotesSocketData() {
       QuotesMessage = "";
       QuotesDataToSend = false;
   }

   // Put a symbol on the list of symbols to go back into the symbols to be considered for a possible trade.
   synchronized public static void AddSymbolToRetry( String Symbol ) {
        try { 
            ReturnSymbols.add( Symbol );
        }  catch (Exception ex)         {
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
       }
   }   
   
   // Remove a symbol from the list of symbols to go back into the list of symbols to be considered for a trade.
   synchronized public static void RemoveSymbolToRetry( String Symbol ) {
       try {
           ReturnSymbols.remove( Symbol );
        }  catch (Exception ex)         {
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
       }
   }   

   // Find and Remove a symbol from the list of symbols to go back into the list of symbols to be considered for a trade.
   //  Return true is the symbol was found and removed.
   synchronized public static boolean FindAndRemoveSymbolToRetry( String Symbol ) {
       boolean returnValue = false;
       try {
            for ( int abc = 0; abc < ReturnSymbols.size(); abc++ ) {
                if ( ReturnSymbols.equals( Symbol ) ) {
                    ReturnSymbols.remove( Symbol );
                    returnValue = true;
                    break;
                }
            }
        }  catch (Exception ex)         {
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
       }
       return returnValue;
   }   
   
   synchronized public static boolean MarketOpen(  ) {

       if ( MarketOpen ) {
           // do nothing
       }  else {
            LocalTime rightNow = LocalTime.now();
            LocalTime Open = LocalTime.parse( OpenTime );
            MarketOpen = rightNow.isAfter( Open );
       }
        return MarketOpen;
   }   
   
   synchronized public static void setOpenTime( String newValue ) {
       OpenTime = newValue;
   }   

   synchronized public static String getOpenTime( ) {
       return OpenTime;
   }   

   synchronized public static void setStreaming( boolean newValue ) {
       Streaming = newValue;
   }   

   synchronized public static boolean getStreaming( ) {
       return Streaming;
   }   

    synchronized public static void setAccounts(String[][] accts) {
        accounts = accts;
    }

   synchronized public static String[][] getAccounts()
      {
      return accounts;
      }

   synchronized public static void setQuoteSpeed(String newSpeed)
      {
      quoteSpeed = newSpeed;
      }

   synchronized public static String getQuoteSpeed()
      {
      return quoteSpeed;
      }

   synchronized public static void setOrderProcessingSpeed(String newSpeed)
      {
      orderProcessingSpeed = newSpeed;
      }

   synchronized public static String getOrderProcessingSpeed()
      {
      return orderProcessingSpeed;
      }

   synchronized public static void setStrikesPerSymbol(int newStrikes)
      {
      numberStrikes = newStrikes;
      }

   synchronized public static int getStrikesPerSymbol()
      {
      return numberStrikes;
      }

   synchronized public static int getStrikeCount(String symbol)
      {
          int strikeCount = 0;
          for (int i = 0; i < Symbol_Data_Table.getRowCount(); i++) {
              if (Symbol_Data_Table.getValueAt(i, symbolColumn).toString().equalsIgnoreCase(symbol)) {
                  strikeCount = Integer.parseInt(Symbol_Data_Table.getValueAt(i, numberOfStrikesColumn).toString());      
                  break;
              }
          }
          return strikeCount;
      }

   synchronized public static void LoadTestData()
      {
      try
         {
         // Load all of the selected orders from the StraddlesData table into the Orders Table.
//        File testFile = new File("20Symbol1MinuteData.txt");
         File testFile = new File("2019_07_26_2019_test_data_file.txt");
//        File testFile = new File("GS_Only.txt");
         if (testFile.exists())
            {
            Scanner inputStream = new Scanner(testFile);  // Read the file.
            while (inputStream.hasNext())
               {
               String data = inputStream.nextLine();     // Read the next line.
               if ( data.contains( "From server ...{\"data\":" ) ) {
                   String[] Values = data.split("From server ...");  

                   JSONObject temp = new JSONObject( Values[1] );
                   TestData.put(temp);
                   }
               }
            inputStream.close();                            // Close the file.
            count = 0;
            }
         }
      catch (Exception ex)
         {
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
         }
      }

   synchronized public static JSONObject getNextTestPacket()
      {
      JSONObject returnValue = null;
      if (count < TestData.length())
         {
         returnValue = new JSONObject(TestData.get(count).toString());
         count = count + 1;
         }
      return returnValue;
      }

   synchronized public static void setDebug(boolean value)
      {
      debug = value;
      }

   synchronized public static boolean getDebug()
      {
      return debug;
      }

   synchronized public static void setLogMoreData(boolean value)
      {
      logMoreData = value;
      }

   synchronized public static boolean getLogMoreData()
      {
      return logMoreData;
      }

   synchronized public static void setOrderProcessing(boolean value)      {
            orderProcessing = value;
      }

   synchronized public static boolean getOrderProcessing()
      {
      return orderProcessing;
      }

   synchronized public static void initFundsAvailable( double[] funds) {
       fundsUsed = new double[ funds.length ];
       for (int index = 0; index < funds.length; index++ ) {
           fundsUsed[ index ] = 0.0;
       }
   }
   
   synchronized public static void initTradesAvailable( int[] trades) {
       tradesSent = new int[ trades.length ];
       for (int index = 0; index < trades.length; index++ ) {
           tradesSent[ index ] = 0;
       }
   }
   
   synchronized public static void updateFundsUsed( int index, double funds) {
       fundsUsed[index] = funds;
   }
   
   synchronized public static void updateTradesSent( int index, int trades) {
       tradesSent[index] = trades;
   }
   
   synchronized public static double getFundsUsed( int index ) {
       return fundsUsed[index];
   }
      
   synchronized public static int getTradesSent( int index ) {
       return tradesSent[index];
   }   
   
   synchronized public static void setAcceptingQuotes(boolean value)
      {
      acceptingQuotes = value;
      }

   synchronized public static boolean getAcceptingQuotes()
      {
      return acceptingQuotes;
      }

   synchronized public static void setShutDown(boolean value)
      {
      shutDown = value;
      }

   synchronized public static boolean getShutDown()
      {
      return shutDown;
      }

   synchronized public static void setQuotesSocketInitDone(boolean value)
      {
      quotesSocketInitDone = value;
      }

   synchronized public static boolean getQuotesSocketInitDone()
      {
      return quotesSocketInitDone;
      }

   synchronized public static void setOrdersSocketInitDone(boolean value)
      {
      ordersSocketInitDone = value;
      }

   synchronized public static boolean getOrdersSocketInitDone()
      {
      return ordersSocketInitDone;
      }

   synchronized public static String getGoogleSpreadsheetID()
      {
      return googleSpreadsheetID;
      }

   synchronized public static void setGoogleSpreadsheetID(String newID)
      {
      googleSpreadsheetID = newID;
      }

}
