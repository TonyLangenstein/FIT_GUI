/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.

 */
package FITGUI;

import java.io.File;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
//import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.*;
import org.apache.logging.log4j.Level;

/**
 *
 * @author thela
 */
public class TT_API {

    public static final String sandBox_URL = "https://api.cert.tastyworks.com/";
    public static final String production_URL = "https://api.tastyworks.com/"; 
    private static String base_URL = sandBox_URL; // Start with sandbox always

    private static String refreshToken = "";
    private static String accessToken = "";
    private static String clientSecret = "";
    private static int tokenUpdateWaitTime = 15;  // Minutes
    private static LocalDateTime lastTokenUpdate = null; //LocalDateTime.now().minusMinutes(tokenUpdateWaitTime+3);  // Force token update to start.
    private static Log4J2AsyncLogger LogData =new Log4J2AsyncLogger();
    private static final String TOKENS_FILENAME = "TTTokens2.txt";
    
    synchronized public static void setEnvironment( String env ) {
        if (env.contains("sand")) {
            base_URL = sandBox_URL;
        } else {
            base_URL = production_URL;
        }
    }

    synchronized public static void sendTokenUpdate() {
        try {
             while ( !GUIData.LockQuoteSocketData() ) { Thread.sleep(10);}
                GUIData.SetQuotesSocketData( GUIData.TokenUpdate + "," + getRefreshToken() + "," + getAccessToken() + ", TASTY" );
        } catch (Exception e) {
              Utility.dumpExceptionInfo(e);
              e.printStackTrace();
        }
    }
              
