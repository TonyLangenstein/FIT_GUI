/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PESGUI;

/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 *
 * @author thela
 */
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Scanner;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import org.apache.logging.log4j.Level;
/**
 * @author Yaniv Inbar
 */
public class GoogleOperations {
 
//  private static final java.io.File DATA_STORE_DIR =
//      new java.io.File("C:\\Users\\thela\\Documents\\NetBeansProjects\\GitPES\\GoogleSheets\\resources");
//          new java.io.File(System.getProperty("user.home"), "\\Documents\\NetBeansProjects\\GitPES\\GoogleSheets\\resources"); //" .store/plus_sample");
//    private static final String CREDENTIALS_FILE_PATH = System.getProperty("user.home") + "\\Documents\\NetBeansProjects\\GitPES\\GoogleSheets\\resources\\";
    private static final String CREDENTIALS_FILE_PATH = "\\lib\\resources\\";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = CREDENTIALS_FILE_PATH;
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static Credential Cred = null;
    private static NetHttpTransport HTTP_TRANSPORT = null;

    
  //  This will read the google credentials from a file that are needed for google authentication  
  private static InputStreamReader ReadGoogleCredentials(String Name)                                                      
   {                                                          
      InputStreamReader R = null;
      // This procedure loads a .csv file where it will place generated stats: 
      //        OI OK?, B/A OK?, Start Price, Exp Price 1 Series Later, Exp Price 4 Series Later, Think Back Price, Buy Wiggle, and ATM Night B4.
      // It will place these generated and provided values into a new CSV file with the same name and the word "Updated_" placed at the front of the file name.
      try
         {
         String fileName = Name; //Name;  
         //  Another way to get the file is ...   Thread.currentThread().getContextClassLoader().getResource("filename.ext").toURI()
         File file = new File(fileName);
         String dateInFile = "";

         // Read the file.
         Scanner inputStream = new Scanner(file);
         while (inputStream.hasNext()) {
            String data = inputStream.nextLine();     // Read the next line.
            JSONObject J = new JSONObject( data.toString() ); 
            String S = J.toString();
            InputStream T = new ByteArrayInputStream(S.getBytes());
            R = new InputStreamReader(T);
        }

        // Close the file.
        inputStream.close();
        // Close the newly created file
       
      } catch (Exception ex) {
         TT_PESUtility.dumpExceptionInfo(ex);
         ex.printStackTrace();
      }
      return R;
   }            
    
    public static boolean hasCred() {
        return (Cred != null);
    }
    
    
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, ReadGoogleCredentials("./Google.json"));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
    // 1/1/21 - Commenting out line below removed Invalid Grant error.  Now on 9/22/23 I added it back and it all works (so much better).
