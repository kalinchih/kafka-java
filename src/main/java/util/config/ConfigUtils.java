package util.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigUtils {

    private static final ConfigUtils instance = new ConfigUtils();
    private final Map<String, Properties> configPropertiesCache = new HashMap<>();

    private ConfigUtils() {
    }

    public static ConfigUtils build() {
        return instance;
    }

    public Properties getProperties(String fileName) throws ConfigFileNotFoundException {
        if (configPropertiesCache.containsKey(fileName)) {
            return configPropertiesCache.get(fileName);
        } else {
            Properties configProperties = new Properties();
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
            try {
                configProperties.load(inputStream);
            } catch (IOException | NullPointerException e) {
                throw new ConfigFileNotFoundException("Properties file (" + fileName + ") not found." + fileName, e);
            }
            configPropertiesCache.put(fileName, configProperties);
            return configProperties;
        }
    }

    public String getProperty(Properties properties, String key) throws ConfigNotFoundException {
        String keyValue = null;
        if (properties.containsKey(key)) {
            keyValue = properties.getProperty(key);
        } else {
            throw new ConfigNotFoundException("Property (" + key + ") not found.");
        }
        return keyValue;
    }
}