    // For Oauth2 the only login required is to gen an Access Token.
    synchronized public static String generateAccessToken() {
        
        Map<String, String> params = new HashMap<>();
        HttpURLConnection httpConn = null;

        params.put("grant_type", "refresh_token");
        params.put("refresh_token", getRefreshToken() );
        params.put("client_secret", getClientSecret() );
        String requestURL = base_URL + "oauth/token";

        // Send the request off for the token
        try {
            httpConn = HttpUtility.sendPostRequestNoAuth(requestURL, params);
        } 
        catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "generateAccessToken failed to send request");
            ex.printStackTrace();
        }

        // Read in the results
        String response = null;
        try {
            response = HttpUtility.readSingleLineRespone(httpConn);
            /*
            {"access_token":"eyJhbGciOiJFZERTQSIsInR5cCI6ImF0K2p3dCIsImtpZCI6IkZqVTdUT25qVEQ2WnVySlg2cVlwWmVPbzBDQzQ5TnIzR1pUN1E4MTc0cUkiLCJqa3UiOiJodHRwczovL2ludGVyaW9yLWFwaS5jaDIudGFzdHl3b3Jrcy5jb20vb2F1dGgvandrcyJ9.eyJpc3MiOiJodHRwczovL2FwaS50YXN0eXRyYWRlLmNvbSIsInN1YiI6IlUwMDAwMzY0ODIyIiwiaWF0IjoxNzU4MTU4NjQ0LCJleHAiOjE3NTgxNjEwNTYsImF1ZCI6IjBiNGQyYWY2LTAzYTQtNDE4YS1hZTQ0LWVlNGVhZDk5Zjk4MyIsImdyYW50X2lkIjoiR2VjZWYzNzdhLTlkM2UtNDUxMS1hYTFjLTNjNWNiNTE5YTQwMiIsInNjb3BlIjoicmVhZCB0cmFkZSBvcGVuaWQiLCJjbGllbnRfZG9tYWluIjoiQyIsInR3dW0iOiJkalJ6V0ZFMlFUVlFVbTRyTldwSmNuVXJXWFF2Y1dnNU9EVkpSMFZzVDA5VlJrbzFhM1IwWlVkb2VrOVFXRkJ5WkdGaFdHUmhhbWxyZFhsNVpHRm5UV1F6VkZkRFZFOVFVRkl2Y201blpGUlZaRTVPVnpKWlUwbG5VMUJxUzJaU2IycG1UVXBsTWxaalpUaG5NR3M1VkVSUVJHaFFXUzlqTjBwSU5XeG1abVl2U2xnMVZuVjBUWEJCWVRjMWNITm9XbXBNUW10VlRVMWhWRTF2ZDJSclVHTkJSWFZQU1VaRWNqQTNaV001ZWtKME9FNDNPSE5WUzFWM1lUUXlWRUpJTFMxWGVWaHJRM3BYUWxoWFoxTjFWalJxUWxGcWFFZDNQVDA9LS0zZGVlOWI1ZjFmY2QyYTBlZGJkMzU0MjA4YzkxZmI5ZThiY2VjYjRmIn0.1UxbKSqTiLH0MqTOIdcR3InJIR3fCstI-SHM6FH7kvlFuDooyAfH22ObFe4xV3NkqRfwmAP9lmRwFsCrZzfYCw","token_type":"Bearer","expires_in":900,"id_token":"eyJhbGciOiJFZERTQSIsImtpZCI6IkZqVTdUT25qVEQ2WnVySlg2cVlwWmVPbzBDQzQ5TnIzR1pUN1E4MTc0cUkiLCJqa3UiOiJodHRwczovL2ludGVyaW9yLWFwaS5jaDIudGFzdHl3b3Jrcy5jb20vb2F1dGgvandrcyJ9.eyJpc3MiOiJodHRwczovL2FwaS50YXN0eXRyYWRlLmNvbSIsImlhdCI6MTc1ODE1ODY0NCwiZXhwIjoxNzU4MTYxMDU2LCJhdWQiOiIwYjRkMmFmNi0wM2E0LTQxOGEtYWU0NC1lZTRlYWQ5OWY5ODMiLCJzdWIiOiJVMDAwMDM2NDgyMiIsImdpdmVuX25hbWUiOiJBbnRob255IiwiZmFtaWx5X25hbWUiOiJMYW5nZW5zdGVpbiIsImVtYWlsIjoidG9ueWxhbmdlbnN0ZWluQGdtYWlsLmNvbSIsImxvY2FsZSI6ImVuLVVTIn0.jYFdmI3yYdScDsL7ML00Li7biHgjnttmdhlLcvFnDIPsJ-NMXeg3qSWwSdUFA3inGM7_lIhp2EoKjdhW3RSfCA"}"
            */
            String[] payload = response.split("\"access_token\":");
            String[] subPayload = payload[1].split("\"");
            setAccessToken( subPayload[1] );
            
            // Save the token creation time, only valid for 15 minutes.
            lastTokenUpdate = LocalDateTime.now();
        } 
        catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "generate access token failed to read data");
            ex.printStackTrace();
            setAccessToken( "" );
        }
        HttpUtility.disconnect(httpConn);
        SaveTokens();
        return response;
    }
        
    synchronized public static String getClientSecret() {
        return clientSecret;
    }
    
    synchronized public static void setClientSecret(String newID) {
        clientSecret = newID;
    }
     
    synchronized public static String getAccessToken() {
        return accessToken;
    }
    
    synchronized public static void setAccessToken(String newToken) {
        accessToken = newToken;
    }
     
    synchronized public static String getRefreshToken() {
        return refreshToken;
    }
    
    synchronized public static void setRefreshToken(String newToken) {
        refreshToken = newToken;
    }

   synchronized public static boolean checkTimeToUpdateTokens() {
       boolean returnValue = false;
       if ((refreshToken == "") || (accessToken == "") || (clientSecret == "")) {
            // Do nothing.
        } else {
            if (LocalDateTime.now().isAfter(lastTokenUpdate.plusMinutes(tokenUpdateWaitTime))) {
                generateAccessToken();         
                returnValue = true;
            }
        }
       return returnValue;
   }
               
    synchronized public static void SaveTokens()                                                 
    {                                                     
        try {
            LocalDateTime tempDate = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String theDateTime = lastTokenUpdate.format(formatter);

            // Save the Access and Refresh tokens to a file so we can load then later.
            // This will allow us to bypass the login process ... providing the refresh
            // token has not expired.
            PrintWriter tokensWriter = new PrintWriter(TOKENS_FILENAME);
            tokensWriter.println(theDateTime + "," + refreshToken);
            tokensWriter.println(theDateTime + "," + accessToken);
            tokensWriter.println(theDateTime + "," + clientSecret);
            tokensWriter.close();
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "SaveToken");
            ex.printStackTrace();
        }
    }                                                

   // returns True if token is valid, false otherwise
   synchronized public static boolean LoadTokens()                                                 
   {                                                     
       boolean returnValue = false;
       try
         {
            File tokensFile = new File(TOKENS_FILENAME);
            if (tokensFile.exists() ) {
               Scanner inputStream = new Scanner(tokensFile);  // Read the file.
               String data = inputStream.nextLine();           // Read the line.
               String[] values = data.split(",");              // Get the values from the line.
               String theDateTime = values[0];
               DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
               boolean expired = true;
               if (lastTokenUpdate != null) {
                    lastTokenUpdate = LocalDateTime.parse(theDateTime, formatter);
                    LocalDateTime todayIs = LocalDateTime.now();
                    // A 15 minute token
                    expired = todayIs.isAfter(lastTokenUpdate.plusMinutes(tokenUpdateWaitTime));             
               }
               refreshToken = values[1];
               data = inputStream.nextLine();           // Read the line.
               String[] values1 = data.split(",");              // Get the values from the line.
               accessToken = values1[1];
               data = inputStream.nextLine();           // Read the line.
               String[] values2 = data.split(",");              // Get the values from the line.
               clientSecret = values2[1];
               if (expired == true) {
                   returnValue = !generateAccessToken().equalsIgnoreCase("");
               }
               returnValue = true;
               inputStream.close();                            // Close the file.
            }
         } catch (Exception ex)
         {
           Utility.dumpExceptionInfo(ex, "LoadToken");
           ex.printStackTrace();
         }
      return returnValue;
   }                                                
       
    // This will login in to TastyTrade and store the token information in this module.
    synchronized public static String getDxInformation() {
        
        HttpURLConnection httpConn = null;
        String response = null;
        String requestURL = base_URL + "api-quote-tokens";

        // Send the request off for the token
        try {
            httpConn = HttpUtility.sendGetRequest(requestURL, "Bearer " + getAccessToken(), true);
        } 
        catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "login failed to send request");
            ex.printStackTrace();
        }

        // Read in the results
        try {
            response = HttpUtility.readSingleLineRespone(httpConn);
        } 
        catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "login failed to read data");
            ex.printStackTrace();
        }
        HttpUtility.disconnect(httpConn);
        return response;
    }

    // Get all the option chain info but store only a reduced amount of that for use in the application.
    /*  The data structure is
    {"data":{ "items": [
                        {   "underlying-symbol": "KMB","root-symbol":"KMB","option-chain-type":"Standard","shares-per-contract":100,
                            "tick-sizes":[
                                            {"value":"0.05","threshold":"3.0"},
                                            {"value":"0.1"}],
                                         ]
                            "deliverables":[
                                            {"id":1397,"root-symbol":"KMB","deliverable-type":"Shares","description":"100 shares of KMB","amount":"100.0","symbol":"KMB","instrument-type":"Equity","percent":"100"}
                                           ],
                            "expirations":[
                                            {"expiration-type":"Weekly","expiration-date":"2024-04-05","days-to-expiration":0,"settlement-type":"PM",
                                             "strikes":[
                                                            {"strike-price":"75.0","call":"KMB   240405C00075000","call-streamer-symbol":".KMB240405C75","put":"KMB   240405P00075000","put-streamer-symbol":".KMB240405P75"},
                                                            {"strike-price":"80.0","call":"KMB   240405C00080000","call-streamer-symbol":".KMB240405C80","put":"KMB   240405P00080000","put-streamer-symbol":".KMB240405P80"},
                                                            ...
                                                            {"strike-price":"170.0","call":"KMB   240405C00170000","call-streamer-symbol":".KMB240405C170","put":"KMB   240405P00170000","put-streamer-symbol":".KMB240405P170"},
                                                            {"strike-price":"175.0","call":"KMB   240405C00175000","call-streamer-symbol":".KMB240405C175","put":"KMB   240405P00175000","put-streamer-symbol":".KMB240405P175"}
                                                        ]},
                                            {"expiration-type":"Weekly","expiration-date":"2024-04-12",...
                                            {"expiration-type":"Regular","expiration-date":"2024-04-19","days-to-expiration":14,"settlement-type":"PM","strikes":[{"strike-price":"60.0","call":"KMB   240419C00060000","call-streamer-symbol":".KMB240419C60","put":"KMB   240419P00060000","put-streamer-symbol":".KMB240419P60"},{"strike-price":"65.0","call":"KMB   240419C00065000","call-streamer-symbol":".KMB240419C65","put":"KMB   240419P00065000","put-streamer-symbol":".KMB240419P65"},{"strike-price":"70.0","call":"KMB   240419C00070000","call-streamer-symbol":".KMB240419C70","put":"KMB   240419P00070000","put-streamer-symbol":".KMB240419P70"},{"strike-price":"75.0","call":"KMB   240419C00075000","call-streamer-symbol":".KMB240419C75","put":"KMB   240419P00075000","put-streamer-symbol":".KMB240419P75"},{"strike-price":"80.0","call":"KMB   240419C00080000","call-streamer-symbol":".KMB240419C80","put":"KMB   240419P00080000","put-streamer-symbol":".KMB240419P80"},{"strike-price":"85.0","call":"KMB   240419C00085000","call-streamer-symbol":".KMB240419C85","put":"KMB   240419P00085000","put-streamer-symbol":".KMB240419P85"},{"strike-price":"90.0","call":"KMB   240419C00090000","call-streamer-symbol":".KMB240419C90","put":"KMB   240419P00090000","put-streamer-symbol":".KMB240419P90"},{"strike-price":"95.0","call":"KMB   240419C00095000","call-streamer-symbol":".KMB240419C95","put":"KMB   240419P00095000","put-streamer-symbol":".KMB240419P95"},{"strike-price":"100.0","call":"KMB   240419C00100000","call-streamer-symbol":".KMB240419C100","put":"KMB   240419P00100000","put-streamer-symbol":".KMB240419P100"},{"strike-price":"105.0","call":"KMB   240419C00105000","call-streamer-symbol":".KMB240419C105","put":"KMB   240419P00105000","put-streamer-symbol":".KMB240419P105"},{"strike-price":"110.0","call":"KMB   240419C00110000","call-streamer-symbol":".KMB240419C110","put":"KMB   240419P00110000","put-streamer-symbol":".KMB240419P110"},{"strike-price":"111.0","call":"KMB   240419C00111000","call-streamer-symbol":".KMB240419C111","put":"KMB   240419P00111000","put-streamer-symbol":".KMB240419P111"},{"strike-price":"112.0","call":"KMB   240419C00112000","call-streamer-symbol":".KMB240419C112","put":"KMB   240419P00112000","put-streamer-symbol":".KMB240419P112"},{"strike-price":"113.0","call":"KMB   240419C00113000","call-streamer-symbol":".KMB240419C113","put":"KMB   240419P00113000","put-streamer-symbol":".KMB240419P113"},{"strike-price":"114.0","call":"KMB   240419C00114000","call-streamer-symbol":".KMB240419C114","put":"KMB   240419P00114000","put-streamer-symbol":".KMB240419P114"},{"strike-price":"115.0","call":"KMB   240419C00115000","call-streamer-symbol":".KMB240419C115","put":"KMB   240419P00115000","put-streamer-symbol":".KMB240419P115"},{"strike-price":"116.0","call":"KMB   240419C00116000","call-streamer-symbol":".KMB240419C116","put":"KMB   240419P00116000","put-streamer-symbol":".KMB240419P116"},{"strike-price":"117.0","call":"KMB   240419C00117000","call-streamer-symbol":".KMB240419C117","put":"KMB   240419P00117000","put-streamer-symbol":".KMB240419P117"},{"strike-price":"118.0","call":"KMB   240419C00118000","call-streamer-symbol":".KMB240419C118","put":"KMB   240419P00118000","put-streamer-symbol":".KMB240419P118"},{"strike-price":"119.0","call":"KMB   240419C00119000","call-streamer-symbol":".KMB240419C119","put":"KMB   240419P00119000","put-streamer-symbol":".KMB240419P119"},{"strike-price":"120.0","call":"KMB   240419C00120000","call-streamer-symbol":".KMB240419C120","put":"KMB   240419P00120000","put-streamer-symbol":".KMB240419P120"},{"strike-price":"121.0","call":"KMB   240419C00121000","call-streamer-symbol":".KMB240419C121","put":"KMB   240419P00121000","put-streamer-symbol":".KMB240419P121"},{"strike-price":"122.0","call":"KMB   240419C00122000","call-streamer-symbol":".KMB240419C122","put":"KMB   240419P00122000","put-streamer-symbol":".KMB240419P122"},{"strike-price":"123.0","call":"KMB   240419C00123000","call-streamer-symbol":".KMB240419C123","put":"KMB   240419P00123000","put-streamer-symbol":".KMB240419P123"},{"strike-price":"124.0","call":"KMB   240419C00124000","call-streamer-symbol":".KMB240419C124","put":"KMB   240419P00124000","put-streamer-symbol":".KMB240419P124"},{"strike-price":"125.0","call":"KMB   240419C00125000","call-streamer-symbol":".KMB240419C125","put":"KMB   240419P00125000","put-streamer-symbol":".KMB240419P125"},{"strike-price":"126.0","call":"KMB   240419C00126000","call-streamer-symbol":".KMB240419C126","put":"KMB   240419P00126000","put-streamer-symbol":".KMB240419P126"},{"strike-price":"127.0","call":"KMB   240419C00127000","call-streamer-symbol":".KMB240419C127","put":"KMB   240419P00127000","put-streamer-symbol":".KMB240419P127"},{"strike-price":"128.0","call":"KMB   240419C00128000","call-streamer-symbol":".KMB240419C128","put":"KMB   240419P00128000","put-streamer-symbol":".KMB240419P128"},{"strike-price":"129.0","call":"KMB   240419C00129000","call-streamer-symbol":".KMB240419C129","put":"KMB   240419P00129000","put-streamer-symbol":".KMB240419P129"},{"strike-price":"130.0","call":"KMB   240419C00130000","call-streamer-symbol":".KMB240419C130","put":"KMB   240419P00130000","put-streamer-symbol":".KMB240419P130"},{"strike-price":"131.0","call":"KMB   240419C00131000","call-streamer-symbol":".KMB240419C131","put":"KMB   240419P00131000","put-streamer-symbol":".KMB240419P131"},{"strike-price":"132.0","call":"KMB   240419C00132000","call-streamer-symbol":".KMB240419C132","put":"KMB   240419P00132000","put-streamer-symbol":".KMB240419P132"},{"strike-price":"133.0","call":"KMB   240419C00133000","call-streamer-symbol":".KMB240419C133","put":"KMB   240419P00133000","put-streamer-symbol":".KMB240419P133"},{"strike-price":"134.0","call":"KMB   240419C00134000","call-streamer-symbol":".KMB240419C134","put":"KMB   240419P00134000","put-streamer-symbol":".KMB240419P134"},{"strike-price":"135.0","call":"KMB   240419C00135000","call-streamer-symbol":".KMB240419C135","put":"KMB   240419P00135000","put-streamer-symbol":".KMB240419P135"},{"strike-price":"136.0","call":"KMB   240419C00136000","call-streamer-symbol":".KMB240419C136","put":"KMB   240419P00136000","put-streamer-symbol":".KMB240419P136"},{"strike-price":"137.0","call":"KMB   240419C00137000","call-streamer-symbol":".KMB240419C137","put":"KMB   240419P00137000","put-streamer-symbol":".KMB240419P137"},{"strike-price":"138.0","call":"KMB   240419C00138000","call-streamer-symbol":".KMB240419C138","put":"KMB   240419P00138000","put-streamer-symbol":".KMB240419P138"},{"strike-price":"139.0","call":"KMB   240419C00139000","call-streamer-symbol":".KMB240419C139","put":"KMB   240419P00139000","put-streamer-symbol":".KMB240419P139"},{"strike-price":"140.0","call":"KMB   240419C00140000","call-streamer-symbol":".KMB240419C140","put":"KMB   240419P00140000","put-streamer-symbol":".KMB240419P140"},{"strike-price":"141.0","call":"KMB   240419C00141000","call-streamer-symbol":".KMB240419C141","put":"KMB   240419P00141000","put-streamer-symbol":".KMB240419P141"},{"strike-price":"145.0","call":"KMB   240419C00145000","call-streamer-symbol":".KMB240419C145","put":"KMB   240419P00145000","put-streamer-symbol":".KMB240419P145"},{"strike-price":"150.0","call":"KMB   240419C00150000","call-streamer-symbol":".KMB240419C150","put":"KMB   240419P00150000","put-streamer-symbol":".KMB240419P150"},{"strike-price":"155.0","call":"KMB   240419C00155000","call-streamer-symbol":".KMB240419C155","put":"KMB   240419P00155000","put-streamer-symbol":".KMB240419P155"},{"strike-price":"160.0","call":"KMB   240419C00160000","call-streamer-symbol":".KMB240419C160","put":"KMB   240419P00160000","put-streamer-symbol":".KMB240419P160"},{"strike-price":"165.0","call":"KMB   240419C00165000","call-streamer-symbol":".KMB240419C165","put":"KMB   240419P00165000","put-streamer-symbol":".KMB240419P165"},{"strike-price":"170.0","call":"KMB   240419C00170000","call-streamer-symbol":".KMB240419C170","put":"KMB   240419P00170000","put-streamer-symbol":".KMB240419P170"},{"strike-price":"175.0","call":"KMB   240419C00175000","call-streamer-symbol":".KMB240419C175","put":"KMB   240419P00175000","put-streamer-symbol":".KMB240419P175"},{"strike-price":"180.0","call":"KMB   240419C00180000","call-streamer-symbol":".KMB240419C180","put":"KMB   240419P00180000","put-streamer-symbol":".KMB240419P180"},{"strike-price":"185.0","call":"KMB   240419C00185000","call-streamer-symbol":".KMB240419C185","put":"KMB   240419P00185000","put-streamer-symbol":".KMB240419P185"},{"strike-price":"190.0","call":"KMB   240419C00190000","call-streamer-symbol":".KMB240419C190","put":"KMB   240419P00190000","put-streamer-symbol":".KMB240419P190"}]},
                                            ...
                                            {"expiration-type":"Regular","expiration-date":"2025-06-20","days-to-expiration":441,"settlement-type":"PM","strikes":[{"strike-price":"65.0","call":"KMB   250620C00065000","call-streamer-symbol":".KMB250620C65","put":"KMB   250620P00065000","put-streamer-symbol":".KMB250620P65"},{"strike-price":"70.0","call":"KMB   250620C00070000","call-streamer-symbol":".KMB250620C70","put":"KMB   250620P00070000","put-streamer-symbol":".KMB250620P70"},{"strike-price":"75.0","call":"KMB   250620C00075000","call-streamer-symbol":".KMB250620C75","put":"KMB   250620P00075000","put-streamer-symbol":".KMB250620P75"},{"strike-price":"80.0","call":"KMB   250620C00080000","call-streamer-symbol":".KMB250620C80","put":"KMB   250620P00080000","put-streamer-symbol":".KMB250620P80"},{"strike-price":"85.0","call":"KMB   250620C00085000","call-streamer-symbol":".KMB250620C85","put":"KMB   250620P00085000","put-streamer-symbol":".KMB250620P85"},{"strike-price":"90.0","call":"KMB   250620C00090000","call-streamer-symbol":".KMB250620C90","put":"KMB   250620P00090000","put-streamer-symbol":".KMB250620P90"},{"strike-price":"95.0","call":"KMB   250620C00095000","call-streamer-symbol":".KMB250620C95","put":"KMB   250620P00095000","put-streamer-symbol":".KMB250620P95"},{"strike-price":"100.0","call":"KMB   250620C00100000","call-streamer-symbol":".KMB250620C100","put":"KMB   250620P00100000","put-streamer-symbol":".KMB250620P100"},{"strike-price":"105.0","call":"KMB   250620C00105000","call-streamer-symbol":".KMB250620C105","put":"KMB   250620P00105000","put-streamer-symbol":".KMB250620P105"},{"strike-price":"110.0","call":"KMB   250620C00110000","call-streamer-symbol":".KMB250620C110","put":"KMB   250620P00110000","put-streamer-symbol":".KMB250620P110"},{"strike-price":"115.0","call":"KMB   250620C00115000","call-streamer-symbol":".KMB250620C115","put":"KMB   250620P00115000","put-streamer-symbol":".KMB250620P115"},{"strike-price":"120.0","call":"KMB   250620C00120000","call-streamer-symbol":".KMB250620C120","put":"KMB   250620P00120000","put-streamer-symbol":".KMB250620P120"},{"strike-price":"125.0","call":"KMB   250620C00125000","call-streamer-symbol":".KMB250620C125","put":"KMB   250620P00125000","put-streamer-symbol":".KMB250620P125"},{"strike-price":"130.0","call":"KMB   250620C00130000","call-streamer-symbol":".KMB250620C130","put":"KMB   250620P00130000","put-streamer-symbol":".KMB250620P130"},{"strike-price":"135.0","call":"KMB   250620C00135000","call-streamer-symbol":".KMB250620C135","put":"KMB   250620P00135000","put-streamer-symbol":".KMB250620P135"},{"strike-price":"140.0","call":"KMB   250620C00140000","call-streamer-symbol":".KMB250620C140","put":"KMB   250620P00140000","put-streamer-symbol":".KMB250620P140"},{"strike-price":"145.0","call":"KMB   250620C00145000","call-streamer-symbol":".KMB250620C145","put":"KMB   250620P00145000","put-streamer-symbol":".KMB250620P145"},{"strike-price":"150.0","call":"KMB   250620C00150000","call-streamer-symbol":".KMB250620C150","put":"KMB   250620P00150000","put-streamer-symbol":".KMB250620P150"},{"strike-price":"155.0","call":"KMB   250620C00155000","call-streamer-symbol":".KMB250620C155","put":"KMB   250620P00155000","put-streamer-symbol":".KMB250620P155"},{"strike-price":"160.0","call":"KMB   250620C00160000","call-streamer-symbol":".KMB250620C160","put":"KMB   250620P00160000","put-streamer-symbol":".KMB250620P160"},{"strike-price":"165.0","call":"KMB   250620C00165000","call-streamer-symbol":".KMB250620C165","put":"KMB   250620P00165000","put-streamer-symbol":".KMB250620P165"},{"strike-price":"170.0","call":"KMB   250620C00170000","call-streamer-symbol":".KMB250620C170","put":"KMB   250620P00170000","put-streamer-symbol":".KMB250620P170"},{"strike-price":"175.0","call":"KMB   250620C00175000","call-streamer-symbol":".KMB250620C175","put":"KMB   250620P00175000","put-streamer-symbol":".KMB250620P175"},{"strike-price":"180.0","call":"KMB   250620C00180000","call-streamer-symbol":".KMB250620C180","put":"KMB   250620P00180000","put-streamer-symbol":".KMB250620P180"},{"strike-price":"185.0","call":"KMB   250620C00185000","call-streamer-symbol":".KMB250620C185","put":"KMB   250620P00185000","put-streamer-symbol":".KMB250620P185"},{"strike-price":"190.0","call":"KMB   250620C00190000","call-streamer-symbol":".KMB250620C190","put":"KMB   250620P00190000","put-streamer-symbol":".KMB250620P190"}]},
                                            {"expiration-type":"Regular","expiration-date":"2026-01-16","days-to-expiration":651,"settlement-type":"PM","strikes":[{"strike-price":"60.0","call":"KMB   260116C00060000","call-streamer-symbol":".KMB260116C60","put":"KMB   260116P00060000","put-streamer-symbol":".KMB260116P60"},{"strike-price":"65.0","call":"KMB   260116C00065000","call-streamer-symbol":".KMB260116C65","put":"KMB   260116P00065000","put-streamer-symbol":".KMB260116P65"},{"strike-price":"70.0","call":"KMB   260116C00070000","call-streamer-symbol":".KMB260116C70","put":"KMB   260116P00070000","put-streamer-symbol":".KMB260116P70"},{"strike-price":"75.0","call":"KMB   260116C00075000","call-streamer-symbol":".KMB260116C75","put":"KMB   260116P00075000","put-streamer-symbol":".KMB260116P75"},{"strike-price":"80.0","call":"KMB   260116C00080000","call-streamer-symbol":".KMB260116C80","put":"KMB   260116P00080000","put-streamer-symbol":".KMB260116P80"},{"strike-price":"85.0","call":"KMB   260116C00085000","call-streamer-symbol":".KMB260116C85","put":"KMB   260116P00085000","put-streamer-symbol":".KMB260116P85"},{"strike-price":"90.0","call":"KMB   260116C00090000","call-streamer-symbol":".KMB260116C90","put":"KMB   260116P00090000","put-streamer-symbol":".KMB260116P90"},{"strike-price":"95.0","call":"KMB   260116C00095000","call-streamer-symbol":".KMB260116C95","put":"KMB   260116P00095000","put-streamer-symbol":".KMB260116P95"},{"strike-price":"100.0","call":"KMB   260116C00100000","call-streamer-symbol":".KMB260116C100","put":"KMB   260116P00100000","put-streamer-symbol":".KMB260116P100"},{"strike-price":"105.0","call":"KMB   260116C00105000","call-streamer-symbol":".KMB260116C105","put":"KMB   260116P00105000","put-streamer-symbol":".KMB260116P105"},{"strike-price":"110.0","call":"KMB   260116C00110000","call-streamer-symbol":".KMB260116C110","put":"KMB   260116P00110000","put-streamer-symbol":".KMB260116P110"},{"strike-price":"115.0","call":"KMB   260116C00115000","call-streamer-symbol":".KMB260116C115","put":"KMB   260116P00115000","put-streamer-symbol":".KMB260116P115"},{"strike-price":"120.0","call":"KMB   260116C00120000","call-streamer-symbol":".KMB260116C120","put":"KMB   260116P00120000","put-streamer-symbol":".KMB260116P120"},{"strike-price":"125.0","call":"KMB   260116C00125000","call-streamer-symbol":".KMB260116C125","put":"KMB   260116P00125000","put-streamer-symbol":".KMB260116P125"},{"strike-price":"130.0","call":"KMB   260116C00130000","call-streamer-symbol":".KMB260116C130","put":"KMB   260116P00130000","put-streamer-symbol":".KMB260116P130"},{"strike-price":"135.0","call":"KMB   260116C00135000","call-streamer-symbol":".KMB260116C135","put":"KMB   260116P00135000","put-streamer-symbol":".KMB260116P135"},{"strike-price":"140.0","call":"KMB   260116C00140000","call-streamer-symbol":".KMB260116C140","put":"KMB   260116P00140000","put-streamer-symbol":".KMB260116P140"},{"strike-price":"145.0","call":"KMB   260116C00145000","call-streamer-symbol":".KMB260116C145","put":"KMB   260116P00145000","put-streamer-symbol":".KMB260116P145"},{"strike-price":"150.0","call":"KMB   260116C00150000","call-streamer-symbol":".KMB260116C150","put":"KMB   260116P00150000","put-streamer-symbol":".KMB260116P150"},{"strike-price":"155.0","call":"KMB   260116C00155000","call-streamer-symbol":".KMB260116C155","put":"KMB   260116P00155000","put-streamer-symbol":".KMB260116P155"},{"strike-price":"160.0","call":"KMB   260116C00160000","call-streamer-symbol":".KMB260116C160","put":"KMB   260116P00160000","put-streamer-symbol":".KMB260116P160"},{"strike-price":"165.0","call":"KMB   260116C00165000","call-streamer-symbol":".KMB260116C165","put":"KMB   260116P00165000","put-streamer-symbol":".KMB260116P165"},{"strike-price":"170.0","call":"KMB   260116C00170000","call-streamer-symbol":".KMB260116C170","put":"KMB   260116P00170000","put-streamer-symbol":".KMB260116P170"},{"strike-price":"175.0","call":"KMB   260116C00175000","call-streamer-symbol":".KMB260116C175","put":"KMB   260116P00175000","put-streamer-symbol":".KMB260116P175"},{"strike-price":"180.0","call":"KMB   260116C00180000","call-streamer-symbol":".KMB260116C180","put":"KMB   260116P00180000","put-streamer-symbol":".KMB260116P180"},{"strike-price":"185.0","call":"KMB   260116C00185000","call-streamer-symbol":".KMB260116C185","put":"KMB   260116P00185000","put-streamer-symbol":".KMB260116P185"},{"strike-price":"190.0","call":"KMB   260116C00190000","call-streamer-symbol":".KMB260116C190","put":"KMB   260116P00190000","put-streamer-symbol":".KMB260116P190"}
                                          ]
                        }
                       ]
            }
     "context":"//option-chains/KMB/nested"
    }
    aka ... JSONObject( "data" = JSONObject
                                 "items" = JSONArray of JSONObjects
                                                        "underlying-symbol" - String
                                                        "root-symbol":"KMB" - String
                                                        "option-chain-type" - don't care
                                                        "shares-per-contract" - don't care
                                                        "tick-sizes" - don't care
                                                        "deliverables" - don't care
                                                        "expirations" = JSONArray of JSONObjects
                                                                                    "expiration-type" - String
                                                                                    "expiration-date" - String
                                                                                    "days-to-expiration" - integer
                                                                                    "settlement-type" - String
                                                                                    "strikes" = JSONArray of JSONObjects
                                                                                                             "strike-price" - String
                                                                                                             "call" - String
                                                                                                             "call-streamer-symbol" - String
                                                                                                             "put" - String
                                                                                                             "put-streamer-symbol" - String
                        "context" - String  )

    We want to remove all extra stuff and just have a JSONObject that looks like this:
        "underlying-symbol" - String
        "root-symbol" - String
        "expiration-date" - String
        "strikes" - JSONArray of JSONObjects
                                "strike-price" - String
                                "call" - String
                                "call-streamer-symbol" - String
                                "put" - String
                                "put-streamer-symbol" - String                
    This assumes the first object is always the next expiration.
    Since the stock symbol price isn't given here will be unable to pare down the list of strikes at this point in time.
    */
