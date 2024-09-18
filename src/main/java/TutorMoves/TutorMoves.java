package TutorMoves;

import TutorMoves.commands.TutorCommands;
import TutorMoves.config.ConfigVersionUpdater;
import TutorMoves.config.Configuration;
import TutorMoves.config.YamlConfiguration;
import TutorMoves.events.EntityInteractEvent;
import TutorMoves.util.LangManager;
import TutorMoves.npc.NPCUtil;
import TutorMoves.util.PermissionHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TutorMoves implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("TutorMoves");
    public static PermissionHelper perms = null;
    private static Configuration mainConfig;
    private static Configuration langConfig;
    private boolean isImpactorAvailable;

    public static Map<UUID, String> npcModePlayers = new HashMap<>();
    public static Map<UUID, String> npcEntities = new HashMap<>();

    @Override
    public void onInitialize() {
        // novoro signature ;)
        displayAsciiArt();

        // Initialize configuration
        this.configManager();

        // Check if Impactor API is available
        checkImpactorDependency();

        // Register all the commands available in the mod.
        registerCommands();

        // Execute tasks and listeners that should run when the server starts. (For LuckPerms)
        registerServerStartListeners();

        UseEntityCallback.EVENT.register(new EntityInteractEvent());
    }

    public static Configuration getMainConfig() {
        return mainConfig;
    }

    public static Configuration getLangConfig() {
        return langConfig;
    }

    /**
     * Register all commands provided by the mod.
     */
    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TutorCommands.register(dispatcher);
        });
    }

    /**
     * Initialize and setup permissions using the LuckPerms API.
     * This method ensures the permissions system is active and running.
     */
    private void setupPermissions() {
        try {
            LuckPermsProvider.get();
            // Attempt to get an instance of LuckPermsProvider, signaling that permissions have been set up.
            perms = new PermissionHelper();
            LOGGER.info("Permissions system initialized!");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize permissions system!", e);
        }
    }

    /**
     * Register listeners that should be executed when the server starts.
     */
    private void registerServerStartListeners() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            setupPermissions();
            NPCUtil.loadNPCEntities();
        });
    }

    public void configManager() {
        mainConfig = getConfig("config.yml");
        langConfig = getConfig("lang.yml");
        ConfigVersionUpdater updater = new ConfigVersionUpdater(mainConfig, langConfig, "1.4.0");
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

    public static File getConfigFolder() {
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
        if (tutorsFolder.isDirectory() && tutorsFolder.list().length == 0) {
            File defaultTutorFile = new File(tutorsFolder, "specifictutor.yml");
            if (!defaultTutorFile.exists()) {
                try (FileOutputStream outputStream = new FileOutputStream(defaultTutorFile)) {
                    Path path = Paths.get("tutormoves", "tutors", "specifictutor.yml");
                    InputStream in = getClass().getClassLoader().getResourceAsStream(path.toString().replace("\\", "/"));
                    if (in == null) {
                        throw new RuntimeException("specifictutor.yml resource not found");
                    }
                    in.transferTo(outputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void reloadConfigurations() {
        try {
            mainConfig = YamlConfiguration.loadConfiguration(new TutorMoves().getOrCreateConfigurationFile("config.yml"));
            langConfig = YamlConfiguration.loadConfiguration(new TutorMoves().getOrCreateConfigurationFile("lang.yml"));
            LangManager.loadConfig(langConfig);

            File tutorsFolder = new File(new TutorMoves().getConfigFolder(), "tutors");
            if (tutorsFolder.exists() && tutorsFolder.isDirectory()) {
                for (File file : tutorsFolder.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".yml")) {
                        YamlConfiguration.loadConfiguration(file);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays an ASCII Art representation of the mod's name in the log.
     */
    private void displayAsciiArt() {
        LOGGER.info(" _____      _             __  __                     ");
        LOGGER.info("|_   _|   _| |_ ___  _ __|  \\/  | _____   _____  ___ ");
        LOGGER.info("  | || | | | __/ _ \\| '__| |\\/| |/ _ \\ \\ / / _ \\/ __|");
        LOGGER.info("  | || |_| | || (_) | |  | |  | | (_) \\ V /  __/\\__ \\");
        LOGGER.info("  |_| \\__,_|\\__\\___/|_|  |_|  |_|\\___/ \\_/ \\___||___/");
    }

    private void checkImpactorDependency() {
        isImpactorAvailable = FabricLoader.getInstance().isModLoaded("impactor");
        if (isImpactorAvailable) {
            LOGGER.info("Impactor API is available, enabling Impactor-specific features.");
        } else {
            LOGGER.warn("Impactor API is not available, disabling Impactor-specific features.");
        }
    }

    public static void saveNPCEntities() {
        NPCUtil.saveNPCEntities();
    }
}
