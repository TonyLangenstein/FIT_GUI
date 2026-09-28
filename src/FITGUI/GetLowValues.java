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
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 *
 * @author thela
 */
public class GetLowValues {
    private static boolean lowValuesCollected = false;
    private static JSONArray lowValues = new JSONArray();
    private static DecimalFormat df2 = new DecimalFormat(".##");
    private static final String NoValue = "Nope";
    private static final String symbol = "symbol";
    private static final String mark = "mark";
    private static final String time = "time";

    private static void collectLowValues( javax.swing.JTextArea Results_Text_Area, String logFile ) {
      try {
         // Read the file.
         File file = new File(logFile);
         Scanner inputStream = new Scanner(file);
         Results_Text_Area.append("Starting search for low values ...");
         lowValuesCollected = false;
         lowValues = new JSONArray();
         
         // Loop until the end of the file.
         Integer row = 0;
         while (inputStream.hasNext()) {
            String data = inputStream.nextLine();     // Read the next line.
            if ( data.contains( "Low Values") ) {
                while ( inputStream.hasNext() ) {
                    String lowline = inputStream.nextLine();
                    if ( lowline.contains("lowStraddle")) {
                        String[] values = lowline.split(":");
                        //  Build a JSONObject with the values then add that object to a JSONArray for updating the file down below.
                        JSONObject lowInfo = new JSONObject();
                        String[] minorsplit = values[11].split("\"");
                        lowInfo.put(symbol, minorsplit[1]);  
                        // Must convert the time to a value that represents minutes after the open.     
                        int minutes = Integer.parseInt( values[8] );
                        if ( minutes >= 30 ) {
                            lowInfo.put(time,  Integer.toString(minutes - 30) + ":" + values[9] );    
                            minorsplit = values[6].split(",");
                            lowInfo.put(mark, minorsplit[0]);
                        } else {
                            lowInfo.put(time,  NoValue );                        
                            lowInfo.put(mark, NoValue );
                        } 
                        lowValues.put(lowInfo);
                    } else if ( lowline.contains( "Stop here") ) {
                        // We are done
                        lowValuesCollected = true;
                        break;
                    } 
                }
            }
            if ( lowValuesCollected ) {
                break;
            }
         }
         // Close the file.
         inputStream.close();

      } catch (Exception ex) {
         Results_Text_Area.append("collectLowValues FAILED!!!!!  Check source Data." + "\n");
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
      }        
    }

    //  This will take the names input file and collect the low values from it.
    //  Then it will open the specified daygen or guidownload file and put the low value info into the file.
    //  It needs:  log file name,  output file name
    public static void getLowValues(javax.swing.JTextArea Results_Text_Area, String logFile, String inputFile, String outputFile) {
        // TODO add your handling code here:
//        GetLowValues.getLowValues(Results_Text_Area, "LowValueTest.log", "Morning Straddles - DayGenData 07 22.tsv", "Mod Morning Straddles - DayGenData 07 22.tsv");         
        try {
            collectLowValues( Results_Text_Area, logFile );

            if ( lowValuesCollected ) {
                Results_Text_Area.append("low value search complete ... now updating output file");
                File file = new File(inputFile);

                // Read the file.
                Scanner inputStream = new Scanner(file);
                Results_Text_Area.append("Starting getLowValues generation ...");

                // This is the file we will write the data to after it is modified.
                PrintWriter tokensWriter = new PrintWriter( outputFile );    

                String dateInFile = "";                
                String Delimeter = ",";
                String newLine = "";   
                String lowMark = NoValue;
                String lowTime = NoValue;
                boolean symbolFound = false;

                // Loop until the end of the file.
                Integer row = 0;
                while (inputStream.hasNext()) {
                    String data = inputStream.nextLine();     // Read the next line.
                   
                    String[] values = data.split("\\t");        // Get the values from the line.  JUST \\t for tab delimited file instead.

                    // If the values are for the current date, then insert them we have a valid row to process.
                    dateInFile = values[ Config.DG_Date_Column ];   // Date is 0 in the list     

                    // If the values are for the current date, then process the line.
                    if ( OHLC.IsValidDate( dateInFile ) && (values.length >= Config.DG_NumberValidColumns ) ) {  
                        Results_Text_Area.append("Working on data for symbol ... " + values[ Config.DG_Symbol_Column ] + "\n");
                        System.out.println( "Working on data for symbol ... " + values[ Config.DG_Symbol_Column ] );
                        
                        // Loop thru the JSONArray until you find the symbol - then extract the JSONObject and then extract the low values needed.
                        for (int i = 0; i < lowValues.length(); i++) {
                            JSONObject current = new JSONObject( lowValues.getJSONObject(i).toString() );
                            if ( values[ Config.DG_Symbol_Column ].equalsIgnoreCase( current.getString(symbol) ) ) {
                                // Lets write to the output file, just the data we need though.                    
                                // We will replace data in the following columns:  BD, BE, BO, BP; these are "Entry Point", "Day Range", "Max Possible Exit", "Ride to expiry"
                                if ( !current.getString(mark).contains(NoValue) ) {
                                    lowMark = df2.format( Double.parseDouble( current.getString( mark ) ) );
                                }
                                lowTime = current.getString( time );
                                break;
                            }
                        }
                        newLine = values[ Config.DG_Date_Column ] + Delimeter + 
                                  values [ Config.DG_Symbol_Column ] + Delimeter + 
                                  lowMark + Delimeter + 
                                  lowTime;
                        // Only write these out if the line has meaningful data
                        System.out.println( newLine );
                        Results_Text_Area.append(Utility.getTimeStamp() + newLine + ".\n");
                        lowMark = NoValue;
                        lowTime = NoValue;
                        tokensWriter.println( newLine );
                    }
                }

                // Close the file.
                inputStream.close();
                // Close the newly created file
                tokensWriter.close();
                Results_Text_Area.append("getLowValues complete." + "\n");
            }
      } catch (Exception ex) {
         Results_Text_Area.append("genLowValues FAILED!!!!!  Check source Data." + "\n");
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
      }        
    }
    