//                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8080).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
  
  public static void Doit() {
      try {
        if ( HTTP_TRANSPORT == null ) {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        }  
        if ( Cred == null ) {
            Cred = getCredentials(HTTP_TRANSPORT);
        }
        // TBD - Need a way to select this value.
        String spreadsheetId = "1iapAADCBBLEuIXL7wnT7vIv60nhuehDrlONm3WL1L64";
        // TBD Need a way to select this rage or do the data filter
//        String range = "DayGenData!A:E";

        // Now go get the data from the spreadsheet
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, Cred)
                .setApplicationName("PES GUI")     // APPLICATION_NAME)
                .build();
//        ValueRange response = service.spreadsheets().values()
//                .get(spreadsheetId, range)
//                .execute();

        // Get data using a batch request - more efficient.
    
        // How values should be represented in the output.
        // The default render option is ValueRenderOption.FORMATTED_VALUE.
        String valueRenderOption = "FORMATTED_VALUE"; // TODO: Update placeholder value.

        // How dates, times, and durations should be represented in the output.
        // This is ignored if value_render_option is
        // FORMATTED_VALUE.
        // The default dateTime render option is [DateTimeRenderOption.SERIAL_NUMBER].
        String dateTimeRenderOption = "SERIAL_NUMBER"; // TODO: Update placeholder value.
        
        // The A1 notation of the values to retrieve.
        List<String> ranges = new ArrayList<>(); // TODO: Update placeholder value.
//        ranges.add("DayGenData!A:F");  // Retrieve a range in the spreadsheet
//        ranges.add("Current Q");  // Retrieve the whole tab in the spreadsheet
        ranges.add("Weekly E's");  // Retrieve the whole tab in the spreadsheet
        ranges.add("New GUI Download");  // Retrieve the whole tab in the spreadsheet
//        ranges.add("All Q's");  // Retrieve the whole tab in the spreadsheet
        ranges.add("Last Q");  // Retrieve the whole tab in the spreadsheet
//        ranges.add("Last 8 Q's");  // Retrieve the whole tab in the spreadsheet
        // ranges.add("Current Q!");  // Bad format - DO NOT USE!
        Sheets.Spreadsheets.Values.BatchGet request =
                service.spreadsheets().values().batchGet(spreadsheetId);
            request.setRanges(ranges);
        request.setValueRenderOption(valueRenderOption);
        request.setDateTimeRenderOption(dateTimeRenderOption);
        BatchGetValuesResponse responseBatch = request.execute();
        // TBD - do something with the data now.
      } catch (Exception ex) {  // Get exception when Access is Denied.  ioexception.
         TT_PESUtility.dumpExceptionInfo(ex);
         ex.printStackTrace();
      }
  }  
  

  public static BatchGetValuesResponse GetSheet( String sheetName ) {
      BatchGetValuesResponse returnValue = null;
      try {
        if ( HTTP_TRANSPORT == null ) {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        }  
        if ( Cred == null ) {
            Cred = getCredentials(HTTP_TRANSPORT);
        }
        // TBD - Need a way to select this value.
        String spreadsheetId = "1iapAADCBBLEuIXL7wnT7vIv60nhuehDrlONm3WL1L64";
        // TBD Need a way to select this rage or do the data filter
//        String range = "DayGenData!A:E";

        // Now go get the data from the spreadsheet
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, Cred)
                .setApplicationName("PES GUI")     // APPLICATION_NAME)
                .build();

        // How values should be represented in the output.
        // The default render option is ValueRenderOption.FORMATTED_VALUE.
        String valueRenderOption = "FORMATTED_VALUE"; // TODO: Update placeholder value.

        // How dates, times, and durations should be represented in the output.
        // This is ignored if value_render_option is
        // FORMATTED_VALUE.
        // The default dateTime render option is [DateTimeRenderOption.SERIAL_NUMBER].
        String dateTimeRenderOption = "SERIAL_NUMBER"; // TODO: Update placeholder value.
        
        // The A1 notation of the values to retrieve.
        List<String> ranges = new ArrayList<>(); // TODO: Update placeholder value.
//        ranges.add("DayGenData!A:F");  // Retrieve a range in the spreadsheet
        ranges.add(sheetName);  // Retrieve the whole tab in the spreadsheet
        Sheets.Spreadsheets.Values.BatchGet request =
                service.spreadsheets().values().batchGet(spreadsheetId);
            request.setRanges(ranges);
        request.setValueRenderOption(valueRenderOption);
        request.setDateTimeRenderOption(dateTimeRenderOption);
        returnValue = request.execute();
        // TBD - do something with the data now.
      } catch (Exception ex) {  // Get exception when Access is Denied.  ioexception.
         TT_PESUtility.dumpExceptionInfo(ex);
         ex.printStackTrace();
      }
      return returnValue;
  }  


  
  public static Spreadsheet GetSpreadsheetSheetInfo(String[] sheetNames) {
    Spreadsheet response = null;
    try {      
        if ( HTTP_TRANSPORT == null ) {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        }  
        if ( Cred == null ) {
            Cred = getCredentials(HTTP_TRANSPORT);
        }
        // TBD - Need a way to select this value.
        String spreadsheetId = "1iapAADCBBLEuIXL7wnT7vIv60nhuehDrlONm3WL1L64";
        boolean includeGridData = false;  // TBD - What does this actually do?

        // Now go get the data from the spreadsheet
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, Cred)
                .setApplicationName("PES GUI")     // APPLICATION_NAME)
                .build();
        List<String> ranges = new ArrayList<>(); // TODO: Update placeholder value.
        for (int x = 0; x < sheetNames.length; x++) {
            ranges.add(sheetNames[x]);
        }  // Retrieve the whole tab in the spreadsheet
        Sheets.Spreadsheets.Get request = service.spreadsheets().get(spreadsheetId);
        request.setRanges(ranges);
        request.setIncludeGridData(includeGridData);
        response = request.execute();        
      } catch (Exception ex) {  // Get exception when Access is Denied.  ioexception.
         TT_PESUtility.dumpExceptionInfo(ex);
         ex.printStackTrace();
      }
    return response;
  }  

  // Each request here is to paste one cell values only into the coordinate
  public static Request AppendDimensionRequest( int sheetId, int rowsToAdd ) {
      return 
            new Request().setAppendDimension(
                new AppendDimensionRequest().setDimension("ROWS").setLength(rowsToAdd).setSheetId(sheetId)
          );
  }

  // Each request here is to paste one cell values only into the coordinate
  public static Request AddRequest( int sheetId, int rowIndex, int colIndex, String data, String delimiter ) {
      return 
            new Request().setPasteData(
                new PasteDataRequest().
                    setData(data).setDelimiter(delimiter).         
                    setCoordinate( new GridCoordinate().setColumnIndex(colIndex).setRowIndex(rowIndex).setSheetId(sheetId) )
          );
  }

  
  // Each request here is to copy and paste a range of data - taking all values, formats, formulas, and merges.
  public static Request CopyPasteAddRequest( GridRange source, GridRange destination ) {
      CopyPasteRequest CPR = new CopyPasteRequest();
      CPR.setDestination( destination );
      CPR.setSource( source );
      CPR.setPasteType( "PASTE_NORMAL" );
      CPR.setPasteOrientation( "NORMAL" );
      return 
            new Request().setCopyPaste( CPR );
  }

  public static void SetCurrentQData(List<Request> requests, Spreadsheet S) {
    try {      
        if ( HTTP_TRANSPORT == null ) {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        }  
        if ( Cred == null ) {
            Cred = getCredentials(HTTP_TRANSPORT);
        }
        
        // How the input data should be interpreted.
//        String valueInputOption = "USER_ENTERED"; // USER_ENTERED vs RAW.
        BatchUpdateSpreadsheetRequest requestBody = new BatchUpdateSpreadsheetRequest();
        requestBody.setRequests(requests);
    
        // Now go get the data from the spreadsheet
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, Cred)
                .setApplicationName("PES GUI")     // APPLICATION_NAME)
                .build();        
        Sheets.Spreadsheets.BatchUpdate request =
        service.spreadsheets().batchUpdate(S.getSpreadsheetId(), requestBody);

        BatchUpdateSpreadsheetResponse response = request.execute();
    
      } catch (Exception ex) {  // Get exception when Access is Denied.  ioexception.
         TT_PESUtility.dumpExceptionInfo(ex);
         ex.printStackTrace();
      }
  }

}


