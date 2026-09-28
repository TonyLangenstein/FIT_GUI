/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FITGUI;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.logging.log4j.Level;
import java.util.concurrent.locks.LockSupport;
/**
 *
 * @author thela
 */
public class Utility {

   private static final double under100Gap = 0.201;
   private static final double under150Gap = 0.401;
   private static final double over150Gap = 1.01;
   private static Log4J2AsyncLogger LogData =new Log4J2AsyncLogger();

// ========================================================
// ========================================================
// ========================================================
//              GENERAL USE ROUTINES BELOW
// ========================================================
// ========================================================
// ========================================================
//
   
   
    // Convert an option symbol from TDA format to Tasty format.
    // 6 characters including spaces) | Expiration (6 characters) | Call/Put (1 character) | Strike Price (5+3=8 characters) 
    //  Schwab Format is ... "KMB   040524P00285000"          Tasty format is ... ".KMB240405C142"
    synchronized public static String convertSymbolFromSchwabToTasty(String PutOrCallLegSymbol) {
        Double fractional_part = Double.parseDouble("0." + PutOrCallLegSymbol.substring(18, 21));
        Double dStrike = 0.0;
        String tastyPutOrCallSymbol = "";
        String strike = "";
        if (fractional_part < 0.01) {
            dStrike = Double.parseDouble(PutOrCallLegSymbol.substring(13, 18));
            strike = dStrike.toString();
            strike = strike.substring(0, strike.length()-2);
        } else {
            dStrike = Double.parseDouble(PutOrCallLegSymbol.substring(13, 18) + "." + PutOrCallLegSymbol.substring(18, 21));
            strike = Double.toString(dStrike);
        }
        tastyPutOrCallSymbol = "." + PutOrCallLegSymbol.substring(0, 6).trim() + // Symbol
                                      PutOrCallLegSymbol.substring(6,13) + // Expiration plus C or P
                                      strike;  // Strike      
        return tastyPutOrCallSymbol;
    }
   
    // Convert an option symbol from TDA format to Tasty format.
    //  Schwab Format is ... "KMB   240405P00285000"          Tasty format is ... ".KMB240405C142"
    //  Schwab format is ... "symbol":"DE    240517C00400000","description":"Deere & Co 05/17/2024 $400 Call"
    synchronized public static String convertSymbolFromTastyToSchwab(String PutOrCallLegSymbol) {
        // "eventSymbol": "MSFT240412C425",  Format = (Symbol, 1 to 4 chars)YYMMDD[p/c](strike, i.e. 425.5, 1 to 6+chars)
        LocalDate today = LocalDate.now();
        String year = Integer.toString( today.getYear() ).substring(2, 4);  // format would be "2024"
        String[] split = PutOrCallLegSymbol.split(year);
        String Symbol = split[0].substring(1);
        while (Symbol.length() < 6) { Symbol = Symbol + " "; }
        Double value = Double.parseDouble(PutOrCallLegSymbol.substring(split[0].length()+7));
        String SchwabPutOrCallSymbol =  Symbol + 
                                        year + 
                                        PutOrCallLegSymbol.substring(split[0].length()+year.length(),split[0].length()+year.length()+5) + 
                                        String.format("%09.3f", value);
        SchwabPutOrCallSymbol = SchwabPutOrCallSymbol.substring(0, 18) + SchwabPutOrCallSymbol.substring(19);
        return SchwabPutOrCallSymbol;
    }
   
    // Convert an option symbol from TDA format to Tasty format.
    //  TDA Format is ... "KR_070618P28.5"          Tasty format is ... ".KMB240405C142"
    synchronized public static String convertSymbolFromTDAToTasty(String PutOrCallLegSymbol) {
        String[] underscore = PutOrCallLegSymbol.split("_");
        String tastyPutOrCallSymbol = "." + underscore[0] + underscore[1].substring(4,6) + underscore[1].substring(0,4) + underscore[1].substring(6);
        return tastyPutOrCallSymbol;
    }
   
    // Convert an option symbol from TDA format to Tasty format.
    //  TDA Format is ... "KR_070618P28.5"          Tasty format is ... ".KMB240405C142"
    synchronized public static String convertSymbolFromTastyToTDA(String PutOrCallLegSymbol) {
        // "eventSymbol": "MSFT240412C425",  Format = (Symbol, 1 to 4 chars)YYMMDD[p/c](strike, i.e. 425.5, 1 to 6+chars)
        LocalDate today = LocalDate.now();
        String year = Integer.toString( today.getYear() ).substring(2, 4);  // format would be "2024"
        String[] splits = PutOrCallLegSymbol.split(year);
        String remaining = PutOrCallLegSymbol.substring(splits[0].length());
        String TDAPutOrCallSymbol = splits[0].substring(1) + "_" + remaining.substring(2,6) + year + remaining.substring(6); 
        return TDAPutOrCallSymbol;
    }
   
   public static void WriteOutLogFile( String FileName, JSONArray LogData ) {
       try {
            PrintWriter LogFile = new PrintWriter( FileName );
            for (int i = 0; i < LogData.length(); i++) {
                LogFile.println( LogData.get(i).toString() );
            }
            LogFile.close();
       } catch (Exception ex) {
            Utility. dumpExceptionInfo(ex, " WriteOutLogFile "+ FileName);
            ex.printStackTrace();
       }
   }

