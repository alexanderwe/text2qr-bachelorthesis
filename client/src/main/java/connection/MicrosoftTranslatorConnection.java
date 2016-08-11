package connection;

import model.Translation;

import org.json.JSONException;
import org.json.JSONObject;
import service.PropertyService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by alexanderweiss on 12.03.2016
 * Class for representing a connection to the Microsoft Translator API
 */
public class MicrosoftTranslatorConnection extends Connection {

    //Define all variables
    private final static String API_ENDPOINT = "http://api.microsofttranslator.com/v2/Http.svc/Translate?text=";;
    private final static  String ENCODING = "UTF-8";
    private final static String DATAMARKET_ACCESS_URI = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13";
    private final static String CLIENT_ID = PropertyService.getMicrosoftClientID();
    private final static String CLIENT_SECRET = PropertyService.getMicrosoftApiKey();
    private final static String SCOPE = "http://api.microsofttranslator.com ";
    private final static String GRANTY_TYPE = "client_credentials";
    private  static String access_token;
    private static Date access_token_valid = new Date(); // Used for access token validation
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MicrosoftTranslatorConnection.class);


    /**
     * Default constructor
     */
    public MicrosoftTranslatorConnection(){
        super();
    }

    /**
     * Get a translation from microsoft translator api
     * @param parameters
     * @return
     * @throws Exception
     */
    public Translation getTranslation(HashMap<String, String> parameters) throws IOException, JSONException {
        try{
            String sourceContent = parameters.get("sourceContent");
            String sourceLang = parameters.get("sourceLang");
            String targetLang = parameters.get("targetLang");
            String authToken;

            //Check if access token is valid, if not get a new one
            if(new Date().after(access_token_valid)){
                authToken ="Bearer " + getAccessToken();
            }else{
                authToken ="Bearer " + access_token;
            }

            URL url = new URL(API_ENDPOINT+URLEncoder.encode(sourceContent,ENCODING)+"&from="+sourceLang+"&to="+targetLang);
            String translationString = do_get_with_auth(url,authToken);
            String toContent =  translationString.split(">")[1].split("<")[0];

            return new Translation(sourceContent,toContent,sourceLang,targetLang);
        }catch(IndexOutOfBoundsException iuobe){ // translation is empty
            logger.error("Microsoft translation is empty.");
            return null;
        }

    }

    /**
     * Get the access token for our application
     */
    private static String getAccessToken() throws IOException, JSONException {
        logger.info("Get new access token for Microsoft translation service");
        final String parameters = "grant_type="+ GRANTY_TYPE +"&scope="+ SCOPE
                + "&client_id=" + URLEncoder.encode(CLIENT_ID,ENCODING)
                + "&client_secret=" + URLEncoder.encode(CLIENT_SECRET,ENCODING) ;

        final URL url = new URL(DATAMARKET_ACCESS_URI);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=" + ENCODING);
        connection.setRequestProperty("Accept-Charset",ENCODING);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // Writing the post data to the HTTP request body
        BufferedWriter httpRequestBodyWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        httpRequestBodyWriter.write(parameters);
        httpRequestBodyWriter.close();

        // Reading from the HTTP response body
        Scanner httpResponseScanner = new Scanner(connection.getInputStream());
        String inputLine;
        StringBuilder response = new StringBuilder();
        try{
            while ((inputLine = httpResponseScanner.nextLine()) != null) {
                response.append(inputLine.replace("\uFEFF", ""));
            }
        }catch(NoSuchElementException nsee){
            logger.error("No response line found");
        }

        httpResponseScanner.close();


        //parse the response to obatin the access token
        JSONObject tokenJSON = new JSONObject(response.toString());

        int seconds_till_access_expires = tokenJSON.getInt("expires_in");
        Calendar calendar = Calendar.getInstance(); // get a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, seconds_till_access_expires);
        access_token_valid = calendar.getTime();


        access_token = tokenJSON.getString("access_token");
        logger.info("Access token retrieved: " + access_token);
        return access_token;
    }


}
