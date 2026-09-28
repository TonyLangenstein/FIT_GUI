/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.

 */
package FITGUI;

import java.io.File;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;
//import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.*;
import org.apache.logging.log4j.Level;

/**
 *
 * @author thela
 */
public class SchwabAPI {

    private static String RefreshToken = "";
    private static String AccessToken = "";
    private static String IDToken = "";
    private static String redirectURI = "";
    private static String appKey = "";
    private static String secret;
    
    private static boolean accountNumbersRetrieved = false;
    private static boolean loggedIn = false;
    private static JSONArray accountNumbers = null;
    private static String AuthCode = "";
    private static String tokenExpirationDate = "";
    private static LocalTime lastTokenUpdate = LocalTime.now();
    private static LocalTime lastTimeAPIUsed = LocalTime.now();
    private static final long rateLimit = 990000000; // Nano Seconds  Up to 99 from 50 here to deal with TDA issues.
    private static int tokenUpdateWaitTime = 25;
    private static final String TOKENS_FILENAME = "SchwabTokens.txt";
    private static Log4J2AsyncLogger LogData =new Log4J2AsyncLogger();    
    private static String base_Auth_URL = "https://api.schwabapi.com/v1";
    private static String base_Trader_URL = "https://api.schwabapi.com/trader/v1";
    private static String base_MarketData_URL = "https://api.schwabapi.com/marketdata/v1";
    // The URL was https://api.tdameritrade.com/v1
    

    synchronized private static String getBase64EncodedClientAuth() {
        // TBD - these parameters must go into a resource file: Client Secret, that way Mike, Maya and others can just update that.
//        String clientSecret = "JYkG6jU7cUkOFFeX";
        String authInfo = appKey + ":" + secret;
        byte[] byteAuthInfo = Base64.encodeBase64(authInfo.getBytes());
        return (new String(byteAuthInfo));
    }

    synchronized public static String getBaseURL(String URLType) {
        String returnValue = base_Auth_URL;
        if (URLType.equalsIgnoreCase("Trader")) {
            returnValue = base_Trader_URL;
        } else {
            if (URLType.equalsIgnoreCase("MarketData")) {
                returnValue = base_MarketData_URL;
            }
        }
        return returnValue;
    }
    
    synchronized public static void setBaseURL(String URL, String URLType) {
        if (URLType.equalsIgnoreCase("Trader")) {
            base_Trader_URL = URL;
        } else {
            if (URLType.equalsIgnoreCase("MarketData")) {
                base_MarketData_URL = URL;
            } else {
                base_Auth_URL = URL;
            }
        }
    }
    
    synchronized public static void UpdateRefreshToken(String Token) {
        RefreshToken = Token;
    }

    synchronized public static void UpdateAccessToken(String Token) {
        AccessToken = Token;
    }

    synchronized public static String getRedirectURI() {
        return redirectURI;
    }
    
    synchronized public static void setRedirectURI(String RURI) {
        redirectURI = RURI;
    }
    
    synchronized public static String getAppKey() {
        return appKey;
    }
    
    synchronized public static void setAppKey(String newAppKey) {
        appKey = newAppKey;
    }
    
    synchronized public static void setSecret(String newSecret) {
        secret = newSecret;
    }
    
    synchronized public static String getAccessToken() {
        return AccessToken;
    }
    
    synchronized public static void setAccessToken(String AT) {
        AccessToken = AT;
    }
    
    synchronized public static String getRefreshToken() {
        return RefreshToken;
    }
    
    synchronized public static void setRefreshToken(String RT) {
        RefreshToken = RT;
    }
    
    synchronized public static boolean getLoggedIn() {
        return loggedIn;
    }
    
    synchronized public static void setLoggedIn(boolean newValue) {
        loggedIn = newValue;
    }
    
    synchronized public static boolean loadAccountNumbers() {
        if (accountNumbersRetrieved == false) {
            if (loggedIn == true) {
                JSONArray response = new JSONArray();
                HttpResponse httpResp = null;
                String auth = "Bearer " + getAccessToken();
                String requestURL = base_Trader_URL + "/accounts/accountNumbers";
                try {
                    waitForRateLimit("Get");
                    httpResp = HttpUtility.sendGetHttpRequest(requestURL, auth);
                    int responseCode = httpResp.statusCode();
                    String resp = httpResp.body().toString();    
                    if (responseCode == HttpUtility.requestSuccessful) {
                        accountNumbersRetrieved = true;
                        accountNumbers = new JSONArray( resp );
                    } else {
                        accountNumbersRetrieved = false;
                        accountNumbers = null;
                        LogData.LogThis(Level.ERROR, " Response body = " + resp);
                    }
                } catch (Exception ex) {
                   Utility.dumpExceptionInfo(ex, "loadAccountNumbers");
                   ex.printStackTrace();
                }
            }
        }
        return accountNumbersRetrieved;
    }
    
