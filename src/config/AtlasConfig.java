package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AtlasConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream in = new FileInputStream("atlas.properties")) {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Impossibile leggere atlas.properties", e);
        }
    }

    public static final String RIOT_ID = "daxs#EUW";

    public static String riotApiKey() {
        return properties.getProperty("riot.api.key");
    }

    public static String riotRegion() {
        return properties.getProperty("riot.region", "europe");
    }

    private AtlasConfig() {}
}
