package listener;

import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import encryption.Encryptor;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import model.OperatingSystem;
import model.Translation;
import org.controlsfx.control.Notifications;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.json.JSONException;
import qrcode.QRCodeManager;
import service.PropertyService;
import ui.MainUi;
import service.TranslationGenerator;

import javax.crypto.SecretKey;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;



/**
 * Created by alexanderweiss
 * Listener for listening to key events. Only a minimum is then mapped.
*/


public class GlobalKeyListener implements NativeKeyListener {


    private MainUi mainUi;
    private TranslationGenerator translationGenerator;
    private String sourceLang; //Not used
    private String targetLang; //Not used

    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(GlobalKeyListener.class);

    public GlobalKeyListener(MainUi mainUi){
        this.mainUi = mainUi;
        this.translationGenerator = new TranslationGenerator(mainUi.getConnectionService());
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {

        int modifiers = e.getModifiers();


        /*
         * Listen for action to generate qrcode from Clipboardcontent  shift+strg+q
         */
        if (((modifiers & NativeInputEvent.SHIFT_MASK) != 0) && ((modifiers & NativeInputEvent.CTRL_MASK) != 0)  && (e.getKeyCode()==NativeKeyEvent.VC_Q)) {
            logger.info("Generate QR-Code (stealth mode)");
            // When stealth mode is active generate translation and qrcode
            if(mainUi.getPreferences().getBoolean("stealthMode",false)){
                try{
                    if(mainUi.userIsLoggedIn()){

                        mainUi.showInfo("QR-Code will be generated");
                        generateAutomaticQrCode(mainUi.getClipboardManager().getClipboardContents());
                        triggerPaste();
                        mainUi.showSuccess("QR-Code inserted");
                    }else{
                        mainUi.showWarning("You has to be logged in !");
                        logger.error("User is not logged in or lost connection (stealth mode)");
                    }
                } catch (NullPointerException ex) {
                    mainUi.showWarning("Your clipboard content is not a text");
                    logger.error("Clipboard content is not a text");
                } catch (IOException ex) {
                    mainUi.showWarning("You must be logged in to generate QR-Codes");
                    logger.error("User is not logged in or lost connection (stealth mode) " + "\n" + ex);
                } catch (JSONException ex) {
                    mainUi.showError("Something went wrong - look at logs.");
                    logger.error("JSON Exception (stealth mode) " + "\n" + ex);
                } catch (LangDetectException ex) {
                    mainUi.showError("Something went wrong - look at logs.");
                    logger.error("Language detection error (stealth mode) " + "\n" + ex);
                } catch (Exception ex) {
                    mainUi.showError("Something went wrong - look at logs.");
                    logger.error("Exception (stealth mode) " + "\n" + ex);
                }
            }
        }

        /*
         *Toggle stealth mode Shift+ctrl+alt+e
         */
        if (((modifiers & NativeInputEvent.SHIFT_MASK) != 0) && ((modifiers & NativeInputEvent.CTRL_MASK) != 0) && ((modifiers & NativeInputEvent.ALT_MASK) != 0) && (e.getKeyCode()==NativeKeyEvent.VC_E)) {
            if(!mainUi.getPreferences().getBoolean("stealthMode", false)){
                mainUi.getPreferences().putBoolean("stealthMode", true);
                logger.info("Stealth mode enabled");
                Platform.runLater(() -> {
                    mainUi.getStealthWindow().show();
                    mainUi.showInfo("Stealth mode enabled");
                    mainUi.getWindow().hide();

                });
            }else{
                mainUi.getPreferences().putBoolean("stealthMode", false);
                logger.info("Stealth mode disabled");
                Platform.runLater(() -> {
                    mainUi.showInfo("Stealth mode disabled");
                    mainUi.getWindow().show();
                    mainUi.getWindow().toFront();
                    mainUi.getWindow().requestFocus();
                    mainUi.getStealthWindow().hide();
                });
            }
        }
    }

    /**
     * Generate a translation with a responding QR-Code and place the QR-Code in the clipboard
     * @param sourceContent
     * @throws Exception
     */
    private void generateAutomaticQrCode(String sourceContent) throws Exception {

        Translation translationToSend = null;
        /* This approach is not implemented but could be used to define source and target language selection when running stealth mode
        // Will call two dialogs, so the user can decide which is the target and source language. Main Thread stops until user made his decisions.
        FutureTask languageHolder = new FutureTask(new Callable() {
            @Override
            public Object call() throws Exception {
                mainUi.getStealthWindow().show();
                mainUi.getStealthWindow().toFront();
                showsourceLanguageChoice(sourceContent);
                showtargetLanguageChoice();
                mainUi.getStealthWindow().hide();
                return new String("<undefined>");
            }
        });

        Platform.runLater(languageHolder);
        languageHolder.get(); // gets null, but current thread is waiting till the sourceLang and to lang is set
        */

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("translationProvider", mainUi.getPreferences().get("translationProvider","Bing Translation API"));
        parameters.put("sourceContent", sourceContent);
        parameters.put("sourceLang", mainUi.getPreferences().get("defaultSourceLanguage", "af"));
        parameters.put("targetLang", mainUi.getPreferences().get("defaultTargetLanguage", "ar"));

        String targetContent = translationGenerator.generateTranslation(parameters);

        translationToSend = new Translation();
        translationToSend.setSourceLang(parameters.get("sourceLang"));
        translationToSend.setTargetLang(parameters.get("targetLang"));
        translationToSend.setSourceContent(sourceContent);
        translationToSend.setTargetContent(targetContent);

        Encryptor encryptor = new Encryptor();
        SecretKey secretKey = Encryptor.generateKey();
        String secretKeyHexRepresentation = Encryptor.getHex(secretKey.getEncoded());

        translationToSend.setSourceContent(encryptor.encrypt(translationToSend.getSourceContent(), secretKey));
        translationToSend.setTargetContent(encryptor.encrypt(translationToSend.getTargetContent(), secretKey));


        Translation postedTranslation = mainUi.getConnectionService().postTranslation(translationToSend);

        System.out.println(PropertyService.getServerUrl()+"?id=" + postedTranslation.getId() + "&key=" + secretKeyHexRepresentation + "&iv=" + encryptor.getIVHex());
        BufferedImage qrCode = QRCodeManager.generateQRCode(PropertyService.getServerUrl()+"?id=" + postedTranslation.getId() + "&key=" + secretKeyHexRepresentation + "&iv=" + encryptor.getIVHex(), Integer.parseInt(mainUi.getPreferencesLayout().getQrCodeWidth().getText()), Integer.parseInt(mainUi.getPreferencesLayout().getQrCodeHeight().getText()));
        mainUi.getClipboardManager().putImageToClipboard(SwingFXUtils.toFXImage(qrCode,null));
    }

    /**
     * Show a choice box for choosing source language
     * @param sourceContent
     */
    private void showSourceLanguageChoice(String sourceContent){

            String selectedLang = null;
            java.util.List<String> choices = DetectorFactory.getLangList();

        ChoiceDialog<String> dialog = null;
        try {
            dialog = new ChoiceDialog<>(mainUi.detect(sourceContent), choices);
        } catch (LangDetectException e) {
            e.printStackTrace();
        }

        dialog.setTitle("Choice Dialog");
        dialog.setHeaderText("Text detected!");
        dialog.setContentText("Choose your source language to start the qrcode generation:");
        dialog.initOwner(mainUi.getStealthWindow());

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            selectedLang = result.get();
        }
        sourceLang = selectedLang;
    }