//    synchronized public static JSONObject getOptionChains(String stockSymbol, String range, boolean useMark) {
    synchronized public static JSONObject getOptionChains(String stockSymbol) {
        JSONObject returnValue = null;
        String response = "";
        HttpURLConnection httpConn = null;
//        String requestURL = base_URL + "/option-chains/" + stockSymbol + "/nested";
        String requestURL = base_URL + "option-chains/" + stockSymbol + "/nested";
        // Send the request off for the option chain information
        try {
            httpConn = HttpUtility.sendGetRequest(requestURL, "Bearer " + getAccessToken(), true);
        } 
        catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "getOptionChains failed to send request");
            ex.printStackTrace();
        }

        // Read in the results
        try {
            response = HttpUtility.readSingleLineRespone(httpConn);
        } 
        catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "getOptionChains failed to read data");
            ex.printStackTrace();
        }
        HttpUtility.disconnect(httpConn);

        try {
            JSONObject currentDataStructure = new JSONObject( response );
            returnValue = new JSONObject();
            
            // Adjust the data structure to the desired result described above.
            JSONObject data = new JSONObject( currentDataStructure.getJSONObject("data").toString() );
            JSONArray items = new JSONArray( data.getJSONArray("items").toString() );
            JSONObject allSymbolInfo = new JSONObject( items.get(0).toString() );
            returnValue.put("underlying-symbol", allSymbolInfo.getString("underlying-symbol"));
            returnValue.put("root-symbol", allSymbolInfo.getString("root-symbol"));
            JSONArray expirations = new JSONArray( allSymbolInfo.getJSONArray("expirations").toString() );
            JSONObject oneExpiry = new JSONObject( expirations.get(0).toString() );
            returnValue.put("expiration-date", oneExpiry.getString("expiration-date") );
            returnValue.put("strikes", oneExpiry.getJSONArray("strikes"));
        } catch (Exception ex) {
           Utility.dumpExceptionInfo(ex, "getQuote");
           ex.printStackTrace();
        }
        return returnValue;
    }
    
 synchronized public static void placeBuyStraddleOrder(String primayLegSymbol, String secondaryLegSymbol, String price, String contracts, String accountNumber) { 
     // Watched a tutorial on using JSON and led me to create this heap below.
        JSONObject Leg1 = new JSONObject();
        Leg1.put("action", "Buy to Open");
        Leg1.put("symbol", primayLegSymbol);
        Leg1.put("quantity", contracts);        
        Leg1.put("instrument-type","Equity Option");

        JSONObject Leg2 = new JSONObject();
        Leg2.put("action", "Buy to Open");
        Leg2.put("symbol", secondaryLegSymbol);
        Leg2.put("quantity", contracts);        
        Leg2.put("instrument-type","Equity Option");
        
        JSONArray Legs = new JSONArray();
        Legs.put(Leg1);
        Legs.put(Leg2);

        JSONObject Order = new JSONObject();  
        Order.put("time-in-force", "Day");
        Order.put("order-type", "Limit");
        Order.put("price-effect", "Debit");
        Order.put("price", price);
        Order.put("legs", Legs);


/*  Testing - remove after working well
        JSONObject TestLegs = new JSONObject();
        TestLegs.put("instrument-type","Equity");
        TestLegs.put("action", "Buy to Open");
        TestLegs.put("quantity", 100);        
        TestLegs.put("symbol", "AAPL");

        JSONArray Legs = new JSONArray();
        Legs.put(TestLegs);
        
        JSONObject Testing = new JSONObject();
        Testing.put("order-type", "Limit");
        Testing.put("price", 1.5);
        Testing.put("price-effect", "Debit");
        Testing.put("time-in-force", "Day");
        Testing.put("legs", Legs);
*/                
        HttpURLConnection httpConn = null; 
        String requestURL = base_URL + "accounts/"+accountNumber+"/orders";
        try {
            // waitForRateLimit("Order");  Not needed since Orders are not Rate Limited
                httpConn = HttpUtility.sendPostRequest(requestURL, Order, "Bearer " + getAccessToken());
                String[] response = HttpUtility.readMultipleLinesRespone(false, httpConn);
                HttpUtility.disconnect(httpConn);
                // TBD with the response
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "placeBuyStraddleOrder");
            ex.printStackTrace();
        }
 }     
    
 // All above this line worked with the new OAuth platform
