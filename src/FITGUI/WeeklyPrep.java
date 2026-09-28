/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FITGUI;

import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 *
 * @author thela
 */
public class WeeklyPrep {
    private static JSONArray lowValues = new JSONArray();
    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private static final String NoValue = "Nope";
    private static final String symbol = "symbol";
    private static final String mark = "mark";
    private static final String time = "time";


    private static List<String> CopyList(List<String> Source) {
        List<String> Destination = new ArrayList<>();
        for (int i = 0; i < Source.size(); i++ ) {
            Destination.add( Source.get(i) );
        }
        return Destination;
    }
    //  This will look at the "Weekly E's" data and fill in the "Current Q" tab based on a Monday start date, for 1 week of data.
    //  Basically, generate the D1 and D2 lines for all the stocks on the appropriate days.  That being:
    //      Mon, Tue, Wed:  D1 only
    //      Thu, Fri: D1 and D2
    //  It needs:  Weekly E tab to look at, Current Q tab to generate data, Monday starting Date
    //  Assumes:   12/31/<current year> with ACN has the example lines to copy for D1 and D2.
    public static void GoogleWeeklyPrep(javax.swing.JTextArea Results_Text_Area, String WeeklySheetName, String CurrentQSheetName, String Last8QName, String StartingDate) {
        // TODO add your handling code here:
        try {
                // Get the data
                List<Request> requests = new ArrayList<>();

                // Read the data from the Sheets needed in the spreadsheet.
                String[] sheetNames = {WeeklySheetName, CurrentQSheetName, Last8QName};
                Spreadsheet googleSpreadSheet = GoogleOperations.GetSpreadsheetSheetInfo(sheetNames);
                Results_Text_Area.append("Starting processing Weekly Prep ...");         
                int WeeklySheetId = 0;  // S.getSheets().get(0).getProperties().getSheetId();
                int CurrentQSheetId = 0; // S.getSheets().get(1).getProperties().getSheetId();
                int Last8QId = 0; // S.getSheets().get(2).getProperties().getSheetId();
                for (int i = 0; i < 3; i++) {
                    String Title = googleSpreadSheet.getSheets().get(i).getProperties().getTitle();
                    if (Title.equalsIgnoreCase(WeeklySheetName)) {
                        WeeklySheetId = googleSpreadSheet.getSheets().get(i).getProperties().getSheetId();
                    } 
                    if (Title.equalsIgnoreCase(CurrentQSheetName)) {
                        CurrentQSheetId = googleSpreadSheet.getSheets().get(i).getProperties().getSheetId();
                    } 
                    if (Title.equalsIgnoreCase(Last8QName)) {
                        Last8QId = googleSpreadSheet.getSheets().get(i).getProperties().getSheetId();
                    } 
                }
         
                // Data for the three sheets is gathered here.
                BatchGetValuesResponse WeeklyData = GoogleOperations.GetSheet(WeeklySheetName);
                List<ValueRange> WeeklyVRs = WeeklyData.getValueRanges();
                ValueRange WeeklyVR = WeeklyVRs.get(0);
                List<List<Object>> WeeklyValues = WeeklyVR.getValues();  

                BatchGetValuesResponse CurrentQ = GoogleOperations.GetSheet(CurrentQSheetName);
                List<ValueRange> CurrentQVRs = CurrentQ.getValueRanges();
                ValueRange CurrentQVR = CurrentQVRs.get(0);
                List<List<Object>> CurrentQValues = CurrentQVR.getValues();  

                // Loop thru the CurrentQ data until you find the examples for D1 and D2
                // Note the rows for D1 and D2 example data for use below into local variables.
                boolean D1_Found = false;
                boolean D2_Found = false;
                int D1_Row = -1;
                int D2_Row = -1;
                int LastRowCurrentQ = -1;
                int OriginalLastRowCurrentQ = -1;
                int DimensionGrowth = 0;
                for( int rowIndex = 0; rowIndex < CurrentQValues.size(); rowIndex++) {
                    List<Object> row = CurrentQValues.get(rowIndex);
                    if ( row.size() > 0 ) {
                        if ( row.get( Config.DG_Symbol_Column ).toString().equalsIgnoreCase( "ACN" ) &&
                             row.get( Config.DG_Date_Column).toString().equalsIgnoreCase( "12/31" ) &&
                             row.get( Config.DG_DayPlay).toString().equalsIgnoreCase("1") &&
                             D1_Found == false) {
                            D1_Row = rowIndex;
                            D1_Found = true;
                        }
                        if ( row.get( Config.DG_Symbol_Column ).toString().equalsIgnoreCase( "ACN" ) &&
                             row.get( Config.DG_Date_Column).toString().equalsIgnoreCase( "12/31" ) &&
                             row.get( Config.DG_DayPlay).toString().equalsIgnoreCase("2") &&
                             D2_Found == false) {
                            D2_Row = rowIndex;
                            D2_Found = true;
                        }
                    }
                    if ( ( D1_Found == true ) && ( D2_Found == true ) ) {
                        // Goto the end of the data in CurrentQ - this is where items will begin to be added.
                        LastRowCurrentQ = CurrentQValues.size();
                        OriginalLastRowCurrentQ = LastRowCurrentQ;
                        break;
                    }  
                }  // Ideally all below is w/in if statement for D1_Found and D2_Found
                // Loop thru the Weekly data until find the first date.
                int StartingWeeklyRow = -1;
                boolean YearFound = false;
                Calendar now = Calendar.getInstance();
                int year = now.get(Calendar.YEAR);
                String CurrentYear = String.valueOf(year);
                for( int WeeklyRowIndex = 0; WeeklyRowIndex < WeeklyValues.size(); WeeklyRowIndex++) {
                    List<Object> WeeklyRow = WeeklyValues.get(WeeklyRowIndex);
                    // Move thru the lists until you see year 2021 in Column B
                    if ( YearFound == false ) {
                        if ( WeeklyRow.size() > 1 ) {
                            if ( WeeklyRow.get( 1 ).toString().equalsIgnoreCase( CurrentYear ) ) {
                                YearFound = true;
                            }
                        }
                    } else {
                        if ( WeeklyRow.size() > 6 ) {  // Looking for rows with dates on them, not blank rows.
                            if ( OHLC.IsValidDate( WeeklyRow.get(2).toString() ) ) {
                                // NOTE:  This relies heavily on the date being exact in terms of the string entered to look at start of week.
/*                                DateFormat format = new SimpleDateFormat("dd/MM");
                                SimpleDateFormat sdf = new SimpleDateFormat("M/d");
                                java.util.Date d = sdf.parse( StartingDate );    
                                String LookingForDate = d.toString();
                                d = sdf.parse( WeeklyRow.get( 2 ).toString() );
                                String DateInData = d.toString();    */
                                if ( WeeklyRow.get( 2 ).toString().equalsIgnoreCase( StartingDate ) ) {   // 3rd Column is Monday
                                    StartingWeeklyRow = WeeklyRowIndex + 1;  // Go down 1 row below the date row.
                                    break;
                                }
                            }
                        }
                    }
                }
                
                // Now loop thru each day of the week
                List<Object> B4OpenRow = WeeklyValues.get(StartingWeeklyRow);
                List<Object> AfterMarketCloseRow = WeeklyValues.get(StartingWeeklyRow+1);
                List<String> D2List = new ArrayList<>();
                List<String> D2ThuList = new ArrayList<>();
                List<String> D2FriList = new ArrayList<>();
                // Days of week start in column 3 - value 2.
                for ( int DayOfWeek = 2; DayOfWeek < 7; DayOfWeek++) {
                    // Now look at Before Market Open symbols and add lines for them
                    String AnyStocks = B4OpenRow.get( DayOfWeek ).toString();
                    String[] Stocks = AnyStocks.split(",");
                    // Process Each Stock listed
                    for ( int StockIndex = 0; StockIndex < Stocks.length; StockIndex++ ) {
                        String Stock = Stocks[ StockIndex ];
                        if ( Stock.length() > 0 ) {
                            // Column 0 = date
                            requests.add( GoogleOperations.AddRequest(CurrentQSheetId, LastRowCurrentQ, 0, WeeklyValues.get(StartingWeeklyRow-1).get(DayOfWeek).toString(), ";") );
                            // Column 1 = symbol
                            requests.add( GoogleOperations.AddRequest(CurrentQSheetId, LastRowCurrentQ, 1, Stock, ";") );
                            GridRange SourceGR = new GridRange().
                                        setSheetId( CurrentQSheetId ).
                                        setStartRowIndex( D1_Row ).
                                        setEndRowIndex( D1_Row + 1 ).
                                        setStartColumnIndex( 2 ).
                                        setEndColumnIndex( CurrentQValues.get(D1_Row).size() - 1 );
                            GridRange DestinationGR = new GridRange().
                                        setSheetId( CurrentQSheetId ).
                                        setStartRowIndex( LastRowCurrentQ ).
                                        setEndRowIndex( LastRowCurrentQ + 1 ).
                                        setStartColumnIndex( 2 ).
                                        setEndColumnIndex( CurrentQValues.get(D1_Row).size() - 1 );
                            requests.add( GoogleOperations.CopyPasteAddRequest( SourceGR, DestinationGR ) );
                            LastRowCurrentQ++;
                            DimensionGrowth++;
                            D2List.add( Stock );
                        }
                    }
                    
                    // Here is where we add lines for the D2 stocks into the table, but only for Thu and Fri
                    if ( DayOfWeek == 4 ) {
                        D2ThuList = CopyList(D2List);
                    } else {
                        if ( DayOfWeek == 5 ) {
                            D2FriList = CopyList(D2List);
                            D2List = CopyList(D2ThuList);
                        } else {
                            if ( DayOfWeek == 6 ) {
                                D2List = CopyList(D2FriList);
                            }
                        }
                    }
                    if ( ( DayOfWeek == 5 ) || ( DayOfWeek == 6) ) {  // 5 = Thu, 6 = Fri
                        for ( int d2 = 0; d2 < D2List.size(); d2++ ) {
                            // Column 0 = date
                            requests.add( GoogleOperations.AddRequest(CurrentQSheetId, LastRowCurrentQ, 0, WeeklyValues.get(StartingWeeklyRow-1).get(DayOfWeek).toString(), ";") );
                            // Column 1 = symbol
                            requests.add( GoogleOperations.AddRequest(CurrentQSheetId, LastRowCurrentQ, 1, D2List.get( d2 ).toString(), ";") );
                            GridRange SourceGR = new GridRange().
                                        setSheetId( CurrentQSheetId ).
                                        setStartRowIndex( D2_Row ).
                                        setEndRowIndex( D2_Row + 1 ).
                                        setStartColumnIndex( 2 ).
                                        setEndColumnIndex( CurrentQValues.get(D2_Row).size() - 1 );
                            GridRange DestinationGR = new GridRange().
                                        setSheetId( CurrentQSheetId ).
                                        setStartRowIndex( LastRowCurrentQ ).
                                        setEndRowIndex( LastRowCurrentQ + 1 ).
                                        setStartColumnIndex( 2 ).
                                        setEndColumnIndex( CurrentQValues.get(D2_Row).size() - 1 );
                            requests.add( GoogleOperations.CopyPasteAddRequest( SourceGR, DestinationGR ) );
                            LastRowCurrentQ++;
                            DimensionGrowth++;
                        }
                    }
                    D2List.clear();
// TBD - Add a line here - black w/ xxx in first column.                    
                    // Now look at After Market Close symbols and add lines for them
                    AnyStocks = AfterMarketCloseRow.get( DayOfWeek ).toString();
                    Stocks = AnyStocks.split(",");
                    
                    // Process Each Stock listed
                    for ( int StockIndex = 0; StockIndex < Stocks.length; StockIndex++ ) {
                        String Stock = Stocks[ StockIndex ];
                        if ( Stock.length() > 0 ) {
                            // Column 0 = date
                            requests.add( GoogleOperations.AddRequest(CurrentQSheetId, LastRowCurrentQ, 0, WeeklyValues.get(StartingWeeklyRow-1).get(DayOfWeek+1).toString(), ";") );
                            // Column 1 = symbol
                            requests.add( GoogleOperations.AddRequest(CurrentQSheetId, LastRowCurrentQ, 1, Stock, ";") );
                            GridRange SourceGR = new GridRange().
                                        setSheetId( CurrentQSheetId ).
                                        setStartRowIndex( D1_Row ).
                                        setEndRowIndex( D1_Row + 1 ).
                                        setStartColumnIndex( 2 ).
                                        setEndColumnIndex( CurrentQValues.get(D1_Row).size() - 1 );
                            GridRange DestinationGR = new GridRange().
                                        setSheetId( CurrentQSheetId ).
                                        setStartRowIndex( LastRowCurrentQ ).
                                        setEndRowIndex( LastRowCurrentQ + 1 ).
                                        setStartColumnIndex( 2 ).
                                        setEndColumnIndex( CurrentQValues.get(D1_Row).size() - 1 );
                            requests.add( GoogleOperations.CopyPasteAddRequest( SourceGR, DestinationGR ) );
                            LastRowCurrentQ++;
                            DimensionGrowth++;
                            D2List.add( Stock );
                        }
                    }
                }

                // First add in the extra rows needed to support the new data.
                if ( DimensionGrowth > 0 ) {
                    List<Request> Drequests = new ArrayList<>();
                    Drequests.add( GoogleOperations.AppendDimensionRequest( CurrentQSheetId, DimensionGrowth ) );
                    if ( Drequests.size() > 0 ) {
                        GoogleOperations.SetCurrentQData( Drequests );
                        Drequests.clear();
                    }
                }
                // Now place all the data, formats, etc.
                if ( requests.size() > 0 ) {
                    GoogleOperations.SetCurrentQData( requests );
                    requests.clear();
                }
                
                // Now must go thru them all again and add in data from the Last8Q's into columns AE and AF 
                // Reload the CurrentQ spreadsheet here to get updated information.
                CurrentQ = GoogleOperations.GetSheet(CurrentQSheetName);
                CurrentQVRs = CurrentQ.getValueRanges();
                CurrentQVR = CurrentQVRs.get(0);
                CurrentQValues = CurrentQVR.getValues();  

                BatchGetValuesResponse Last8Q = GoogleOperations.GetSheet(Last8QName);
                List<ValueRange> Last8QVRs = Last8Q.getValueRanges();
                ValueRange Last8QVR = Last8QVRs.get(0);
                List<List<Object>> Last8QValues = Last8QVR.getValues();  
                
                // Loop thru for the symbol and D1 or D2 match from new lines in CurrentQ with Last8Q's.
                for( int rowIndex = OriginalLastRowCurrentQ; rowIndex < CurrentQValues.size(); rowIndex++) {
                    // Must loop thru Last 8 Q's for each Symbol and D1/D2 needed
                    List<Object> row = CurrentQValues.get(rowIndex);
                    if ( row.size() > 0 ) {
                        String Symbol = row.get( Config.DG_Symbol_Column ).toString();
                        String WhichDay = row.get( Config.DG_DayPlay ).toString();
                        String PrevTradeEntries = "";
                        String PrevTradeResults = "";
                        for ( int Last8Index = 0; Last8Index < Last8QValues.size(); Last8Index++ ) {
                            List<Object> Last8Row = Last8QValues.get( Last8Index );
                            if ( Last8Row.size() > 52 ) {
                                if ( ( Last8Row.get( Config.DG_Symbol_Column ).toString().equalsIgnoreCase( Symbol ) ) &&
                                     ( Last8Row.get( Config.DG_DayPlay ).toString().equalsIgnoreCase( WhichDay ) ) ) {
                                    // Found match collect and build the string
                                    if ( Double.parseDouble(Last8Row.get( Config.DG_ActualEntry ).toString()) > 0.01 ) {
//                                    if ( !Last8Row.get( Config.DG_ActualEntry ).toString().equalsIgnoreCase("0") ) {  // Line above on 7/10/22
                                        if ( PrevTradeEntries != "" ) {
                                            PrevTradeEntries = PrevTradeEntries + " ";
                                            PrevTradeResults = PrevTradeResults + " ";
                                        }
                                        String[] DateToUse = Last8Row.get( Config.DG_Date_Column ).toString().split("/");
                                        PrevTradeEntries =  PrevTradeEntries + 
                                                            df2.format( Double.parseDouble( Last8Row.get( Config.DG_ActualEntry ).toString() ) ) + " " +
                                                            Last8Row.get( Config.DG_DayOfWeek_Column ).toString().substring(0, 2) + " " +
                                                            DateToUse[0] + "/" + DateToUse[2];
                                        PrevTradeResults =  PrevTradeResults + 
                                                            Last8Row.get( Config.DG_Profit ).toString() + "/ " +
                                                            Last8Row.get( Config.DG_Duration ).toString() + "m";
                                    }
                                }
                            }
                        }
                        if ( PrevTradeEntries != "" ) {
                            requests.add( GoogleOperations.AddRequest(CurrentQSheetId, rowIndex, Config.DG_PrevTradeEntries, PrevTradeEntries, ";") );
                            requests.add( GoogleOperations.AddRequest(CurrentQSheetId, rowIndex, Config.DG_PrevTradeResults, PrevTradeResults, ";") );
                        }
                    }
                    // When match found, need to copy "Actual Entry" = AG, Duration = AI, %Profit = AJ, Entry Date = A, Day or week = C
                    //   This data is then appended together to any previous data for this cell as follows:
                    //      if previous data then start with a CR and/or LF then add
                    //           3 digit Entry + " " + 2 char day of week + " " + 2 char month + "/" + 2 char year === goes into Column AE when all done looking
                    //   Must build Column AF also as follows:
                    //      if previous data then start with a CR and/or LF then add
                    //           3 digit % + "%/ " + 3 char duration + "m" === goes into Column AF when all done looking
                }
                // Now place all the data, formats, etc.
                if ( requests.size() > 0 ) {
                    GoogleOperations.SetCurrentQData( requests );
                    requests.clear();
                }

                Results_Text_Area.append("weekly data generation complete." + "\n");
      } catch (Exception ex) {
         Results_Text_Area.append("genLowValues FAILED!!!!!  Check source Data." + "\n");
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
      }        
    }
}
