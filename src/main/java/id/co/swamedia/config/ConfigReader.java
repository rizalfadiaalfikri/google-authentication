package id.co.swamedia.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.yaml.snakeyaml.Yaml;

/**
 * Utility class for reading configuration files.
 */
public class ConfigReader {

    /**
     * Reads the client ID from the application.properties file.
     *
     * @param filename The name of the properties file.
     * @return The client ID or null if an error occurs.
     */
    public static String getClientIdFromProperties(String filename) {
        Properties properties = new Properties();
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(filename)) {
            if (input == null) {
                throw new IllegalArgumentException("Sorry, unable to find: " + filename);
            }
            properties.load(input);
            return properties.getProperty("google.client-id");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Reads the client ID from the application.yml file.
     *
     * @param filename The name of the YAML file.
     * @return The client ID or null if an error occurs.
     */
    public static String getClientIdFromYaml(String filename) {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = ConfigReader.class.getClassLoader().getResourceAsStream(filename)) {
            Map<String, Object> config = yaml.load(inputStream);
            Object googleObj = config.get("google");
            if (googleObj instanceof Map) {
                Map<?, ?> google = (Map<?, ?>) googleObj;
                Object clientId = google.get("client-id");
                return clientId != null ? clientId.toString() : null;
            } else {
                throw new IllegalArgumentException("'google' section not found or is not a map in " + filename);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