    public static void hybridPrecisionWait(long milliseconds) {
        long deadline = System.nanoTime() + (milliseconds * 1_000_000L);

        // Park for 99ms to save CPU health
        LockSupport.parkNanos((milliseconds - 1) * 1_000_000L);

        // Spin-wait for the final 1ms for sub-microsecond precision resume
        while (System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
    }

    
    public static String getTimeStamp() {
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS").format(new Date());  
        return timeStamp;
    }
    
    public static void dumpExceptionInfo(Exception ex, javax.swing.JTextArea jTextAreaResults) {
        jTextAreaResults.append("\n" + ex.getLocalizedMessage() + "\n");
        LogData.LogThis( Level.ERROR, "ex  = " + ex.toString() );
        // Check for 429 == Rate Limit error
        // Check for 400 == Some kind of request format error
    }
    
    public static void dumpExceptionInfo(Exception ex, javax.swing.JTextArea jTextAreaResults, String routine) {
        jTextAreaResults.append("\n" + ex.getLocalizedMessage() + "\n");
        LogData.LogThis( Level.ERROR, routine + " exception = " + ex.toString() );
        // Check for 429 == Rate Limit error
        // Check for 400 == Some kind of request format error
    }

    public static void dumpExceptionInfo(Exception ex) {
        LogData.LogThis( Level.ERROR, "ex  = " + ex.toString() );
    }

    public static void dumpExceptionInfo(Exception ex, String routine) {
        LogData.LogThis( Level.ERROR, routine + " exception  = " + ex.toString() );
    }
  
     //       private LocalDate calcNextFriday(LocalDate d) {
 //        return d.with(TemporalAdjusters.next(DayOfWeek.FRIDAY)); }
 //   private static LocalDate calcNextFriday(LocalDate d) {
 //     return d.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
 //   }
    private static LocalDate calcNextFriday(LocalDate d) {
        LocalDate ReturnValue = d.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
        String abcd = d.getDayOfWeek().toString();
        if (d.getDayOfWeek().toString().equalsIgnoreCase("Friday")) {
           ReturnValue = d;   
        }
      return ReturnValue;
    }
    
  // YYYY-MM-DD  format
    public static String nextExpiryIs(String format) {  
        // Can we generate the expiration automatically?  It needs to be in the format YYYY-MO-DD for this below.
        // Let's get the date for the very next Friday and format it as YYYY-MO-DD
        // 
        // TDAmeritrade APIs use two different formats
        //      yyyy-MM-dd and mmddyy so lets handle both
        LocalDate nextFriday = calcNextFriday(LocalDate.now());
        String monthValue = Integer.toString(nextFriday.getMonthValue());
        String dayValue = Integer.toString(nextFriday.getDayOfMonth());
        String yearValue = Integer.toString(nextFriday.getYear());
        if ( nextFriday.getMonthValue() < 10 ) {   // Need to manually add a 0 in the string
            monthValue = "0" + monthValue;
        } 
        if ( nextFriday.getDayOfMonth() < 10 ) {  // Need to manually add a 0 in the string
            dayValue = "0" + dayValue;
        }
        String nextExpiry;
        if ("mmddyy".equals(format)) {
            nextExpiry = monthValue + dayValue + yearValue.substring(2);
        } else {
            nextExpiry = yearValue + "-" + monthValue + "-" + dayValue;
        }
        return nextExpiry;
    }
    
public static boolean isValidJSONObject(JSONObject j, String item, String ofType) {

        // ofType can be one of: JSONArray, JSONObject
        boolean result = true;
        try {
        if (j.has(item)) {
            switch (ofType) {
                case "JSONArray":
                    JSONArray ja = new JSONArray(j.getJSONArray(item).toString());
                    if (ja == null) { 
                        result = false;
                    }
                    break;
                case "JSONObject":
                    JSONObject jo = new JSONObject(j.getJSONObject(item).toString());
                    if (jo == null) { 
                        result = false;
                    }
                    break;
                default:
                    break;
            }        
          } else {
            result = false;
          }         
        }
        catch (JSONException ex) {
          Utility.dumpExceptionInfo(ex);
          result = false;
        }
        return result;
    }

   public static boolean gapOk(double strike, double gap) {
    boolean returnValue = false;
    if (  ((strike < 100) && (gap < under100Gap)) ||
          ((strike >= 100) && (strike < 150) && (gap < under150Gap)) ||
          ((strike >= 150) && (gap < over150Gap))  )  {
        returnValue = true;
    }
    return returnValue;
   }

   // For TT this will add the symbol for quoting specified
   public static JSONObject JSONObjectForQuotes( String Symbol ) {
        JSONObject theMsg = new JSONObject();
        JSONObject parms = new JSONObject();
        theMsg.put("type", "FEED_SUBSCRIPTION" );
        theMsg.put("channel", 1);       // Quote always on Channel 1
        parms.put("symbol", Symbol);
        parms.put("type", "Quote");
        JSONArray items = new JSONArray();
        items.put(parms);
        theMsg.put("add", items);       
        
        return theMsg;
   }

}
