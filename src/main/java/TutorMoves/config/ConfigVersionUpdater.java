package TutorMoves.config;

import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ConfigVersionUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigVersionUpdater.class);
    private final Configuration mainConfig;
    private final Configuration langConfig;
    private final String currentVersion;

    public ConfigVersionUpdater(Configuration mainConfig, Configuration langConfig, String currentVersion) {
        this.mainConfig = mainConfig;
        this.langConfig = langConfig;
        this.currentVersion = currentVersion;
    }

    public void updateConfig() {}

    private void mergeConfigs(Configuration target, Configuration source) {
        for (String key : source.getKeys()) {
            if (source.get(key) instanceof Configuration) {
                if (!(target.get(key) instanceof Configuration)) {
                    target.set(key, new Configuration());
                }
                mergeConfigs(target.getSection(key), source.getSection(key));
            } else {
                if (!target.contains(key)) {
                    target.set(key, source.get(key));
                }
            }
        }
    }

    private boolean isOlderVersion(String currentVersion, String targetVersion) {
        int[] currentParts = parseVersion(currentVersion);
        int[] targetParts = parseVersion(targetVersion);

        for (int i = 0; i < currentParts.length; i++) {
            if (currentParts[i] < targetParts[i]) {
                return true;
            } else if (currentParts[i] > targetParts[i]) {
                return false;
            }
        }
        return false;
    }

    private int[] parseVersion(String version) {
        String[] parts = version.split("\\.");
        int[] numbers = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            numbers[i] = Integer.parseInt(parts[i]);
        }
        return numbers;
    }

    private void saveConfigWithComments(Configuration config, String resourcePath, File file) throws IOException {
        // Load the default configuration from the resource file
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        String defaultConfigContent = new String(resourceStream.readAllBytes(), Charsets.UTF_8);

        // Save the configuration to the file
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
            writer.write(defaultConfigContent);
        }
    }
}
