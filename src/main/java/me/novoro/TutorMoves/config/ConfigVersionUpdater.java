package me.novoro.TutorMoves.config;

import me.novoro.TutorMoves.TutorMoves;
import me.novoro.TutorMoves.util.TutorMovesLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigVersionUpdater {

    private final Configuration mainConfig;
    private final Configuration langConfig;
    private final String currentVersion;

    public ConfigVersionUpdater(Configuration mainConfig, Configuration langConfig, String currentVersion) {
        this.mainConfig = mainConfig;
        this.langConfig = langConfig;
        this.currentVersion = currentVersion;
    }

    public void updateConfig() {
        updateConfigFile(mainConfig, "config.yml", true);
        updateConfigFile(langConfig, "lang.yml", false);
        updateTutorConfigs();
    }

    private void updateConfigFile(Configuration config, String fileName, boolean addOverrides) {
        String configVersion = config.getString("Config-Version", "1.0.0");

        // Only update if the current version is older than the new version
        if (isNewerVersion(configVersion, currentVersion)) {
            TutorMovesLogger.info("Updating " + fileName + " from version " + configVersion + " to " + currentVersion);
            config.set("Config-Version", currentVersion);

            if (addOverrides) {
                addMoveOverrides(config, fileName);
            }

            saveConfigPreservingComments(new File(TutorMoves.getConfigFolder(), fileName), currentVersion);
        } else {
            TutorMovesLogger.info(fileName + " is already up to date (version " + configVersion + "). No update needed.");
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

                        if (isNewerVersion(tutorVersion, currentVersion)) {
                            TutorMovesLogger.info("Updating " + tutorFile.getName() + " from version " + tutorVersion + " to " + currentVersion);
                            tutorConfig.set("Config-Version", currentVersion);
                            addSpecificTutorMoveOverrides(tutorConfig, tutorFile.getName());
                            saveConfigPreservingComments(tutorFile, currentVersion);
                        } else {
                            TutorMovesLogger.info(tutorFile.getName() + " is already up to date. No update needed.");
                        }
                    } catch (IOException e) {
                        TutorMovesLogger.error("Failed to update tutor config: " + tutorFile.getName());
                        TutorMovesLogger.printStackTrace(e);
                    }
                }
            }
        }
    }

    private boolean isNewerVersion(String currentVersion, String newVersion) {
        String[] currentParts = currentVersion.split("\\.");
        String[] newParts = newVersion.split("\\.");

        int maxLength = Math.max(currentParts.length, newParts.length);

        for (int i = 0; i < maxLength; i++) {
            int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            int newPart = i < newParts.length ? Integer.parseInt(newParts[i]) : 0;

            if (currentPart < newPart) {
                return true; // The new version is greater
            } else if (currentPart > newPart) {
                return false; // The current version is greater, no update needed
            }
        }
        return false; // The versions are the same
    }

    private void addMoveOverrides(Configuration config, String fileName) {
        if (!config.contains("Move-Overrides")) {
            config.set("Move-Overrides.outrage", 6000);
            config.set("Move-Overrides.dracometeor", 10000);

            saveConfigPreservingComments(new File(TutorMoves.getConfigFolder(), fileName), currentVersion);
        }
    }

    private void addSpecificTutorMoveOverrides(Configuration tutorConfig, String fileName) {
        if (!tutorConfig.contains("Move-Overrides")) {
            tutorConfig.set("Move-Overrides.outrage", 7);
            tutorConfig.set("Move-Overrides.dracometeor", 10);

            saveConfigPreservingComments(new File(TutorMoves.getConfigFolder(), "tutors/" + fileName), currentVersion);
        }
    }

    private void saveConfigPreservingComments(File file, String newVersion) {
        try {
            Path filePath = file.toPath();
            String originalContent = Files.readString(filePath, StandardCharsets.UTF_8);
            String updatedContent = originalContent.replaceFirst("(?m)^Config-Version: .*$", "Config-Version: " + newVersion);
            Path tempFilePath = filePath.getParent().resolve(filePath.getFileName() + ".tmp");
            Files.writeString(tempFilePath, updatedContent, StandardCharsets.UTF_8);
            Files.move(tempFilePath, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            TutorMovesLogger.error("Failed to preserve comments while saving config: " + file.getName());
            TutorMovesLogger.printStackTrace(e);
        }
    }
}
