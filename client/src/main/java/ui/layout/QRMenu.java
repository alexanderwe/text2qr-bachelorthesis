package ui.layout;

import connection.RestServiceConnection;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;


import javafx.stage.FileChooser;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import service.PropertyService;
import ui.MainUi;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

/**
 * Created by alexanderweiss
 * Class for the menu which is displayed always on top in the user interface
 */
public class QRMenu extends MenuBar {


    private Menu menuFile;
    private MenuItem readDoc;
    private FileChooser fileChooser;
    private MenuItem logoutItem;
    private MenuItem exitItem;


    private Menu menuWebService;
    private MenuItem checkWebservice;

    private Menu menuViews;
    private MenuItem showHomeViewItem;

    private Menu menuPreferences;
    private MenuItem preferencesItem;

    private Menu menuAccount;

    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(QRMenu.class);

    private MainUi mainUi;

    /**
     * Default constructor
     * @param ui
     */
    public QRMenu(MainUi ui){
        this.mainUi = ui;
        initMenu();
    }

    /**
     * Init the menu
     */
    private void initMenu(){
        menuFile = new Menu("File");
        exitItem = new MenuItem("Exit");
        exitItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        exitItem.setOnAction(e -> this.mainUi.closeProgram());

        readDoc = new MenuItem("Read file");
        readDoc.setOnAction(event -> {
            fileChooser.setTitle("Open file");
            FileChooser.ExtensionFilter msWordExtensionFilter = new FileChooser.ExtensionFilter("Word documents (.doc/.docx)", "*.doc", "*.docx");
            FileChooser.ExtensionFilter txtExtensionFilter = new FileChooser.ExtensionFilter("Text documents (.txt)", "*.txt");
            fileChooser.getExtensionFilters().addAll(msWordExtensionFilter,txtExtensionFilter);
            mainUi.getHomeLayout().getsourceContentTextArea().setText(readDoc(fileChooser.showOpenDialog(mainUi.getWindow())));
            mainUi.getWindow().setScene(mainUi.getHomeScene());
        });

        fileChooser = new FileChooser();

        logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(event -> {
            logout();
            mainUi.getPreferences().putBoolean("stealthMode", false);
        });


        menuFile.getItems().addAll( readDoc,new SeparatorMenuItem(),logoutItem,exitItem);
        menuWebService = new Menu("Webservice");
        checkWebservice = new MenuItem("Check webservice connection");
        checkWebservice.setOnAction(event -> checkWebservice());

        menuWebService.getItems().addAll(checkWebservice);

        menuViews = new Menu("Views");
        showHomeViewItem = new MenuItem("Home view");
        showHomeViewItem.setOnAction(event -> mainUi.getWindow().setScene(mainUi.getHomeScene()));

        menuViews.getItems().addAll(showHomeViewItem);


        menuPreferences = new Menu("Preferences");
        preferencesItem = new MenuItem("Preferences");
        preferencesItem.setOnAction(event -> mainUi.getWindow().setScene(mainUi.getPreferencesScene()));
        menuPreferences.getItems().addAll(preferencesItem);

        this.getMenus().addAll(menuFile, menuWebService, menuViews, menuPreferences);
    }

    /**
     * Check if webservice is up
     */
    private void checkWebservice(){
        try{
            HttpsURLConnection connection = (HttpsURLConnection) new URL(PropertyService.getServerUrl()).openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) { //Webservice is not available
                mainUi.showConnectionDialog(Alert.AlertType.ERROR);
            }else{
               mainUi.showConnectionDialog(Alert.AlertType.INFORMATION);
            }
        } catch (IOException e) {
            mainUi.showConnectionDialog(Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Read a doc file
     * @param file
     * @return String
     */
    private String readDoc(File file){
        StringBuilder documentContent = new StringBuilder();

        if(mainUi.getFileExtension(file.getName()).equals("docx")){
            logger.info("Read docx file");
            try {
                FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
                XWPFDocument document = new XWPFDocument(fileInputStream);
                List<XWPFParagraph> paragraphs = document.getParagraphs();

                for (XWPFParagraph paragraph : paragraphs) {
                    documentContent.append(paragraph.getText());
                    documentContent.append(System.lineSeparator());
                }
                fileInputStream.close();
            } catch (Exception ex) {
                logger.error("Error while reading doc file: " + file + "\n" + ex);
            }
        }else if (mainUi.getFileExtension(file.getName()).equals("doc")){
          logger.info("Read doc file");
          try{
              FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
              HWPFDocument document = new HWPFDocument(fileInputStream);
              WordExtractor wordExtractor = new WordExtractor(document);
              documentContent.append(wordExtractor.getText());
          }catch (Exception ex){
              logger.error("Error while reading doc file:" + file +  "\n" + ex);
          }
        }else if (mainUi.getFileExtension(file.getName()).equals("txt")){
            logger.info("Read txt file");
            FileReader fileReader;
            BufferedReader bufferedReader;
            try {
                fileReader = new FileReader(file);
                bufferedReader = new BufferedReader(fileReader);

                String line;
                line = bufferedReader.readLine();
                while (line != null) {
                    documentContent.append(line);
                    line = bufferedReader.readLine();
                }
                fileReader.close();
            }
            catch (IOException ioex){
                logger.error("Error while reading doc file" + file +  "\n" + ioex);
            }
        }
        return documentContent.toString();
    }

    //reset all inputs
    private void logout(){
        mainUi.getConnectionService().setRestServiceConnection(new RestServiceConnection("",""));
        mainUi.getHomeLayout().resetLayout();
        mainUi.getWindow().setScene(mainUi.getLoginScene());
        logger.info("User logged out. Reset RestServiceConnection.");
    }

    public Menu getMenuAccount(){
        return this.menuAccount;
    }

}
