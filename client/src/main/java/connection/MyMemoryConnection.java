package connection;

import model.Translation;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by alexanderweiss
 * Class for representing a connection to http://www.mymemory.translated.net
 */
public class MyMemoryConnection extends Connection {

    private static final String API_ENDPOINT = "http://api.mymemory.translated.net/";
    private static final String ENCODING = "UTF-8";
    
    /**
     * Default constructor
     */
    public MyMemoryConnection(){
        super();
    }

    /**
     * Get a translation from mymemory.translated.net
     * @param parameters
     * @return Translation
     * @throws Exception
     */
    public Translation getTranslation(HashMap<String, String> parameters) throws JSONException, IOException {

        JSONObject initial_request = new JSONObject();
        initial_request.put("sourceContent", parameters.get("sourceContent"));
        initial_request.put("sourceLang", parameters.get("sourceLang"));
        initial_request.put("targetLang", parameters.get("targetLang"));
        System.out.println(initial_request);

        String get_request_endpoint = API_ENDPOINT+"get?q="+ URLEncoder.encode(parameters.get("sourceContent"),ENCODING)+"&langpair="+parameters.get("sourceLang")+"|"+parameters.get("targetLang");

        JSONObject jsonTranslation = new JSONObject(do_get(new URL(get_request_endpoint)));
        jsonTranslation.put("initial_request", initial_request);

        JSONObject initialRequest = jsonTranslation.getJSONObject("initial_request");
        JSONObject responseData = jsonTranslation.getJSONObject("responseData");

        return new Translation(initialRequest.getString("sourceContent"), responseData.getString("translatedText"),initialRequest.getString("sourceLang"), initialRequest.getString("targetLang"),responseData.getDouble("match"));
    }
}
