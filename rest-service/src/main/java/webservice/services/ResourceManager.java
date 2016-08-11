package webservice.services;

import org.apache.poi.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by alexanderweiss on 22.04.16.
 * Class for managing some resource loadings
 */
public class ResourceManager {


    /**
     * Load the language codes from resources
     * @return
     */
    public static String loadLanguageCodes(){
        try {
            return loadResource("language_codes.txt");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Load the register email template from resources
     * @return
     */
    public static String loadRegisterEmailTemplate(){
        try {
            return loadResource("email-templates/activate_account_email.html");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Load the password reset email template from resources
     * @return
     */
    public static String loadPasswordResetEmailTemplate(){
        try {
            return loadResource("email-templates/reset_password_email.html");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Load the password changed email template from resources
     * @return
     */
    public static String loadPasswordChangedEmailTemplate(){
        try {
            return loadResource("email-templates/password_changed_email.html");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Load resource as string
     * @param path
     * @return String
     * @throws IOException
     */
    private static String loadResource(String path) throws IOException {
        InputStream is = ResourceManager.class.getClassLoader().getResourceAsStream(path);
        String resource = new String(IOUtils.toByteArray(is));
        is.close();
        return resource;
    }



}
