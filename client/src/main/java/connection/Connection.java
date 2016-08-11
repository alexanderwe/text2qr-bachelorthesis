package connection;


import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Created by alexanderweiss
 * Base Class for representing http methods to webservices
 */
public class Connection {

    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(Connection.class);

    public Connection(){
        createAllCertf();
    }

    /**
     * Do a simple get request
     * @return String
     */
    public String do_get(URL url) throws IOException {

        HttpURLConnection con = null;
        //Check protocol
        if (isHTTPS(url)) {
            con = (HttpsURLConnection) url.openConnection();
        } else {
            con = (HttpURLConnection) url.openConnection();
        }
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        logger.info("Sending 'GET' request to URL : " + url);
        logger.info("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        logger.info("Response data: " + response.toString());
        return response.toString();
    }

    /**
     * Do a simple get request with authentication
     * @return String
     */
    public String do_get_with_auth(URL url, String authToken) throws IOException {

        HttpURLConnection con = null;
        //Check protocol
        if (isHTTPS(url)) {
            con = (HttpsURLConnection) url.openConnection();
        } else {
            con = (HttpURLConnection) url.openConnection();
        }

        // optional default is GET
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", authToken);

        int responseCode = con.getResponseCode();
        logger.info("Sending 'GET' request to URL : " + url);
        logger.info("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        logger.info("Response data: " + response.toString());
        return response.toString();
    }

    /**
     * Do a post request
     * @param url
     * @param post_string
     * @return String
     */
    public String do_post(URL url, String post_string) throws IOException {

        HttpURLConnection con = null;
        //Check protocol
        if (isHTTPS(url)) {
            con = (HttpsURLConnection) url.openConnection();
        } else {
            con = (HttpURLConnection) url.openConnection();
        }
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        // Indicate that we want to write to the HTTP request body
        con.setDoOutput(true);
        con.setRequestMethod("POST");

        // Writing the post data to the HTTP request body
        BufferedWriter httpRequestBodyWriter = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
        httpRequestBodyWriter.write(post_string);
        httpRequestBodyWriter.close();

        int responseCode = con.getResponseCode();
        logger.info("Sending 'POST' request to URL : " + url + "\n" + "Send data: " + post_string);
        logger.info("Response Code : " + responseCode);

        // Reading from the HTTP response body
        Scanner httpResponseScanner = new Scanner(con.getInputStream());
        String inputLine;
        StringBuilder response = new StringBuilder();
        try {
            while ((inputLine = httpResponseScanner.nextLine()) != null) {
                response.append(inputLine);
            }
        } catch (NoSuchElementException nsee) {
            logger.error("No response line found");
        }

        httpResponseScanner.close();
        return response.toString();
    }

    /**
     * Do a post request with authentication
     *
     * @param url
     * @param post_string
     * @return String
     */
    public String do_post_with_auth(URL url, String post_string, String authToken) throws IOException {

        HttpURLConnection con = null;
        //Check protocol
        if (isHTTPS(url)) {
            con = (HttpsURLConnection) url.openConnection();
        } else {
            con = (HttpURLConnection) url.openConnection();
        }
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        // Indicate that we want to write to the HTTP request body
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", authToken);


        // Writing the post data to the HTTP request body
        BufferedWriter httpRequestBodyWriter = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
        httpRequestBodyWriter.write(post_string);
        httpRequestBodyWriter.close();

        int responseCode = con.getResponseCode();
        logger.info("\nSending 'POST' request to URL : " + url + System.lineSeparator() + "Send data: " + post_string);
        logger.info("Response Code : " + responseCode + "\n");

        // Reading from the HTTP response body
        Scanner httpResponseScanner = new Scanner(con.getInputStream());
        String inputLine;
        StringBuilder response = new StringBuilder();
        try {
            while ((inputLine = httpResponseScanner.nextLine()) != null) {
                response.append(inputLine);
            }
        } catch (NoSuchElementException nsee) {
            logger.error("No response line found");
        }

        httpResponseScanner.close();
        return response.toString();
    }

    /**
     * Do a delete request
     * @param url
     * @return String
     * @throws Exception
     */
    public String do_delete(URL url) throws IOException {

        HttpURLConnection con = null;
        //Check protocol
        if (isHTTPS(url)) {
            con = (HttpsURLConnection) url.openConnection();
        } else {
            con = (HttpURLConnection) url.openConnection();
        }

        con.setRequestMethod("DELETE");

        int responseCode = con.getResponseCode();
        logger.info("Sending 'DELETE' request to URL : " + url);
        logger.info("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    /**
     * Do a delete request with authentication
     * @param url
     * @param authToken
     * @return String
     * @throws Exception
     */
    public String do_delete_with_auth(URL url, String authToken) throws IOException {

        HttpURLConnection con = null;
        //Check protocol
        if (isHTTPS(url)) {
            con = (HttpsURLConnection) url.openConnection();
        } else {
            con = (HttpURLConnection) url.openConnection();
        }

        con.setRequestMethod("DELETE");
        con.setRequestProperty("Authorization", authToken);

        int responseCode = con.getResponseCode();
        logger.info("Sending 'DELETE' request to URL : " + url);
        logger.info("Response Code : " + responseCode + "\n");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    /**
     * Check if user is able to access webservice
     * @param url
     * @param authToken
     * @return String
     * @throws IOException
     */
    public int checkStatusCode(URL url, String authToken) throws IOException {
        HttpURLConnection con = null;
        //Check protocol
        if (isHTTPS(url)) {
            con = (HttpsURLConnection) url.openConnection();
        } else {
            con = (HttpURLConnection) url.openConnection();
        }

        // optional default is GET
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", authToken);

        int responseCode = con.getResponseCode();
        logger.info("Sending 'GET' request to URL : " + url);
        logger.info("Response Code : " + responseCode);
        return responseCode;
    }

    /**
     * Only for production, because our spring rest service uses an unsigned certificate!
     */
    public void createAllCertf() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }

    static {
        //for localhost testing only
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
                new javax.net.ssl.HostnameVerifier(){

                    public boolean verify(String hostname,
                                          javax.net.ssl.SSLSession sslSession) {
                        if (hostname.equals("localhost")) {
                            return true;
                        }
                        return false;
                    }
                });
    }

    /**
     * Check if url supports https
     * @param url
     * @return boolean
     * @return boolean
     */
    private boolean isHTTPS(URL url) {
        if (url.getProtocol().equals("https")) {
            return true;
        } else {
            return false;
        }
    }

}
