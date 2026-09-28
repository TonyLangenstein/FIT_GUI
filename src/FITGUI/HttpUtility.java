package FITGUI;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.json.*;
import org.apache.commons.codec.binary.Base64;

import java.net.http.*;
import java.time.Duration;

/**
 * This class encapsulates methods for requesting a server via HTTP GET/POST and
 * provides methods for parsing response from the server.
 *
 * @author www.codejava.net
 *
 */
public class HttpUtility {

    public static final int requestSuccessful = 200;
    
    // Reuse one static client globally
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .connectTimeout(Duration.ofSeconds(5))
        .build();    
    
    private static Log4J2AsyncLogger LogData =new Log4J2AsyncLogger();

    synchronized private static URL toURL( String requestURL ) throws URISyntaxException {
        URL url;
        try {
            URI u = new URI(requestURL);
            url = u.toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new URISyntaxException("request URL",requestURL + " = Something unexpected!");
        }
        return url;
    }
 
    /**
     * Makes an HTTP request using GET method to the specified URL.
     *
     * @param requestURL
     *            the URL of the remote server
     * @return An HttpURLConnection object
     * @throws IOException
     *             thrown if any I/O error occurred
     */
    synchronized public static HttpURLConnection sendGetRequest(String requestURL, String Auth)
            throws IOException {
        HttpURLConnection httpConn;
        try {
            URL url = toURL(requestURL);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("accept", "application/JSON");
            httpConn.setRequestProperty("Authorization", Auth);
        } catch (URISyntaxException e) {
            throw new IOException(e.getReason());
        }
        return httpConn;
    }
    
    //   These are things to look at in the return from all the HttpRequests
    //            int responseCode = response.statusCode();
    //            String jsonResponse = response.body();    
    synchronized public static HttpResponse sendGetHttpRequest(String requestURL, String Auth) 
            throws IOException {        
        try {
            // Build the request
            HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(requestURL))
            .header("Authorization", Auth)
            .header("Accept", "application/json")
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();

            // Send the request and return the result
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return response;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    
    /**
     * Makes an HTTP request using GET method to the specified URL.
     *
     * @param requestURL
     *            the URL of the remote server
     * @return An HttpURLConnection object
     * @throws IOException
     *             thrown if any I/O error occurred
     */
    synchronized public static HttpURLConnection sendGetRequest(String requestURL, String Auth, boolean JSON)
            throws IOException {
        HttpURLConnection httpConn;
        try {
            URL url = toURL(requestURL);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);

            if (JSON) {
                httpConn.addRequestProperty("Content-type", "application/json");
            }
            httpConn.setRequestProperty("Authorization", Auth);

            httpConn.setDoInput(true); // true if we want to read server's response
            httpConn.setDoOutput(false); // false indicates this is a GET request
        } catch (URISyntaxException e) {
            throw new IOException(e.getReason());
        }
        return httpConn;
    }

 
    /**
     * Makes an HTTP request using GET method to the specified URL.
     *
     * @param requestURL
     *            the URL of the remote server
     * @return An HttpURLConnection object
     * @throws IOException
     *             thrown if any I/O error occurred
     */
    synchronized public static HttpURLConnection TDAsendDeleteRequest(String requestURL, String Auth)
            throws IOException {
        HttpURLConnection httpConn;
        try {
            URL url = toURL(requestURL);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("DELETE");  
            httpConn.setRequestProperty("Authorization", Auth); 
            httpConn.setRequestProperty("accept", "*/*");
            httpConn.setDoInput(true); // true if we want to read server's response
        } catch (URISyntaxException e) {
            throw new IOException(e.getReason());
        }
        return httpConn;
    }