// ===============================================================    
/*
    synchronized public static void waitForRateLimit(String whichOne)
    {
        long wait = rateLimit;
        if (whichOne.equalsIgnoreCase("Order")) {
            // No wait needed.
        } else {
            wait = rateLimit;  // Everything else has a rate limit
            try {
                // Wait the remaining time
                long ND = LocalTime.now().toNanoOfDay() - lastTimeAPIUsed.toNanoOfDay();
                if ( ND < rateLimit ) {
                    long ST = (rateLimit - ND)/1000000;  // Convert to milliseconds from Nano seconds
                    Thread.sleep( ST );  
                }                
            } catch (Exception ex) {
                LogData.LogThis(Level.ERROR, " Exception 2 CheckForTradesStreamingThread.  Keep on processing.");
            }
        }                
        lastTimeAPIUsed = LocalTime.now();
    }    


    synchronized public static JSONArray getAccounts() {
        JSONArray returnValue = null;
        JSONArray resp = null;
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + getAccessToken();
        String requestURL = "https://api.tdameritrade.com/v1/accounts?fields=positions,orders";
        try {
            waitForRateLimit("Accounts");
            httpConn = TT_HttpUtility.sendGetRequest(requestURL, auth, true);
            resp = new JSONArray(TT_HttpUtility.readArrayResponse(httpConn).toString());
            TT_HttpUtility.disconnect(httpConn);
            if (resp.length() == 0) {
                returnValue = null;
            } else {
                returnValue = resp;
            }
        } catch (Exception ex) {
          Utility. dumpExceptionInfo(ex, "getAccounts");
           ex.printStackTrace();
        }
        return returnValue;
    }
    
    // For the parameters going into this routine, if you don't want to specify, then pass a null in for them and they will be ignored.
    // As an example:  To get 2 days of end of day data for MSFT the call might look like this:
    //      getPriceHistory( MSFT, "month", "1", "daily", "1", "1560527467000", "1560441067000", false ) or
    //      getPriceHistory( MSFT, "month", null, "daily", null, "1560527467000", "1560441067000", false )
    synchronized public static JSONObject getPriceHistory(String Symbol, String PeriodType, String Period, String FrequencyType, String Frequency, String EndDate, String StartDate, boolean NeedExtendedHoursData ) {
        JSONObject returnValue = null;
        JSONObject resp = null;
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + getAccessToken();
//  "https://api.tdameritrade.com/v1/marketdata/MSFT/pricehistory?apikey=tlang&periodType=month&period=1&frequencyType=daily&frequency=1&endDate=1560527467000&startDate=1560441067000&needExtendedHoursData=false"
// TBD - May have to strip the oAuthUserID down to the first part before the @, i.e. - tlang
        String[] values = userID.split("@");
        String requestURL = "https://api.tdameritrade.com/v1/marketdata/" + Symbol + "/pricehistory?apikey=" + values[0];
        if ( PeriodType != null ) {
            requestURL = requestURL + "&periodType=" + PeriodType;  
        }
        if ( Period != null ) {
            requestURL = requestURL + "&period=" + Period;
        }
        if ( FrequencyType != null ) {
            requestURL = requestURL + "&frequencyType=" + FrequencyType;
        }
        if ( Frequency != null ) {
            requestURL = requestURL + "&frequency=" + Frequency;
        }
        if ( EndDate != null ) {
            requestURL = requestURL + "&endDate=" + EndDate;
        }
        if ( StartDate != null ) {
            requestURL = requestURL + "&startDate=" + StartDate;
        }
        if ( NeedExtendedHoursData ) {
            requestURL = requestURL + "&needExtendedHoursData=true";
        } else {
            requestURL = requestURL + "&needExtendedHoursData=false";
        }
        try {
            waitForRateLimit("Accounts");
            httpConn = TT_HttpUtility.sendGetRequest(requestURL, auth, true);
//            String[] res = HttpUtility.readMultipleLinesRespone( true, httpConn);
            resp = new JSONObject(TT_HttpUtility.readObjectResponse(httpConn).toString());
            TT_HttpUtility.disconnect(httpConn);
            if (resp.length() == 0) {
                returnValue = null;
            } else {
                returnValue = resp;
            }
        } catch (Exception ex) {
          Utility. dumpExceptionInfo(ex, "getAccounts");
           ex.printStackTrace();
        }
        return returnValue;
    }

    
    // Cancel an open order
    synchronized public static void cancelOrder(String accountNumber, String orderId)
    {
        String[] response;
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + getAccessToken();
        String requestURL = "https://api.tdameritrade.com/v1/accounts/" + accountNumber + "/orders/" + orderId;
        try {
           waitForRateLimit("Order");
           httpConn = TT_HttpUtility.TDAsendDeleteRequest(requestURL, auth);
           response = TT_HttpUtility.readMultipleLinesRespone(false, httpConn);
           TT_HttpUtility.disconnect(httpConn);
        } catch (Exception ex) {
           Utility.dumpExceptionInfo(ex, "cancelOrder");
           ex.printStackTrace();
        }
    }    

    synchronized public static JSONObject getQuote(String stockSymbol) {

        JSONObject resp = null;
        Map<String, String> params = new HashMap<>();
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + getAccessToken();
        String requestURL = "https://api.tdameritrade.com/v1/marketdata/chains?" +
                "apikey="+userID +
                "&symbol=" + stockSymbol +
                "&contractType=ALL" +
                "&includeQuotes=TRUE" +
                "&strategy=STRADDLE" +
                "&range=SNK" +
                "&fromDate=" + Utility.nextExpiryIs("yyyy-MM-dd") +
                "&optionType=S";   
        try {
            // AccessToken could have been updated.
            waitForRateLimit("Chains");
            httpConn = TT_HttpUtility.sendPostRequest(requestURL, params, auth);
//            httpConn = HttpUtility.sendGetRequest(requestURL, auth, true);
            resp = TT_HttpUtility.readObjectResponse(httpConn);
            TT_HttpUtility.disconnect(httpConn);
        } catch (Exception ex) {
           Utility.dumpExceptionInfo(ex, "getQuote");
           ex.printStackTrace();
        }
        return resp;
    }

    // Strategy parameters usually = STRADDLE, but for OHLC calls we need SINGLE to get the OI for the options
    synchronized public static JSONObject getQuoteSpecifics(String stockSymbol, String range, String strategy) {

        JSONObject resp = null;
        Map<String, String> params = new HashMap<>();
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + getAccessToken();
        String requestURL = "https://api.tdameritrade.com/v1/marketdata/chains?" +
                "apikey="+userID +
                "&symbol=" + stockSymbol +
                "&contractType=ALL" +
                "&includeQuotes=TRUE" +
                "&strategy=" + strategy +
                "&strikeCount=" + range +
                "&range=ALL" +
                "&toDate=" + Utility.nextExpiryIs("yyyy-MM-dd") +
                "&optionType=S";   
        try {
            // AccessToken could have been updated.
            waitForRateLimit("Chains");
            httpConn = TT_HttpUtility.sendPostRequest(requestURL, params, auth);
            resp = TT_HttpUtility.readObjectResponse(httpConn);
            TT_HttpUtility.disconnect(httpConn);
        } catch (Exception ex) {
           Utility.dumpExceptionInfo(ex, "getQuote");
           ex.printStackTrace();
        }
        return resp;
    }
    
    
    synchronized public static JSONObject getPreferences(String account) {
    
        // Need to be able to enter stocks to use and the parameters for the trade:
        JSONObject resp = null;
        Map<String, String> params = new HashMap<>();
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + getAccessToken();
        String requestURL = "https://api.tdameritrade.com/v1/accounts/"+ account+"/preferences";
    
        try {
            waitForRateLimit("Preferences");
            httpConn = TT_HttpUtility.sendPostRequest(requestURL, params, auth);
            resp = TT_HttpUtility.readObjectResponse(httpConn);
            TT_HttpUtility.disconnect(httpConn);
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "getPreferences");
            ex.printStackTrace();
        }
        return resp;
    }
    
    
    synchronized public static JSONObject getUserPrincipals() {
    
        // Need to be able to enter stocks to use and the parameters for the trade:
        JSONObject resp = null;
        Map<String, String> params = new HashMap<>();
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + getAccessToken();
        String requestURL = "https://api.tdameritrade.com/v1/userprincipals" + "?fields=streamerConnectionInfo,streamerSubscriptionKeys";
    
        try {
            waitForRateLimit("Principals");
            httpConn = TT_HttpUtility.sendPostRequest(requestURL, params, auth);
            resp = TT_HttpUtility.readObjectResponse(httpConn);
            TT_HttpUtility.disconnect(httpConn);
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "getPrincipals");
            ex.printStackTrace();
        }
        return resp;
    }


    synchronized public static String getSocketURL(JSONObject UP) {
        String returnValue = "";

        try {
            JSONObject streamerInfo = new JSONObject(UP.getJSONObject("streamerInfo").toString());
            returnValue = streamerInfo.getString("streamerSocketUrl");
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "getSocketURL");
            ex.printStackTrace();
        }        
        return returnValue;
    }
    
    
    synchronized public static JSONArray getAccounts(boolean orders, boolean positions) {
    
        // Need to be able to enter stocks to use and the parameters for the trade:
        JSONArray returnValue = null;
        JSONArray resp = null;
        Map<String, String> params = new HashMap<>();
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + AccessToken;            
        String requestURL = "https://api.tdameritrade.com/v1/accounts";
        if (orders && positions) {
            requestURL = requestURL + "?fields=positions,orders";
        } else if (orders) {
            requestURL = requestURL + "?fields=orders";
        } else if (positions) {
            requestURL = requestURL + "?fields=positions";
        }
        // This really should be a GET request.
        try {
            waitForRateLimit("Accounts");
            httpConn = TT_HttpUtility.sendPostRequest(requestURL, params, auth);
            // Can read results as a string or JSONArray.
            resp = TT_HttpUtility.readArrayResponse(httpConn);
            TT_HttpUtility.disconnect(httpConn);
            if (resp.length() == 0) {
                returnValue = null;
            } else {
                returnValue = resp;
            }
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "getAccounts");
            ex.printStackTrace();
        }
        return returnValue;
    }

    synchronized public static JSONObject getSpecificOrder(String accountNumber, long buyOrderId) {
        JSONObject returnValue = null;

        // Need to be able to enter stocks to use and the parameters for the trade:
        JSONArray resp = null;

        Map<String, String> params = new HashMap<>();
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + AccessToken;            
        String requestURL = "https://api.tdameritrade.com/v1/accounts/" + accountNumber + "/orders/" + Long.toString(buyOrderId) ;

        // This really should be a GET request.
        try {
            waitForRateLimit("Order");
            httpConn = TT_HttpUtility.sendPostRequest(requestURL, params, auth);
            // Can read results as a string or JSONArray.
            returnValue = TT_HttpUtility.readObjectResponse(httpConn);
            TT_HttpUtility.disconnect(httpConn);
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "getSpecificOrder");
            ex.printStackTrace();
        }
        return returnValue;
    }

    // orderstatus can be one of (or comma separated) - FILLED, QUEUED, CANCELED, etc.  See the TDAAPI for a full list
    synchronized public static JSONArray getOrders(String accountNumber, String orderStatus) {
   
        JSONArray returnOrders = new JSONArray();
        
        try {
            JSONArray AllOrders = GUIData.getAllAcctsOrders();
            if ( AllOrders == null ) {
                // Nothing to do
            } else {
                for (int i = 0; i < AllOrders.length(); i++)   {
                    JSONObject AnOrder = new JSONObject( AllOrders.get(i).toString() );
                    // Is it the right account?
                    String acct = Integer.toString( AnOrder.getInt( "accountId" ) );
                    if ( acct.equalsIgnoreCase( accountNumber ) ) {
                        if ( AnOrder.has("status") ) {
                            // If orderStatus is blank then you want all Orders
                            if ( ( AnOrder.getString("status").equalsIgnoreCase( orderStatus ) ) ||
                                 ( orderStatus.equalsIgnoreCase( "" ) ) ) {
                                returnOrders.put( AnOrder );
                            }
                        } else {
                            LogData.LogThis(Level.INFO, "getOrders(A,S): Skipping an order - missing status  ==>" + AnOrder.toString());
                        }
                    }
                 }
            }
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "Exception getOrders 1");
            ex.printStackTrace();
        }
        return returnOrders;
    }


    // orderstatus can be one of (or comma separated) - FILLED, QUEUED, CANCELED, etc.  See the TDAAPI for a full list
    synchronized public static JSONArray getOrders(String orderStatus) {
        JSONArray returnOrders = new JSONArray();
        try {
            JSONArray AllOrders = GUIData.getAllAcctsOrders();

            if ( AllOrders == null ) {
                // Nothing to do
            } else {
                for (int i = 0; i < AllOrders.length(); i++)   {
                    JSONObject AnOrder = new JSONObject( AllOrders.get(i).toString() );
                    if ( AnOrder.has("status") ) {
                        // If orderStatus is blank then you want all Orders
                        if ( ( AnOrder.getString("status").equalsIgnoreCase( orderStatus ) ) ||
                             ( orderStatus.equalsIgnoreCase( "" ) ) ) {
                            returnOrders.put( AnOrder );
                        }
                    } else {
                        LogData.LogThis(Level.INFO, "getOrders(S): Skipping an order - missing status  ==>" + AnOrder.toString());
                    }
                 }
            }
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "Exception getOrders 2");
            ex.printStackTrace();
        }
        return returnOrders;
    }
    


    // orderstatus can be one of (or comma separated) - FILLED, QUEUED, CANCELED, etc.  See the TDAAPI for a full list
    //  This routine will only keep orders we care about:  FILLED, QUEUED, WORKING.  All others will be dropped.
    synchronized public static JSONArray reallyGetAllAcctsOrders() {
    
        // Need to be able to enter stocks to use and the parameters for the trade:
        JSONArray resp = null;
        JSONArray returnValue = null;
        Map<String, String> params = new HashMap<>();
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + AccessToken;            
        String requestURL = "https://api.tdameritrade.com/v1/orders" ;

        // This really should be a GET request.
        try {
            waitForRateLimit("Order");
            httpConn = TT_HttpUtility.sendPostRequest(requestURL, params, auth);
            // Can read results as a string or JSONArray.
            resp = TT_HttpUtility.readArrayResponse(httpConn);
            TT_HttpUtility.disconnect(httpConn);
            if (resp.length() == 0) {
                returnValue = null;
            } else {
                returnValue = resp;
            }

            // Now filter out orders that aren't needed.  This reduces the clutter and eliminates lots of looping in 
            //      other areas of the application.
            // This code could be embedded with above, but I wanted to introduce this as a concept that could some 
            //      day just be stripped away if need be.
            JSONArray AllOrders = returnValue;
            JSONArray Temp = new JSONArray();
            if ( AllOrders == null ) {
                // Nothing to do
            } else {
                for (int i = 0; i < AllOrders.length(); i++)   {
                    JSONObject AnOrder = new JSONObject( AllOrders.get(i).toString() );
                    if ( AnOrder.has("status") ) {
                        String Status = AnOrder.getString( "status" );
                        // If orderStatus is blank then you want all Orders
                        if ( ( Status.equalsIgnoreCase( "QUEUED" ) ) ||
                             ( Status.equalsIgnoreCase( "FILLED") ) ||
                             ( Status.equalsIgnoreCase( "WORKING") ) ) {
                            Temp.put( AnOrder );
                        }
                    } else {
                        LogData.LogThis(Level.INFO, "reallyGetAllAcctsOrders: Skipping an order - missing status  ==>" + AnOrder.toString());
                    }
                 }
            }
            if ( Temp.length() > 0 ) {
                returnValue = Temp; 
            } else {
                returnValue = null;
            }
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "reallyGetAllAcctsOrders");
            ex.printStackTrace();
        }
        return returnValue;
    }

 //  This version deprecated
 synchronized public static void placeSellStraddleOrder(String primayLegSymbol, String secondaryLegSymbol, double price, int contracts, String accountNumber) {

        JSONObject Leg1 = new JSONObject();
        JSONObject Inst1 = new JSONObject();
        Leg1.put("instruction", "SELL_TO_CLOSE");
        Leg1.put("orderLegType", "OPTION");        
        Leg1.put("quantity", contracts);          
        Inst1.put("symbol", primayLegSymbol);
        Inst1.put("assetType","OPTION");
        Inst1.put("putCall","CALL");
        Leg1.put("instrument",Inst1);

        JSONObject Leg2 = new JSONObject();
        JSONObject Inst2 = new JSONObject();
        Leg2.put("instruction", "SELL_TO_CLOSE");
        Leg2.put("orderLegType", "OPTION");        
        Leg2.put("quantity", contracts);        
        Inst2.put("symbol",secondaryLegSymbol);
        Inst2.put("assetType","OPTION");
        Inst2.put("putCall","PUT");
        Leg2.put("instrument",Inst2);
        
        JSONArray Legs = new JSONArray();
        Legs.put(Leg1);
        Legs.put(Leg2);
        
        JSONObject Order = new JSONObject();  
        Order = Order.put("session", "NORMAL");
        Order = Order.put("duration", "GOOD_TILL_CANCEL");
        Order = Order.put("orderType", "NET_CREDIT");
        Order = Order.put("complexOrderStrategyType", "STRADDLE");
        Order = Order.put("quantity", contracts);        
        Order = Order.put("requestedDestination", "AUTO");
        Order = Order.put("price", price);
        Order = Order.put("orderStrategyType", "SINGLE");
        Order = Order.put("orderLegCollection", Legs);
   
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + AccessToken;
        String requestURL = "https://api.tdameritrade.com/v1/accounts/"+accountNumber+"/orders";
        
        try {
            httpConn = TT_HttpUtility.sendPostRequest(requestURL, Order, auth);
            String[] response = TT_HttpUtility.readMultipleLinesRespone(false, httpConn);
            TT_HttpUtility.disconnect(httpConn);       
            //  TBD - What to do with response
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "placeSellStraddleOrder");
            ex.printStackTrace();
        }
 }

 synchronized public static void replaceBuylStraddleOrder(JSONObject Order, String accountNumber, long orderID) {

        HttpURLConnection httpConn = null;
        String auth = "Bearer " + AccessToken;
        String requestURL = "https://api.tdameritrade.com/v1/accounts/"+accountNumber+"/orders/" + orderID;
        
        try {
            httpConn = TT_HttpUtility.sendPutRequest(requestURL, Order, auth);
            String[] response = TT_HttpUtility.readMultipleLinesRespone(true, httpConn);
            for (String line : response) {
            }
            TT_HttpUtility.disconnect(httpConn);                
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "replaceBuylStraddleOrder");
            ex.printStackTrace();
        }
    }
   
 synchronized public static void replaceSellStraddleOrder(JSONObject Order, String accountNumber, long orderID) {

        HttpURLConnection httpConn = null;
        String auth = "Bearer " + AccessToken;
        String requestURL = "https://api.tdameritrade.com/v1/accounts/"+accountNumber+"/orders/" + orderID;
        
        try {
            httpConn = TT_HttpUtility.sendPutRequest(requestURL, Order, auth);
            String[] response = TT_HttpUtility.readMultipleLinesRespone(true, httpConn);
            for (String line : response) {
            }
            TT_HttpUtility.disconnect(httpConn);                
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "replaceSelllStraddleOrder");
            ex.printStackTrace();
        }
    }

 
     // Check the order results here and place a sell order if it was filled.
    //      Note:  accountNumber will be either 1 or 2.
    //             row is the row in the table where the orders are placed (this is so we can get data from it in this routine) 
    //  Returns an updated orderStatus if a Sell was initiated.
 synchronized public static JSONObject getAccountInfo(String accountNumber, JSONArray allAccts)
    {
        JSONObject returnValue = null;
        JSONObject securities = null;
        JSONObject tempValue = null;
        // Loop thru JSONArray
        for (int i = 0; i < allAccts.length(); i++) {
            // Get the current JSONObject
            tempValue = new JSONObject(allAccts.getJSONObject(i).toString());
            securities = new JSONObject(tempValue.getJSONObject("securitiesAccount").toString());
            // Now get the Securities JSONObject
            if (accountNumber.equalsIgnoreCase(securities.getString("accountId"))) {
                returnValue = tempValue;
                break;
            }  
        }
        return returnValue;
    }

    // Check the order results here and close down any remaining Buy orders that didn't fill.
 synchronized public static void dealWithUnfilledBuyOrders(String accountNumber)
    {   // This can be further broken down and reused, but rather spend time on new stuff.  
        String orderType = "";
        String tdaStatus = "";
        String complexOrder = "";

        JSONArray allAccts = getAccounts();
        JSONObject response = getAccountInfo(accountNumber, allAccts);
        JSONObject acctInfo = new JSONObject(response.getJSONObject("securitiesAccount").toString());
           
        // We now have an JSON Object representing the account to parse through and find what we need.
        // Let's get the positions
        if(acctInfo.has("orderStrategies")){               
            if(acctInfo.isNull("orderStrategies")){
                //nothing to do with the account
            } else {

                // This gets us the array of orders. Now loop thru each order.  If it is a buy and it is time to chase then do that.        
                JSONArray positions = new JSONArray(acctInfo.getJSONArray("orderStrategies").toString());

               // Now loop thru the order trying to find the symbol in question
                for (int aaa = 0; aaa < positions.length(); aaa++) {
                    // Get the order information  If there is a Net_Debit then it is a buy order.
                    JSONObject anOrder = new JSONObject(positions.getJSONObject(aaa).toString());
                    if ( anOrder.has("orderType") && anOrder.has("status") && anOrder.has("complexOrderStrategyType") ) {
                        orderType = anOrder.getString("orderType");
                        tdaStatus = anOrder.getString("status");
                        complexOrder = anOrder.getString("complexOrderStrategyType");
                        if (orderType.equalsIgnoreCase("NET_DEBIT") && 
                                (tdaStatus.equalsIgnoreCase("QUEUED") || tdaStatus.equalsIgnoreCase("WORKING"))&&
                                complexOrder.equalsIgnoreCase("STRADDLE")) {  //  Best to check other data too
                                // We have a Buy Order - lets cancel it
                                long orderID = anOrder.getLong("orderId");
                                String oid = Long.toString(orderID);
                                cancelOrder(accountNumber,oid);                            
                                // TBD If it was a partial fill then submit a sell order for those that were filled.
                                //  Pull the info needed out of the Order to place the Sell Order here.
    //                         placeSellStraddleOrder(String primayLegSymbol, String secondaryLegSymbol, String price, String contracts, String accountNumber) {
                                int remaining = anOrder.getInt("remainingQuantity");
                                if (remaining > 0) {
    //                                    executor.submit(new PlaceStraddleOrderSell(callSymbol, putSymbol, finalPrice, anOrder.getInt("filledQuantity"), anOrder.getString("accountId"), AccessToken, jTextAreaResults));
                                }
                        }
                    } else {
                        LogData.LogThis(Level.INFO, "dealWithUnfilledBuyOrders: Skipping an order - missing status  ==>" + anOrder.toString());
                    }
                 }  // For loop
            }  // Else
        }  // If has orderStrategies

    } //  dealWithUnfilledBuyOrders

    // Be sure we don't do something that can be interpretted as Manipulation
    //     sellCheck is True when placing a Sell Order, false otherwise
    //      the Stirng returned would when we found manipulation possible and cancelled at least one order.
    synchronized public static String sellManipulationCheck(String L1SymbolOrder, String L2SymbolOrder, double priceCheck  ) {
        String returnValue = "";
        try {
            String[][] AllAccounts = GUIData.getAccounts();
            for (int ooo = 0; ooo < AllAccounts.length; ooo++) {
                if ( AllAccounts[ooo][1].equalsIgnoreCase( GUIData.tastyTrade ) ) {
                    JSONArray OpenOrders = getOrders( AllAccounts[ooo][0], "QUEUED" );
                    if ( OpenOrders != null ) {
                        for (int abc = 0; abc < OpenOrders.length(); abc++) {
                            JSONObject anOpenOrder = new JSONObject(OpenOrders.getJSONObject(abc).toString());
                            // Check the quantity first if good then go deeper  (Could check other parameters
                            String openOrderType = anOpenOrder.getString("orderType");
                            double openPrice = anOpenOrder.getDouble("price");
                            JSONArray Legs = new JSONArray( anOpenOrder.getJSONArray( "orderLegCollection").toString() );
                            JSONObject Leg = new JSONObject( Legs.get(0).toString() );
                            JSONObject openInstrument = new JSONObject( Leg.getJSONObject("instrument").toString() );
                            String openSymbol = openInstrument.getString("symbol");
                            if ( ( openOrderType.equalsIgnoreCase( "NET_DEBIT" ) ) &&   // It is a Buy Order
                                 ( openPrice >= priceCheck ) &&
                                 (  openSymbol.equalsIgnoreCase(L1SymbolOrder) || openSymbol.equalsIgnoreCase(L2SymbolOrder)  )  ) {
                                // We must cancel this open order before placing the Sell Order.
                                cancelOrder( AllAccounts[ooo][0], Long.toString( anOpenOrder.getLong("orderId") ) );
                                returnValue = Utility.getTimeStamp() + " MANIPULATION ATTEMP for SELL Order in account " + AllAccounts[ooo][0] + "  SELL ORDER IS "+ L1SymbolOrder + "/" + L2SymbolOrder + " Price: " + Double.toString(priceCheck);
                                LogData.LogThis(Level.INFO, returnValue );
                            }
                        }
                    }
                }
            }
        } catch ( Exception ex ) {
                    Utility.dumpExceptionInfo(ex, "ManipulationCheck - Manipulation Check");
                    ex.printStackTrace();
        }
        return returnValue;
    }

    // Be sure we don't do something that can be interpretted as Manipulation
    //     sellCheck is True when placing a Sell Order, false otherwise
    //      the Stirng returned would when we found manipulation possible and cancelled at least one order.
    synchronized public static boolean buyManipulationPossible(String L1SymbolOrder, String L2SymbolOrder, double priceCheck  ) {
        boolean returnValue = false;
        try {
            String[][] AllAccounts = GUIData.getAccounts();
            for (int ooo = 0; ooo < AllAccounts.length; ooo++) {
                if ( AllAccounts[ooo][1].equalsIgnoreCase( GUIData.tastyTrade ) ) {
                    JSONArray OpenOrders = getOrders( AllAccounts[ooo][0], "QUEUED" );
                    if ( OpenOrders != null ) {
                        for (int abc = 0; abc < OpenOrders.length(); abc++) {
                            JSONObject anOpenOrder = new JSONObject(OpenOrders.getJSONObject(abc).toString());
                            // Check the quantity first if good then go deeper  (Could check other parameters
                            String openOrderType = anOpenOrder.getString("orderType");
                            double openPrice = anOpenOrder.getDouble("price");
                            JSONArray Legs = new JSONArray( anOpenOrder.getJSONArray( "orderLegCollection").toString() );
                            JSONObject Leg = new JSONObject( Legs.get(0).toString() );
                            JSONObject openInstrument = new JSONObject( Leg.getJSONObject("instrument").toString() );
                            String openSymbol = openInstrument.getString("symbol");
                            if ( (openOrderType.equalsIgnoreCase( "NET_CREDIT" ) ) &&   // It is a Sell Order
                                 ( openPrice <= priceCheck ) &&
                                 (  openSymbol.equalsIgnoreCase(L1SymbolOrder) || openSymbol.equalsIgnoreCase(L2SymbolOrder)  )   ) {
                                // We must cancel this open order before placing the Sell Order.
                                returnValue = true;
                                break;
                            }
                        }
                    }
                }
            }
        } catch ( Exception ex ) {
                    Utility.dumpExceptionInfo(ex, "ManipulationCheck - Manipulation Check");
                    ex.printStackTrace();
        }
        return returnValue;
    }
*/
/* TESTED 7/11/19
    orderLegCollection =>
    [   {"orderLegType":"OPTION","quantity":3,"instruction":"BUY_TO_OPEN","legId":1,
                "instrument":{"symbol":"DAL_071219C60","cusip":"0DAL..GC90060000","description":"DAL JUL 12 2019 60.0 Call","assetType":"OPTION"},
                "positionEffect":"OPENING"},
        {"orderLegType":"OPTION","quantity":3,"instruction":"BUY_TO_OPEN","legId":2,
                "instrument":{"symbol":"DAL_071219P60","cusip":"0DAL..SC90060000","description":"DAL JUL 12 2019 60.0 Put","assetType":"OPTION"},
                "positionEffect":"OPENING"}]
    */
    // Be sure we don't do something that can be interpretted as Manipulation
    //     sellCheck is True when placing a Sell Order, false otherwise
    //      the Stirng returned would when we found manipulation possible and cancelled at least one order.
