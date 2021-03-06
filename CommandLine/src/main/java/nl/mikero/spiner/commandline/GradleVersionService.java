package nl.mikero.spiner.commandline;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns the current project version set in build.gradle.
 */
public class GradleVersionService implements VersionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GradleVersionService.class);
    private static final String PROPERTIES_FILE = "version.properties";
    private static final String UNKNOWN_VERSION = "UNKNOWN";

    /**
     * Returns the version set in version.properties, which is expanded from build.gradle.
     *
     * @return gradle project version
     */
    @Override
    public final String get() {
        Properties prop = new Properties();

        try(InputStream input = GradleVersionService.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            // load a properties file
            prop.load(input);

            // get the property value and print it out
            return prop.getProperty("version", UNKNOWN_VERSION);
        } catch (IOException e) {
            LOGGER.error("Couldn't read version from version.properties.", e);
            return UNKNOWN_VERSION;
        }
    }
}
