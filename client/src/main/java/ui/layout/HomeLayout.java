package ui.layout;

import clipboard.ClipboardManager;
import connection.*;
import encryption.Encryptor;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.Translation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import qrcode.QRCodeManager;
import service.PropertyService;
import ui.MainUi;
import service.TranslationGenerator;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.*;


/**
 * Created by alexanderweiss
 */
public class HomeLayout extends BorderPane {

    private MainUi mainUi;
    private QRMenu menu;
    private ClipboardManager clipboardManager;
    private ConnectionService connectionService;

    private TextArea sourceContentTextArea;
    private TextArea targetContentTextArea;


    private Button generateTranslationButton;
    private Button generateQR;
    private ImageView qrCodeViewer;
    private Button copyToClipboard;
    private Label qrCodeCopied;
    private Label sendToTranslationService;

    private CheckBox autoDetectLanguage;
    private ComboBox sourceLangComboBox;
    private ComboBox targetLangComboBox;

    private ProgressIndicator progressIndicator;

    private Translation translationToSend; // the translation we want to send to our webservice. Only send when the user is clear that he really want to post the translation.
    private TranslationGenerator translationGenerator;

    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(HomeLayout.class);

    VBox leftPanel;
    VBox rightPanel;
    HBox bottomPanel;


    /**
     * Default constructor
     * @param ui
     */
    public HomeLayout(MainUi ui ){
        this.mainUi = ui;
        this.clipboardManager = mainUi.getClipboardManager();
        this.connectionService = mainUi.getConnectionService();
        this.translationGenerator = new TranslationGenerator(connectionService);
        init();
    }

    /**
     * Build the home layout panel
     */
    private void init(){
        this.menu = new QRMenu(this.mainUi);
        this.setTop(this.menu);

        leftPanel = initLeftPanel();
        this.setLeft(leftPanel);
        this.setMargin(leftPanel, new Insets(12,12,12,12));
        this.setCenter(null);

        rightPanel = initRightPanel();
        this.setRight(rightPanel);
        this.setMargin(rightPanel, new Insets(12,12,12,12));

        bottomPanel = initBottomPanel();
        this.setBottom(bottomPanel);
        this.setMargin(bottomPanel, new Insets(12,12,12,12));
    }

