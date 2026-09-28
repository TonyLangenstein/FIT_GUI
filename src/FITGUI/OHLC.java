/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FITGUI;
 
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Scanner;
import org.json.*;
import java.util.List;
import java.util.ArrayList;
import java.util.ArrayList;
import javax.swing.JTextArea;
import java. util. Iterator;
import java.time.Year;

/**
 *
 * @author thela
 */
public class OHLC {

private static DecimalFormat df2 = new DecimalFormat(".##");

public static String getYear() {
    Year thisYear = Year.now();
    String theYear = Year.now().toString();
    if (theYear.length() > 2) {
        // Shrink it to just the last 2 digits of the year
        theYear = theYear.substring(2);
    }
    return theYear;    
}

public static boolean IsValidDate( String date ) {
    boolean returnValue = false;
    DateFormat shortFormat = new SimpleDateFormat("dd/MM");
    DateFormat longFormat = new SimpleDateFormat("dd/MM/YY");
// Changed to above line 1/2/21    DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
//    String[] values = date.split("\\/");
//    if ( values[0].length() == 1 ) { 
//        values[0] = "0" + values[0];
//    }
//    if ( values[1].length() == 1 ) { 
//        values[1] = "0" + values[1];
//    }
//    date = values[0] + "/" + values[1] + "/" + values[2];

    // Input to be parsed should strictly follow the defined date format
    // above.
//    format.setLenient(false);
    try {
        if (date.length() > 5) {
            longFormat.parse(date);
        } else {
            shortFormat.parse(date);
        }
        returnValue = true;
    } catch (Exception e) {
        System.out.println("Date " + date + " is not valid");
    }
    return returnValue;
}

private static String GetEntryPoint ( double open ) {
    String returnValue = "0.0";
    String str = Double.toString(open);
    String[] values = str.split("\\.");
    double rem = Double.parseDouble( values[1] )/100;
    if ( open < 100.01 ) {  // Assume 0.5 strikes
        if ( rem >= 0.75 ) {
            returnValue = Double.toString( Double.parseDouble( values[0] ) + 1.0 );
        } else {
            if ( rem >= 0.25 ) {
                // Take the integer value and add 0.5 to it.
                returnValue = values[ 0 ] + ".5";
            } else {
                returnValue = values[ 0 ];
            }
        }
    } else { 
        if ( open < 150.01 ) {  // Assume 1.0 wide strikes
            if ( rem >= 0.5 ) {
                returnValue = Double.toString( Double.parseDouble( values[0] ) + 1.0 );
            } else {
                returnValue = values[ 0 ];
            }
        } else {
            // Assume 2.5 wide strikes
            double Num2dot5s = open/2.5;
            str = Double.toString( Num2dot5s );
            values = str.split("\\.");
            rem = Double.parseDouble( values[1] )/10;
            if ( rem >= 0.5 ) {
                returnValue = df2.format( (Double.parseDouble(values[0]) + 1.0) * 2.5 );  
            } else {
                returnValue = df2.format( Double.parseDouble(values[0]) * 2.5 );  
            }            
        }
    }   
    return returnValue;
}

    public static Date addDays(Date date, int days) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(date);
            cal.add(Calendar.DATE, days);

            return cal.getTime();
    }

// Reads the lines from "Morning Straddes - Data Current Q.csv" looking for a valid date in a line.  If found then it update the values and puts them back into the file and writes out a new
//      file called "Updated_OHLC_Data.CSV",  the new file will only contain, the Date, Symbol, and the 4 calculated values.  Cut and paste in the Google Spreadsheet after that is required.

