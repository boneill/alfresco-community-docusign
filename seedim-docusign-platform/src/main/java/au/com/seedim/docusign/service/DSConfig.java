package au.com.seedim.docusign.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This class consists of static constant configuration value that used by docusign examples
 * this members are set once access any member first time.
 * configuration can be loaded from environment variables or from properties file config.properties.
 * loading order is respectively first trying to load environment variables if not found will try
 * loaf from config.properties file.
 */
public final class DSConfig {

    public static final String CLIENT_ID;
    public static final String IMPERSONATED_USER_GUID;
    public static final String TARGET_ACCOUNT_ID;
    public static final String OAUTH_REDIRECT_URI = "https://www.docusign.com";
    public static final String SIGNER_EMAIL;
    public static final String SIGNER_NAME;
    public static final String CC_EMAIL;
    public static final String CC_NAME;
    public static final String PRIVATE_KEY;
    public static final String AUTHENTICATION_URL = "https://account-d.docusign.com";
    public static final String DS_AUTH_SERVER;
    public static final String API = "restapi/v2";
    public static final String PERMISSION_SCOPES = "signature%20impersonation";
    public static final String JWT_SCOPE = "signature";

    public static final String AUD () {
        if(DS_AUTH_SERVER != null && DS_AUTH_SERVER.startsWith("https://"))
            return DS_AUTH_SERVER.substring(8);
        else if(DS_AUTH_SERVER != null && DS_AUTH_SERVER.startsWith("http://"))
            return DS_AUTH_SERVER.substring(7);

        
        return DS_AUTH_SERVER;
    }

    static {
        // Try load from environment variables
        Map<String, String> config = loadFromEnv();

        if (config == null) {
            // Try load from properties file
            config = loadFromProperties();
        }

        CLIENT_ID = fetchValue(config, "DS_CLIENT_ID");
        IMPERSONATED_USER_GUID = fetchValue(config, "DS_IMPERSONATED_USER_GUID");
        TARGET_ACCOUNT_ID = fetchValue(config, "DS_TARGET_ACCOUNT_ID");
        SIGNER_EMAIL = fetchValue(config, "DS_SIGNER_1_EMAIL");
        SIGNER_NAME = fetchValue(config, "DS_SIGNER_1_NAME");
        CC_EMAIL = fetchValue(config, "DS_CC_1_EMAIL");
        CC_NAME = fetchValue(config, "DS_CC_1_NAME");
        PRIVATE_KEY = fetchValue(config, "DS_PRIVATE_KEY");
        DS_AUTH_SERVER = fetchValue(config, "DS_AUTH_SERVER"); // use account.docusign.com for production
    }

    /**
     * fetch configuration value by key.
     *
     * @param config preloaded configuration key/value map
     * @param name key of value
     * @return value as string or default empty string
     */
    private static String fetchValue(Map<String, String> config, String name) {
        String val = config.get(name);

        if("DS_TARGET_ACCOUNT_ID".equals(name) && "FALSE".equals(val)) {
            return null;
        }

        return ((val != null) ? val : "");
    }


    /**
     * This method check if environment variables exists and load it into Map
     *
     * @return Map of key/value of environment variables if exists otherwise, return null
     */
    private static Map<String, String> loadFromEnv() {
        String clientId = System.getenv("DS_CLIENT_ID");

        if (clientId != null && clientId.length() > 0) {
            return System.getenv();
        }

        return null;
    }

    /**
     * This method load properties located in config.properties file in the working directory.
     *
     * @return Map of key/value of properties
     */
    private static Map<String, String> loadFromProperties() {
        Properties properties = new Properties();
        InputStream input = null;

        try {
            input = DSConfig.class.getResourceAsStream("/config.properties");
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("can not load configuration file", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    throw new RuntimeException("error occurs will closing input stream: ", e);
                }
            }
        }

        Set<Map.Entry<Object, Object>> set = properties.entrySet();
        Map<String, String> mapFromSet = new HashMap<String, String>();

        for (Map.Entry<Object, Object> entry : set) {
            mapFromSet.put((String) entry.getKey(), (String) entry.getValue());
        }

        return mapFromSet;
    }
}
