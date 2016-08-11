package connection;

import model.Translation;
import org.json.JSONArray;
import org.json.JSONObject;
import service.PropertyService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by alexanderweiss on 12.03.16.
 */
public class GoogleTranslatorConnection extends Connection {


    private static final String APIKEY = PropertyService.getGoogleApiKey();
    private static final String API_ENDPOINT = "https://www.googleapis.com/language/translate/v2?";
    private static final String ENCODING = "UTF-8";


    /**
     * Default constructor
     */
    public GoogleTranslatorConnection(){
        super();
    }

    /**
     * Get a translation from the google translation api
     * @param parameters
     * @return
     * @throws IOException
     */
    public Translation getTranslation(HashMap<String, String> parameters) throws IOException {

        String sourceContent = parameters.get("sourceContent");
        String sourceLang = parameters.get("sourceLang");
        String targetLang = parameters.get("targetLang");
        String request = API_ENDPOINT+"key="+APIKEY+"&q="+URLEncoder.encode(sourceContent,ENCODING)+"&source="+sourceLang+"&target="+targetLang;

        JSONObject response = new JSONObject(do_get(new URL(request)));
        JSONObject data = response.getJSONObject("data");
        JSONArray translations = data.getJSONArray("translations");

        return new Translation(sourceContent, translations.getString(0),sourceLang,targetLang);
    }




}
