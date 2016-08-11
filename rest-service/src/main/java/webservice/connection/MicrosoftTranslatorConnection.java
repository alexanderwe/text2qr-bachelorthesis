package webservice.connection;


import org.json.JSONException;
import org.json.JSONObject;
import webservice.model.Translation;
import webservice.services.PropertyService;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by alexanderweiss
 * Class for representing a connection to the Microsoft Translator API
 */
public class MicrosoftTranslatorConnection extends Connection {

    private final static String API_ENDPOINT = "http://api.microsofttranslator.com/v2/Http.svc/Translate?text=";;
    protected static final String ENCODING = "UTF-8";
    private static String DatamarketAccessUri = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13";
    private final static String client_id = PropertyService.getMicrosoftClientID();
    private final static String client_secret = PropertyService.getMicrosoftApiKey();
    private final static String  scope = "http://api.microsofttranslator.com";
    private final static String granty_type = "client_credentials";
    private  static String access_token;
    private static Date access_token_valid = new Date();


    public MicrosoftTranslatorConnection(){
        super();
    }

    /**
     * Get a translation from microsoft translator
     * @param parameters
     * @return Translation
     * @throws Exception
     */
    public Translation getTranslation(HashMap<String, String> parameters) throws IOException, JSONException {

        String sourceContent = parameters.get("sourceContent");
        String sourceLang = parameters.get("sourceLang");
        String targetLang= parameters.get("targetLang");
        String authToken;

        //Check if access token is valid, if not get a new one
        if(new Date().after(access_token_valid)){
            authToken ="Bearer " + getAccessToken();
        }else{
            authToken ="Bearer " + access_token;
        }

        URL url = new URL(API_ENDPOINT+URLEncoder.encode(sourceContent,ENCODING)+"&from="+sourceLang+"&to="+targetLang);
        String translationString = do_get_with_auth(url,authToken);
        String targetContent =  translationString.split(">")[1].split("<")[0];

       return new Translation(sourceContent,targetContent,sourceLang,targetLang);
    }

    /**
     * Get the access token for our application
     */
    private static String getAccessToken() throws IOException, JSONException {
        final String parameters = "grant_type="+granty_type+"&scope="+scope
                + "&client_id=" + URLEncoder.encode(client_id,ENCODING)
                + "&client_secret=" + URLEncoder.encode(client_secret,ENCODING) ;

        final URL url = new URL(DatamarketAccessUri);
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
            nsee.printStackTrace();
        }

        httpResponseScanner.close();


        //parse the response to obatin the access token
        JSONObject tokenJSON = new JSONObject(response.toString());

        int seconds_till_access_expires = tokenJSON.getInt("expires_in");
        Calendar calendar = Calendar.getInstance(); // get a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, seconds_till_access_expires);
        access_token_valid = calendar.getTime();


        access_token = tokenJSON.getString("access_token");
        return access_token;
    }


}
