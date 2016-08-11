package service;

import connection.ConnectionService;
import model.Translation;
import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by alexanderweiss on 06.04.16.
 * Wrapper class for generating translations.
 */
public class TranslationGenerator {

    private ConnectionService connectionService;


    /**
     * Default constructor.
     * @param connectionService
     */
    public TranslationGenerator (ConnectionService connectionService){
        this.connectionService = connectionService;
    }


    /**
     * Generate a translation. Use a hashmap with the values: sourceContent, sourceLang, targetLang and translationProvider.
     * 1. Split text at linebreak, to save formating.
     * 2. Send every single splitted text to the translation provider.
     * 3.If the translation provider is mymemory.
     *      3.1 Check if the splitted text has more than 500 characters
     *      3.2 if so, split the text again and translate the splitted paragraph
     *  4. After getting each translation add it all together and return the final translated content.
     * @param parameters
     * @return String
     * @throws IOException
     * @throws JSONException
     */
    public String generateTranslation(HashMap<String, String> parameters) throws java.net.UnknownHostException , IOException, JSONException {
        Translation translation = null;

        String sourceContent = parameters.get("sourceContent");

        String[] lines = sourceContent.split("\n");
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

                    if (line.length() > 500) { // if mymemory and over 500 characters split line again
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
                        translation = connectionService.getTranslation(translationProvider, tempParameters); // mymemory and not more than 500 characters
                    }

                }else{
                    translation = connectionService.getTranslation(translationProvider,tempParameters);  // any other translation service provider
                }

                // Put all translated lines together
                if(mymemoryLineParts != null){ // if mymemory use the mymemory params to create final text
                    finaltargetContent.append(mymemoryLineParts.toString());
                    finaltargetContent.append(System.lineSeparator());
                }else{
                    finaltargetContent.append(translation.getTargetContent());
                    finaltargetContent.append(System.lineSeparator());
                }
            }
        }

        String returntargetContent = finaltargetContent.toString();

        //Fix for directly creating qrcode with word documents in stealth mode
        if(returntargetContent.contains("&#xD;")){
            returntargetContent = returntargetContent.replace("&#xD;", System.lineSeparator());
        }

        return returntargetContent;
    }

}
