package connection;

import model.Translation;
import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by alexanderweiss on 04.04.16.
 * Wrapper class for all used connections
 */
public class ConnectionService {

    private MyMemoryConnection myMemoryConnection;
    private MicrosoftTranslatorConnection microsoftTranslatorConnection;
    private GoogleTranslatorConnection googleTranslatorConnection;
    private RestServiceConnection restServiceConnection;

    /**
     * Default constructor
     */
    public ConnectionService(){
        this.myMemoryConnection = new MyMemoryConnection();
        this.microsoftTranslatorConnection = new MicrosoftTranslatorConnection();
        this.googleTranslatorConnection = new GoogleTranslatorConnection();
        this.restServiceConnection = new RestServiceConnection("","");  // init a empty connection to our webserver. No user nor password provided.
    }

    /**
     * Get a translation. Decide which translation provider to use, depending on the translation provider paramter.
     * Possible values: 'mymemory.translated.net' , 'Bing Translation API' ,  'Google Translation API'
     * @param translationProvider
     * @param parameters
     * @return Translation
     * @throws IOException
     * @throws JSONException
     */
    public Translation getTranslation (String translationProvider, HashMap<String, String> parameters) throws IOException, JSONException {
        Translation translation = null;
        switch (translationProvider){
            case "mymemory.translated.net": translation  = myMemoryConnection.getTranslation(parameters);break;
            case "Bing Translation API": translation = microsoftTranslatorConnection.getTranslation(parameters);break;
            case "Google Translation API":translation = googleTranslatorConnection.getTranslation(parameters);break;
            default:break;
        }
        return translation;
    }

    /**
     * Send a translation to our webservice
     * @param translation
     * @return Translation
     * @throws Exception
     */
    public Translation postTranslation (Translation translation) throws Exception {
         return restServiceConnection.postTranslation(translation);
    }

    public MyMemoryConnection getMyMemoryConnection() {
        return myMemoryConnection;
    }

    public MicrosoftTranslatorConnection getMicrosoftTranslatorConnection() {
        return microsoftTranslatorConnection;
    }

    public GoogleTranslatorConnection getGoogleTranslatorConnection() {
        return googleTranslatorConnection;
    }

    public RestServiceConnection getRestServiceConnection() {
        return restServiceConnection;
    }

    public void setRestServiceConnection(RestServiceConnection restServiceConnection) {
        this.restServiceConnection = restServiceConnection;
    }
}
