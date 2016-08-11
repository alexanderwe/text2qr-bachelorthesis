package ui;

import clipboard.ClipboardManager;
import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import connection.ConnectionService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import listener.GlobalKeyListener;
import model.OperatingSystem;
import org.apache.poi.util.IOUtils;
import org.controlsfx.control.Notifications;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import service.PropertyService;
import ui.layout.HomeLayout;
import ui.layout.LoginLayout;
import ui.layout.PreferencesLayout;

import java.awt.*;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Created by alexanderweiss
 * Main class of the program, starts ui
 */
public class MainUi extends Application {


    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MainUi.class);

    private Stage window;
    private Stage stealthWindow;


    private LoginLayout loginLayout;
    private Scene loginScene;

    private HomeLayout homeLayout;
    private Scene homeScene;

    private PreferencesLayout preferencesLayout;
    private Scene preferencesScene;

    private Preferences preferences;

    private String theme;

    private ClipboardManager clipboardManager;
    private ConnectionService connectionService;

    private static final String[] defaultLanguages= new String[] { "af", "ar",
            "bg", "bn", "cs", "da", "de", "el", "en", "es", "et", "fa", "fi",
            "fr", "gu", "he", "hi", "hr", "hu", "id", "it", "ja", "kn", "ko",
            "lt", "lv", "mk", "ml", "mr", "ne", "nl", "no", "pa", "pl", "pt",
            "ro", "ru", "sk", "sl", "so", "sq", "sv", "sw", "ta", "te", "th",
            "tl", "tr", "uk", "ur", "vi", "zh-cn", "zh-tw" }; // Array to get all language codes from google langdetect jar



    public static void main(String [] args){
        launch(args);
    }

    /**
     * Start the ui
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("Application started on " + OperatingSystem.OS);
        this.clipboardManager = new ClipboardManager();
        this.connectionService = new ConnectionService();
        window = primaryStage;
        window.setTitle("text2qr-translator");
        window.setOnCloseRequest(e -> closeProgram());


        //Needed to display notifications
        stealthWindow = new Stage();
        stealthWindow.initStyle(StageStyle.TRANSPARENT);
        final Scene scene = new Scene(new Group(),300, 250);
        scene.setFill(null);
        stealthWindow.setScene(scene);

        theme = getClass().getResource("/css/style.css").toExternalForm();
        this.preferences = Preferences.userNodeForPackage(this.getClass());
        loadDetector();

        loginLayout = new LoginLayout(this);
        loginScene = new Scene(loginLayout, 400,200);
        loginScene.getStylesheets().addAll(theme);

        homeLayout = new HomeLayout(this);
        homeScene = new Scene(homeLayout,1000,600);
        homeScene.getStylesheets().addAll(theme);

        preferencesLayout = new PreferencesLayout(this);
        preferencesScene = new Scene(preferencesLayout,500,600);
        preferencesScene.getStylesheets().addAll(theme);

        window.setScene(loginScene);
        window.getIcons().add(new javafx.scene.image.Image(getClass().getResource("/img/text2qr-translator-logo.png").toExternalForm()));
        window.show();
        window.setResizable(true);
        this.getPreferences().putBoolean("stealthMode", false); //disable stealth mode by start


        //Init global keylistener
        try {
            GlobalScreen.registerNativeHook();
        }
        catch (NativeHookException ex) {
            logger.error("Could not register JNativeHook" +  "\n" +  ex);

            System.exit(1);
        }

        //Seems to be working in IDE but not wen executing jar or .app on a running system
        if (SystemTray.getSystemTray().isSupported()) {
            logger.info("System tray is supported");
            setTrayIcon(window);
        }else{
            logger.info("System tray not supported");
        }

        GlobalScreen.addNativeKeyListener(new GlobalKeyListener(this));
        // Get the logger for "org.jnativehook" and set the level to off.
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        // Don't forget to disable the parent handlers.
        logger.setUseParentHandlers(false);

        Platform.setImplicitExit(false);
    }

    /**
     * Set the tray icon on mac and windwos
     * @param window
     */
    private void setTrayIcon(Stage window){

        Image image = null;

        //Set high res system tray icon on Mac OS
        if(OperatingSystem.isMac()){

           image = Toolkit.getDefaultToolkit().getImage(MainUi.class.getResource("/img/text2qr-translator-logo.png").getFile());
        }else{
            image = Toolkit.getDefaultToolkit().getImage(MainUi.class.getResource("/img/text2qr-translator-logo-16x.png").getFile());
        }

        PopupMenu popup = new PopupMenu();
        MenuItem toggleStealth = new MenuItem("Toggle stealth mode");
        MenuItem closeProgram = new MenuItem("Close");

        toggleStealth.addActionListener(e -> {
            if(!getPreferences().getBoolean("stealthMode", false)){
                getPreferences().putBoolean("stealthMode", true);
                logger.info("Stealth mode enabled");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        showInfo("Stealth mode enabled");
                        getWindow().hide();
                        getStealthWindow().show();
                }
                });
            }else{
                getPreferences().putBoolean("stealthMode", false);
                logger.info("Stealth mode disabled");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        getWindow().show();
                        getWindow().toFront();
                        getWindow().requestFocus();
                        showInfo("Stealth mode disabled");
                        getStealthWindow().hide();
                    }
                });
            }
        });

        closeProgram.addActionListener( e->
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        closeProgram();
                    }
                })
        );

        popup.add(toggleStealth);
        popup.add(closeProgram);

        TrayIcon icon = new TrayIcon(image, "Text2QR", popup);

        try {
            SystemTray.getSystemTray().add(icon);
        }
        catch (AWTException awte) {
            logger.error("Error setting system tray icon on "+ System.getProperty("os.name") + "\n" + awte);
        }

    }

    /**
     * Close the program properly
     */
    public void closeProgram(){
        logger.info("Application closed");
        window.close();
        preferences.putBoolean("stealthMode", false);
        stealthWindow.close();
        Platform.exit();
        System.exit(0);
    }

    /**
     * Show connection dialog
     * @param type
     */
    public void showConnectionDialog(Alert.AlertType type){
        Platform.runLater(() ->{
            Alert alert = new Alert(type);
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStyleClass().addAll("alertDialog");
            dialogPane.getStylesheets().add(theme);
            if (type.equals(Alert.AlertType.ERROR)){
                alert.setTitle("Error Dialog");
                alert.setHeaderText("Webservice is not available");
                alert.setContentText("Webservice is not available at " + PropertyService.getServerUrl());
                alert.showAndWait();
            }else if (type.equals(Alert.AlertType.INFORMATION)){
                alert.setTitle("Information Dialog");
                alert.setHeaderText("Webservice available");
                alert.setContentText("Webservice is available at "+PropertyService.getServerUrl());
                alert.showAndWait();
            }
        });
    }

    /**
     * Show language not supported dialog
     */
    public void showLanguageNotSupportedDialog(){
        Platform.runLater(() ->{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStyleClass().addAll("alertDialog");
            dialogPane.getStylesheets().add(theme);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("Language is not supported by this translation provider");
            alert.setContentText("Langauge is not supported by this translation provider. Please try another one");
            alert.showAndWait();
        });
    }

    /* Show language not supportedDialog
    *
    */
    public void showInternetErrorDialog(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().addAll("alertDialog");
        dialogPane.getStylesheets().add(theme);
        alert.setTitle("Error Dialog");
        alert.setHeaderText("Connection Error");
        alert.setContentText("It seems that you do not have a internet connection. Please check this!");
        alert.showAndWait();
    }

    /**
     * Show error notification
     * @param message
     */
    public void showError(String message){
        Platform.runLater(() -> Notifications.create().
                title("Error")
                .text(message)
                .showError()
        );
    }

    /**
     * Show warning notification
     * @param message
     */
    public void showWarning(String message){
        Platform.runLater(() -> Notifications.create().
                title("Warning")
                .text(message)
                .showWarning()
        );
    }

    /**
     * Show success notification
     * @param message
     */
    public void showSuccess(String message){
        Platform.runLater(() -> Notifications.create().
                title("Success")
                .text(message)
                .showInformation()
        );
    }

    /**
     * Show info notification
     * @param message
     */
    public void showInfo(String message){
        Platform.runLater(() -> Notifications.create().
                title("Info")
                .text(message)
                .showInformation()
        );
    }

    /**
     * Detect the language of a string using google's detector api
     * @param text
     * @return String
     * @throws LangDetectException
     */
    public String detect(String text) throws LangDetectException {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.detect();
    }

    /**
     * Get the extension of a file
     * @param fileName
     * @return String
     */
    public String getFileExtension(String fileName) {
        int i = fileName.lastIndexOf(".");
        if (i > 0) {
            return fileName.substring(i + 1);
        }
        return null;
    }

    /**
     * Workaround to get google langdetect work in jar file.
     * @throws IOException
     */
    private static void loadDetector() throws IOException {
        List<String> languages = new ArrayList<>();

        for(String languageCode: defaultLanguages){
            languages.add(loadLanguageProfile(languageCode));
        }
        try {
            DetectorFactory.loadProfile(languages);
        } catch (LangDetectException ex) {
            logger.error("Could not load language profiles for google langdetect " + "\n"+  ex);
        }
    }

    /**
     * Load a json language file from the classpath of the google language detector class
     * @param langCode
     * @return String
     * @throws IOException
     */
    private static String loadLanguageProfile(String langCode) throws IOException {
        InputStream is=DetectorFactory.class.getClassLoader().getResourceAsStream("profiles/" + langCode);
        String profile=new String(IOUtils.toByteArray(is));
        is.close();
        return profile;
    }

    public boolean userIsLoggedIn(){
        return !getConnectionService().getRestServiceConnection().getUsername().equals("");
    }

    public Stage getWindow(){
        return this.window;
    }

    public Scene getHomeScene(){
        return this.homeScene;
    }

    public HomeLayout getHomeLayout(){
        return this.homeLayout;
    }

    public Scene getPreferencesScene(){
        return this.preferencesScene;
    }

    public PreferencesLayout getPreferencesLayout(){
        return this.preferencesLayout;
    }

    public Scene getLoginScene(){
        return this.loginScene;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public ClipboardManager getClipboardManager() {
        return clipboardManager;
    }

    public void setClipboardManager(ClipboardManager clipboardManager) {
        this.clipboardManager = clipboardManager;
    }

    public ConnectionService getConnectionService() {
        return connectionService;
    }

    public void setConnectionService(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    public Stage getStealthWindow(){
        return this.stealthWindow;
    }

}
