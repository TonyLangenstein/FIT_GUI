/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FITGUI;

import java.text.DecimalFormat;
import org.apache.logging.log4j.Level;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

/**
 *
 * @author Tony Langenstein
 */
public class PositionsData {

    private static Log4J2AsyncLogger LogData =new Log4J2AsyncLogger();
    private static JSONArray allAcctPositions = new JSONArray();

    // This operates 2 ways.  1) By doingall the work and ignoring these first 2 functions. Or,
    //  2) By receiving over a socket the position data only and just placing it in here to be used.
    // To take advantage of either just change calls w/in the GUI to be getPositions or GetPositionsData.
    synchronized public static void setAllAcctPositions(JSONArray theData) {
        allAcctPositions = theData;
    }

    synchronized public static String[] getPositions(String acctNumber) {
        String[] returnValue = null;
        try {
            if (allAcctPositions.toString().contains(acctNumber)) {
                JSONObject theData = new JSONObject();
                for (int i = 0; i < allAcctPositions.length(); i++) {
                    theData = new JSONObject( allAcctPositions.get(i).toString() );
                    String acct = theData.getString("accountNumber");
                    if (acct.equalsIgnoreCase(acctNumber)) {
                        JSONArray details = theData.getJSONArray("positions");
                        returnValue = new String[ details.length() ];
                        for (int j = 0; j < details.length(); j++) {
                            returnValue[j] = details.get(j).toString();
                        }
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            Utility. dumpExceptionInfo(ex, " Exception 24 getPositions.  Keep on processing - Acct number not found = " + acctNumber);
        }
        return returnValue;
    }

    // Currently not used
    synchronized public static void AccountUpdates(JSONObject Content)
      {
      try
         {
         if (Content.has("2"))
            {
            // We will only do something with certain message types ... those are OrderFill, OrderPartialFill
            String AcctNum = Content.getString("1");
            String MsgType = Content.getString("2");
            JSONObject MsgData = XML.toJSONObject(Content.getString("3"));
            if (MsgType.equalsIgnoreCase("OrderFill"))
               {
               LogData.LogThis(Level.INFO," Account Updates ===>  Order Fill " + Content.toString());
               JSONObject FillMessage = new JSONObject(MsgData.getJSONObject("OrderFillMessage").toString());
               }
            else
               {
               if (MsgType.equalsIgnoreCase("OrderPartialFill"))
                  {
                  LogData.LogThis(Level.INFO, "Account Updates ===>  Order Partial Fill " + Content.toString());
                  JSONObject PartialFillMessage = new JSONObject(MsgData.getJSONObject("OrderPartialFillMessage").toString());
                  }
               else
                  {
                  if (MsgType.equalsIgnoreCase("UROUT"))
                     {
                     LogData.LogThis(Level.INFO," Account Updates ===>  UROUT " + Content.toString());
                     JSONObject UROutMessage = new JSONObject(MsgData.getJSONObject("UROUTMessage").toString());
                     }
                  }
               }
            }
         }
      catch (Exception ex)
         {
         Utility. dumpExceptionInfo(ex, " Exception 14 TT_PositionsData/AccountUpdates.  Keep on processing." + Content);
         }
      }

   // TESTED: 7/9/19
   // Assumes the FullSymbol is in Schwab format, so convert to TDA format and do like the old days :)
   // Schwab format is Underlying Symbol (6 characters including spaces) | Expiration (6 characters) | Call/Put (1 character) | Strike Price (5+3=8 characters) 
   synchronized private static String GetOrderKey(String AccountNumber, String FullSymbol)
      {
      String OrderKey = "";
      try
         {
         // First convert symbol then do normal processing.
         String TDASymbolFormat = Utility.convertSymbolFromTastyToTDA( Utility.convertSymbolFromSchwabToTasty(FullSymbol) );
         // Determine OrderKey
         String[] Values = TDASymbolFormat.split("_");
         String Symbol = Values[0];
         String[] Values2;
         String PutOrCall = "";
         if (Values[1].contains("P"))
            {
            Values2 = Values[1].split("P");
            PutOrCall = "P";
            }
         else
            {
            Values2 = Values[1].split("C");
            PutOrCall = "C";
            }
         String Strike = Values2[1];
         OrderKey = AccountNumber + "_" + Symbol + "_" + Strike;
         }
      catch (Exception ex)
         {
         Utility. dumpExceptionInfo(ex, " Exception 15 TT_PositionsData/GetOrderKey.  Keep on processing.");
         }
      return OrderKey;
      }

   // The FullSymbol must be what is generated from GetOrderKey
   synchronized private static String GetUnderlyingSymbol(String FullSymbol)
      {
          String symbol = "";
          try {
             String[] Values = FullSymbol.split("_");
             if (Values.length > 1) {
                 symbol = Values[1];
             }
          } catch (Exception ex) {
             Utility. dumpExceptionInfo(ex, " Exception 16 TT_PositionsData/GetUnderlyingSymbol.  Keep on processing.");
          }
          return symbol;
      }

   // Must be in Schwab Format
   synchronized private static String GetPutOrCall(String FullSymbol)
      {
      String PorC = "";
      try {
            PorC = FullSymbol.substring(12, 13);  // TBD this may be wrong
         }
      catch (Exception ex)
         {
         Utility. dumpExceptionInfo(ex, " Exception 16 TT_PositionsData/GetPutOrCall.  Keep on processing.");
         }
      return PorC;
      }

   // Get expiration from FullSymbol which is in Schwab format and convert it to TDA format for the return value.
   synchronized private static String GetExpiration(String FullSymbol)
      {
// Schwab format is Underlying Symbol (6 characters including spaces) | Expiration (6 characters) | Call/Put (1 character) | Strike Price (5+3=8 characters) 
         String Expiration = "";
      try
         {
         // Determine OrderKey
         Expiration = FullSymbol.substring(6, 12);
         Expiration = Expiration.substring(2) + Expiration.substring(0, 2);
         }
      catch (Exception ex)
         {
         Utility. dumpExceptionInfo(ex, " Exception 17 TT_PositionsData/GetExpiration.  Keep on processing.");
         }
      return Expiration;
      }

   // This defines the Order Action type.
   public static enum OrderActionType
      {
      SellStraddle,
      SellHalf,
      SellCalls,
      SellPuts,
      Cancel
      }

   // , but only for Cancel and Sell orders.
   synchronized public static void Process_Order_Action(String OrderKey, OrderActionType OrderAction, Double Price, String expiration, String contracts)
      {
      /*
       Before placing the new Sell order, cancel any/all existing Buy/Sell
       orders.
       There might be profit to take while we are waiting on a partial buy order
       that
       might never get filled. It will also emulate the TOS Cancel Replace
       functionality.

       *** Tony *** - Your code goes where the println statements are.

       */
// This may not be correct - TBD
      String[] Values = OrderKey.split("_");
      String AccountNumber = Values[0];
      String Symbol = Values[1];
      String Strike = Values[2];
      DecimalFormat df2 = new DecimalFormat(".##");
      if (OrderAction == OrderActionType.SellStraddle)
         {
         LogData.LogThis(Level.INFO,"Placing Sell Straddles order for " + OrderKey);
         // This will cancel open buy and sell orders
         SchwabAPI.GUICancelOrderRequest(AccountNumber, Symbol, Strike);
         // Need expiration, these need to be converted into Schwab format.
         String primayLegSymbol = Utility.convertSymbolFromTastyToSchwab( Utility.convertSymbolFromTDAToTasty(Symbol + "_" + expiration + "C" + Strike) );
         String secondaryLegSymbol = Utility.convertSymbolFromTastyToSchwab( Utility.convertSymbolFromTDAToTasty(Symbol + "_" + expiration + "P" + Strike) );
         String strPrice = df2.format( Price );
         // Now place a sell order for the open position.
         SchwabAPI.placeSellStraddleOrder(primayLegSymbol, secondaryLegSymbol, Double.parseDouble( strPrice ), Integer.parseInt( contracts ), AccountNumber);
         LogData.LogThis(Level.INFO,"Sell order placed ... " + primayLegSymbol + "; " + secondaryLegSymbol + "; Price=" + strPrice + "; Contracts=" + contracts + "; AcctNum=" + AccountNumber );

         // Next, kick off a thread to chase the sell order.
         // TBD
         }
      else
         {
         /*
          else if (OrderAction == OrderActionType.SellCalls)
          {
//          System.out.println("Placing Sell Calls order for " + OrderKey);
          // TBD
          }
          else if (OrderAction == OrderActionType.SellPuts)
          {
//          System.out.println("Placing Sell Puts order for " + OrderKey);
          // TBD
          }
          else if (OrderAction == OrderActionType.Cancel)
          */
         LogData.LogThis(Level.INFO,"Cancel Orders request for " + OrderKey);
         // This will cancel open buy and sell orders
         SchwabAPI.GUICancelOrderRequest(Values[0], Values[1], Values[2]);
         }
      }

   // 
   synchronized private static double GetTotalFunds( JSONObject AnOrder ) {
       double TotalFunds = 0.0;
        JSONArray orderActivityCollection = new JSONArray( AnOrder.getJSONArray( "orderActivityCollection" ).toString() ); // TBD
        for ( int abc = 0; abc < orderActivityCollection.length(); abc++ ) {
            JSONObject OrderActivity = new JSONObject( orderActivityCollection.getJSONObject(abc).toString() );
            JSONArray executionLegs = new JSONArray( OrderActivity.getJSONArray("executionLegs").toString() );  // TBD
            for ( int def = 0; def < executionLegs.length(); def++ ) {
                JSONObject Leg = new JSONObject( executionLegs.getJSONObject(def).toString() );
                TotalFunds = TotalFunds + Leg.getInt("quantity") * Leg.getDouble("price");     // TBD
            }
        }
        return TotalFunds;
   }
   

   // TBD - This needs to be looked at for doing something with the symbol which is an option symbol
   synchronized private static String GetFullSymbol( String AnOrder ) {
        String[] V1 = AnOrder.split("\"symbol\":");
        String[] V2 = V1[1].split(",");
        return V2[0].replace("\"", "");
}

   // 
   /*  This routine is basically collecting orders by OrderKey - Each order key may have 1 or more Buy or Sell orders.
        Data Structure
        JSONArray of JSONObjects (JSONObjects represent a position with orders)
                Position contains
                           String OrderKey
                           JSONArray of buy orders
                           JSONArray of sell orders
   */
   synchronized private static JSONArray BuildAccounts( String AnOrder, String AcctNum, String FullSymbol, JSONArray CurrentPositions )  {  
       JSONArray returnValue = CurrentPositions;
       JSONArray WorkingOrders = new JSONArray();
        boolean Found = false;
       try  {
         JSONObject Order = new JSONObject( AnOrder );

         String OrderKey = GetOrderKey(AcctNum, FullSymbol);
         for (int i = 0; i < CurrentPositions.length(); i++) {
             // If there is an Position already then add the order to it
             JSONObject Position = new JSONObject( CurrentPositions.getJSONObject(i).toString() );
             if ( OrderKey.equalsIgnoreCase( Position.getString("OrderKey") ) ) {
                 // Is it a buy or sell order?
                if ( AnOrder.contains( "BUY_TO_OPEN" ) ) {
                    if ( Position.has("BuyOrders") ) {
                        WorkingOrders = new JSONArray( Position.getJSONArray( "BuyOrders").toString() );
                        Position.remove("BuyOrders");
                    }
                    WorkingOrders.put(  Order );
                    Position.put("BuyOrders", WorkingOrders);
                } else {
                    if ( Position.has("SellOrders") ) {
                        WorkingOrders = new JSONArray( Position.getJSONArray( "SellOrders").toString() );
                        Position.remove("SellOrders");
                    }
                   WorkingOrders.put(  Order );
                    Position.put("SellOrders", WorkingOrders);
                }
                returnValue = ReplacePositionArrayItem( CurrentPositions, OrderKey,  Position);
                Found = true;
                break;
             }
         }         
         if ( !Found ) {
            JSONObject Position = new JSONObject();
            Position.put("OrderKey",OrderKey );
            JSONArray Orders = new JSONArray();
            Orders.put( Order );
            if ( AnOrder.contains( "BUY_TO_OPEN" ) ) {
                Position.put("BuyOrders", Orders);
            } else {
                Position.put("SellOrders", Orders);
            }
            CurrentPositions.put( Position );
         }
      }  catch (Exception ex) {
             Utility. dumpExceptionInfo(ex, " Exception 22 TT_PositionsData/BuildAccounts.  Keep on processing."+ AcctNum + "   " + " OrderData " + "  " + AnOrder);
      }
       return returnValue;
   }

   // T
   // Replace an existing item in an array.  Return a new array with the updates.  No lock required here.
   synchronized private static JSONArray ReplacePositionArrayItem(JSONArray Positions, String OrderKey, JSONObject Position)
      {  // Assumes Locked when coming in
      JSONArray returnValue = new JSONArray();
      try
         {
         for (int i = 0; i < Positions.length(); i++)
            {
            JSONObject JTemp = new JSONObject(Positions.get(i).toString());
            if (JTemp.getString("OrderKey").equalsIgnoreCase(OrderKey))
               {
               returnValue.put(Position);
               }
            else
               {
               returnValue.put(JTemp);
               }
            }
         }
      catch (Exception ex)
         {
             Utility. dumpExceptionInfo(ex, " Exception 23 TT_PositionsData/ReplacePositionArrayItem.  Keep on processing."+ OrderKey);
         }
      return returnValue;
      }
   
}