    // Will return the HashedAccountValue for a given account number.  If not found then will return "NotFound".
    synchronized public static String getHashedAccountNumber(String acctNum) {
        String returnValue = "NotFound";
        try {
            for (int i = 0; i < accountNumbers.length(); i++) {
                JSONObject accountInfo = new JSONObject( accountNumbers.get(i).toString() );
                if (accountInfo.getString("accountNumber").equalsIgnoreCase( acctNum )) {
                    returnValue = accountInfo.getString("hashValue");
                    break;
                }
            }
        } catch (Exception ex) {
           Utility.dumpExceptionInfo(ex, "getHashedAccountNumber");
           ex.printStackTrace();
        }
        return returnValue;
    }
    
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
                    Utility.hybridPrecisionWait( ST );  
                }                
            } catch (Exception ex) {
                LogData.LogThis(Level.ERROR, " Exception 2 CheckForTradesStreamingThread.  Keep on processing.");
            }
        }                
        lastTimeAPIUsed = LocalTime.now();
    }    

   synchronized public static boolean checkTimeToUpdateTokens() {
       boolean returnValue = false;
       if ((RefreshToken == "") || (AccessToken == "") || (appKey == "")) {
            // Do nothing.
        } else {
            if (LocalTime.now().isAfter(lastTokenUpdate.plusMinutes(tokenUpdateWaitTime))) {
                returnValue = updateTokens();         
            }
        }
       return returnValue;
   }
       

   synchronized public static void SaveToken()                                                 
   {                                                     
      try
         {
         // Save the Access and Refresh tokens to a file so we can load later.
         // This will allow us to bypass the login process ... providing the refresh
         // token has not expired.  It is valid for 7 days.
         PrintWriter tokensWriter = new PrintWriter(TOKENS_FILENAME);
         if (tokenExpirationDate.isEmpty()) {
            LocalDate tempDate = LocalDate.now();
            LocalDate adjustedDate = tempDate.plusDays(7);
            Date date = java.sql.Date.valueOf(adjustedDate);
            tokenExpirationDate = new SimpleDateFormat("yyyy-MM-dd").format(date);  
            lastTokenUpdate = LocalTime.now();
         }
         tokensWriter.println(tokenExpirationDate + "," + RefreshToken+ "," + IDToken);
         tokensWriter.close();

         } catch (Exception ex)
         {
           Utility.dumpExceptionInfo(ex, "SaveToken");
           ex.printStackTrace();
         }
   }                                                

   // returns True if token is valid, false otherwise.
   synchronized public static boolean LoadToken()                                                 
   {                                                     
       boolean returnValue = false;
       try
         {
         // Load all of the selected orders from the StraddlesData table into the Orders Table.

            File tokensFile = new File(TOKENS_FILENAME);
            if (tokensFile.exists() ) {
               Scanner inputStream = new Scanner(tokensFile);  // Read the file.
               String data = inputStream.nextLine();           // Read the line.
               String[] values = data.split(",");              // Get the values from the line.
               tokenExpirationDate = values[0];
               RefreshToken = values[1];
               IDToken = values[2];
               inputStream.close();                            // Close the file.

               LocalDate expirationDate = LocalDate.parse(tokenExpirationDate);
               LocalDate todayIs = LocalDate.now();
      //         jTextFieldRefreshToken.setText(RefreshToken);                
               returnValue = (todayIs.compareTo(expirationDate) < 0);             
               loggedIn = returnValue;
               loadAccountNumbers();
            }
         } catch (Exception ex)
         {
           Utility.dumpExceptionInfo(ex, "LoadToken");
           ex.printStackTrace();
         }
      return returnValue;
   }                                                

    // The new Schwab stuff is as follows:
    // Example request ... Exchange "code" for Access Token
    //  {    curl -X POST \    https://api.schwabapi.com/v1/oauth/token \    
    //          -H 'Authorization: Basic {BASE64_ENCODED_Client_ID:Client_Secret} \    
    //          -H 'Content-Type: application/x-www-form-urlencoded' \    
    //          -d 'grant_type=authorization_code&code={AUTHORIZATION_CODE_VALUE}&redirect_uri=https://example_url.com/callback_example'    }
    // Example response ... 
    //  {      "expires_in": 1800,  //Number of seconds access_token is valid for      "token_type": "Bearer",      
    //          "scope": "api",      
    //          "refresh_token": "{REFRESH_TOKEN_HERE}", 
    //          //Valid for 7 days      "access_token": "{ACCESS_TOKEN_HERE}", 
    //          //Valid for 30 minutes      "id_token": "{JWT_HERE}"    }
    //
    //  To Refresh an Access Token with existing Refresh Token
    // Example request ... Get Access Token with Refresh Token
    //  curl -X POST \    https://api.schwabapi.com/v1/oauth/token \    
    //          -H 'Authorization: Basic {BASE64_ENCODED_Client_ID:Client_Secret} \    
    //          -H 'Content-Type: application/x-www-form-urlencoded' \    
    //          -d 'grant_type=refresh_token&refresh_token={REFRESH_TOKEN_GENERATED_FROM_PRIOR_STEP}
    // Example response ...
    //  {      "expires_in": 1800,  //Number of seconds access_token is valid for      "token_type": "Bearer",      
    //          "scope": "api",      
    //          "refresh_token": "{REFRESH_TOKEN_HERE}", 
    //          //Valid for 7 days      "access_token":  "{NEW_ACCESS_TOKEN_HERE}",
    //          //Valid for 30 minutes      "id_token": "{JWT_HERE}"    }
    // This will update the tokens to the internal variables.  
    synchronized public static boolean getTokens(String grantType, String refreshToken, String code, String currentAppKey, String currentRedirectURI) {
        boolean returnvalue = false;
        
        // Update the clientID and redirectURI in case they have changed
        appKey = currentAppKey;
        redirectURI = currentRedirectURI;
        Map<String, String> params1 = new HashMap<>();
        HttpURLConnection httpConn = null;
        params1.put("grant_type", grantType);
        if (grantType.contains("refresh_token")) {
            params1.put("refresh_token", refreshToken);
        } else {
            params1.put("code", code);
            params1.put("redirect_uri", redirectURI);
        }

        String requestURL = base_Auth_URL + "/oauth/token"; //  -H 'Authorization: Basic JYkG6jU7cUkOFFeX' -H 'Content-Type: application/x-www-form-urlencoded'";
        try {
            waitForRateLimit("Token");
            httpConn = HttpUtility.sendPostRequest(requestURL, params1, getBase64EncodedClientAuth());
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "getTokens");
            ex.printStackTrace();
        }

        // Read in the results
        String response = "";
        try {
            response = HttpUtility.readSingleLineRespone(httpConn);
            /*  Below is what the response looks like
            {   "expires_in":1800,
                "token_type":"Bearer",
                "scope":"api",
                "refresh_token":"hTFNAfEizzWh4OwLXuE4bfybwztxDHf3u2ktyAEjNkGmFErRMQ3xZcwG9DIhmtE6gA5sZ--Qn9wFFHvnAsH9QjfbhYTM0zXM",
                "access_token":"I0.b2F1dGgyLmNkYy5zY2h3YWIuY29t.o7XIsJifRcXINByqvS3NagYi5786AEtCN5sDbGFEmIc@",
                "id_token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI2ZjU5YWI3NmQ1YmZmMjFlZTlmMzRiMThkNTVlOTk3NzEwODlkZDRjYThhYTFmYWQ0NWJlMjI2ZmQ3MWQ4NTNlIiwiYXVkIjoiY1U4clB0Z2RVWVp0MlB2aEJVWjZYcUR5VDFFNWVrZWMiLCJpc3MiOiJ1cm46Ly9hcGkuc2Nod2FiYXBpLmNvbSIsImV4cCI6MTcxNTcxMTUzMSwiaWF0IjoxNzE1NzA3OTMxLCJqdGkiOiJjM2QxZmQxZi0wZjM1LTRmMzctOTgyMS1hYmJjMDYzZGQxYTEifQ._fDSX3Kwokc7yjsLCDm0YYJjK2BMzmpQpkzFtOevRYA"
            }
            */
            JSONObject resp = new JSONObject(response);
            // Parse out and store the tokens 
            RefreshToken = resp.getString("refresh_token");
            AccessToken = resp.getString("access_token");
            IDToken = resp.getString("id_token");
            loggedIn = true;
            loadAccountNumbers();
            // Save the tokens
            SaveToken();
            returnvalue = true;
            try {
                 while ( !GUIData.LockOrderSocketData() ) { Utility.hybridPrecisionWait(10);}
                 GUIData.SetOrdersSocketData( GUIData.TokenUpdate + "," + getRefreshToken() + "," + getAccessToken() + ", SCHWAB" );
            } catch (Exception e) {
                  Utility.dumpExceptionInfo(e);
                  e.printStackTrace();
            }
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "getTokens");
            ex.printStackTrace();
        }
        HttpUtility.disconnect(httpConn);
        return returnvalue;
    }

    // This just updates the tokens by trying to use what exists and reply if that was successful or not.  If so then it updates them and returns true.
    //  False is returned in all other situations.  Likely this causes a full login sequence.
    synchronized public static boolean updateTokens() {
        boolean returnvalue = false;
        try {
            if ( getTokens("refresh_token", RefreshToken, "", appKey, redirectURI) ) {
                //  Save the refresh token to a file for use next time you run the program.
                LocalDate tempDate = LocalDate.now();
                LocalDate adjustedDate = tempDate.plusDays(85);
                Date date = java.sql.Date.valueOf(adjustedDate);
                tokenExpirationDate = new SimpleDateFormat("yyyy-MM-dd").format(date);  
                SaveToken();
                lastTokenUpdate = LocalTime.now();
                returnvalue = true;
            }
        } catch (Exception ex)         {
           Utility.dumpExceptionInfo(ex, "updateTokens");
           ex.printStackTrace();
        }
        return returnvalue;
    }
    
    synchronized public static JSONArray getAccounts(boolean positions) {
    
        // Need to be able to enter stocks to use and the parameters for the trade:
        JSONArray returnValue = null;
        Map<String, String> params = new HashMap<>();
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + AccessToken;            
        String requestURL = base_Trader_URL + "/accounts";
        if (positions) {
            requestURL = requestURL + "?fields=positions";
        }
        try {
            waitForRateLimit("Accounts");
            httpConn = HttpUtility.sendGetRequest(requestURL, auth);
            // Can read results as a string or JSONArray.
            returnValue = HttpUtility.readArrayResponse(httpConn);
            HttpUtility.disconnect(httpConn);
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "getAccounts");
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
        // https://api.schwabapi.com/marketdata/v1/pricehistory?symbol=AAPL&periodType=year&period=1&frequencyType=daily&frequency=1&needExtendedHoursData=false&needPreviousClose=true
        //   "month", "1", "daily", "1", EndDate, StartDate, false 
        //  "https://api.tdameritrade.com/v1/marketdata/MSFT/pricehistory?apikey=tlang&periodType=month&period=1&frequencyType=daily&frequency=1&endDate=1560527467000&startDate=1560441067000&needExtendedHoursData=false"
        String requestURL = base_MarketData_URL + "/pricehistory?symbol=" + Symbol;
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
            httpConn = HttpUtility.sendGetRequest(requestURL, auth);
            resp = new JSONObject(HttpUtility.readObjectResponse(httpConn).toString());
            HttpUtility.disconnect(httpConn);
            if (resp.length() == 0) {
                returnValue = null;
            } else {
                returnValue = resp;
            }
        } catch (Exception ex) {
          Utility. dumpExceptionInfo(ex, "getPriceHistory");
           ex.printStackTrace();
        }
        return returnValue;
    }