    /**
     * Show a choice box for choosing target language
     */
    private void showTargetLanguageChoice(){

        String selectedLang = null;
        java.util.List<String> choices = DetectorFactory.getLangList();

        ChoiceDialog<String> dialog = null;

        dialog = new ChoiceDialog<>("af", choices);

        dialog.setTitle("Choice Dialog");
        dialog.setHeaderText("Text detected!");
        dialog.setContentText("Choose your target language:");
        dialog.initOwner(mainUi.getStealthWindow());

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            selectedLang = result.get();
        }
        targetLang = selectedLang;
    }



    /**
     * Trigger paste action
     */
    private void triggerPaste(){
        try {
            Robot r = new Robot();

            if(OperatingSystem.isMac()){            // OSX
                r.keyPress(KeyEvent.VK_META);
                r.keyPress(KeyEvent.VK_V);

                r.keyRelease(KeyEvent.VK_V);
                r.keyRelease(KeyEvent.VK_META);
            }else if(OperatingSystem.isWindows()){  // WINDOWS
                r.keyPress(KeyEvent.VK_CONTROL);
                r.keyPress(KeyEvent.VK_V);

                r.keyRelease(KeyEvent.VK_V);
                r.keyRelease(KeyEvent.VK_CONTROL);
            } else  {                               // ELSE
                r.keyPress(KeyEvent.VK_CONTROL);
                r.keyPress(KeyEvent.VK_V);

                r.keyRelease(KeyEvent.VK_V);
                r.keyRelease(KeyEvent.VK_CONTROL);
            }

        } catch (AWTException awte) {
            logger.error("Error triggering paste \n" + awte.toString());
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        //doNothing
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
        //doNothing
    }

}
