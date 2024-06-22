package TutorMoves.config;

import TutorMoves.TutorMoves;
import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public void updateConfig() {
        updateConfigFile(mainConfig, "config.yml");
        updateConfigFile(langConfig, "lang.yml");
        updateTutorConfigs();
    }

    private void updateConfigFile(Configuration config, String fileName) {
        String configVersion = config.getString("Config-Version", "1.0.0");

        if (!configVersion.equals(currentVersion)) {
            LOGGER.info("Updating " + fileName + " from version " + configVersion + " to " + currentVersion);
            config.set("Config-Version", currentVersion);
            saveConfigPreservingComments(new File(TutorMoves.getConfigFolder(), fileName), currentVersion);
        }
    }

    private void updateTutorConfigs() {
        File tutorsFolder = new File(TutorMoves.getConfigFolder(), "tutors");
        if (tutorsFolder.exists() && tutorsFolder.isDirectory()) {
            File[] tutorFiles = tutorsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (tutorFiles != null) {
                for (File tutorFile : tutorFiles) {
                    try {
                        Configuration tutorConfig = YamlConfiguration.loadConfiguration(tutorFile);
                        String tutorVersion = tutorConfig.getString("Config-Version", "1.0.0");

                        if (!tutorVersion.equals(currentVersion)) {
                            LOGGER.info("Updating " + tutorFile.getName() + " from version " + tutorVersion + " to " + currentVersion);
                            tutorConfig.set("Config-Version", currentVersion);
                            saveConfigPreservingComments(tutorFile, currentVersion);
                        }
                    } catch (IOException e) {
                        LOGGER.error("Failed to update tutor config: " + tutorFile.getName(), e);
                    }
                }
            }
        }
    }

    private void saveConfigPreservingComments(File file, String newVersion) {
        try {
            // Read the original content
            Path filePath = file.toPath();
            String originalContent = Files.readString(filePath, StandardCharsets.UTF_8);

            // Update the version manually in the original content
            String updatedContent = originalContent.replaceFirst("Config-Version: \\d+\\.\\d+\\.\\d+", "Config-Version: " + newVersion);

            // Save the updated content back to the file
            Files.writeString(filePath, updatedContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Failed to preserve comments while saving config: " + file.getName(), e);
        }
    }
}