/*
Schema
{
  "AAPL": {
    "assetMainType": "EQUITY",    "symbol": "AAPL",    "quoteType": "NBBO",    "realtime": true,
    "ssid": 1973757747,    
    "reference": {  "cusip": "037833100",   "description": "Apple Inc",  "exchange": "Q",   "exchangeName": "NASDAQ"  },
    "quote": {
      "52WeekHigh": 169,      "52WeekLow": 1.1,     "askMICId": "MEMX",     "askPrice": 168.41,
      "askSize": 400,      "askTime": 1644854683672,      "bidMICId": "IEGX",      "bidPrice": 168.4,
      "bidSize": 400,      "bidTime": 1644854683633,      "closePrice": 177.57,      "highPrice": 169,
      "lastMICId": "XADF",      "lastPrice": 168.405,      "lastSize": 200,      "lowPrice": 167.09,      "mark": 168.405,
      "markChange": -9.164999999999992,      "markPercentChange": -5.161344821760428,      "netChange": -9.165,
      "netPercentChange": -5.161344821760428,
      "openPrice": 167.37,
      "quoteTime": 1644854683672,      "securityStatus": "Normal",      "totalVolume": 22361159,      "tradeTime": 1644854683408,      "volatility": 0.0347
    },
  }
}
 */
    // Tested 5/26/24
    synchronized public static double getOpenPrice(String Symbol) {
        double returnValue = 0.0;
        JSONObject resp = null;
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + getAccessToken();
        //  curl -X 'GET' \
        //  'https://api.schwabapi.com/marketdata/v1/quotes?symbols=AAPL&fields=quote&indicative=false' \
        //      -H 'accept: application/json'
        //  https://api.schwabapi.com/marketdata/v1/quotes?symbols=AAPL&fields=quote&indicative=false
        String requestURL = base_MarketData_URL + "/quotes?symbols=" + Symbol + "&fields=quote&indicative=false";
        try {
            waitForRateLimit("Accounts");
            httpConn = HttpUtility.sendGetRequest(requestURL, auth);
            resp = new JSONObject(HttpUtility.readObjectResponse(httpConn).toString());
            HttpUtility.disconnect(httpConn);
            if (resp.length() == 0) {
                returnValue = 0.0;
            } else {
                JSONObject firstStep = new JSONObject(resp.getJSONObject(Symbol).toString());
                JSONObject secondStep = new JSONObject(firstStep.getJSONObject("quote").toString());
                returnValue = secondStep.getDouble("openPrice");
            }
        } catch (Exception ex) {
          Utility. dumpExceptionInfo(ex, "getOpenPrice");
           ex.printStackTrace();
        }
        return returnValue;
    }

    // Not used in FIT Orders
    // Strategy parameters usually = STRADDLE, but for OHLC calls we need SINGLE to get the OI for the options
    synchronized public static JSONObject getQuote(String stockSymbol, String range, String strategy, boolean useMark) {

        JSONObject resp = null;
        Map<String, String> params = new HashMap<>();
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + getAccessToken();
//        https://api.schwabapi.com/marketdata/v1/chains?symbol=AAPL&contractType=ALL&strikeCount=6&includeUnderlyingQuote=true&strategy=STRADDLE
        String requestURL = base_MarketData_URL + "/chains?" +
                "symbol=" + stockSymbol +
//                "&contractType=ALL" +
                "&includeUnderlyingQuote=true" +
                "&strategy=" + strategy; //+
//                "&range=ALL" +
//                "&fromDate=" + Utility.nextExpiryIs("yyyy-MM-dd"); // +
  //              "&optionType=S";   
        try {
            // AccessToken could have been updated.
            waitForRateLimit("Chains");
            httpConn = HttpUtility.sendGetRequest(requestURL, auth);
            resp = HttpUtility.readObjectResponse(httpConn);
            HttpUtility.disconnect(httpConn);           
            
            // Adjust the strikes now to the desired amount.
            if ( !range.equalsIgnoreCase("ALL") ) { 
                int StrikeCount = Integer.parseInt(range);
                int ATMStrikePosition = 0;
                double LastStrike = 0.0;
                JSONObject Underlying = new JSONObject( resp.getJSONObject("underlying").toString() );
                double LastStockPrice = 0.0;
                if ( useMark ) {
                    LastStockPrice = Underlying.getDouble("mark");   
                } else {
                    LastStockPrice = Underlying.getDouble("last");   
                }
                
                // Since we get ALL strikes for a symbol, reduce the number of strikes to the desired amount.
                // Assume the array has the strikes in ascending order from TDA  - THIS IA A BIG ASSUMPTION.
                // We have an array of strikes and we know the count.  Determine how many to remove at the front and back.
                // The ATM strikes may not be in the middle of the range so find this point then determine how many to remove from the front and back

                // Now loop thru removing the first item in the list over and over again until half the count is removed.
                // Now loop thru removig the items in the array after the desired count until they are all removed.
                // Should be left with the desired amount.
                JSONArray MonthlyList = new JSONArray( resp.getJSONArray("monthlyStrategyList").toString() );
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
                           ATMStrikePosition = m+1;
                        } else {
                           ATMStrikePosition = m;
                        }
                        break;
                    } else {
                        LastStrike = CurrentStrike;
                    }
                }

                // loop thru each strike until we find the Call or Put that is the symbol we are updating
                if ( StrikeCount < OptionsList.length() ) {
                    int HalfStrikes = Math.round( (StrikeCount+1)/2);
                    int RemoveToPosition = ATMStrikePosition - HalfStrikes;
                    for (int j = 0; j < RemoveToPosition; j++) {
                        // Remove
                        OptionsList.remove(0);
                    }                
                    int CurLen = OptionsList.length();
                    for (int k = StrikeCount; k < CurLen; k++){
                        OptionsList.remove(StrikeCount);
                    }
                    FridayExpiry.remove("optionStrategyList");
                    FridayExpiry.put("optionStrategyList", OptionsList);
                    int MLen = MonthlyList.length();
                    for (int l = 0; l < MLen; l++) {
                        MonthlyList.remove(0);
                    }
                    MonthlyList.put(FridayExpiry);
                    resp.remove("monthlyStrategyList");
                    resp.put("monthlyStrategyList",MonthlyList);
                }
            } // else do nothing as you have ALL already
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
        String requestURL = base_MarketData_URL + "/chains?" +
                "symbol=" + stockSymbol +
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
            httpConn = HttpUtility.sendGetRequest(requestURL, auth);
            resp = HttpUtility.readObjectResponse(httpConn);
            HttpUtility.disconnect(httpConn);
        } catch (Exception ex) {
           Utility.dumpExceptionInfo(ex, "getQuoteSpecifics");
           ex.printStackTrace();
        }
        return resp;
    }
    

   //========================================================================================================================
    
    // Cancel an open order  Tested 5/24/24
    synchronized public static void cancelOrder(String accountNumber, String orderId)
    {
        String response;
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + getAccessToken();
        String requestURL = base_Trader_URL + "/accounts/" + getHashedAccountNumber(accountNumber) + "/orders/" + orderId;
        //  https://api.schwabapi.com/trader/v1/accounts/27074798/orders/1000516602361
        try {
           waitForRateLimit("Order");
           httpConn = HttpUtility.TDAsendDeleteRequest(requestURL, auth);
           response = HttpUtility.readSingleLineRespone(httpConn);
           HttpUtility.disconnect(httpConn);
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
        String requestURL = base_MarketData_URL + "/chains?" +
                "symbol=" + stockSymbol +
                "&contractType=ALL" +
                "&includeQuotes=TRUE" +
                "&strategy=STRADDLE" +
                "&range=SNK" +
                "&fromDate=" + Utility.nextExpiryIs("yyyy-MM-dd") +
                "&optionType=S";   
        try {
            // AccessToken could have been updated.
            waitForRateLimit("Chains");
            httpConn = HttpUtility.sendGetRequest(requestURL, auth);
            resp = HttpUtility.readObjectResponse(httpConn);
            HttpUtility.disconnect(httpConn);
        } catch (Exception ex) {
           Utility.dumpExceptionInfo(ex, "getQuote");
           ex.printStackTrace();
        }
        return resp;
    }
 
    // Not used in FIT Orders
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
    
    synchronized public static JSONObject getSpecificOrder(String accountNumber, long buyOrderId) {
        JSONObject returnValue = null;

        // Need to be able to enter stocks to use and the parameters for the trade:
        JSONObject resp = null;
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + AccessToken;            
// https://api.schwabapi.com/trader/v1/accounts/12312321/orders/123
        String requestURL = base_Trader_URL + "/accounts/" + getHashedAccountNumber(accountNumber) + "/orders/" + Long.toString(buyOrderId) ;

        // This really should be a GET request.
        try {
            waitForRateLimit("Order");
            httpConn = HttpUtility.sendGetRequest(requestURL, auth);
            // Can read results as a string or JSONArray.
            returnValue = HttpUtility.readObjectResponse(httpConn);
            HttpUtility.disconnect(httpConn);
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "getSpecificOrder");
            ex.printStackTrace();
        }
        return returnValue;
    }

    // orderstatus can be one of (or comma separated) - FILLED, QUEUED, CANCELED, etc.  See the TDAAPI for a full list
    // Tested 5/20/24
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
                    if ( AnOrder.has("accountNumber") && AnOrder.has("status") ) {
                    String acct = Integer.toString( AnOrder.getInt( "accountNumber" ) );
                    if ( acct.equalsIgnoreCase( accountNumber ) ) {
                            // If orderStatus is blank then you want all Orders
                            if ( ( orderStatus.contains( AnOrder.getString("status") ) ) ||
                                 ( orderStatus.equalsIgnoreCase( "" ) ) ) {
//                            if ( ( AnOrder.getString("status").equalsIgnoreCase( orderStatus ) ) ||
//                                 ( orderStatus.equalsIgnoreCase( "" ) ) ) {
                                returnOrders.put( AnOrder );
                            }
                        }
                        } else {
                        LogData.LogThis(Level.INFO, "GetOrders(A,S): Skipping an order - missing status or accountNumber ==>" + AnOrder.toString());
                        }
                    }
                 }
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "Exception getOrders 1");
            ex.printStackTrace();
        }
        return returnOrders;
    }

    // Not used in FIT Orders
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
                    // If orderStatus is blank then you want all Orders
                    if ( AnOrder.has("status") ) {
                        if ( ( orderStatus.contains( AnOrder.getString("status") ) ) ||
//                        if ( ( AnOrder.getString("status").equalsIgnoreCase( orderStatus ) ) ||
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
    // Tested 5/20/24
    synchronized public static JSONArray reallyGetAllAcctsOrders() {
      
        // Need to be able to enter stocks to use and the parameters for the trade:
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        DateFormat df = new SimpleDateFormat(pattern);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        JSONArray resp = null;
        JSONArray returnValue = null;
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + AccessToken;   
        Date today = Calendar.getInstance().getTime();
        String toDate = df.format(today);
        String hours = toDate.substring(11, 13);
        int theHours = Integer.parseInt(hours) - 8;
        String hrsToSubtract = Integer.toString(theHours);
        if (theHours < 10) {
            hrsToSubtract = "0" + hrsToSubtract;
        }
        String fromDate = toDate.substring(0, 11) + hrsToSubtract + toDate.substring(13);
        toDate = toDate.substring(0, 23) + "Z"; 
        fromDate = fromDate.substring(0, 23) + "Z";    
        //https://api.schwabapi.com/trader/v1/orders?fromEnteredTime=2024-05-13%27T%2710%3A10%3A20%3A000Z&toEnteredTime=2024-05-15%27T%2710%3A10%3A20%3A000Z
        String requestURL = base_Trader_URL + "/orders?maxResults=1000&fromEnteredTime=" + fromDate + "&toEnteredTime=" + toDate;
//        String requestURL = base_Trader_URL + "/accounts/" + getHashedAccountNumber("27074798") + "/orders?maxResults=1000&fromEnteredTime=" + fromDate + "&toEnteredTime=" + toDate;
        // This really should be a GET request.
        try {
            waitForRateLimit("Order");
            httpConn = HttpUtility.sendGetRequest(requestURL, auth);
            // Can read results as a string or JSONArray.
            resp = HttpUtility.readArrayResponse(httpConn);
            HttpUtility.disconnect(httpConn);
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
                             ( Status.equalsIgnoreCase( "WORKING") ) || 
                             ( Status.equalsIgnoreCase( "PENDING_ACTIVATION" ) ) ) {
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
    
    // Tested 5/20/24
 synchronized public static void placeBuyStraddleOrder(String primayLegSymbol, String secondaryLegSymbol, String price, String contracts, String accountNumber) {

        JSONObject Leg1 = new JSONObject();
        JSONObject Inst1 = new JSONObject();
        Leg1.put("instruction", "BUY_TO_OPEN");
        Leg1.put("quantity", contracts);        
        Inst1.put("symbol", primayLegSymbol);
        Inst1.put("assetType","OPTION");
        Leg1.put("instrument",Inst1);

        JSONObject Leg2 = new JSONObject();
        JSONObject Inst2 = new JSONObject();
        Leg2.put("instruction", "BUY_TO_OPEN");
        Leg2.put("quantity", contracts);        
        Inst2.put("symbol",secondaryLegSymbol);
        Inst2.put("assetType","OPTION");
        Leg2.put("instrument",Inst2);
        
        JSONArray Legs = new JSONArray();
        Legs.put(Leg1);
        Legs.put(Leg2);
        
        JSONObject Order = new JSONObject();  
        Order = Order.put("orderType", "NET_DEBIT");
        Order = Order.put("session", "NORMAL");
        Order = Order.put("price", price);
        Order = Order.put("duration", "DAY");
        Order = Order.put("orderStrategyType", "SINGLE");
        Order = Order.put("orderLegCollection", Legs);
        Order = Order.put("complexOrderStrategyType", "STRADDLE"); // May need this for the order to work now.
        HttpURLConnection httpConn = null; 
        String auth = "Bearer " + AccessToken;
        String requestURL = base_Trader_URL + "/accounts/"+getHashedAccountNumber(accountNumber)+"/orders";
        try {
            // waitForRateLimit("Order");  Not needed since Orders are not Rate Limited
                httpConn = HttpUtility.sendPostRequest(requestURL, Order, auth);
            String response = HttpUtility.readSingleLineRespone(httpConn);  // What to do with response???  This returns null when it works.  
                HttpUtility.disconnect(httpConn);
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "placeBuyStraddleOrder");
            ex.printStackTrace();
        }
 }     

 // Tested 5/21/24
 synchronized public static void placeSellStraddleOrder(String primayLegSymbol, String secondaryLegSymbol, double price, int contracts, String accountNumber) {
        JSONObject Leg1 = new JSONObject();
        JSONObject Inst1 = new JSONObject();
        Leg1.put("instruction", "SELL_TO_CLOSE");
        Leg1.put("quantity", contracts);          
        Inst1.put("symbol", primayLegSymbol);
        Leg1.put("orderLegType", "OPTION");        
        Inst1.put("assetType","OPTION");
        Leg1.put("instrument",Inst1);

        JSONObject Leg2 = new JSONObject();
        JSONObject Inst2 = new JSONObject();
        Leg2.put("instruction", "SELL_TO_CLOSE");
        Leg2.put("quantity", contracts);        
        Inst2.put("symbol",secondaryLegSymbol);
        Inst2.put("assetType","OPTION");
        Leg2.put("instrument",Inst2);
        
        JSONArray Legs = new JSONArray();
        Legs.put(Leg1);
        Legs.put(Leg2);
        
        JSONObject Order = new JSONObject();  
        Order = Order.put("orderType", "NET_CREDIT");
        Order = Order.put("session", "NORMAL");
        Order = Order.put("price", price);
        Order = Order.put("duration", "GOOD_TILL_CANCEL");
        Order = Order.put("orderStrategyType", "SINGLE");
        Order = Order.put("orderLegCollection", Legs);
        Order = Order.put("complexOrderStrategyType", "STRADDLE");
   
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + AccessToken;
        String requestURL = base_Trader_URL + "/accounts/"+getHashedAccountNumber(accountNumber)+"/orders";
        try {
            httpConn = HttpUtility.sendPostRequest(requestURL, Order, auth);
            String[] response = HttpUtility.readMultipleLinesRespone(false, httpConn);  // TBD Object or String possible
            HttpUtility.disconnect(httpConn);       
            //  TBD - What to do with response
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "placeSellStraddleOrder");
            ex.printStackTrace();
        }
 }

    // Tested 5/26/24
    synchronized public static void replaceBuylStraddleOrder(JSONObject Order, String accountNumber, long orderID) {

        // Schwab won't allow us to change the strike when doing a replace order.  So to make this seamless I will
        //     cancel the existing order and submit a new order for all replace buy orders.
        cancelOrder(accountNumber, Long.toString(orderID) );                            
        HttpURLConnection httpConn = null; 
        String auth = "Bearer " + AccessToken;
        String requestURL = base_Trader_URL + "/accounts/"+getHashedAccountNumber(accountNumber)+"/orders";
        try {
            // waitForRateLimit("Order");  Not needed since Orders are not Rate Limited
            httpConn = HttpUtility.sendPostRequest(requestURL, Order, auth);
            String response = HttpUtility.readSingleLineRespone(httpConn);  // What to do with response???  This returns null when it works.  
            HttpUtility.disconnect(httpConn);
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "placeBuyStraddleOrder");
            ex.printStackTrace();
        }

        /*     HttpURLConnection httpConn = null;
                String auth = "Bearer " + AccessToken;
                String requestURL = base_Trader_URL + "/accounts/"+getHashedAccountNumber(accountNumber)+"/orders/" + orderID;

                try {
                    httpConn = TT_HttpUtility.sendPutRequest(requestURL, Order, auth);
                    String[] response = TT_HttpUtility.readMultipleLinesRespone(true, httpConn);  // TBD Single or Object possible
                    for (String line : response) {
                    }
                    TT_HttpUtility.disconnect(httpConn);                
                } catch (Exception ex) {
                    Utility.dumpExceptionInfo(ex, "replaceBuylStraddleOrder");
                    ex.printStackTrace();
                }
        */
    }
   
 synchronized public static void replaceSellStraddleOrder(JSONObject Order, String accountNumber, long orderID) {

        HttpURLConnection httpConn = null;
        String auth = "Bearer " + AccessToken;
        String requestURL = base_Trader_URL + "/accounts/"+getHashedAccountNumber(accountNumber)+"/orders/" + orderID;
        
        try {
            httpConn = HttpUtility.sendPutRequest(requestURL, Order, auth);
            String[] response = HttpUtility.readMultipleLinesRespone(true, httpConn);  // TBD String or Object possible
            for (String line : response) {
            }
            HttpUtility.disconnect(httpConn);                
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "replaceSelllStraddleOrder");
            ex.printStackTrace();
        }
    }
 
     // Check the order results here and place a sell order if it was filled.
    //      Note:  accountNumber will be either 1 or 2.
    //             row is the row in the table where the orders are placed (this is so we can get data from it in this routine) 
    //  Returns an updated orderStatus if a Sell was initiated.
    // Tested 5/20/24
    synchronized public static JSONObject getAccountInfo(String accountNumber, JSONArray allAccts) {
        JSONObject returnValue = null;
        JSONObject securities = null;
        JSONObject tempValue = null;
        // Loop thru JSONArray
        for (int i = 0; i < allAccts.length(); i++) {
            // Get the current JSONObject
            tempValue = new JSONObject(allAccts.getJSONObject(i).toString());
            securities = new JSONObject(tempValue.getJSONObject("securitiesAccount").toString());
            // Now get the Securities JSONObject
            String temp = securities.getString("accountNumber");
            if (accountNumber.equalsIgnoreCase(securities.getString("accountNumber"))) {
                returnValue = tempValue;
                break;
            }  
        }
        return returnValue;
    }

    // Check the order results here and close down any remaining Buy orders that didn't fill.
    // Tested 5/24/24
    synchronized public static void dealWithUnfilledBuyOrders(String accountNumber) {   
        // This can be further broken down and reused, but rather spend time on new stuff.  
        String orderType = "";
        String tdaStatus = "";
        String complexOrder = "";

//        JSONArray allAccts = getAccounts(true);
//        JSONObject response = getAccountInfo(accountNumber, allAccts);
//        JSONObject acctInfo = new JSONObject(response.getJSONObject("securitiesAccount").toString());
        JSONArray currentOrders = getOrders(accountNumber, "QUEUED WORKING PENDING_ACTIVATION");
           
        // We now have an JSON Object representing the account to parse through and find what we need.
        // Let's get the positions
        if(currentOrders.length() > 0){               
               // Now loop thru the order trying to find the symbol in question
            for (int aaa = 0; aaa < currentOrders.length(); aaa++) {
                    // Get the order information  If there is a Net_Debit then it is a buy order.
                JSONObject anOrder = new JSONObject(currentOrders.getJSONObject(aaa).toString());
                if ( anOrder.has("status") && anOrder.has("orderType") && anOrder.has("complexOrderStrategyType") ) {
                        orderType = anOrder.getString("orderType");
                        tdaStatus = anOrder.getString("status");
                        complexOrder = anOrder.getString("complexOrderStrategyType");

                    // 1/7/24 This will be used to be sure and only kill orders submitted the same day.  Thus straddlle orders
                    //      for OD2 plays will not get killed off.  
                    //      Not sure this is the only place, may have to go with "Place missing sell orders too.'
                    // TBD 1/7/24 - Use the "enteredTime" value to only cancel orders submitted today.  Format looks like "2024-01-07T17:09:56+0000"
                    String dateStamp = new SimpleDateFormat("YYYY-MM-DD").format(new Date());  // On 2/1/24 this is returning 2024-02-32 so the compare is failing.
                    String orderDateOnly = anOrder.getString("enteredTime").substring(0, 10);
                        if (orderType.equalsIgnoreCase("NET_DEBIT") && 
                            (tdaStatus.equalsIgnoreCase("QUEUED") || tdaStatus.equalsIgnoreCase("WORKING") || (tdaStatus.equalsIgnoreCase("PENDING_ACTIVATION")))&&
//                                (dateStamp.equalsIgnoreCase(orderDateOnly))&&
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
//                                    executor.submit(new PlaceStraddleOrderSell(callSymbol, putSymbol, finalPrice, anOrder.getInt("filledQuantity"), anOrder.getString("accountNumber"), AccessToken, jTextAreaResults));
                                }
                        }
                    } else {
                        LogData.LogThis(Level.INFO, "dealWithUnfilledBuyOrders: Skipping an order - missing status  ==>" + anOrder.toString());
                    }
                 }  // For loop
        }  // If has orderStrategies

    } //  dealWithUnfilledBuyOrders

    // Be sure we don't do something that can be interpretted as Manipulation
    //     sellCheck is True when placing a Sell Order, false otherwise
    //      the Stirng returned would when we found manipulation possible and cancelled at least one order.
    synchronized public static String sellManipulationCheck(String L1SymbolOrder, String L2SymbolOrder, double priceCheck ) {
        String returnValue = "";
        try {
            String[][] AllAccounts = GUIData.getAccounts();
            for (int ooo = 0; ooo < AllAccounts.length; ooo++) {
                if ( AllAccounts[ooo][1].equalsIgnoreCase( GUIData.charlesSchwab ) ) {
                    JSONArray OpenOrders = getOrders( AllAccounts[ooo][0], "QUEUED WORKING PENDING_ACTIVATION" );
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
                if ( AllAccounts[ooo][1].equalsIgnoreCase( GUIData.charlesSchwab ) ) {
                    JSONArray OpenOrders = getOrders( AllAccounts[ooo][0], "QUEUED WORKING PENDING_ACTIVATION" );
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

    /* 
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
    // Tested 5/24/24
    synchronized public static void GUICancelOrderRequest(String AccountNumber, String Symbol, String Strike) {
        try {   // TBD - This assumes that any open orders are Option orders.  It it is a stock order then this crashes.  That would be an issue when running the program
                //      and trying to sell something if a stock order is present.
            String L1SymbolOrder = "";
            String L2SymbolOrder = "";
            JSONArray OpenOrders = getOrders( AccountNumber, "QUEUED WORKING PENDING_ACTIVATION" );
            if ( OpenOrders != null ) {
                for (int abc = 0; abc < OpenOrders.length(); abc++) {
                    JSONObject anOpenOrder = new JSONObject(OpenOrders.getJSONObject(abc).toString());
                    // Check the quantity first if good then go deeper  (Could check other parameters
                    String openOrderType = anOpenOrder.getString("orderType");
                    double openPrice = anOpenOrder.getDouble("price");
                    JSONArray Legs = new JSONArray( anOpenOrder.getJSONArray( "orderLegCollection").toString() );
                    if ( Legs.toString().contains("OPTION") ) {
                    JSONObject Leg = new JSONObject( Legs.get(0).toString() );
                    JSONObject openInstrument = new JSONObject( Leg.getJSONObject("instrument").toString() );
                        String TempSymbol = Utility.convertSymbolFromTastyToTDA(Utility.convertSymbolFromSchwabToTasty(openInstrument.getString("symbol")));
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
            }
        } catch ( Exception ex ) {
                    Utility.dumpExceptionInfo(ex, "GUICancelOrderRequest - Cancel Order Request from the GUI");
                    ex.printStackTrace();
        }
    }

 // returns 0 if not a Straddle order, otherwise returns the Quatnity for the Straddle
    // Tested 5/24/24
 private static int isStraddlePosition( String PorC, JSONArray AllPositions, String UnderlyingSymbol, int FirstQuantity, String Strike ) {

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
         if ( instrument.getString("assetType").equalsIgnoreCase("OPTION") &&
              GetStrike( instrument.getString("symbol") ).equalsIgnoreCase(Strike) ) {
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
 
 private static boolean SymbolInPlayToday( String Symbol, javax.swing.JTable Table ) {
     boolean ReturnValue = false;
     for ( int SSS = 0; SSS < Table.getRowCount(); SSS++ ) {
         if ( Table.getValueAt(SSS, GUIData.symbolColumn) == null ) {
             break;  // We have reached the end of valid symbols in the table
           } else {
            if ( Table.getValueAt(SSS, GUIData.symbolColumn).toString().equalsIgnoreCase( Symbol ) ) {
                ReturnValue = true;
                break;
           }
       }
     }
     return ReturnValue;
 }
 
 private static String GetStrike(String FullSymbol)  {
    String Strike = "";
    Strike = FullSymbol.substring(13);
    Strike = Strike.substring(0, 5) + "." + Strike.substring(5);
       return Strike;
 }
 
    // Check the order results here and close down any remaining Buy orders that didn't fill (this is for Straddle Orders only).
 public static void PlaceMissingSellOrders(String accountNumber, javax.swing.JTable jTableSymbolData, JSONArray ordersPlacedToday)
    {   // This can be further broken down and reused, but rather spend time on new stuff.  
        String orderType = "";
        String tdaStatus = "";
        String complexOrder = "";

        JSONArray allAccts = getAccounts(true);
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
                    // TBD - Only continue from here if the Underlying Symbol is in the stocks for the day.
                    // Note here:  The evening testing shows that there may be one position related to money market fund activity, but there is no UnderlyingSymbol.  So this throws 
                    //              an exception.  Gonna put in a check here to avoid that without having to impact all this code.
                    String UnderlyingSymbol = "NOSYMBOL";
                    if ( instrument.has( "underlyingSymbol") ) {
                        UnderlyingSymbol = instrument.getString("underlyingSymbol");
                    }
//                    if ( ( instrument.getString("assetType").equalsIgnoreCase("OPTION") ) && ( ordersPlacedToday.toString().contains(UnderlyingSymbol) ) ) { // SymbolInPlayToday( UnderlyingSymbol, jTableSymbolData) ) {
                    if ( ( instrument.getString("assetType").equalsIgnoreCase("OPTION") ) && ( true ) ) { // SymbolInPlayToday( UnderlyingSymbol, jTableSymbolData) ) {
                        String OptionSymbol = instrument.getString("symbol");
                        boolean isCall = instrument.getString("putCall").equalsIgnoreCase("Call");
                        String Strike = GetStrike( instrument.getString("symbol") );
                        int StraddleQuantity = isStraddlePosition( instrument.getString("putCall"), AllPositions, UnderlyingSymbol, Quantity, Strike );
                        if ( StraddleQuantity > 0 ) {  // We have found a straddle position  (not sure if Quantity and StraddleQuantity are different.  Should be the same.  TBD)                            
                            // Loop thru the orders to see if there is one that matches (all but quantity)
                                // If quantity matches then done
                                // else adjust the quantity to the correct amount
                                // if there is no order then place one
                            boolean doneCheckingPosition = false;   // TBD - After checking the Put or Call for the other half, how to avoid looking again for the order?
                                    // This gets us the array of orders that are QUEUED up to sell and is updated for every new position (this is to handle the PUT and CALL issue for duplicates).
                                    JSONArray currentOrders = getOrders(accountNumber, "QUEUED WORKING PENDING_ACTIVATION");
                                    if ( currentOrders == null ) {
                                       // We need to place a sell order. 
                                    } else {
                                        // Now loop thru the order trying to find the symbol in question
                                        for (int OrderStratIndex = 0; OrderStratIndex < currentOrders.length(); OrderStratIndex++) {
                                            // Get the order information  If there is a Net_Debit then it is a buy order.
                                            JSONObject anOrder = new JSONObject(currentOrders.getJSONObject(OrderStratIndex).toString());
                                            if ( anOrder.has("status") && anOrder.has("orderType") && anOrder.has("complexOrderStrategyType") ) {
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
                            if ( doneCheckingPosition == false ) {
                                // Must place a Sell Order  
                                String L1SymbolOrder = OptionSymbol;
                                String PorC = L1SymbolOrder.substring(12,13);
                                String L2SymbolOrder = OptionSymbol;
                                //  Option Symbols look like ==  KR_070618P28.5
                                if (L1SymbolOrder.substring(12,13).equalsIgnoreCase("C")) {
                                    L2SymbolOrder = OptionSymbol.substring(0,12) + "P" + OptionSymbol.substring(13);
                                } else {
                                    L2SymbolOrder = OptionSymbol.substring(0,12) + "c" + OptionSymbol.substring(13);
                                }
                                String formattedPrice = getFormattedPrice( UnderlyingSymbol, jTableSymbolData );
                                placeSellStraddleOrder( L1SymbolOrder, L2SymbolOrder, Double.parseDouble( formattedPrice ),  StraddleQuantity, accountNumber); 
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
            Leg1.put("instrument",Inst1);

            JSONObject Leg2 = new JSONObject();
            JSONObject Inst2 = new JSONObject();
            Leg2.put("instruction", "SELL_TO_CLOSE");
            Leg2.put("quantity", newQuantity);        
            Inst2.put("assetType","OPTION");
            Inst2.put("symbol",secondaryLegSymbol);
            Leg2.put("instrument",Inst2);

            JSONArray Legs = new JSONArray();
            Legs.put(Leg1);
            Legs.put(Leg2);

            JSONObject Order = new JSONObject();  
            Order = Order.put("orderType", "NET_CREDIT");
            Order = Order.put("session", "NORMAL");
            Order = Order.put("price", Double.parseDouble( formattedPrice ) );
            Order = Order.put("duration", "DAY");
            Order = Order.put("orderStrategyType", "SINGLE");
            Order = Order.put("orderLegCollection", Legs);
            Order = Order.put("complexOrderStrategyType", "STRADDLE");
            replaceSellStraddleOrder(Order, accountNumber, orderID);
            
            LogData.LogThis( Level.INFO, "   SELL ORDER QUANTITY UPDATED = " + primaryLegSymbol + " new price= " + formattedPrice + "  quantity= " + Integer.toString( newQuantity ) + "  OrderID= " + orderID);
        } catch (Exception ex) {
            Utility. dumpExceptionInfo(ex, " Exception: replaceSellStraddleOrder.  Keep on processing.");
        }
    }

 //  Tested 05/24/24
 synchronized public static void placeSellStraddleOrder(String primayLegSymbol, String secondaryLegSymbol, String price, String contracts, String accountNumber) {

        JSONObject Leg1 = new JSONObject();
        JSONObject Inst1 = new JSONObject();
        Leg1.put("instruction", "SELL_TO_CLOSE");
        Leg1.put("quantity", contracts);          
//        Leg1.put("orderLegType", "OPTION");        
        Inst1.put("symbol", primayLegSymbol);
        Inst1.put("assetType","OPTION");
//        Inst1.put("putCall","CALL");
        Leg1.put("instrument",Inst1);

        JSONObject Leg2 = new JSONObject();
        JSONObject Inst2 = new JSONObject();
        Leg2.put("instruction", "SELL_TO_CLOSE");
        Leg2.put("quantity", contracts);        
//        Leg2.put("orderLegType", "OPTION");        
        Inst2.put("symbol",secondaryLegSymbol);
        Inst2.put("assetType","OPTION");
//        Inst2.put("putCall","PUT");
        Leg2.put("instrument",Inst2);
        
        JSONArray Legs = new JSONArray();
        Legs.put(Leg1);
        Legs.put(Leg2);
        
        JSONObject Order = new JSONObject();  
        Order = Order.put("orderType", "NET_CREDIT");
        Order = Order.put("session", "NORMAL");
        Order = Order.put("price", price);
        Order = Order.put("duration", "GOOD_TILL_CANCEL");
        Order = Order.put("orderStrategyType", "SINGLE");
        Order = Order.put("orderLegCollection", Legs);
        Order = Order.put("complexOrderStrategyType", "STRADDLE");
//        Order = Order.put("quantity", contracts);        
//        Order = Order.put("requestedDestination", "AUTO");
   
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + AccessToken;
        String requestURL = base_Trader_URL + "/accounts/"+getHashedAccountNumber(accountNumber)+"/orders";
        
        try {
            httpConn = HttpUtility.sendPostRequest(requestURL, Order, auth);
            String[] response = HttpUtility.readMultipleLinesRespone(false, httpConn);  
            HttpUtility.disconnect(httpConn);       
            //  TBD - What to do with response
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "placeSellStraddleOrder");
            ex.printStackTrace();
        }
 }
     
    
    synchronized public static JSONObject getUserPreference() {
    
        // Need to be able to enter stocks to use and the parameters for the trade:
        JSONObject returnValue = null;
        HttpURLConnection httpConn = null;
        String auth = "Bearer " + AccessToken;            
        String requestURL = base_Trader_URL +  "/userPreference";   //  ?fields=streamerConnectionInfo,streamerSubscriptionKeys";
    
        try {
            waitForRateLimit("Principals");
            httpConn = HttpUtility.sendGetRequest(requestURL, auth);
            returnValue = HttpUtility.readObjectResponse(httpConn);
            HttpUtility.disconnect(httpConn);           
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex, "getPrincipals");
            ex.printStackTrace();
        }
        return returnValue;
    }
      
}