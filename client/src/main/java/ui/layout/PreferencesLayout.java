package ui.layout;

import com.cybozu.labs.langdetect.DetectorFactory;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.controlsfx.control.Notifications;
import ui.MainUi;

import java.util.prefs.Preferences;

/**
 * Created by alexanderweiss on 05.03.16.
 */
public class PreferencesLayout extends BorderPane {


    private MainUi mainUi;
    private Preferences preferences;

    private TextField qrCodeWidth;
    private TextField qrCodeHeight;
    private CheckBox automaticCopyToClipboard;

    private ComboBox<String> translationProvider;

    private Button stealthMode;
    private ComboBox<String> sourceLangComboBox;
    private ComboBox<String> targetLangComboBox;

    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(PreferencesLayout.class);

    /**
     * Default constructor
     * @param mainUi
     */
    public PreferencesLayout(MainUi mainUi){
        this.mainUi = mainUi;

        init();
    }

    /**
     * Init ui
     */
    private void init(){
        this.setTop(new QRMenu(this.mainUi));
        this.preferences = mainUi.getPreferences();
        VBox qrcodeoptions = qrCodeOptions();
        this.setCenter(qrcodeoptions);
        setMargin(qrcodeoptions,new Insets(12,12,12,12) );
    }

    /**
     * Create the qrcode options panel
     * @return VBox
     */
    private VBox qrCodeOptions(){
        VBox qrCodeOptions = new VBox(15);
        qrCodeOptions.getChildren().addAll(qrCodeOptionsOne(),qrCodeOptionsTwo(),stealthModePanel());
        return qrCodeOptions;
    }

    /**
     * Create the qrcode options panel part 1
     * @return VBox
     */
    private VBox qrCodeOptionsOne(){
        VBox qrCodeOptionsOne = new VBox(5);

        qrCodeWidth = new TextField(preferences.get("qrcodewidth","250"));
        qrCodeWidth.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (!newPropertyValue)//has lost focus
            {
                logger.info("New preference - QR-Code width:" + qrCodeWidth.getText());
                preferences.put("qrcodewidth",qrCodeWidth.getText());
            }
        });

        qrCodeHeight = new TextField(preferences.get("qrcodeheight","250"));
        qrCodeHeight.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (!newPropertyValue)//has lost focus
            {
                logger.info("New preference - QR-Code height:" + qrCodeHeight.getText());
                preferences.put("qrcodeheight",qrCodeHeight.getText());
            }
        });


        automaticCopyToClipboard = new CheckBox("Automatically add QR Code to clipboard?");
        automaticCopyToClipboard.setSelected(preferences.getBoolean("automaticCopyToClipboard",false));
        automaticCopyToClipboard.selectedProperty().addListener((observable, oldValue, newValue) -> {
            logger.info("New preference - Automatic copy to clipboard:" + newValue);
            preferences.putBoolean("automaticCopyToClipboard", newValue);
        });

        Label qrCodeLabel = new Label("QRCode options");
        qrCodeLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

        qrCodeOptionsOne.getChildren().addAll(qrCodeLabel,new Label("QR Code width: "),qrCodeWidth, new Label("QR code height: "), qrCodeHeight,automaticCopyToClipboard);
        return qrCodeOptionsOne;
    }

    /**
     * Create the qrcode options panel part 2
     * @return
     */
    private VBox qrCodeOptionsTwo(){
        VBox qrCodeOptionsTow = new VBox(5);


        ObservableList<String> translation_provider =
                FXCollections.observableArrayList(
                        "Bing Translation API",
                        "Google Translation API",
                        "mymemory.translated.net"
                );
        translationProvider = new ComboBox<>(translation_provider);

        translationProvider.valueProperty().addListener((ov, oldValue, newValue) -> {
            preferences.put("translationProvider", newValue);
            logger.info("New preference - Translationprovider:" + newValue);
        });

        translationProvider.getSelectionModel().select(preferences.get("translationProvider", "Bing Translation API"));

        Label languageLabel = new Label("Langauage  options");
        languageLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

        qrCodeOptionsTow.getChildren().addAll(languageLabel,new Label("Translation provider: "),translationProvider);
        return qrCodeOptionsTow;
    }


    /**
     * Generate the preferences panel
     * @return VBox
     */
    private VBox stealthModePanel(){
        VBox preferenceView = new VBox(5);
        stealthMode = new Button("Activate stealth mode");
        stealthMode.setOnAction(event -> {
            mainUi.getPreferences().putBoolean("stealthMode", true);
            mainUi.showInfo("Stealth mode activated");
            mainUi.getWindow().hide();
            mainUi.getStealthWindow().show();
        });

        ObservableList<String> language_codes =
                FXCollections.observableArrayList(
                        DetectorFactory.getLangList()
                );
        sourceLangComboBox = new ComboBox<>(language_codes);
        sourceLangComboBox.getSelectionModel().select(mainUi.getPreferences().get("defaultSourceLanguage", "af"));

        sourceLangComboBox.valueProperty().addListener((ov, t, newVal) -> {
            mainUi.getPreferences().put("defaultSourceLanguage", newVal);
            logger.info("New preference - Default source language:" + newVal);
        });

        targetLangComboBox = new ComboBox<>(language_codes);
        targetLangComboBox.getSelectionModel().select(mainUi.getPreferences().get("defaultTargetLanguage", "af"));
        targetLangComboBox.valueProperty().addListener((ov, t, newVal) -> {
            mainUi.getPreferences().put("defaultTargetLanguage", newVal);
            logger.info("New preference - Default target language:" + newVal);
        });


        Label stealthModeLabel = new Label("Stealth mode options");
        stealthModeLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

        preferenceView.getChildren().addAll(stealthModeLabel,stealthMode, new Label("Default source language:"),sourceLangComboBox, new Label("Default target language:"),targetLangComboBox);

        return preferenceView;
    }


    public ComboBox<String> getTranslationProvider(){
        return this.translationProvider;
    }

    public CheckBox getAutomaticCopyToClipboard(){
        return this.automaticCopyToClipboard;
    }

    public TextField getQrCodeWidth(){
        return this.qrCodeWidth;
    }

    public TextField getQrCodeHeight(){
        return this.qrCodeHeight;
    }
}
