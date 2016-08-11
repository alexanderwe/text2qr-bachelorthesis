package webservice.controllers;

import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import webservice.services.TranslationService;
import webservice.encryption.Encryptor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import webservice.model.Translation;
import webservice.services.MailService;
import webservice.services.ResourceManager;
import webservice.services.TranslationGenerator;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.MessagingException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alexanderweiss
 * Class for mapping requests to /
 */

@Controller
public class WebAppController {


    private TranslationService translationService = new TranslationService();
    private MailService mailService = new MailService();
    private Encryptor encryptor = new Encryptor();
    private TranslationGenerator translationGenerator = new TranslationGenerator();

    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(WebAppController.class);

    /**
     * Map the / endpoint   Links used in the qr code is: https://localhost:8443/?id=your_id&key=your_key&iv=your_iv
     * @param id
     * @param hexkey
     * @param ivhex
     * @param model
     * @return
     */
    @RequestMapping("/")
    public String viewTranslation(@RequestParam(value="id", required=true, defaultValue="-1") String id, @RequestParam(value="key", required=true, defaultValue="-1") String hexkey, @RequestParam(value="iv", required=true, defaultValue="-1") String ivhex , Model model) {
        Translation requestedTranslation = null;
        model.addAttribute("languageList", getLanguageList());
        System.out.println(getLanguageList().size());

        if(!id.equals("-1")){
            try{
                requestedTranslation = translationService.findTranslationById(Integer.valueOf(id));
            }catch(NullPointerException npe){ //database is not available or offline etc..
                npe.printStackTrace();
                return "index";
            }catch (NumberFormatException numbe){
                numbe.printStackTrace();
                model.addAttribute("nullError", true);
                return "index";
            }
        }else{
            model.addAttribute("sourceContent", "Translation not available");
            model.addAttribute("targetContent", "Translation not available");
        }


        if(hexkey.equals("-1")){ // no hexkey
            model.addAttribute("sourceContent", "Translation not available");
            model.addAttribute("targetContent", "Translation not available");
        }else{
            if(requestedTranslation == null){  // no translation from database
                model.addAttribute("sourceContent", "Translation not available");
                model.addAttribute("targetContent", "Translation not available");
            }else{
                // normal request from qrcode
                SecretKey originalKey = new SecretKeySpec(Encryptor.hexStringToByteArray(hexkey),"AES");
                encryptor.setIv(Encryptor.hexStringToByteArray(ivhex));

                model.addAttribute("sourceContent", encryptor.decrypt(requestedTranslation.getSourceContent(),originalKey));
                model.addAttribute("targetContent", encryptor.decrypt(requestedTranslation.getTargetContent(),originalKey));
                model.addAttribute("targetLang", requestedTranslation.getTargetLang());
                model.addAttribute("id", id);
                model.addAttribute("key", hexkey);
                model.addAttribute("iv", ivhex);
            }
        }
        return "index";
    }


