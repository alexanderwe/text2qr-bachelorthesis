package webservice.services;

import java.net.URL;
import java.util.Properties;

/**
 * Created by alexanderweiss on 13.04.16.
 *  Class for mapping the config file
 */
public class PropertyService {


    private static Properties properties = loadProperties();


    /**
     * Get the server url
     */
    public static String getServerUrl(){
        return properties.getProperty("server");
    }

    /**
     * Get the google api key
     */
    public static String getGoogleApiKey(){
        return properties.getProperty("google_api_key");
    }

    /**
     * Get the microsoft client id
     */
    public static String getMicrosoftClientID(){
        return properties.getProperty("microsoft_client_id");
    }

    /**
     * Get the mcirosoft api key
     */
    public static String getMicrosoftApiKey(){
        return properties.getProperty("microsoft_api_key");
    }

    /**
     * Load the properties from the resources
     * @return
     */
    private static Properties loadProperties(){
        Properties properties = null;

        URL resourceUrl = null;
        try {
            resourceUrl = new URL(PropertyService.class.getResource("/configuration.properties").toExternalForm());
            properties = new Properties();
            properties.load(resourceUrl.openStream() );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
