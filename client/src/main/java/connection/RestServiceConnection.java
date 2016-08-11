package connection;


import model.Translation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import service.PropertyService;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;

/**
 * Created by alexanderweiss
 * Class for representing a connection to our REST webservice
 */
public class RestServiceConnection extends Connection {


    private final String API_ENDPOINT;
    private  String authToken;
    private String username;
    private String password;

    /**
     * Default constructor. Sets the username and password for this connection.
     * This information is needed to post translations to the webservice.
     * @param username
     * @param password
     */
    public  RestServiceConnection(String username, String password){
        super();

        this.username = username;
        this.password = password;

        this.API_ENDPOINT = PropertyService.getServerUrl()+"api";  // Production location, web service runs on localhost with https
        try {
            this.authToken = "Basic " + new String(Base64.getEncoder().encode((username+":"+password).getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Get a specific translation from our webservice
     * @param id
     * @return Translation
     * @throws IOException
     * @throws JSONException
     */
    public Translation getTranslation(int id) throws IOException, JSONException {
        JSONObject jsonTranslation = new JSONObject(do_get_with_auth(new URL(this.API_ENDPOINT+"/translations/"+id),authToken));
        return new Translation(jsonTranslation);
    }

    /**
     * Get all translation from our webservive
     * NOT USED
     * @return ArrayList
     * @throws IOException
     * @throws JSONException
     */
    public ArrayList<Translation> getTranslations() throws IOException, JSONException{
        ArrayList<Translation> translations = new ArrayList<Translation>();
        JSONArray translationsJSON = new JSONArray(do_get_with_auth(new URL(API_ENDPOINT+"/translations"),authToken));

        for(int i = 0; i < translationsJSON.length(); i++){
            JSONObject translationJSON = translationsJSON.getJSONObject(i);
            translations.add(new Translation(translationJSON.getInt("id"),translationJSON.getString("sourceContent"),translationJSON.getString("targetContent"),translationJSON.getString("sourceLang"),translationJSON.getString("targetLang"),translationJSON.getDouble("match")));
        }
        return translations;
    }

    /**
     * Post translation to our webservice
     * @param translation
     * @return Translation
     * @throws IOException
     * @throws JSONException
     */
    public Translation postTranslation(Translation translation) throws IOException, JSONException{
        Translation postedTranslation;
        System.out.println(authToken);
        JSONObject postedTranslationJSON = new JSONObject(do_post_with_auth(new URL(this.API_ENDPOINT+"/translations/"),translation.getJson_representation().toString(),authToken));
        postedTranslation = new Translation(postedTranslationJSON.getInt("id"),postedTranslationJSON.getString("sourceContent"),postedTranslationJSON.getString("targetContent"),postedTranslationJSON.getString("sourceLang"),postedTranslationJSON.getString("targetLang"),postedTranslationJSON.getDouble("match"));
        return postedTranslation;
    }

    /**
     * Delete a translation in our webservice
     * NOT USED
     * @param id
     * @throws IOException
     */
    public void deleteTranslation(int id) throws IOException {
        do_delete_with_auth(new URL(this.API_ENDPOINT+"/translations/"+id),authToken);
    }

    //check if user is existent in webservice
    public int checkUser() throws IOException {
       return checkStatusCode(new URL(this.API_ENDPOINT+"/checkuser/"), authToken);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