    /**
     * Makes an HTTP request using DELETE method to the specified URL.
     *
     * @param requestURL
     *            the URL of the remote server
     * @param params
     *            A map containing POST data in form of key-value pairs
     * @return An HttpURLConnection object
     * @throws IOException
     *             thrown if any I/O error occurred
     */
    synchronized public static int sendDeleteRequest(String requestURL,
            String sessionToken) throws IOException {
        HttpURLConnection httpConn;
        try {
            URL url = toURL(requestURL);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("DELETE"); 
            httpConn.setRequestProperty("Authorization", sessionToken);
            httpConn.setDoOutput(true);
        } catch (URISyntaxException e) {
            throw new IOException(e.getReason());
        }
        return httpConn.getResponseCode();
    }

    
    /**
     * Makes an HTTP request using POST method to the specified URL.
     *
     * @param requestURL
     *            the URL of the remote server
     * @param params
     *            A map containing POST data in form of key-value pairs
     *            Ensures no authorization is sent along as a parameter
     * @return An HttpURLConnection object
     * @throws IOException
     *             thrown if any I/O error occurred
     */
    synchronized public static HttpURLConnection sendPostRequestNoAuth(String requestURL,
            Map<String, String> params) throws IOException {
        HttpURLConnection httpConn;
        try {
            URL url = toURL(requestURL);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setRequestMethod("POST");  
            httpConn.setDoInput(true); // true indicates the server returns response
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            StringBuilder requestParams = new StringBuilder();

            if (params != null && params.size() > 0) {

                httpConn.setDoOutput(true); // true indicates POST request

                // creates the params string, encode them using URLEncoder
                Iterator<String> paramIterator = params.keySet().iterator();
                while (paramIterator.hasNext()) {
                    String key = paramIterator.next();
                    String value = params.get(key);
                    requestParams.append(URLEncoder.encode(key, "UTF-8"));
                    requestParams.append("=").append(
                            URLEncoder.encode(value, "UTF-8"));
                    requestParams.append("&");
                }

                // sends POST data
                OutputStreamWriter writer = new OutputStreamWriter(
                        httpConn.getOutputStream());
                writer.write(requestParams.toString());
                writer.flush();
            }
        } catch (URISyntaxException e) {
            throw new IOException(e.getReason());
        } 
        return httpConn;
    }
    
    /**
     * Makes an HTTP request using POST method to the specified URL.
     *
     * @param requestURL
     *            the URL of the remote server
     * @param params
     *            A map containing POST data in form of key-value pairs
     * @return An HttpURLConnection object
     * @throws IOException
     *             thrown if any I/O error occurred
     */
    //  This one is used for Schwab login at this point.  If others need it then it may need tweaks.
    synchronized public static HttpURLConnection sendPostRequest(String requestURL,
            Map<String, String> params, String authInfo) throws IOException {
        HttpURLConnection httpConn;
        try {
            URL url = toURL(requestURL);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setRequestMethod("POST");
            httpConn.setDoInput(true); // true indicates the server returns response
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("Authorization", "Basic " + authInfo);
            StringBuilder requestParams = new StringBuilder();

            if (params != null && params.size() > 0) {

                httpConn.setDoOutput(true); // true indicates POST request

                // creates the params string, encode them using URLEncoder
                Iterator<String> paramIterator = params.keySet().iterator();
                while (paramIterator.hasNext()) {
                    String key = paramIterator.next();
                    String value = params.get(key);
                    requestParams.append(URLEncoder.encode(key, "UTF-8"));
                    requestParams.append("=").append(
                            URLEncoder.encode(value, "UTF-8"));
                    requestParams.append("&");
                }

                // sends POST data
                OutputStreamWriter writer = new OutputStreamWriter(
                        httpConn.getOutputStream());
                writer.write(requestParams.toString());
                writer.flush();
            }
        } catch (URISyntaxException e) {
            throw new IOException(e.getReason());
        }
        return httpConn;
    }
 