public static void GenerateOHLCData ( javax.swing.JTextArea Results_Text_Area ) {
    try {
         // This is the file we will write the data to after it is modified.
         String UpdatedDataName = Config.OHLC_Output_File_Name;     
         PrintWriter tokensWriter = new PrintWriter( UpdatedDataName );

         // This is the original file name
         String fileName = Config.OHLC_Input_File_Name;     
         File file = new File(fileName);
         String dateInFile = "";

         // Read the file.
         Scanner inputStream = new Scanner( file );

         Results_Text_Area.append("Starting generation ...");
         // Loop until the end of the file.
         while (inputStream.hasNext())            {
            String data = inputStream.nextLine();     // Read the next line.
            String[] values = data.split(",");        // Get the values from the line.

            // If the values are for the current date, then insert them into the table.
            if ( IsValidDate( values[ Config.OHLC_Date_Column ] ) && (values.length >= Config.OHLC_NumberValidColumns ) ) {   
                    // We will generate data for the following columns:  BD, BE, BO, BP; these are "Entry Point", "Day Range", "Max Possible Exit", "Ride to expiry"
                    //  In order to calculate these values we need to get the Open, High, Low, Close for the day listed as well as every day thru Friday of the week
                    //  Column C in the file should have the day of the week this is which will give us the number of days to extract from TDA.
                    //  NOTE:  I tried to go further, but when dropping the a CSV, if there are cells with multiple lines of data then the it actually ends the line and starts a new one and messes everytihng up.

                    // So lets get the start and end dates setup for this.
                    SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy");
                    java.util.Date d = sdf.parse( values[ Config.OHLC_Date_Column ] );    
                    String StartDate = Long.toString( d.getTime() );

                    String DayOfWeek = values[ Config.OHLC_DayOfWeek_Column ];     
                    String EndDate = null;
                    switch ( DayOfWeek ) {
                        case "Mon" :
                                EndDate = Long.toString( addDays( d, 4).getTime() );
                            break;
                        case "Tue" :
                                EndDate = Long.toString( addDays( d,3).getTime() );
                            break;
                        case "Wed" :
                                EndDate = Long.toString( addDays( d, 2).getTime() );
                            break;
                        case "Thu" :
                                EndDate = Long.toString( addDays( d, 1).getTime() );
                            break;
                        default :
//                                EndDate = StartDate;
                            break;
                    }

                    // The values we need to get below.  Parse through the data from TDA and set them.
                    double D1_Open = 0.0;
                    double D1_High = 0.0;
                    double D1_Low = 0.0;
                    double WeekHigh = 0.0;
                    double WeekLow = 0.0;
                    double WeekClose = 0.0; 
                    
                    //      getPriceHistory( MSFT, "month", "1", "daily", "1", "1560527467000", "1560441067000", false ) or
                    //      getPriceHistory( MSFT, "month", null, "daily", null, "1560527467000", "1560441067000", false )
                    JSONObject PriceData = SchwabAPI.getPriceHistory( values[ Config.OHLC_Symbol_Column ], "month", "1", "daily", "1", EndDate, StartDate, false );
                    if (PriceData != null) {
                        JSONArray Candles = new JSONArray( PriceData.getJSONArray("candles").toString() );
                        if ( Candles != null ) {
                            for ( int abc = 0; abc < Candles.length(); abc++ ) {
                                JSONObject Candle = new JSONObject( Candles.getJSONObject( abc ).toString() );
                                if ( abc == 0 ) {
                                    D1_Open = Candle.getDouble( "open" );
                                    D1_High = Candle.getDouble( "high" );
                                    D1_Low = Candle.getDouble( "low" );
                                    WeekHigh = D1_High;
                                    WeekLow = D1_Low;
                                    WeekClose = Candle.getDouble( "close" ); 
                                } else {  // Days beyond D1 
                                    if ( Candle.getDouble( "high") > WeekHigh ) {
                                        WeekHigh = Candle.getDouble( "high");
                                    }
                                    if ( Candle.getDouble( "low") < WeekLow ) {
                                        WeekLow = Candle.getDouble( "low");
                                    }
                                    WeekClose = Candle.getDouble( "close" );
                                }  
                                if (EndDate == null) {
                                    break;
                                }
                            }

                            String Entry_Point = GetEntryPoint( D1_Open );
                            double EP = Double.parseDouble( Entry_Point );
                            String  Day_Range = df2.format( Math.max( Math.abs( D1_Open - D1_High ), Math.abs( D1_Open - D1_Low ) ) );
                            String Max_Exit = df2.format(Math.max( Math.abs( EP - WeekHigh ), Math.abs( EP - WeekLow ) ) );
                            String Ride_Expiry = df2.format( Math.abs( EP - WeekClose ) );
                            // Update the appropriate columns
                            // We will replace data in the following columns:  BD, BE, BO, BP; these are "Entry Point", "Day Range", "Max Possible Exit", "Ride to expiry"
                            String Delimeter = ",";
                            String newLine = values[ Config.OHLC_Date_Column ] + Delimeter + 
                                            values[ Config.OHLC_Symbol_Column ] + Delimeter + 
                                            values[ Config.OHLC_DayOfWeek_Column ] + Delimeter + 
                                            Entry_Point + Delimeter + 
                                            Day_Range + Delimeter + 
                                            Max_Exit + Delimeter + 
                                            Ride_Expiry;     
                            tokensWriter.println( newLine );
                            System.out.println( newLine );
                            Results_Text_Area.append(Utility.getTimeStamp() + newLine + ".\n");
                        } else {
                            System.out.println( "No candle data" );
                            Results_Text_Area.append(Utility.getTimeStamp() + "No candle data.\n");
                        }
                    } else {
                        System.out.println( "Price data is null" );
                        Results_Text_Area.append(Utility.getTimeStamp() + "Price data is null.\n");
                    }
            }  else {
                    System.out.println( "Either invalid date or a line with not enough info" );
                    Results_Text_Area.append(Utility.getTimeStamp() + "Either invalid date or a line with not enough info.\n");
            }            
         }  // While Loop
         // Close the file.
         inputStream.close();
         Results_Text_Area.append("All done with generation.  ");

        // Close the newly created file
         tokensWriter.close();
    } catch (FileNotFoundException ex) {
         Results_Text_Area.append(" Make sure file name is Updated_OHLC_Data.csv then try again.  ");
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
    } catch (Exception ex) {
         Results_Text_Area.append(" Make sure file name is Updated_OHLC_Data.csv then try again.  ");
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
    }    
}
   
   public static void GenDailyData(javax.swing.JTextArea Results_Text_Area, String dateForGeneration, double desiredDollars)                                                      
   {                                                          
      // This procedure loads a .csv file where it will place generated stats: 
      //        OI OK?, B/A OK?, Start Price, Exp Price 1 Series Later, Exp Price 4 Series Later, Think Back Price, Buy Wiggle, and ATM Night B4.
      // It will place these generated and provided values into a new CSV file with the same name and the word "Updated_" placed at the front of the file name.
      try
         {
         String fileName = Config.DG_Input_File_Name;  
         File file = new File(fileName);
         String dateInFile = "";

         // Read the file.
         Scanner inputStream = new Scanner(file);
         Results_Text_Area.append("Starting generation ...");

         // This is the file we will write the data to after it is modified.
         String UpdatedDataName = Config.DG_Output_File_Name;
         PrintWriter tokensWriter = new PrintWriter( UpdatedDataName );    

         // Get the dateForGeneration in format including the year
         dateForGeneration = normalizeDateFormat( dateForGeneration );

         // Loop until the end of the file.
         Integer row = 0;
         while (inputStream.hasNext()) {
            String data = inputStream.nextLine();     // Read the next line.
//            String[] values = data.split(",");        // Get the values from the line.  JUST \\t for tab delimited file instead.
            String[] values = data.split("\\t");        // Get the values from the line.  JUST \\t for tab delimited file instead.
            String OIValue = "Y";   
            String bidAskValue = "Y";  
            double startPrice = 0.0;   
            double price1Later = 0.0;  
            double price4Later = 0.0;                       
            double ThinkBack = 0.0;
            double buyWiggle = 0.0;  
            double ATMStraddlePrice = 0.0;
            String whichDay = values[ Config.DG_DayPlay ];
            boolean lineChanged = false;

            // If the values are for the current date, then insert them we have a valid row to process.
            dateInFile = normalizeDateFormat( values[ Config.DG_Date_Column ] );
            //              dateInFileFormatted = sdt.format(dateInFile);   /// Can't get this to work at this point.
            
            // If the values are for the current date, then process the line.
            if ( IsValidDate( dateInFile ) && (values.length >= Config.DG_NumberValidColumns ) ) {  
                lineChanged = true;
                Results_Text_Area.append("Working on data for symbol ... " + values[ Config.DG_Symbol_Column ] + "\n");
                System.out.println( "Working on data for symbol ... " + values[ Config.DG_Symbol_Column ] );
                ThinkBack = Double.parseDouble( values[ Config.DG_ThinkBack_Column ] );  
                String symbol = values[ Config.DG_Symbol_Column ].toUpperCase();      
                String DayOfWeek = values[ Config.DG_DayOfWeek_Column ];   
                // Entered from TOS as 193.28, so must represent by dividing by 100
                double IV_1_Series_Later = Double.parseDouble( values[ Config.DG_IV1SeriesLater_Column ] )/100;  
                double IV_4_Series_Later = Double.parseDouble( values[ Config.DG_IV4SeriesLater_Column ] )/100;        
                double strikeToUse = 0.0;

                // Get Option Chain Data for next friday expiry
                JSONObject OIsymbolData = SchwabAPI.getQuoteSpecifics(symbol, Config.DG_OpenInterestStrikes, "SINGLE");     
                double aveOI = getAveOI( OIsymbolData, Results_Text_Area );

                // Get Option Chain Data for next friday expiry
                JSONObject symbolData = SchwabAPI.getQuote(symbol, Config.DG_BidAskStrikes, "STRADDLE", false);               
                double minBidAskSpread = getMinBidAskSpread( symbolData, Results_Text_Area );
                // Gen Buy Wiggle
                buyWiggle = 0.1 * minBidAskSpread;
                // Determine the best strike price to use for Buy Wiggle, ATM Night B4
                // ------------------------------------------------------------------------------------
                startPrice = getStartPrice( symbolData, symbol, Results_Text_Area);
                // Lets get the data needed for B/A, OI, Buy Wiggle, strikeToUse
                strikeToUse = getATMStrike( symbolData, startPrice, Results_Text_Area );
                double year = 365.0;
                double days = 0.0;
                switch ( DayOfWeek ) {
                    case "Mon" :
                            days = 5.0;
                        break;
                    case "Tue" :
                            days = 4.0;
                        break;
                    case "Wed" :
                            days = 3.0;
                        break;
                    case "Thu" :
                            days = 2.0;
                        break;
                    default :
                            days = 1.0;
                        break;
                }
                double timeToExpiry     = days/year;  // 6/365 - Must be in terms of a year;
                // Gen ATM Night B4
                ATMStraddlePrice = getATMStraddlePrice( symbolData, strikeToUse, Results_Text_Area );

                // Gen OI OK?
                double dayRatio = Config.DG_PercentOfD1;
                if ( whichDay.equalsIgnoreCase("2") ) {
                    dayRatio = Config.DG_PercentOfD2;
                }

                if ( desiredDollars/(ATMStraddlePrice*dayRatio*100) <= aveOI ) {
                    OIValue = "Y";
                } else if ( desiredDollars/(ATMStraddlePrice*dayRatio*100) <= Config.DG_MinNumberContracts ) {     
                    OIValue = "?";
                } else {
                    OIValue = "N";
                }
                Results_Text_Area.append(" OI Information \n");
                Results_Text_Area.append("desired $  ..." + desiredDollars + "\n");
                Results_Text_Area.append("ATM Straddle  ..." + ATMStraddlePrice + "\n");
                Results_Text_Area.append("day ratio  ..." + dayRatio + "\n");
                Results_Text_Area.append("ave OI  ..." + aveOI + "\n");
                Results_Text_Area.append("# contracts  ..." + Config.DG_MinNumberContracts + "\n");
                Results_Text_Area.append("OIValue  ..." + OIValue + "\n");
                // Gen B/A OK?
                double percentOfStartPrice = minBidAskSpread/startPrice;
                if ( percentOfStartPrice <= Config.DG_BidAskGreenPercent ) {    
                    bidAskValue = "Y";
                } else if ( percentOfStartPrice <= Config.DG_BidAskYellowPercent ){     
                    bidAskValue = "?";
                } else {
                    bidAskValue = "N";
                }
                Results_Text_Area.append(" Bid Ask \n");
                Results_Text_Area.append("percentOfStartPrice  ..." + percentOfStartPrice + "\n");
                Results_Text_Area.append("minBidAskSpread  ..." + minBidAskSpread + "\n");
                Results_Text_Area.append("startPrice  ..." + startPrice + "\n");
                Results_Text_Area.append("bidAskValue  ..." + bidAskValue + "\n");
                // Gen Price 1 Series Later
                // Gen Price 4 Series Later
                price1Later = getCalculatedStraddlePrice( startPrice, strikeToUse, Config.DG_InterestRate, IV_1_Series_Later, timeToExpiry );
                price4Later = getCalculatedStraddlePrice( startPrice, strikeToUse, Config.DG_InterestRate, IV_4_Series_Later, timeToExpiry );                     
            }            
            // Update the appropriate columns
            // We will replace data in the following columns:  BD, BE, BO, BP; these are "Entry Point", "Day Range", "Max Possible Exit", "Ride to expiry"
            String Delimeter = ",";
            String newLine = "";   
            if ( lineChanged ) {  // Otherwise just copy the line
                for (int c = 0; c < values.length; c++ ) {
                    if ( c == Config.DG_OpenInterest_Column ) {
                        newLine = newLine + "," + OIValue;
                    } else if ( c == Config.DG_BidAsk_Column ) {
                        newLine = newLine + "," + bidAskValue;
                    } else if ( c == Config.DG_StartPrice_Column ) {
                        newLine = newLine + "," + df2.format( startPrice );
                    } else if ( c == Config.DG_1SeriesLater_Column ) {
                        newLine = newLine + "," + df2.format( price1Later );
                    } else if ( c == Config.DG_4SeriesLater_Column ) {
                        newLine = newLine + "," + df2.format( price4Later );
                    } else if ( c == Config.DG_ThinkBack_Column ) {
                        newLine = newLine + "," + df2.format( ThinkBack );
                    } else if ( c == Config.DG_BuyWiggle_Column ) {
                        newLine = newLine + "," + df2.format( buyWiggle );
                    } else if ( c == Config.DG_ATMNightB4_Column ) {
                        newLine = newLine + "," + df2.format( ATMStraddlePrice );
                    } else {
                        if ( c == 0 ) {
                            newLine = values[c];
                        } else {
                            newLine = newLine + "," + values[c];   
                        }
                    }
                } 
                // Only write these out if the line has meaningful data
                System.out.println( newLine );
                Results_Text_Area.append(Utility.getTimeStamp() + newLine + ".\n");
            } else {
                for (int c = 0; c < values.length; c++ ) {
                    newLine = newLine + values[c];
                    if ( c < values.length -1 ) {
                        newLine = newLine + ",";
                    }
                }
            }
            tokensWriter.println( newLine );
        }

        // Close the file.
        inputStream.close();
        // Close the newly created file
        tokensWriter.close();
        Results_Text_Area.append("DayGenData complete." + "\n");
      } catch (Exception ex) {
         Results_Text_Area.append("DayGenData FAILED!!!!!  Check source Data." + "\n");
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
      }
   }                        

   
