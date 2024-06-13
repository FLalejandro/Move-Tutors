package TutorMoves;

import TutorMoves.commands.TutorCommands;
import TutorMoves.config.ConfigVersionUpdater;
import TutorMoves.config.Configuration;
import TutorMoves.config.YamlConfiguration;
import TutorMoves.util.LangManager;
import TutorMoves.util.PermissionHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPermsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TutorMoves implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("TutorMoves");
    public static PermissionHelper perms = null;
    private Configuration mainConfig;
    private Configuration langConfig;

    @Override
    public void onInitialize() {
        LOGGER.info("TutorMoves Loaded!");

        // Initialize configuration
        this.configManager();

        // Register all the commands available in the mod.
        registerCommands();

        // Setup LuckPerms permissions
        setupPermissions();
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TutorCommands.register(dispatcher);
        });
    }

    private void setupPermissions() {
        try {
            LuckPermsProvider.get();
            perms = new PermissionHelper();
            LOGGER.info("Permissions system initialized!");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize permissions system!", e);
        }
    }

    public void configManager() {
        mainConfig = getConfig("config.yml");
        langConfig = getConfig("lang.yml");
        ConfigVersionUpdater updater = new ConfigVersionUpdater(mainConfig, langConfig, "1.0.0");
        updater.updateConfig();
        LangManager.loadConfig(langConfig);

        // Ensure the tutors folder and default tutor file exist
        ensureDefaultTutorFiles();
    }

    public File getOrCreateConfigurationFile(String fileName) throws IOException {
        File configFolder = getConfigFolder();
        File configFile = new File(configFolder, fileName);

        if (!configFile.exists()) {
            try (FileOutputStream outputStream = new FileOutputStream(configFile)) {
                Path path = Paths.get("tutormoves", fileName);
                InputStream in = getClass().getClassLoader().getResourceAsStream(path.toString().replace("\\", "/"));
                if (in == null) {
                    throw new RuntimeException(fileName + " resource not found");
                }
                in.transferTo(outputStream);
            }
        }
        return configFile;
    }

    public File getConfigFolder() {
        File configFolder = FabricLoader.getInstance().getConfigDir().resolve("TutorMoves").toFile();
        if (!configFolder.exists()) configFolder.mkdirs();
        return configFolder;
    }

    public Configuration getConfig(String fileName) {
        Configuration config = null;
        try {
            config = YamlConfiguration.loadConfiguration(getOrCreateConfigurationFile(fileName));
        } catch(IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public void saveConfig(File file, Configuration config) {
        try {
            YamlConfiguration.save(config, file);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureDefaultTutorFiles() {
        File tutorsFolder = new File(getConfigFolder(), "tutors");
        if (!tutorsFolder.exists()) {
            tutorsFolder.mkdirs();
        }
        // Check if the tutors folder is empty
        if (tutorsFolder.isDirectory() && tutorsFolder.list().length == 0) {
            File defaultTutorFile = new File(tutorsFolder, "dragonmaster.yml");
            if (!defaultTutorFile.exists()) {
                try (FileOutputStream outputStream = new FileOutputStream(defaultTutorFile)) {
                    Path path = Paths.get("tutormoves", "tutors", "dragonmaster.yml");
                    InputStream in = getClass().getClassLoader().getResourceAsStream(path.toString().replace("\\", "/"));
                    if (in == null) {
                        throw new RuntimeException("dragonmaster.yml resource not found");
                    }
                    in.transferTo(outputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