        /**
     * Makes an HTTP request using POST method to the specified URL.
     *
     * @param requestURL
     *            the URL of the remote server
     * @param params
     *            A map containing POST data in form of key-value pairs
     * @param Auth
     * @return An HttpURLConnection object
     * @throws IOException
     *             thrown if any I/O error occurred
     */
    synchronized public static HttpURLConnection sendPostRequest(String requestURL,
            String Auth) throws IOException {
        HttpURLConnection httpConn;
        try {
            URL url = toURL(requestURL);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setRequestProperty("Authorization", Auth);
            httpConn.setDoInput(true); // true indicates the server returns response
            httpConn.setDoOutput(true); // true indicates POST request 
        } catch (URISyntaxException e) {
            throw new IOException(e.getReason());
        }
        return httpConn;
    }

    /**
     * Makes an HTTP request using POST method to the specified URL.
     *
     * @param requestURL
     *            the URL of the remote server
     * @param params
     *            A map containing POST data in form of key-value pairs
     * @param Auth
     * @return An HttpURLConnection object
     * @throws IOException
     *             thrown if any I/O error occurred
     */
/*    synchronized public static HttpURLConnection sendPostRequest(String requestURL,
            Map<String, String> params, String Auth) throws IOException {
        URL url = new URL(requestURL);
        HttpURLConnection httpConn;
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setRequestProperty("Authorization", Auth);
        httpConn.setDoInput(true); // true indicates the server returns response

        StringBuilder requestParams = new StringBuilder();
 
        if (params != null && params.size() > 0) {
 
            httpConn.setDoOutput(true); // true indicates POST request
 
            // creates the params string, encode them using URLEncoder
            Iterator<String> paramIterator = params.keySet().iterator();
            while (paramIterator.hasNext()) {
                String key = paramIterator.next();
                String value = params.get(key);
                requestParams.append(URLEncoder.encode(key, "UTF-8"));
                requestParams.append("=").append(
                        URLEncoder.encode(value, "UTF-8"));
                requestParams.append("&");
            }
 
            // sends POST data
            OutputStreamWriter writer = new OutputStreamWriter(
                    httpConn.getOutputStream());
            writer.write(requestParams.toString());
            writer.flush();
        }
 
        return httpConn;
    }
*/
    /**
     * Makes an HTTP request using POST method to the specified URL.
     *
     * @param requestURL
     *            the URL of the remote server
     * @param params
     *            A map containing POST data in form of key-value pairs
     * @param Auth
     * @return An HttpURLConnection object
     * @throws IOException
     *             thrown if any I/O error occurred
     */
    synchronized public static HttpURLConnection sendPostRequest(String requestURL,
            JSONObject body, String Auth) throws IOException {
        HttpURLConnection httpConn = null;
        try {
            if (body.isEmpty()) {
                LogData.LogThis(Level.INFO, "No data");
            } else {
               URL url = toURL(requestURL);
               httpConn = (HttpURLConnection) url.openConnection();
               httpConn.setUseCaches(false);
               httpConn.setRequestMethod("POST");  
               httpConn.setRequestProperty("accept", "*/*");
               httpConn.setRequestProperty("Content-Type", "application/json");
               httpConn.setRequestProperty("Authorization", Auth);
               httpConn.setDoInput(true); // true indicates the server returns response
               httpConn.setDoOutput(true); // true indicates POST request
               String TS = body.toString();
               OutputStreamWriter writer = new OutputStreamWriter(
                       httpConn.getOutputStream());
               writer.write(TS);
               writer.flush();
            }
        } catch (URISyntaxException e) {
            throw new IOException(e.getReason());
        }
        return httpConn;
    }