/*
{
  "symbol": "MSFT",
  "status": "SUCCESS",
  "underlying": {
    "symbol": "MSFT", ...
  },
  "strategy": "SINGLE",
  "interestRate": 0.1,
  "underlyingPrice": 211.95499999999998,
  "putExpDateMap": {
    "2020-07-10:4": {
      "210.0": [
        {
          "putCall": "PUT",
          "symbol": "MSFT_071020P210",
          "description": "MSFT Jul 10 2020 210 Put (Weekly)",
          "totalVolume": 10087,
          "volatility": 33.416,
          "delta": -0.4,
          "openInterest": 526,
          "expirationDate": 1594411200000,
          "daysToExpiration": 4,
          "lastTradingDay": 1594425600000,
        }
      ],
      "212.5": [
        { ... repeat ...
        }
      ],
      "215.0": [
        { ... repeat ...
        }
      ]
    }
  },
  "callExpDateMap": {  ... repeat of Put Map ...
    "2020-07-10:4": 
    {
      "210.0": [
        {
        }
      ],
      "212.5": [
        {
        }
      ],
      "215.0": [
        {
        }
      ]
    }
*/
   private static double getAveOI( JSONObject symbolData, javax.swing.JTextArea Results_Text_Area ) {
        int putCount = 0;
        int putSum = 0;
        int callCount = 0;
        int callSum = 0;
        try {
           // Now loop thru removing the first item in the list over and over again until half the count is removed.
            // Now loop thru removig the items in the array after the desired count until they are all removed.
            // Should be left with the desired amount.
            JSONObject putExpDateMap = new JSONObject( symbolData.getJSONObject("putExpDateMap").toString() );
            JSONObject callExpDateMap = new JSONObject( symbolData.getJSONObject("callExpDateMap").toString() );
            String put = putExpDateMap.toString();
            String[] putValues = put.split(",");
            // Find one that has OpenInterest and split it with ":" and then value[1] is your number.
            for (int i = 0; i < putValues.length; i++) {
                if ( putValues[i].contains("openInterest")) {
                    String[] OIData = putValues[i].split(":");
                    putCount++;
                    putSum = putSum + Integer.parseInt( OIData[1] );
                }
            }
            String call = callExpDateMap.toString();
            String[] callValues = call.split(",");
            // Find one that has OpenInterest and split it with ":" and then value[1] is your number.
            for (int i = 0; i < callValues.length; i++) {
                if ( callValues[i].contains("openInterest")) {
                    String[] OIData = callValues[i].split(":");
                    callCount++;
                    callSum = callSum + Integer.parseInt( OIData[1] );
                }
            }
       } catch (Exception ex) {
          Results_Text_Area.append("Unable to get OI data." + "\n");
          Utility.dumpExceptionInfo(ex);
          ex.printStackTrace();
       }
       return Math.min(callSum/callCount, putSum/putCount);
   }

   // Assumes the data only has information for X strikes (starting with 3)
   private static double getMinBidAskSpread( JSONObject symbolData, javax.swing.JTextArea Results_Text_Area ) {
       double minBidAsk = 100000.0;
       try {
            // Now loop thru removing the first item in the list over and over again until half the count is removed.
            // Now loop thru removig the items in the array after the desired count until they are all removed.
            // Should be left with the desired amount.
            JSONArray MonthlyList = new JSONArray( symbolData.getJSONArray("monthlyStrategyList").toString() );
            // Assuming we only get 1 expiration series of options
            JSONObject FridayExpiry = new JSONObject( MonthlyList.getJSONObject(0).toString() );                    
            // Get the list of strikes - both calls and puts
            JSONArray OptionsList = new JSONArray( FridayExpiry.getJSONArray("optionStrategyList").toString() );

            double currentLow = 10000.0;
            // Find the position of the closest ATM strike
            for (int m = 0; m < OptionsList.length(); m++) {
                JSONObject CurrentOption = new JSONObject( OptionsList.getJSONObject( m ).toString() );
                // Get the current bid/ask spread for the Straddle
                // Make sure there is no bad data for Bid/Ask values   // TBD
                JSONObject SecondaryLeg = new JSONObject(CurrentOption.getJSONObject("secondaryLeg").toString());
                JSONObject PrimaryLeg = new JSONObject(CurrentOption.getJSONObject("primaryLeg").toString());
                double theBid = PrimaryLeg.getDouble("bid") + SecondaryLeg.getDouble("bid");  
                double theAsk = PrimaryLeg.getDouble("ask") + SecondaryLeg.getDouble("ask");
                if ( minBidAsk > ( theAsk-theBid ) ) {
                    minBidAsk = (theAsk-theBid);
                }
            }
       } catch (Exception ex) {
          Results_Text_Area.append("Unable to get Bid/Ask data." + "\n");
          Utility.dumpExceptionInfo(ex);
          ex.printStackTrace();
       }
       return minBidAsk;
   }


   // Assumes the data only has information for X strikes (starting with 3)
   private static double getATMStraddlePrice( JSONObject symbolData, double ATMStrike, javax.swing.JTextArea Results_Text_Area ) {
       double ATMStraddlePrice = 10000.0;
       double LastStrike = 0.0;
       try {
            // Now loop thru removing the first item in the list over and over again until half the count is removed.
            // Now loop thru removig the items in the array after the desired count until they are all removed.
            // Should be left with the desired amount.
            JSONArray MonthlyList = new JSONArray( symbolData.getJSONArray("monthlyStrategyList").toString() );
            // Assuming we only get 1 expiration series of options
            JSONObject FridayExpiry = new JSONObject( MonthlyList.getJSONObject(0).toString() );                    
            // Get the list of strikes - both calls and puts
            JSONArray OptionsList = new JSONArray( FridayExpiry.getJSONArray("optionStrategyList").toString() );

            double straddlePriceAboveATMStrike = 10000.0;
            double straddlePriceATMStrike = 10000.0;
            double straddlePriceBelowATMStrike = 10000.0;
            boolean ATMFound = false;

            // Find the position of the closest ATM strike
            for (int m = 0; m < OptionsList.length(); m++) {
                JSONObject CurrentOption = new JSONObject( OptionsList.getJSONObject( m ).toString() );
                double CurrentStrike = Double.parseDouble( CurrentOption.getString("strategyStrike") );
                // Calculate the straddle price for this strike
                JSONObject SecondaryLeg = new JSONObject(CurrentOption.getJSONObject("secondaryLeg").toString());
                JSONObject PrimaryLeg = new JSONObject(CurrentOption.getJSONObject("primaryLeg").toString());
                double theBid = PrimaryLeg.getDouble("bid") + SecondaryLeg.getDouble("bid");  
                double theAsk = PrimaryLeg.getDouble("ask") + SecondaryLeg.getDouble("ask");
                if (ATMFound == true) {
                    // Now record the value for the strike below - now have 3 straddle prices to choose from
                    straddlePriceBelowATMStrike = (theAsk-theBid)/2 + theBid;
                } else {
                    if ( Math.abs(CurrentStrike - ATMStrike) < 0.1 ) {
                        // This is the ATMStrike
                        straddlePriceATMStrike = (theAsk-theBid)/2 + theBid;
                        ATMFound = true;
                    } else {
                        // Keep updating this value until the ATMStrike is encountered
                        straddlePriceAboveATMStrike = (theAsk-theBid)/2 + theBid;
                    }
                }
            }
            if (ATMFound == true) {
                ATMStraddlePrice = Math.min( Math.min(straddlePriceBelowATMStrike, straddlePriceATMStrike), straddlePriceAboveATMStrike );
            }
       } catch (Exception ex) {
          Results_Text_Area.append("Unable to get Bid/Ask data." + "\n");
          Utility.dumpExceptionInfo(ex);
          ex.printStackTrace();
       }
       return ATMStraddlePrice;
   }   
   
   private static double getATMStrike( JSONObject symbolData, double LastStockPrice, javax.swing.JTextArea Results_Text_Area ) {
       double LastStrike = 0.0;
       try {
                // Now loop thru removing the first item in the list over and over again until half the count is removed.
                // Now loop thru removig the items in the array after the desired count until they are all removed.
                // Should be left with the desired amount.
                JSONArray MonthlyList = new JSONArray( symbolData.getJSONArray("monthlyStrategyList").toString() );
                // Assuming we only get 1 expiration series of options
                JSONObject FridayExpiry = new JSONObject( MonthlyList.getJSONObject(0).toString() );                    
                // Get the list of strikes - both calls and puts
                JSONArray OptionsList = new JSONArray( FridayExpiry.getJSONArray("optionStrategyList").toString() );

                // Find the position of the closest ATM strike
                for (int m = 0; m < OptionsList.length(); m++) {
                    JSONObject CurrentOption = new JSONObject( OptionsList.getJSONObject( m ).toString() );
                    double CurrentStrike = Double.parseDouble( CurrentOption.getString("strategyStrike") );
                    if (LastStockPrice < CurrentStrike) {
                        // We found the first strike below the last stock price so that is the ATM strike we will use, now see which strike it is closest too
                        if ( Math.abs(LastStockPrice - CurrentStrike) < Math.abs(LastStockPrice - LastStrike)) {
                           LastStrike = CurrentStrike;
                        } else {
                            // LastStrike = what is was before getting here
                        }
                        break;
                    } else {
                        LastStrike = CurrentStrike;
                    }
                }
       } catch (Exception ex) {
          Results_Text_Area.append("Unable to get ATM Strike." + "\n");
          Utility.dumpExceptionInfo(ex);
          ex.printStackTrace();
       }
       return LastStrike;
   }

   private static double getCalculatedStraddlePrice(double startPrice, double strikeToUse, double interestRate, double IV, double timeToExpiry) {
       return BlackScholes.getCall(startPrice, strikeToUse, interestRate, timeToExpiry, IV) +
              BlackScholes.getPut(startPrice, strikeToUse, interestRate, timeToExpiry, IV) ;
   }
   
   private static double getStartPrice(JSONObject symbolData, String Symbol, javax.swing.JTextArea Results_Text_Area) {
       double startPrice = 0.0;
       try {
          JSONObject Underlying = new JSONObject( symbolData.getJSONObject("underlying").toString() );
          startPrice = Underlying.getDouble("last");   
       } catch (Exception ex) {
            Results_Text_Area.append("Unable to parse date for symbol " + Symbol + "\n");
            Utility.dumpExceptionInfo(ex);
            ex.printStackTrace();
       }       
    return startPrice;
   }

   
   public static void GenDailyDataGoogle(javax.swing.JTextArea Results_Text_Area, String dateForGeneration, double desiredDollars, String sheetName)                                                      
   {                                                          
      // This procedure loads a .csv file where it will place generated stats: 
      //        OI OK?, B/A OK?, Start Price, Exp Price 1 Series Later, Exp Price 4 Series Later, Think Back Price, Buy Wiggle, and ATM Night B4.
      // It will place these generated and provided values into a new CSV file with the same name and the word "Updated_" placed at the front of the file name.
      try
         {
         String dateInFile = "";
         List<Request> requests = new ArrayList<>();

         // Read the data from the Current Q Tab in Google.
         String[] sheetNames = {sheetName};
         Spreadsheet googleSpreadSheet = GoogleOperations.GetSpreadsheetSheetInfo(sheetNames);
         Results_Text_Area.append("Starting generation ...");         
         int sheetId = googleSpreadSheet.getSheets().get(0).getProperties().getSheetId();
         
         // Must go back to what we had before and get the sheet data and do all the manipulation below.
         // TBD
         BatchGetValuesResponse CurrentQ = GoogleOperations.GetSheet(sheetName);
         List<ValueRange> VRs = CurrentQ.getValueRanges();
         ValueRange VR = VRs.get(0);
         List<List<Object>> values = VR.getValues();

         // Get the dateForGeneration in format including the year
        dateForGeneration = normalizeDateFormat( dateForGeneration );

         // Now loop thru the values which are rows and process them.
         for( int rowIndex = 0; rowIndex < values.size(); rowIndex++) {
            List<Object> row = values.get(rowIndex);
            // If the date is right then this line should be processed, otherwise skipped.
            String theDate = "";
            if (row.size() > 0) {                
                theDate = normalizeDateFormat( row.get( Config.DG_Date_Column ).toString() );
            }
            if ( theDate.equalsIgnoreCase(dateForGeneration) ) {
                String OIValue = "Y";   
                String bidAskValue = "Y";  
                double startPrice = 0.0;   
                double price1Later = 0.0;  
                double price4Later = 0.0;                       
                double ThinkBack = 0.0;
                double buyWiggle = 0.0;  
                double ATMStraddlePrice = 0.0;
                String whichDay = row.get( Config.DG_DayPlay ).toString();
                boolean lineChanged = false;

                // If the values are for the current date, then insert them we have a valid row to process.
                dateInFile = normalizeDateFormat( row.get( Config.DG_Date_Column ).toString() );
                //              dateInFileFormatted = sdt.format(dateInFile);   /// Can't get this to work at this point.
                // If the values are for the current date, then process the line.
                if ( IsValidDate( dateInFile ) && (row.size() >= Config.DG_NumberValidColumns ) ) {  
                    lineChanged = true;
                    Results_Text_Area.append("Working on data for symbol ... " + row.get( Config.DG_Symbol_Column ).toString() + "\n");
                    System.out.println( "Working on data for symbol ... " + row.get( Config.DG_Symbol_Column ).toString() );
                    ThinkBack = Double.parseDouble( row.get( Config.DG_ThinkBack_Column ).toString() );  
                    String symbol = row.get( Config.DG_Symbol_Column ).toString().toUpperCase();      
                    String DayOfWeek = row.get( Config.DG_DayOfWeek_Column ).toString();   
                    // Entered from TOS as 193.28, so must represent by dividing by 100
                    double IV_1_Series_Later = Double.parseDouble( row.get( Config.DG_IV1SeriesLater_Column ).toString() )/100;  
                    double IV_4_Series_Later = Double.parseDouble( row.get( Config.DG_IV4SeriesLater_Column ).toString() )/100;        
                    double strikeToUse = 0.0;

                    // Get Option Chain Data for next friday expiry
                    JSONObject OIsymbolData = SchwabAPI.getQuoteSpecifics(symbol, Config.DG_OpenInterestStrikes, "SINGLE");     
                    double aveOI = getAveOI( OIsymbolData, Results_Text_Area );

                    // Get Option Chain Data for next friday expiry
                    JSONObject symbolData = SchwabAPI.getQuote(symbol, Config.DG_BidAskStrikes, "STRADDLE", false);               
                    double minBidAskSpread = getMinBidAskSpread( symbolData, Results_Text_Area );
                    // Gen Buy Wiggle
                    buyWiggle = 0.1 * minBidAskSpread;
                    // Determine the best strike price to use for Buy Wiggle, ATM Night B4
                    // ------------------------------------------------------------------------------------
                    startPrice = getStartPrice( symbolData, symbol, Results_Text_Area);
                    // Lets get the data needed for B/A, OI, Buy Wiggle, strikeToUse
                    strikeToUse = getATMStrike( symbolData, startPrice, Results_Text_Area );
                    double year = 365.0;
                    double days = 0.0;
                    switch ( DayOfWeek ) {
                        case "Mon" :
                                days = 5.0;
                            break;
                        case "Tue" :
                                days = 4.0;
                            break;
                        case "Wed" :
                                days = 3.0;
                            break;
                        case "Thu" :
                                days = 2.0;
                            break;
                        default :
                                days = 1.0;
                            break;
                    }
                    double timeToExpiry     = days/year;  // 6/365 - Must be in terms of a year;
                    // Gen ATM Night B4
                    ATMStraddlePrice = getATMStraddlePrice( symbolData, strikeToUse, Results_Text_Area );

                    // Gen OI OK?
                    double dayRatio = Config.DG_PercentOfD1;
                    if ( whichDay.equalsIgnoreCase("2") ) {
                        dayRatio = Config.DG_PercentOfD2;
                    }

                    if ( desiredDollars/(ATMStraddlePrice*dayRatio*100) <= aveOI ) {
                        OIValue = "Y";
                    } else if ( desiredDollars/(ATMStraddlePrice*dayRatio*100) <= Config.DG_MinNumberContracts ) {     
                        OIValue = "?";
                    } else {
                        OIValue = "N";
                    }
                    Results_Text_Area.append(" OI Information \n");
                    Results_Text_Area.append("desired $  ..." + desiredDollars + "\n");
                    Results_Text_Area.append("ATM Straddle  ..." + ATMStraddlePrice + "\n");
                    Results_Text_Area.append("day ratio  ..." + dayRatio + "\n");
                    Results_Text_Area.append("ave OI  ..." + aveOI + "\n");
                    Results_Text_Area.append("# contracts  ..." + Config.DG_MinNumberContracts + "\n");
                    Results_Text_Area.append("OIValue  ..." + OIValue + "\n");
                    // Gen B/A OK?
                    double percentOfStartPrice = minBidAskSpread/startPrice;
                    if ( percentOfStartPrice <= Config.DG_BidAskGreenPercent ) {    
                        bidAskValue = "Y";
                    } else if ( percentOfStartPrice <= Config.DG_BidAskYellowPercent ){     
                        bidAskValue = "?";
                    } else {
                        bidAskValue = "N";
                    }
                    Results_Text_Area.append(" Bid Ask \n");
                    Results_Text_Area.append("percentOfStartPrice  ..." + percentOfStartPrice + "\n");
                    Results_Text_Area.append("minBidAskSpread  ..." + minBidAskSpread + "\n");
                    Results_Text_Area.append("startPrice  ..." + startPrice + "\n");
                    Results_Text_Area.append("bidAskValue  ..." + bidAskValue + "\n");
                    // Gen Price 1 Series Later
                    // Gen Price 4 Series Later
                    price1Later = getCalculatedStraddlePrice( startPrice, strikeToUse, Config.DG_InterestRate, IV_1_Series_Later, timeToExpiry );
                    price4Later = getCalculatedStraddlePrice( startPrice, strikeToUse, Config.DG_InterestRate, IV_4_Series_Later, timeToExpiry );                     
                }            
                // Update the appropriate columns
                // We will replace data in the following columns:  BD, BE, BO, BP; these are "Entry Point", "Day Range", "Max Possible Exit", "Ride to expiry"
                String Delimeter = ",";
                String newLine = "";   
                if ( lineChanged ) {  // Otherwise just copy the line
                    for (int c = 0; c < row.size(); c++ ) {
                        if ( c == Config.DG_OpenInterest_Column ) {
                            newLine = newLine + "," + OIValue;
                            requests.add( GoogleOperations.AddRequest(sheetId, rowIndex, c, OIValue, ";") );
                        } else if ( c == Config.DG_BidAsk_Column ) {
                            newLine = newLine + "," + bidAskValue;
                            requests.add( GoogleOperations.AddRequest(sheetId, rowIndex, c, bidAskValue, ";") );
                        } else if ( c == Config.DG_StartPrice_Column ) {
                            newLine = newLine + "," + df2.format( startPrice );
                            requests.add( GoogleOperations.AddRequest(sheetId, rowIndex, c, df2.format( startPrice ), ";" ) );
                        } else if ( c == Config.DG_1SeriesLater_Column ) {
                            newLine = newLine + "," + df2.format( price1Later );
                            requests.add( GoogleOperations.AddRequest(sheetId, rowIndex, c, df2.format( price1Later ), ";") );
                        } else if ( c == Config.DG_4SeriesLater_Column ) {
                            newLine = newLine + "," + df2.format( price4Later );
                            requests.add( GoogleOperations.AddRequest(sheetId, rowIndex, c, df2.format( price4Later ), ";") );
                        } else if ( c == Config.DG_ThinkBack_Column ) {
                            newLine = newLine + "," + df2.format( ThinkBack );
                            requests.add( GoogleOperations.AddRequest(sheetId, rowIndex, c, df2.format( ThinkBack ), ";") );
                        } else if ( c == Config.DG_BuyWiggle_Column ) {
                            newLine = newLine + "," + df2.format( buyWiggle );
                            requests.add( GoogleOperations.AddRequest(sheetId, rowIndex, c, df2.format( buyWiggle ), ";") );
                        } else if ( c == Config.DG_ATMNightB4_Column ) {
                            newLine = newLine + "," + df2.format( ATMStraddlePrice );
                            requests.add( GoogleOperations.AddRequest(sheetId, rowIndex, c, df2.format( ATMStraddlePrice ), ";") );
                        } else {
                            if ( c == 0 ) {
                                newLine = row.get(c).toString();
                            } else {
                                newLine = newLine + "," + row.get(c).toString();   
                            }
                        }
                    } 
                    // Only write these out if the line has meaningful data
                    System.out.println( newLine );
                    Results_Text_Area.append(Utility.getTimeStamp() + newLine + ".\n");
                } else {
                    for (int c = 0; c < row.size(); c++ ) {
                        newLine = newLine + row.get(c).toString();
                        if ( c < row.size() -1 ) {
                            newLine = newLine + ",";
                        }
                    }
                }
            }
//            values.set(rowIndex,row);
        }
        // Now that the data is updated, put it back out to the Google spreadsheet
//        VR.setValues(values);
//        VRs.set(0, VR);   //  TBD only do if requests are there to update.
        if ( requests.size() > 0 ) {
            GoogleOperations.SetCurrentQData( requests );
        }
        
        Results_Text_Area.append("GenDailyDataGoogle complete." + "\n");
      } catch (Exception ex) {
         Results_Text_Area.append("GenDailyDataGoogle FAILED!!!!!  Check source Data." + "\n");
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
      }
   }                        

   private static String normalizeDateFormat( String thisDate ) {
        String updatedDate = thisDate;
        if (updatedDate.length() <= 5) {
            // Append the year
            updatedDate = updatedDate.concat("/" + getYear());
        }
        String firstChar = updatedDate.substring(0, 1);
        if (firstChar.equalsIgnoreCase("0")) {
            updatedDate = updatedDate.substring(1);
        }
        return updatedDate;
    }

    public static void GenTableFIle( javax.swing.JTable Symbol_Data_Table, javax.swing.JTextArea Results_Text_Area, String dateForGeneration, String sheetName) {
        // This procedure loads the Straddle Data from google sheet directly.

        // Note:  For now, I simply copied the rows from the main Straddles Data sheet
        //        and pasted them in a new Excel spreadsheet.  Then I saved the new
        //        Excel spreadsheet as a .csv file.
        //
        // Note:  The following might work to filter the main sheet so that all we have
        //        to do is highlight the current day's rows, and then save as a .csv file.
        //
        //          Create a new sheet (Sheet2) within the spreadsheet.
        //          Set the A1 cell to be =filter(Sheet1!A:X, Sheet1!A:A>1). (See docs on the filter function.)
        //          You should then be able to save or export Sheet2 as CSV with only the filtered values.
        //
        // Clear the existing table.
        try {
            String dateInFile = "";
            int tableRow = 0;

            // Get the dateForGeneration in format including the year
            dateForGeneration = normalizeDateFormat(dateForGeneration);

            // Read the file.
            BatchGetValuesResponse GUIDownload = GoogleOperations.GetSheet( sheetName );
            List<ValueRange> VRs = GUIDownload.getValueRanges();
            ValueRange VR = VRs.get(0);
            List<List<Object>> values = VR.getValues();

            // Now loop thru the values which are rows and process them.
            for( int rowIndex = 0; rowIndex < values.size(); rowIndex++) {
               List<Object> row = values.get(rowIndex);

               // If the values are for the current date, then insert them into the table.
               dateInFile = row.get( Config.TF_Date_Column ).toString();   // Date is 0 in the list    
               // Get the dateForGeneration in format including the year
               dateInFile = normalizeDateFormat( dateInFile );
               if (dateForGeneration.equals(dateInFile))
                  {  
                   Symbol_Data_Table.setValueAt(row.get( Config.TF_Symbol_Column ).toString(), tableRow, GUIData.symbolColumn); // This is doing something crazy to the table
                   Symbol_Data_Table.setValueAt(Double.parseDouble(row.get( Config.TF_MaxPrice_Column ).toString()), tableRow, GUIData.maxPriceColumn);  // This is doing something crazy to the table
                   Symbol_Data_Table.setValueAt(Double.parseDouble(row.get( Config.TF_MinMove_Column ).toString()), tableRow, GUIData.minMoveColumn);
                   Symbol_Data_Table.setValueAt(Double.parseDouble(row.get( Config.TF_AveMove_Column ).toString()), tableRow, GUIData.aveMoveColumn);
                   Symbol_Data_Table.setValueAt(Double.parseDouble(row.get( Config.TF_BuyWiggle_Column ).toString()), tableRow, GUIData.buyWiggleColumn);
                   // Min Wait = EOW Min Value as of 1/12/24
   //                Symbol_Data_Table.setValueAt(Integer.parseInt(row.get( Config.TF_MinWait_Column ).toString()), tableRow, GUIData.minWaitColumn);
                   Symbol_Data_Table.setValueAt(Double.parseDouble(row.get( Config.TF_EOWMin_Column ).toString()), tableRow, GUIData.EOWMinColumn);
                   // Ave Wait = EOW Ave Value as of 1/12/24
    //               Symbol_Data_Table.setValueAt(Integer.parseInt(row.get( Config.TF_MaxWait_Column ).toString()), tableRow, GUIData.maxWaitColumn);
                   Symbol_Data_Table.setValueAt(Double.parseDouble(row.get( Config.TF_EOWAve_Column ).toString()), tableRow, GUIData.EOWAveColumn);
                   Symbol_Data_Table.setValueAt(Double.parseDouble(row.get( Config.TF_PercentGoal_Column).toString()), tableRow, GUIData.percentGoalColumn);
                   Symbol_Data_Table.setValueAt(Integer.parseInt(row.get( Config.TF_ChaseTime_Column ).toString()), tableRow, GUIData.chaseWaitTimeColumn);
                   Symbol_Data_Table.setValueAt(Double.parseDouble(row.get( Config.TF_ChaseAmount_Column ).toString()), tableRow, GUIData.chaseAmountColumn);
                   Symbol_Data_Table.setValueAt(Double.parseDouble(row.get( Config.TF_TradeAmount1_Column ).toString()), tableRow, GUIData.tradeAmount1Column);
                   Symbol_Data_Table.setValueAt(Double.parseDouble(row.get( Config.TF_TradeAmount2_Column ).toString()), tableRow, GUIData.tradeAmount2Column);
                   Symbol_Data_Table.setValueAt(Double.parseDouble(row.get( Config.TF_TradeAmount3_Column ).toString()), tableRow, GUIData.tradeAmount3Column);
                   Symbol_Data_Table.setValueAt(Double.parseDouble(row.get( Config.TF_TradeAmount4_Column ).toString()), tableRow, GUIData.tradeAmount4Column);
                   Symbol_Data_Table.setValueAt(Double.parseDouble(row.get( Config.TF_TradeAmount5_Column ).toString()), tableRow, GUIData.tradeAmount5Column);
                   Symbol_Data_Table.setValueAt(Integer.parseInt(row.get( Config.TF_MinutesToRun_Column ).toString()), tableRow, GUIData.minutesToRunColumn);
                   Symbol_Data_Table.setValueAt(row.get( Config.TF_JustGetIn_Column ).toString(), tableRow, GUIData.justGetInColumn);
                   Symbol_Data_Table.setValueAt(row.get( Config.TF_BlueLine_Column ).toString(), tableRow, GUIData.blueLineColumn);
                   Symbol_Data_Table.setValueAt(Integer.parseInt(row.get( Config.TF_NumberOfStrikes_Column ).toString()), tableRow, GUIData.numberOfStrikesColumn);
                   tableRow++;
                  }
               }
            TOS_Charts.Google_Gen_TOS_Study_Code( dateForGeneration, sheetName );

            // Close the file.
            Results_Text_Area.append("Load worked from " + sheetName +  "!\n");
        } catch (Exception ex) {
            Results_Text_Area.append("LOAD FAILED from " + sheetName + "!!!!!  Check source Data - likely multiple lines in one cell is the issue" + "\n");
            Utility.dumpExceptionInfo(ex);
            ex.printStackTrace();
        }
    }
}
