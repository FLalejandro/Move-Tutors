package TutorMoves.config;

import TutorMoves.TutorMoves;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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
        updateConfigFile(mainConfig, "config.yml", true);
        updateConfigFile(langConfig, "lang.yml", false);
        updateTutorConfigs();
    }

    private void updateConfigFile(Configuration config, String fileName, boolean addOverrides) {
        String configVersion = config.getString("Config-Version", "1.0.0");

        if (!configVersion.equals(currentVersion)) {
            LOGGER.info("Updating " + fileName + " from version " + configVersion + " to " + currentVersion);
            config.set("Config-Version", currentVersion);

            if (addOverrides) {
                addMoveOverrides(config, fileName);
            }

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
                            addSpecificTutorMoveOverrides(tutorConfig, tutorFile.getName());
                            saveConfigPreservingComments(tutorFile, currentVersion);
                        }
                    } catch (IOException e) {
                        LOGGER.error("Failed to update tutor config: " + tutorFile.getName(), e);
                    }
                }
            }
        }
    }

    private void addMoveOverrides(Configuration config, String fileName) {
        if (!config.contains("Move-Overrides")) {
            String overridesSection =
                    "# Override the cost of specific moves\n" +
                            "# Format - move:cost\n" +
                            "Move-Overrides:\n" +
                            "  - outrage: 6000\n" +
                            "  - dracometeor: 10000\n";

            appendToFile(new File(TutorMoves.getConfigFolder(), fileName), overridesSection, false);
        }
    }

    private void addSpecificTutorMoveOverrides(Configuration tutorConfig, String fileName) {
        if (!tutorConfig.contains("Move-Overrides")) {
            String overridesSection =
                    "# Override the cost of specific moves\n" +
                            "# Format - move:cost\n" +
                            "Move-Overrides:\n" +
                            "  - outrage: 7\n" +
                            "  - dracometeor: 10\n";

            appendToFile(new File(TutorMoves.getConfigFolder(), "tutors/" + fileName), overridesSection, true);
        }
    }

    private void saveConfigPreservingComments(File file, String newVersion) {
        try {
            Path filePath = file.toPath();
            String originalContent = Files.readString(filePath, StandardCharsets.UTF_8);
            String updatedContent = originalContent.replaceFirst("Config-Version: \\d+\\.\\d+\\.\\d+", "Config-Version: " + newVersion);
            Files.writeString(filePath, updatedContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Failed to preserve comments while saving config: " + file.getName(), e);
        }
    }

    private void appendToFile(File file, String content, boolean needsNewlineBefore) {
        try {
            String originalContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);

            // Remove excessive empty lines before adding content
            originalContent = originalContent.replaceAll("(?m)^[ \t]*\r?\n", "\n");

            // Ensure there is no extra new line before appending
            if (needsNewlineBefore && !originalContent.endsWith("\n")) {
                content = "\n" + content;
            } else if (!needsNewlineBefore && originalContent.endsWith("\n")) {
                content = content.stripLeading(); // Strip leading newlines if not needed
            }

            Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8), java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.error("Failed to append content to " + file.getName(), e);
        }
    }
}
