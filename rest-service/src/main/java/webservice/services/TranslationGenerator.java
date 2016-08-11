package webservice.services;


import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONException;
import webservice.connection.ConnectionService;
import webservice.model.Translation;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by alexanderweiss on 06.04.16.
 * 'SuperClass' for generatin translations.
 */
public class TranslationGenerator {

    private ConnectionService connectionService;


    public TranslationGenerator(){
        this.connectionService = new ConnectionService();
    }


    /**
     * Generate translation with paramaters.
     * 1. Extract needen informations from hash map. sourceContent, sourceLang, targetLang and translationProvider
     * 2. Check if translation provider is mymemory.
     * 2.1 If mymmemory then ensure that each translation request has a max character size of 500
     * 3. Send requested translation to the choosen translation provider.
     * 4. Parse response
     * 5. Return response
     * @param parameters
     * @return String
     * @throws IOException
     * @throws JSONException
     */
    public String generateTranslation(HashMap<String, String> parameters) throws IOException, JSONException {
        Translation translationToSend = null;
        String[] lines = ((String) parameters.get("sourceContent")).split("\n");
        StringBuilder finaltargetContent = new StringBuilder();

        for(String line:lines){
            StringBuffer mymemoryLineParts = null;
            HashMap<String, String> tempParameters = new HashMap<>();
            tempParameters.put("sourceContent", line);
            tempParameters.put("sourceLang", parameters.get("sourceLang"));
            tempParameters.put("targetLang", parameters.get("targetLang"));
            if (line.isEmpty()){
                finaltargetContent.append(System.lineSeparator());
            }else{
                String translationProvider = parameters.get("translationProvider");

                //mymemory.translated.net only supports 500 chars for each request
                if(translationProvider.equals("mymemory.translated.net")) {

                    if (line.length() > 500) {
                        mymemoryLineParts = new StringBuffer();
                        String[] parts = WordUtils.wrap(line, 300).split(System.lineSeparator());


                        for (String part : parts) {
                            HashMap<String, String> myMemoryParams = new HashMap<>();
                            myMemoryParams.put("sourceContent", part);
                            myMemoryParams.put("sourceLang", (String) parameters.get("sourceLang"));
                            myMemoryParams.put("targetLang", (String) parameters.get("targetLang"));
                            Translation mymemoryPart = connectionService.getTranslation(translationProvider, myMemoryParams);
                            mymemoryLineParts.append(mymemoryPart.getTargetContent());
                        }
                    } else {
                        translationToSend = connectionService.getTranslation(translationProvider, tempParameters);
                    }

                }else{
                    translationToSend = connectionService.getTranslation(translationProvider,tempParameters);
                }

                if(mymemoryLineParts != null){
                    finaltargetContent.append(mymemoryLineParts.toString());
                    finaltargetContent.append(System.lineSeparator());
                }else{
                    finaltargetContent.append(translationToSend.getTargetContent());
                    finaltargetContent.append(System.lineSeparator());
                }
            }
        }
        return finaltargetContent.toString();
    }

}
