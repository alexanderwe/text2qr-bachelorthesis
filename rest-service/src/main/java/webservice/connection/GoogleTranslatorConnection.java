package webservice.connection;


import webservice.model.Translation;
import webservice.services.PropertyService;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by alexanderweiss on 12.03.16.
 */
public class GoogleTranslatorConnection extends Connection {


    private static final String APIKEY = PropertyService.getGoogleApiKey();
    private static final String API_ENDPOINT = "https://www.googleapis.com/language/translate/v2?q=";
    protected static final String ENCODING = "UTF-8";


    public GoogleTranslatorConnection(){
        super();
    }

    /**
     * Get a translation from google
     * @param parameters
     * @return Translation
     * @throws IOException
     */
    public Translation getTranslation(HashMap<String, String> parameters) throws IOException {

        String sourceContent = parameters.get("sourceContent");
        String sourceLang = parameters.get("sourceLang");
        String targetLang = parameters.get("targetLang");
        String request = API_ENDPOINT+ URLEncoder.encode(sourceContent,ENCODING)+"&target="+targetLang+"&format=text&source="+sourceLang+"&key="+APIKEY;
        String response = do_get(new URL(request));

        return new Translation();
    }
}
