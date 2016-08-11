package webservice.connection;

import javafx.scene.control.Alert;
import org.json.JSONException;
import webservice.model.Translation;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by alexanderweiss on 04.04.16.
 * Class for merging all different translation providers
 */
public class ConnectionService {



    private MyMemoryConnection myMemoryConnection;
    private MicrosoftTranslatorConnection microsoftTranslatorConnection;
    private GoogleTranslatorConnection googleTranslatorConnection;



    public ConnectionService (){
        this.myMemoryConnection = new MyMemoryConnection();
        this.microsoftTranslatorConnection = new MicrosoftTranslatorConnection();
        this.googleTranslatorConnection = new GoogleTranslatorConnection();
    }


    /**
     * Get a translation. Use translation provider string to choose your desired translation provider. Use parameters HashMap<String,String> to set the parameters for translating.
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


    public MyMemoryConnection getMyMemoryConnection() {
        return myMemoryConnection;
    }

    public MicrosoftTranslatorConnection getMicrosoftTranslatorConnection() {
        return microsoftTranslatorConnection;
    }

    public GoogleTranslatorConnection getGoogleTranslatorConnection() {
        return googleTranslatorConnection;
    }
}