/*
    synchronized public static void GUICancelOrderRequest(String AccountNumber, String Symbol, String Strike) {
        try {
            String L1SymbolOrder = "";
            String L2SymbolOrder = "";
            JSONArray OpenOrders = getOrders( AccountNumber, "QUEUED" );
            if ( OpenOrders != null ) {
                for (int abc = 0; abc < OpenOrders.length(); abc++) {
                    JSONObject anOpenOrder = new JSONObject(OpenOrders.getJSONObject(abc).toString());
                    // Check the quantity first if good then go deeper  (Could check other parameters
                    String openOrderType = anOpenOrder.getString("orderType");
                    double openPrice = anOpenOrder.getDouble("price");
                    JSONArray Legs = new JSONArray( anOpenOrder.getJSONArray( "orderLegCollection").toString() );
                    JSONObject Leg = new JSONObject( Legs.get(0).toString() );
                    JSONObject openInstrument = new JSONObject( Leg.getJSONObject("instrument").toString() );
                    String TempSymbol = openInstrument.getString("symbol");
                    String[] Values = TempSymbol.split("_");
                    String openSymbol = Values[0];
                    String[] Values2 = Values[1].split("C");
                    String openStrike = "";
                    if ( Values[1].contains("P") ) {
                        Values2 = Values[1].split("P");
                    }
                    openStrike = Values2[1];                        
                        
                    if (  openSymbol.equalsIgnoreCase( Symbol ) && openStrike.equalsIgnoreCase( Strike ) ) {
                        // We must cancel this open order before placing the Sell Order.  Don't care if it is a buy or sell order.
                        cancelOrder( AccountNumber, Long.toString( anOpenOrder.getLong("orderId") ) );
                        LogData.LogThis(Level.INFO, " GUI Cancel Order:  Account" + AccountNumber + "  ORDER IS "+ L1SymbolOrder + "/" + L2SymbolOrder );
                    }
                }
            }
        } catch ( Exception ex ) {
                    Utility.dumpExceptionInfo(ex, "GUICancelOrderRequest - Cancel Order Request from the GUI");
                    ex.printStackTrace();
        }
    }

 // returns 0 if not a Straddle order, otherwise returns the Quatnity for the Straddle
 private static int isStraddlePosition( String PorC, JSONArray AllPositions, String UnderlyingSymbol, int FirstQuantity ) {

     int isStraddle = 0;
     String lookFor = "";
     if ( PorC.equalsIgnoreCase("PUT") ) {
         lookFor = "CALL";
     } else {
         lookFor = "PUT";
     }
     // Loop thru all the orders and find the other half of the straddle.
     for ( int OrderIndex = 0; OrderIndex < AllPositions.length(); OrderIndex++ ) {
         // Get the details about the position needed
         JSONObject anOrder = new JSONObject( AllPositions.getJSONObject(OrderIndex).toString() );
         // These will be listed as Calls and Puts.  So I will have to pair them up before looking at the order strategies
         JSONObject instrument = new JSONObject( anOrder.getJSONObject("instrument").toString() );
         if ( instrument.getString("assetType").equalsIgnoreCase("OPTION") ) {
             if( instrument.getString("underlyingSymbol").equalsIgnoreCase( UnderlyingSymbol ) ) {
                 if( instrument.getString("putCall").equalsIgnoreCase( lookFor ) ) {
                     int SecondQuantity = anOrder.getInt("longQuantity");
                     if ( SecondQuantity >= FirstQuantity ) {
                         isStraddle = FirstQuantity;
                     } else {
                         isStraddle = SecondQuantity;  // Is this even possible?
                     }
                     break;
                 }
             }
         }
     }    
     return isStraddle;
   }

 
    // Check the order results here and close down any remaining Buy orders that didn't fill (this is for Straddle Orders only).
 public static void PlaceMissingSellOrders(String accountNumber, javax.swing.JTable jTableSymbolData)
    {   // This can be further broken down and reused, but rather spend time on new stuff.  
        String orderType = "";
        String tdaStatus = "";
        String complexOrder = "";

        JSONArray allAccts = getAccounts();
        JSONObject response = getAccountInfo(accountNumber, allAccts);
        JSONObject acctInfo = new JSONObject(response.getJSONObject("securitiesAccount").toString());
           
        // If there is a position, then find the corresponding sell order.   If it isn't there then place one.
        if( acctInfo.has("positions") ){  
            if( acctInfo.isNull("positions") ){  
                // Nothing to do - no positions
            }  else { // Examine each position, be mindful of partial sell orders as you go thru this.                
                // Loop thru each position here
                JSONArray AllPositions = new JSONArray( acctInfo.getJSONArray("positions").toString() );
                for ( int PositionIndex = 0; PositionIndex < AllPositions.length(); PositionIndex++ ) {
                    // Get the details about the position needed
                    JSONObject aPosition = new JSONObject( AllPositions.getJSONObject(PositionIndex).toString() );
                    int Quantity = aPosition.getInt("longQuantity");
                    // These will be listed as Calls and Puts.  So I will have to pair them up before looking at the order strategies
                    JSONObject instrument = new JSONObject( aPosition.getJSONObject("instrument").toString() );
                    if ( instrument.getString("assetType").equalsIgnoreCase("OPTION") ) {
                        String UnderlyingSymbol = instrument.getString("underlyingSymbol");
                        String OptionSymbol = instrument.getString("symbol");
                        boolean isCall = instrument.getString("putCall").equalsIgnoreCase("Call");
                        int StraddleQuantity = isStraddlePosition( instrument.getString("putCall"), AllPositions, UnderlyingSymbol, Quantity );
                        if ( StraddleQuantity > 0 ) {  // We have found a straddle position  (not sure if Quantity and StraddleQuantity are different.  Should be the same.  TBD)                            
                            // Loop thru the orders to see if there is one that matches (all but quantity)
                                // If quantity matches then done
                                // else adjust the quantity to the correct amount
                                // if there is no order then place one
                            boolean doneCheckingPosition = false;   // TBD - After checking the Put or Call for the other half, how to avoid looking again for the order?
                            if(acctInfo.has("orderStrategies")){               
                                if(acctInfo.isNull("orderStrategies")){
                                    //nothing to do with the account
                                } else {
                                    // This gets us the array of orders that are QUEUED up to sell and is updated for every new position (this is to handle the PUT and CALL issue for duplicates).
                                    JSONArray currentOrders = getOrders(accountNumber, "QUEUED");
                                    if ( currentOrders == null ) {
                                       // We need to place a sell order. 
                                    } else {
                                        // Now loop thru the order trying to find the symbol in question
                                        for (int OrderStratIndex = 0; OrderStratIndex < currentOrders.length(); OrderStratIndex++) {
                                            // Get the order information  If there is a Net_Debit then it is a buy order.
                                            JSONObject anOrder = new JSONObject(currentOrders.getJSONObject(OrderStratIndex).toString());
                                            if ( anOrder.has("orderType") && anOrder.has("status") && anOrder.has("complexOrderStrategyType") ) {
                                                orderType = anOrder.getString("orderType");
                                                tdaStatus = anOrder.getString("status");
                                                complexOrder = anOrder.getString("complexOrderStrategyType");
                                                if (orderType.equalsIgnoreCase("NET_CREDIT") && 
                                                        (tdaStatus.equalsIgnoreCase("QUEUED") || tdaStatus.equalsIgnoreCase("WORKING"))&&
                                                        complexOrder.equalsIgnoreCase("STRADDLE")) {  //  Best to check other data too
                                                    // Is this the order for the right symbol?
                                                    JSONArray OLC = new JSONArray( anOrder.getJSONArray("orderLegCollection").toString() );
                                                    for ( int OLCIndex = 0; OLCIndex < OLC.length(); OLCIndex++ ) {
                                                        JSONObject OrderLeg = new JSONObject( OLC.get(OLCIndex).toString() );
                                                        String OLT = OrderLeg.getString("orderLegType");
                                                        JSONObject OLInstrument = new JSONObject( OrderLeg.getJSONObject("instrument").toString() );
                                                        String [] breakUp = OLInstrument.getString("symbol").split("_");
                                                        String curUnderlyingSymbol = breakUp[0];
                                                        if( curUnderlyingSymbol.equalsIgnoreCase( UnderlyingSymbol ) ) {
                                                            if ( OLT.equalsIgnoreCase("OPTION") ) {
                                                                if ( OLInstrument.getString("symbol").equalsIgnoreCase( OptionSymbol ) ) {
                                                                    // It has a sell order, now check the quantities
                                                                    int remainingQuantity = anOrder.getInt("remainingQuantity");
                                                                    if ( remainingQuantity == StraddleQuantity ) {
                                                                        // Nothing to do here
                                                                    } else {
                                                                        // Need to adjust the sell quantity here
                                                                         long orderID = anOrder.getLong("orderId");
                                                                         String formattedPrice = getFormattedPrice( UnderlyingSymbol, jTableSymbolData );
                                                                        replaceSellStraddleOrder(anOrder, accountNumber, orderID, StraddleQuantity, formattedPrice);
                                                                    }
                                                                    doneCheckingPosition = true;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                if ( doneCheckingPosition ) {
                                                    break;
                                                }
                                            } else {
                                                LogData.LogThis(Level.INFO, "PlaceMissingSellOrders: Skipping an order - missing status  ==>" + anOrder.toString());
                                            }
                                         }  // For loop
                                    }
                                }  // Else
                            }  // If has orderStrategies                    
                            if ( doneCheckingPosition == false ) {
                                // Must place a Sell Order  
                                String L1SymbolOrder = OptionSymbol;
                                String L2SymbolOrder = OptionSymbol;
                                //  Option Symbols look like ==  KR_070618P28.5
                                String[] OS = OptionSymbol.split("_");
                                if ( isCall ) {
                                    String[] OS2 = OS[1].split("C");
                                    L2SymbolOrder = OS[0] + "_" + OS2[0] + "P" + OS2[1];
                                } else {
                                    String[] OS2 = OS[1].split("P");
                                    L2SymbolOrder = OS[0] + "_" + OS2[0] + "C" + OS2[1];
                                }
                                String formattedPrice = getFormattedPrice( UnderlyingSymbol, jTableSymbolData );
                                placeSellStraddleOrder( L1SymbolOrder, L2SymbolOrder, Double.parseDouble( formattedPrice ),  StraddleQuantity, accountNumber);   // TBD - DIdn't place sell order.
                                LogData.LogThis( Level.INFO, "   PLACED MISSING SELL ORDER FOR = " + L1SymbolOrder + " price= " + formattedPrice + "  quantity= " + Integer.toString( StraddleQuantity) + "  Acct # = " + accountNumber  );
                            }
                        }  // if StraddleQuantity > 0
                    } // if AssetType = OPTION
                } // OrderIndex loop
            } // Else            
        }   // Has positions     
    } //  entire Method

private static String getFormattedPrice( String symbol, javax.swing.JTable jTableSymbolData ) {

    // Loop thru the table and find the Symbol then record the maxPrice, PercentGoal.  Use these values to determine selling price.
    String returnValue = "99.00";
    double percentGoal = 0.0;
    double maxPrice = 0.0;
    for (int row = 0; row < jTableSymbolData.getRowCount(); row++) {
        if (jTableSymbolData.getValueAt( row, GUIData.symbolColumn ) == null) {
            // at end of value data so quit.
            break;
        } else {        
            // Is this the symbol we are looking for here?
            if ( jTableSymbolData.getValueAt( row, GUIData.symbolColumn ).toString().equalsIgnoreCase(symbol) ) {

                // Yes, so calculate selling price based on percent goal of the max price.  This is the value to use when a Sell order is mssing.
                percentGoal =Double.parseDouble( jTableSymbolData.getValueAt( row, GUIData.percentGoalColumn).toString() );
                maxPrice =Double.parseDouble( jTableSymbolData.getValueAt( row, GUIData.maxPriceColumn).toString() );
                double goalValue = maxPrice + (maxPrice * percentGoal/100);
                DecimalFormat df2 = new DecimalFormat(".##");
                returnValue = df2.format(goalValue);
                break;
            }
        }
    }
    return returnValue;
}


 
synchronized public static void replaceSellStraddleOrder(JSONObject anOrder, String accountNumber, long orderID, int newQuantity, String formattedPrice) {

        try {
            JSONArray ja = new JSONArray(anOrder.getJSONArray("orderLegCollection").toString());
            JSONObject oldLeg1 = new JSONObject(ja.getJSONObject(0).toString());
            JSONObject oldInst1 = new JSONObject(oldLeg1.getJSONObject("instrument").toString());
            JSONObject oldLeg2 = new JSONObject(ja.getJSONObject(1).toString());
            JSONObject oldInst2 = new JSONObject(oldLeg2.getJSONObject("instrument").toString());

            String primaryLegSymbol, secondaryLegSymbol; //, price, oid;
            primaryLegSymbol = oldInst1.getString("symbol");
            secondaryLegSymbol = oldInst2.getString("symbol");

            JSONObject Leg1 = new JSONObject();
            JSONObject Inst1 = new JSONObject();
            Leg1.put("instruction", "SELL_TO_CLOSE");
            Leg1.put("quantity", newQuantity );        
            Inst1.put("assetType","OPTION");
            Inst1.put("symbol", primaryLegSymbol);
            String[] v1 = primaryLegSymbol.split("_");              // Get the values from the line.
            if (v1[1].contains("C")) {
                Inst1.put("putCall", "CALL");  
            } else {
                Inst1.put("putCall", "PUT");  
            }
            Leg1.put("instrument",Inst1);

            JSONObject Leg2 = new JSONObject();
            JSONObject Inst2 = new JSONObject();
            Leg2.put("instruction", "SELL_TO_CLOSE");
            Leg2.put("quantity", newQuantity);        
            Inst2.put("assetType","OPTION");
            Inst2.put("symbol",secondaryLegSymbol);
            // Do the opposite of above
            if (v1[1].contains("C")) {
                Inst2.put("putCall", "PUT");  
            } else {
                Inst2.put("putCall", "CALL");  
            }
            Leg2.put("instrument",Inst2);

            JSONArray Legs = new JSONArray();
            Legs.put(Leg1);
            Legs.put(Leg2);

            JSONObject Order = new JSONObject();  
            Order = Order.put("session", "NORMAL");
            Order = Order.put("duration", "DAY");
            Order = Order.put("orderType", "NET_CREDIT");
            Order = Order.put("complexOrderStrategyType", "STRADDLE");
            Order = Order.put("quantity", newQuantity );
            Order = Order.put("requestedDestination", "AUTO");
            Order = Order.put("price", Double.parseDouble( formattedPrice ) );
            Order = Order.put("orderLegCollection", Legs);
            Order = Order.put("orderStrategyType", "SINGLE");
            replaceSellStraddleOrder(Order, accountNumber, orderID);
            
            LogData.LogThis( Level.INFO, "   SELL ORDER QUANTITY UPDATED = " + primaryLegSymbol + " new price= " + formattedPrice + "  quantity= " + Integer.toString( newQuantity ) + "  OrderID= " + orderID);
        } catch (Exception ex) {
            Utility. dumpExceptionInfo(ex, " Exception: replaceSellStraddleOrder.  Keep on processing.");
        }
    }

 //  This version deprecated
 synchronized public static void placeSellStraddleOrder(String primayLegSymbol, String secondaryLegSymbol, String price, String contracts, String accountNumber) {

        JSONObject Leg1 = new JSONObject();
        JSONObject Inst1 = new JSONObject();
        Leg1.put("instruction", "SELL_TO_CLOSE");
        Leg1.put("orderLegType", "OPTION");        
        Leg1.put("quantity", contracts);          
        Inst1.put("symbol", primayLegSymbol);
        Inst1.put("assetType","OPTION");
        Inst1.put("putCall","CALL");
        Leg1.put("instrument",Inst1);

        JSONObject Leg2 = new JSONObject();
        JSONObject Inst2 = new JSONObject();
        Leg2.put("instruction", "SELL_TO_CLOSE");
        Leg2.put("orderLegType", "OPTION");        
        Leg2.put("quantity", contracts);        
        Inst2.put("symbol",secondaryLegSymbol);
        Inst2.put("assetType","OPTION");
        Inst2.put("putCall","PUT");
        Leg2.put("instrument",Inst2);
        
        JSONArray Legs = new JSONArray();
        Legs.put(Leg1);
        Legs.put(Leg2);
        
        JSONObject Order = new JSONObject();  
        Order = Order.put("session", "NORMAL");
        Order = Order.put("duration", "GOOD_TILL_CANCEL");
        Order = Order.put("orderType", "NET_CREDIT");
        Order = Order.put("complexOrderStrategyType", "STRADDLE");
        Order = Order.put("quantity", contracts);        
        Order = Order.put("requestedDestination", "AUTO");
        Order = Order.put("price", price);
        Order = Order.put("orderStrategyType", "SINGLE");
        Order = Order.put("orderLegCollection", Legs);
   
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + AccessToken;
        String requestURL = "https://api.tdameritrade.com/v1/accounts/"+accountNumber+"/orders";
        
        try {
            httpConn = TT_HttpUtility.sendPostRequest(requestURL, Order, auth);
            String[] response = TT_HttpUtility.readMultipleLinesRespone(false, httpConn);
            TT_HttpUtility.disconnect(httpConn);       
            //  TBD - What to do with response
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "placeSellStraddleOrder");
            ex.printStackTrace();
        }
 }
*/       
}