    //  This will take the names input file and collect the low values from it.
    //  Then it will open the specified daygen or guidownload file and put the low value info into the file.
    //  It needs:  log file name,  output file name
    public static void GoogleGetLowValues(javax.swing.JTextArea Results_Text_Area, String logFile, String sheetName, String theDateToFind) {
        // TODO add your handling code here:
//        GetLowValues.getLowValues(Results_Text_Area, "LowValueTest.log", "Morning Straddles - DayGenData 07 22.tsv", "Mod Morning Straddles - DayGenData 07 22.tsv");         
        try {
            collectLowValues( Results_Text_Area, logFile );

            if ( lowValuesCollected ) {
                Results_Text_Area.append("low value search complete ... now updating output file");

                // Get the data
                 List<Request> requests = new ArrayList<>();

                // Read the data from the Current Q Tab in Google.
                String[]sheetNames = {sheetName};
                Spreadsheet googleSpreadSheet = GoogleOperations.GetSpreadsheetSheetInfo(sheetNames);
                Results_Text_Area.append("Starting getLowValues generation ...");         
                int sheetId = googleSpreadSheet.getSheets().get(0).getProperties().getSheetId();
         
                // Must go back to what we had before and get the sheet data and do all the manipulation below.
                BatchGetValuesResponse CurrentQ = GoogleOperations.GetSheet(sheetName);
                List<ValueRange> VRs = CurrentQ.getValueRanges();
                ValueRange VR = VRs.get(0);
                List<List<Object>> values = VR.getValues();

                // Now loop thru the values which are rows and process them.
                for( int rowIndex = 0; rowIndex < values.size(); rowIndex++) {
                    List<Object> row = values.get(rowIndex);
                    String dateInFile = "";                
                    String Delimeter = ",";
                    String newLine = "";   
                    String lowMark = NoValue;
                    String lowTime = NoValue;
                    boolean symbolFound = false;
                    // If the values are for the current date, then insert them we have a valid row to process.
                    if ( row.size() > 0 ) {
                    dateInFile = row.get( Config.DG_Date_Column ).toString();   // Date is 0 in the list     
                    } else {
                        dateInFile = "1/1";
                    }

                    // If the values are for the current date, then process the line.
                    if ( OHLC.IsValidDate( dateInFile ) && (row.size() >= Config.DG_NumberValidColumns ) ) {                          
                        // Loop thru the JSONArray until you find the symbol - then extract the JSONObject and then extract the low values needed.
                        for (int i = 0; i < lowValues.length(); i++) {
                            JSONObject current = new JSONObject( lowValues.getJSONObject(i).toString() );
                            if ( row.get( Config.DG_Symbol_Column ).toString().equalsIgnoreCase( current.getString(symbol) ) &&
                                 row.get( Config.DG_Date_Column).toString().equalsIgnoreCase( theDateToFind ) ) {
                                // Lets write to the output file, just the data we need though.                    
                                Results_Text_Area.append("Working on data for symbol ... " + row.get( Config.DG_Symbol_Column ).toString() + "\n");
                                System.out.println( "Working on data for symbol ... " + row.get( Config.DG_Symbol_Column ).toString() );
                                // We will replace data in the following columns:  BD, BE, BO, BP; these are "Entry Point", "Day Range", "Max Possible Exit", "Ride to expiry"
                                if ( !current.getString(mark).contains(NoValue) ) {
                                    lowMark = df2.format( Double.parseDouble( current.getString( mark ) ) );
                                }
                                lowTime = current.getString( time );
                                requests.add( GoogleOperations.AddRequest(sheetId, rowIndex, Config.LV_Low_Value_Column, lowMark, ";") );
                                requests.add( GoogleOperations.AddRequest(sheetId, rowIndex, Config.LV_Low_Time_Column, lowTime, ";") );
                                newLine = row.get( Config.DG_Date_Column ).toString() + Delimeter + 
                                          row.get( Config.DG_Symbol_Column ).toString() + Delimeter + 
                                          lowMark + Delimeter + 
                                          lowTime;
                                // Only write these out if the line has meaningful data
                                System.out.println( newLine );
                                Results_Text_Area.append(Utility.getTimeStamp() + newLine + ".\n");
                                // Now update the data
                                lowMark = NoValue;
                                lowTime = NoValue;
                                break;
                            }
                        }
                    }  // IsValidDate
                }  // for rowindex

                if ( requests.size() > 0 ) {
                    GoogleOperations.SetCurrentQData( requests );
                }
                Results_Text_Area.append("getLowValues complete." + "\n");
            }
      } catch (Exception ex) {
         Results_Text_Area.append("genLowValues FAILED!!!!!  Check source Data." + "\n");
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
      }        
    }
}