    /**
     * Makes an HTTP request using POST method to the specified URL.
     *
     * @param requestURL
     *            the URL of the remote server
     * @param params
     *            A map containing POST data in form of key-value pairs
     * @param Auth
     * @return An HttpURLConnection object
     * @throws IOException
     *             thrown if any I/O error occurred
     */
    synchronized public static HttpURLConnection sendPutRequest(String requestURL,
            JSONObject params, String Auth) throws IOException {
        HttpURLConnection httpConn = null;
        try {
            if (params.isEmpty()) {
                 LogData.LogThis(Level.INFO, "No data");
            } else {
                        URL url = toURL(requestURL);
                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setUseCaches(false);
                httpConn.setRequestProperty("Authorization", Auth);
                httpConn.addRequestProperty("Content-type", "application/json");
                httpConn.setDoInput(true); // true indicates the server returns response
                httpConn.setDoOutput(true); // true indicates POST request
                httpConn.setRequestMethod("PUT");
                String TS = params.toString();
                OutputStreamWriter writer = new OutputStreamWriter(
                        httpConn.getOutputStream());
                writer.write(TS);
                writer.flush();
            } 
        } catch (URISyntaxException e) {
            throw new IOException(e.getReason());
        }
        return httpConn;
    }
    
    /**
     * Returns only one line from the server's response. This method should be
     * used if the server returns only a single line of String.
     *
     * @return a String of the server's response
     * @throws IOException
     *             thrown if any I/O error occurred
     */
    synchronized public static JSONArray readArrayResponse(HttpURLConnection httpConn) throws IOException {
        InputStream inputStream = null;
        if (httpConn != null) {
            inputStream = httpConn.getInputStream();
        } else {
            throw new IOException("Connection is not established.");
        }
        JSONTokener tokener = new JSONTokener(inputStream);
        JSONArray response = new JSONArray(tokener); 
        return response;
    }

    /**
     * Returns only one line from the server's response. This method should be
     * used if the server returns only a single line of String.
     *
     * @return a String of the server's response
     * @throws IOException
     *             thrown if any I/O error occurred
     */
    synchronized public static JSONObject readObjectResponse(HttpURLConnection httpConn) throws IOException {
        InputStream inputStream = null;
        if (httpConn != null) {
                inputStream = httpConn.getInputStream();
        } else {
            throw new IOException("Connection is not established.");
        }
        JSONTokener tokener = new JSONTokener(inputStream);
        JSONObject response = new JSONObject(tokener); 
        return response;
    }

    
    /**
     * Returns only one line from the server's response. This method should be
     * used if the server returns only a single line of String.
     *
     * @return a String of the server's response
     * @throws IOException
     *             thrown if any I/O error occurred
     */
    synchronized public static String readSingleLineRespone(HttpURLConnection httpConn) throws IOException {
        InputStream inputStream = null;
        if (httpConn != null) {
            inputStream = httpConn.getInputStream();
        } else {
            throw new IOException("Connection is not established.");
        }
        String response;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream))) {
            response = reader.readLine();
        }
 
        return response;
    }
 
    /**
     * Returns an array of lines from the server's response. This method should
     * be used if the server returns multiple lines of String.
     *
     * @return an array of Strings of the server's response
     * @throws IOException
     *             thrown if any I/O error occurred
     */
    synchronized public static String[] readMultipleLinesRespone(boolean debug, HttpURLConnection httpConn) throws IOException {
        InputStream inputStream = null;

        if (httpConn != null) {
            if (debug == true) {
                int statusCode = httpConn.getResponseCode();
                if (statusCode >= 200 && statusCode < 400) {
                   // Create an InputStream in order to extract the response object
                   inputStream = httpConn.getInputStream();
                }
                else {
                   inputStream = httpConn.getErrorStream();
                }        
            }        
            else {
               inputStream = httpConn.getInputStream();
            }
        } else {
            throw new IOException("Connection is not established.");
        }
 
        List<String> response;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream))) {
            response = new ArrayList<>();
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }
        }
 
        return (String[]) response.toArray(new String[0]);
    }
     
    /**
     * Closes the connection if opened
     */
    synchronized public static void disconnect(HttpURLConnection httpConn) {
        if (httpConn != null) {
            httpConn.disconnect();
        }
    }
}