    /**
     * Create the left panel
     * @return
     */
    private VBox initLeftPanel(){
        VBox leftPanel = new VBox(5);
        sourceContentTextArea = new TextArea();
        sourceContentTextArea.setWrapText(true);
        sourceContentTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if(autoDetectLanguage.isSelected()){
                try{
                    sourceLangComboBox.getSelectionModel().select(mainUi.detect(sourceContentTextArea.getText()));
                }catch(Exception ex){
                    logger.error("No text in source text content area");
                }
            }else {
                //doNothing
            }

        });

        targetContentTextArea = new TextArea();
        targetContentTextArea.setWrapText(true);
        targetContentTextArea.setEditable(false);

        Label sourceContentLabel = new Label("Source content:");
        sourceContentLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

        Label languageOptionsLabel = new Label("Language options:");
        languageOptionsLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

        Label qrCodeOptionsLabel =  new Label("QR Code options:");
        qrCodeOptionsLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

        Label targetContentLabel = new Label("Target content:");
        targetContentLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

        leftPanel.getChildren().addAll(sourceContentLabel,sourceContentTextArea, languageOptionsLabel,languageOptions(),targetContentLabel,targetContentTextArea);
        return leftPanel;
    }

    /**
     * Create the right panel
     * @return
     */
    private VBox initRightPanel(){
        VBox rightPanel = new VBox();
        qrCodeViewer = new ImageView();
        qrCodeViewer.setFitHeight(325);
        qrCodeViewer.setFitWidth(325);

        copyToClipboard = new Button("Copy QR-Code to clipboard");

        copyToClipboard.setOnAction(event -> {
            clipboardManager.putImageToClipboard(qrCodeViewer.getImage());
            Text t = new Text();
            t.setText("Copied to clipboard");
            qrCodeCopied.setText(t.getText());
            qrCodeCopied.setTextFill(Color.web("#4CAF50"));
            logger.info("QR-Code copied to clipboard, with button click on homelayout");
        });

        Label yourQRCodeLabel = new Label("Your QR Code:");
        yourQRCodeLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

        qrCodeCopied = new Label();
        sendToTranslationService = new Label();

        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.getChildren().addAll(yourQRCodeLabel,qrCodeViewer,copyToClipboard, qrCodeCopied,sendToTranslationService);
        return rightPanel;
    }

    /**
     * Create the bottom panel
     * @return
     */
    private HBox initBottomPanel(){
        HBox bottomPanel = new HBox(5);
        generateQR = new Button("Generate QR Code");
        generateQR.setOnAction(event -> {
            logger.info("Sending translation to webservice");
            generateQRCode(translationToSend);
        });

        generateQR.setDisable(true);


        generateTranslationButton = new Button("Generate translation");
        generateTranslationButton.setOnAction( event -> {
            logger.info("Generate translation");
            Task<Void> task = new Task<Void>() {  // used for updating the ui while translating
                @Override
                public Void call() throws Exception {
                    generateTranslation();
                    return null ;
                }
            };
            new Thread(task).start();
        });



        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);

        bottomPanel.getChildren().addAll(generateQR, generateTranslationButton, progressIndicator);
        return bottomPanel;
    }

    /**
     * Create the language options panel
     * @return
     */
    private HBox languageOptions(){
        HBox languageOptions = new HBox(5);
        ObservableList<String> language_codes =
                FXCollections.observableArrayList(
                      DetectorFactory.getLangList()
                );
        sourceLangComboBox = new ComboBox(language_codes);
        targetLangComboBox = new ComboBox(language_codes);

        autoDetectLanguage = new CheckBox();
        autoDetectLanguage.setSelected(mainUi.getPreferences().getBoolean("autoDetectLang",false));
        autoDetectLanguage.selectedProperty().addListener((ov, old_val, new_val) -> {
            mainUi.getPreferences().putBoolean("autoDetectLang", new_val);
            if(new_val){
                try {
                    sourceLangComboBox.getSelectionModel().select(mainUi.detect(sourceContentTextArea.getText()));
                } catch (LangDetectException e) {
                    e.printStackTrace();
                }
            }

        });

        languageOptions.getChildren().addAll(new Label("From Language: "),sourceLangComboBox, new Label("To language: "),targetLangComboBox, new Label("Auto detect language: "),autoDetectLanguage);
        return languageOptions;
    }

    /**
     * 1. Check if source content is empty
     * 2. Check if source and target language is the same
     * 3. Generate translation with sepecific translation provider
     * 4. Display translation on ui
     * 5. make translation to send ready, when the user want to generate a QR-Code
     */
    private void generateTranslation(){
        startLoadingIndicator();
        try{
           if (sourceContentTextArea.getText().isEmpty()){
               targetContentTextArea.setStyle("-fx-background-color: red;");
               targetContentTextArea.setText("ERROR:Source textfield is empty!");
               logger.error("Source textfield is empty!");
               stopLoadingIndicator();
           }else {
               targetContentTextArea.setStyle("-fx-background-color: grey;");
               if (sourceLangComboBox.getSelectionModel().getSelectedItem().equals(targetLangComboBox.getSelectionModel().getSelectedItem())) {
                   targetContentTextArea.setStyle("-fx-background-color: red;");
                   targetContentTextArea.setText("ERROR:Source and target language is the same!");
                   logger.error("Same source and target language");
                   stopLoadingIndicator();
               } else {
                   HashMap<String, String> parameters = new HashMap<>();
                   parameters.put("translationProvider", mainUi.getPreferences().get("translationProvider", "Bing Translation API"));
                   parameters.put("sourceLang", (String) sourceLangComboBox.getSelectionModel().getSelectedItem());
                   parameters.put("targetLang", (String) targetLangComboBox.getSelectionModel().getSelectedItem());
                   parameters.put("sourceContent",  sourceContentTextArea.getText().trim());

                   String targetContent = translationGenerator.generateTranslation(parameters);

                   targetContentTextArea.setText(targetContent);
                   translationToSend = new Translation();
                   translationToSend.setSourceContent(sourceContentTextArea.getText());
                   translationToSend.setTargetContent(targetContent);
                   translationToSend.setSourceLang((String) sourceLangComboBox.getSelectionModel().getSelectedItem());
                   translationToSend.setTargetLang((String) targetLangComboBox.getSelectionModel().getSelectedItem());
                   generateQR.setDisable(false);
                   targetContentTextArea.setEditable(true);
                   stopLoadingIndicator();
               }
           }
        }catch(java.net.UnknownHostException ex){
            logger.error("No internet connection or unkown url " + "\n" + ex);
            mainUi.showInternetErrorDialog();
            stopLoadingIndicator();
        }catch(IOException ex){
            logger.error("Exception when generating translation " + "\n" + ex);
            mainUi.showLanguageNotSupportedDialog();
            stopLoadingIndicator();
        }
    }

    private void startLoadingIndicator() {
        progressIndicator.setVisible(true);
    }

    private void stopLoadingIndicator() {
        progressIndicator.setVisible(false);
    }

    /**
     * Send trnaslation to webservice
     * 1. Check if target content has changed
     * 2. Encrypt translation
     * 3. Send translation to web service
     * 4. Create QR-Code with information from web service response
     * 5. Display QR-Code
     * @param translation
     */
    private void generateQRCode(Translation translation){
        try {
            startLoadingIndicator();
            Encryptor encryptor = new Encryptor();
            SecretKey secretKey = Encryptor.generateKey();
            String secretKeyHexRepresentation = Encryptor.getHex(secretKey.getEncoded());

            String plain_translationText = translation.getTargetContent();

            translation.setSourceContent(encryptor.encrypt(translation.getSourceContent(),secretKey));

            //When user changed toText
            if(!(translation.getTargetContent().equals(targetContentTextArea.getText()))){
                translation.setTargetContent(encryptor.encrypt(targetContentTextArea.getText(),secretKey));
            }else{
                translation.setTargetContent(encryptor.encrypt(translation.getTargetContent(),secretKey));
            }
            Translation postedTranslation = connectionService.postTranslation(translation); // Send translation

            logger.info(PropertyService.getServerUrl()+"?id="+postedTranslation.getId()+"&key="+secretKeyHexRepresentation+"&iv="+encryptor.getIVHex());
            qrCodeViewer.setImage(SwingFXUtils.toFXImage(QRCodeManager.generateQRCode(PropertyService.getServerUrl()+"?id="+postedTranslation.getId()+"&key="+secretKeyHexRepresentation+"&iv="+encryptor.getIVHex(), Integer.parseInt(mainUi.getPreferencesLayout().getQrCodeWidth().getText()), Integer.parseInt(mainUi.getPreferencesLayout().getQrCodeHeight().getText())),null));

            if(mainUi.getPreferencesLayout().getAutomaticCopyToClipboard().isSelected()){
                clipboardManager.putImageToClipboard(qrCodeViewer.getImage());
                qrCodeCopied.setText("Copied to clipboard");
                qrCodeCopied.setTextFill(Color.web("#4CAF50"));
                targetContentTextArea.setStyle("-fx-background-color: grey;");
            }else {
                targetContentTextArea.setStyle("-fx-background-color: grey;");
                qrCodeCopied.setText("");
            }
            generateQR.setDisable(true);
            targetContentTextArea.setText("");
            targetContentTextArea.setEditable(false);
            sourceContentTextArea.setText("");
            stopLoadingIndicator();
        } catch (Exception ex) {
            logger.error("Exception when sending translation to webservice " + "\n" + ex);
            stopLoadingIndicator();
        }
    }

    /**
     * Reset all changed inputs and labels
     */
    public void resetLayout(){
        sourceContentTextArea.setText("");
        targetContentTextArea.setText("");
        targetContentTextArea.setStyle("-fx-background-color: grey;");
        sourceLangComboBox.getSelectionModel().select("");
        targetLangComboBox.getSelectionModel().select("");
        qrCodeViewer.setImage(null);
        qrCodeCopied.setText("");
        sendToTranslationService.setText("");
        logger.info("Reset homelayout");
    }

    public TextArea getsourceContentTextArea(){
        return this.sourceContentTextArea;
    }

}