    /**
     * Map request to /newtranslation. This method is called when a user requests a new translation of a existing translation, while visiting the website.
     * @param id
     * @param hexkey
     * @param ivhex
     * @param newTargetLang
     * @param newProvider
     * @param model
     * @return
     */
    @RequestMapping("/newtranslation")
    public ResponseEntity<String> newTranslation(@RequestParam(value="id", required=true, defaultValue="-1") String id, @RequestParam(value="key", required=true, defaultValue="-1") String hexkey, @RequestParam(value="iv", required=true, defaultValue="-1") String ivhex , @RequestParam(value="newTargetLang", required=true) String newTargetLang, @RequestParam(value="newProvider", required=true) String newProvider, Model model) {

        Translation requestedTranslation = new Translation();
        Translation reloadTranslation = new Translation();
        HashMap<String, String> parameters = new HashMap<>();
        String originalSourceContent = null;
        String originalTargetContent = null;
        try{
            requestedTranslation = translationService.findTranslationById(Integer.valueOf(id));
            SecretKey originalKey = new SecretKeySpec(Encryptor.hexStringToByteArray(hexkey),"AES");
            encryptor.setIv(Encryptor.hexStringToByteArray(ivhex));

            originalSourceContent = encryptor.decrypt(requestedTranslation.getSourceContent(),originalKey); // need to encrypt the sourceContent from the database translation
            originalTargetContent =  encryptor.decrypt(requestedTranslation.getTargetContent(),originalKey);


            parameters.put("translationProvider", newProvider);
            parameters.put("sourceContent", originalSourceContent );
            parameters.put("sourceLang", requestedTranslation.getSourceLang());
            parameters.put("targetLang", newTargetLang);

        }catch (NumberFormatException nfe){
            logger.error("No valid translation ID \n" + nfe);
        }
        catch (NullPointerException npe){
            logger.error("No translation found \n" + npe);
        }

        // Get the new translation
        try{

            String targetContent = translationGenerator.generateTranslation(parameters);
            reloadTranslation = new Translation();
            reloadTranslation.setSourceContent(originalSourceContent);
            reloadTranslation.setTargetContent(targetContent);
            reloadTranslation.setSourceLang(parameters.get("sourceLang"));
            reloadTranslation.setTargetLang(parameters.get("targetLang"));

        } catch (JSONException e) {
            model.addAttribute("sameLanguageError", true);
            model.addAttribute("sourceContent", originalSourceContent);
            model.addAttribute("targetContent", originalTargetContent);
            model.addAttribute("targetLang", requestedTranslation.getTargetLang());
            model.addAttribute("id", id);
            model.addAttribute("key", hexkey);
            model.addAttribute("iv", ivhex);
            return new ResponseEntity<String>("", HttpStatus.BAD_REQUEST);

        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("languageNotSupported", true);
            model.addAttribute("sourceContent", originalSourceContent);
            model.addAttribute("targetContent", originalTargetContent);
            model.addAttribute("targetLang", requestedTranslation.getTargetLang());
            model.addAttribute("id", id);
            model.addAttribute("key", hexkey);
            model.addAttribute("iv", ivhex);
            return new ResponseEntity<String>("", HttpStatus.NOT_IMPLEMENTED);

        } catch (NullPointerException npe) {
            model.addAttribute("sameLanguageError", true);
            model.addAttribute("sourceContent", originalSourceContent);
            model.addAttribute("targetContent", originalTargetContent);
            model.addAttribute("targetLang", requestedTranslation.getTargetLang());
            model.addAttribute("id", id);
            model.addAttribute("key", hexkey);
            model.addAttribute("iv", ivhex);
            return new ResponseEntity<String>("", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Fill our model with the new translation, but remember the id from the original translation
        model.addAttribute("sourceContent",originalSourceContent);
        model.addAttribute("targetContent", reloadTranslation.getTargetContent());  // take the information from the new tranlsation
        model.addAttribute("targetLang", reloadTranslation.getTargetLang());        // take the information from the new tranlsation
        model.addAttribute("id", id);
        model.addAttribute("key", hexkey);
        model.addAttribute("iv", ivhex);
        return new ResponseEntity<String>(reloadTranslation.getTargetContent(), HttpStatus.OK);
    }


    /**
     * Map the / endpoint   Links used in the qr code is: https://localhost:8443/?id=your_id&key=your_key&iv=your_iv
     * @param model
     * @return
     */
    @RequestMapping("/index")
    public String showIndex( Model model) {
        Translation requestedTranslation = null;
        model.addAttribute("languageList", getLanguageList());
        model.addAttribute("sourceContent", "Translation not available");
        model.addAttribute("targetContent", "Translation not available");
        return "index";
    }


    /**
     * Map the /contact. Shows contact page.
     * @param success
     * @param error
     * @param model
     * @return
     */
    @RequestMapping(value="/contact", method= RequestMethod.GET)
    public String contact(@RequestParam(value="success", required=false) String success,@RequestParam(value="error", required=false) String error, Model model) {

        if (success != null) {
            model.addAttribute("success",true);
        }
        if (error != null) {
            model.addAttribute("error",true);
        }

        return "contact";
    }


    /**
     * Map /contact/sendmessage. Send an email to the admin of the webservice, when the conact form is submitted.
     * @param name
     * @param email
     * @param message
     * @param model
     * @return
     */
    @RequestMapping(value="/contact/sendmessage", method= RequestMethod.POST)
    public String sendContact(@RequestParam(value="name", required=true, defaultValue="name") String name, @RequestParam(value="email", required=true, defaultValue="email") String email, @RequestParam(value="message", required=true, defaultValue="message") String message, Model model) {
        try {
            mailService.sendContactFormSubmitToAdmin(name,email,message);
        } catch (MessagingException e) {
            e.printStackTrace();
            return "redirect:/contact?error";
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/contact?error";
        }

        return "redirect:/contact?success";
    }


    /**
     * Map request /status. Shows status page.
     * @param model
     * @return
     */
    @RequestMapping("/status")
    public String showStatus(Model model){

        if(translationService.connect()){
            translationService.disconnect();
        }else {
            model.addAttribute("databaseError",true);
        }
        return "status";
    }


    /**
     * Get the language list for using it on the webservice
     * @return
     */
    private ArrayList<String> getLanguageList(){
        ArrayList<String> languageList = new ArrayList<>();
        for (String language : ResourceManager.loadLanguageCodes().split(",")){
            languageList.add(language);
        }
        return languageList;
    }

}