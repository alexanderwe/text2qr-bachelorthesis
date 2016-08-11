package ui.layout;

import connection.RestServiceConnection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import ui.MainUi;

/**
 * Created by alexanderweiss on 19.03.16.
 */
public class LoginLayout extends GridPane {

    private MainUi mainUi;

    private TextField username;
    private  PasswordField password;
    private Label error;


    private CheckBox rememberMe;
    private Button loginButton;
    private Button quitButton;

    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(LoginLayout.class);


    public LoginLayout(MainUi mainUi){
        this.mainUi = mainUi;
        init();
    }

    /**
     * Init the ui
     */
    private void init(){
        this.setAlignment(Pos.CENTER);
        this.setHgap(10);
        this.setVgap(10);


        username = new TextField();
        username.setPromptText("Username");

        if(mainUi.getPreferences().getBoolean("rememberMe", false)){
            username.setText(mainUi.getPreferences().get("username", "username"));
        }
        username.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER))
            {
                login();
            }
        });

        password = new PasswordField();
        password.setPromptText("Password");
        password.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                login();
            }
        });


        rememberMe = new CheckBox("Remember username");
        rememberMe.setSelected(mainUi.getPreferences().getBoolean("rememberMe", false));
        rememberMe.selectedProperty().addListener((ov, old_val, new_val) -> {

            mainUi.getPreferences().putBoolean("rememberMe", new_val);
            logger.info("New preference - Remember me:" + new_val);
            if(new_val){
               //doNothing
            }else{
                mainUi.getPreferences().remove("username");
            }
        });

        loginButton = new Button("Login");
        loginButton.setOnAction(event -> login());
        quitButton = new Button("Quit");
        quitButton.setOnAction(event -> mainUi.closeProgram());

        error = new Label();

        this.add(new Label("Username:"), 0, 0);
        this.add(username, 1, 0);
        this.add(new Label("Password:"), 0, 1);
        this.add(password, 1, 1);
        this.add(rememberMe ,0,2);
        this.add(error,1,3);
        this.add(quitButton,0,4);
        this.add(loginButton,1,4);
    }


    /**
     * Set the rest service connection: 'login' the user.
     */
    private void login(){
        logger.info("User logs in");
        RestServiceConnection restServiceConnection = new RestServiceConnection(username.getText(), password.getText());
        try {
            if(restServiceConnection.checkUser()==200){
                if(rememberMe.isSelected()){
                    mainUi.getPreferences().put("username", username.getText());
                }
                mainUi.getConnectionService().setRestServiceConnection(restServiceConnection);
                mainUi.getWindow().setScene(mainUi.getHomeScene());

                username.setStyle("");
                password.setStyle("");

                username.setText(mainUi.getPreferences().get("username",""));
                password.setText("");
                error.setStyle("-fx-text-fill: black;");
                error.setText("");
                logger.info("User successfully logged in");

            }else{
                error.setStyle("-fx-text-fill: red;");
                error.setText("User/password is wrong!");
                username.setStyle("-fx-text-box-border: red ;");
                password.setStyle("-fx-text-box-border: red ;");
                mainUi.getPreferences().put("username", username.getText());
                logger.error("Wrong user/password combination");
            }
        } catch (Exception ex) {
            logger.error("Exception when loggin in. No connection to server" + "\n"+  ex);
            error.setStyle("-fx-text-fill: red;");
            error.setText("Something went wrong");
        }

    }

}
