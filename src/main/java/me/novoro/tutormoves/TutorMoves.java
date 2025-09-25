package me.novoro.tutormoves;

import com.mojang.brigadier.CommandDispatcher;
import me.novoro.tutormoves.api.permissions.DefaultPermissionProvider;
import me.novoro.tutormoves.api.permissions.LuckPermsPermissionProvider;
import me.novoro.tutormoves.api.permissions.PermissionProvider;
import me.novoro.tutormoves.api.configuration.Configuration;
import me.novoro.tutormoves.api.configuration.YamlConfiguration;
import me.novoro.tutormoves.commands.MenuCommand;
import me.novoro.tutormoves.commands.NPCCommand;
import me.novoro.tutormoves.commands.TutorMovesReloadCommand;
import me.novoro.tutormoves.config.ConfigManager;
import me.novoro.tutormoves.config.LangManager;
import me.novoro.tutormoves.config.TutorsManager;
import me.novoro.tutormoves.events.EntityInteractEvent;
import me.novoro.tutormoves.npc.NPCUtil;
import me.novoro.tutormoves.utils.TutorMovesLogger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

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
    public static final String MOD_PREFIX = "<gray><bold>[<gradient:#6a3093:#a044ff><bold>TᴜᴛᴏʀMᴏᴠᴇꜱ</gradient><gray><bold>]&f ";
    private static TutorMoves instance;
    private MinecraftServer server;
    private PermissionProvider permissionProvider = null;
    private final LangManager langManager = new LangManager();
    private final ConfigManager configManager = new ConfigManager();
    private final TutorsManager tutorsManager = new TutorsManager();

    public static boolean isCobbleEconomyAvailable = false;
    public static boolean isImpactorAvailable = false;
    public static boolean isPebblesAvailable = false;

    public static Map<UUID, String> npcModePlayers = new HashMap<>();
    public static Map<UUID, String> npcEntities = new HashMap<>();

    @Override
    public void onInitialize() {
        TutorMoves.instance = this;

        // novoro signature ;)
        displayAsciiArt();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.server = server;
            this.checkPermissionProvider();
            this.reloadConfigs();
        });

        // Check for Impactor, LuckPerms, or CobbleEconomy
        checkEconomyDependency();

        // Register all the commands available in the mod.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> this.registerCommands(dispatcher));

        // Execute tasks and listeners that should run when the server starts. (For LuckPerms)
        registerServerStartListeners();

        UseEntityCallback.EVENT.register(new EntityInteractEvent());
    }

    // Reloads TutorMoves' various configs.
    public void reloadConfigs() {
        // Lang
        this.langManager.reload();
        // Settings
        this.configManager.reload();
        // Tutors
        this.tutorsManager.reload();
    }

    /**
     * Register all commands provided by the mod.
     */
    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        new TutorMovesReloadCommand().register(dispatcher);
        new MenuCommand().register(dispatcher);
        new NPCCommand().register(dispatcher);
    }

    /**
     * Register listeners that should be executed when the server starts.
     */
    private void registerServerStartListeners() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            NPCUtil.loadNPCEntities();
        });
    }

    public static File getConfigFolder() {
        File configFolder = FabricLoader.getInstance().getConfigDir().resolve("tutormoves").toFile();
        if (!configFolder.exists()) configFolder.mkdirs();
        return configFolder;
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

    /**
     * Displays an ASCII Art representation of the mod's name in the log.
     */
    private void displayAsciiArt() {
        TutorMovesLogger.info("\u001B[1;35m _____      _             __  __                     \u001B[0m");
        TutorMovesLogger.info("\u001B[1;35m|_   _|   _| |_ ___  _ __|  \\/  | _____   _____  ___ \u001B[0m");
        TutorMovesLogger.info("\u001B[1;35m  | || | | | __/ _ \\| '__| |\\/| |/ _ \\ \\ / / _ \\/ __|\u001B[0m");
        TutorMovesLogger.info("\u001B[1;35m  | || |_| | || (_) | |  | |  | | (_) \\ V /  __/\\__ \\\u001B[0m");
        TutorMovesLogger.info("\u001B[1;35m  |_| \\__,_|\\__\\___/|_|  |_|  |_|\\___/ \\_/ \\___||___/\u001B[0m");
        TutorMovesLogger.info("\u001B[1;35m  By Novoro: https://discord.gg/wzpp8jeJ9s \u001B[0m");
    }

    /**
     * Checks if either Impactor, Pebbles Economy or CobbleEconomy is loaded,
     * and sets the appropriate boolean flags.
     */
    public void checkEconomyDependency() {
        isCobbleEconomyAvailable = FabricLoader.getInstance().isModLoaded("cobbleeconomy");
        isImpactorAvailable = FabricLoader.getInstance().isModLoaded("impactor");
        isPebblesAvailable = FabricLoader.getInstance().isModLoaded("pebbles-economy");

        if (isCobbleEconomyAvailable) {
            TutorMovesLogger.info("CobbleEconomy is available, enabling CobbleEconomy-specific features.");
        } else if (isImpactorAvailable) {
            TutorMovesLogger.info("Impactor API is available, enabling Impactor-specific features.");
        } else if (isPebblesAvailable) {
            TutorMovesLogger.info("Pebbles Economy is available, enabling Pebbles-specific features.");
        } else {
            TutorMovesLogger.warn("No recognized economy mod found! Economy features will be unavailable.");
        }
    }

    /**
     * Gets TutorMoves' current instance. It is not recommended to use externally.
     */
    public static TutorMoves inst() {
        return TutorMoves.instance;
    }

    /**
     * Gets the current {@link MinecraftServer} TutorMoves is currently running on.
     */
    public static MinecraftServer getServer() {
        return TutorMoves.instance.server;
    }

    /**
     * Gets the {@link PermissionProvider} TutorMoves is currently using.
     */
    public static PermissionProvider getPermissionProvider() {
        return TutorMoves.instance.permissionProvider;
    }

    /**
     * Sets what {@link PermissionProvider} TutorMoves will use to handle all permissions.
     */
    public static void setPermissionProvider(PermissionProvider provider) {
        TutorMoves.instance.permissionProvider = provider;
        TutorMovesLogger.info("Registered " + provider.getName() + " as TutorMoves' permission provider.");
    }

    // Checks the server for the built-in permission providers.
    private void checkPermissionProvider() {
        if (this.permissionProvider != null) return;
        try {
            Class.forName("net.luckperms.api.LuckPerms");
            this.permissionProvider = new LuckPermsPermissionProvider();
            TutorMovesLogger.info("Found LuckPerms! Permission support enabled.");
            return;
        } catch (ClassNotFoundException ignored) {}
        this.permissionProvider = new DefaultPermissionProvider();
        TutorMovesLogger.warn("Couldn't find a built in permission provider.. falling back to permission levels.");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File getDataFolder() {
        File folder = FabricLoader.getInstance().getConfigDir().resolve("TutorMoves").toFile();
        if (!folder.exists()) folder.mkdirs();
        return folder;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File getFile(String fileName) {
        File file = new File(this.getDataFolder(), fileName);
        if (!file.exists()) file.getParentFile().mkdirs();
        return file;
    }

    public Configuration getConfig(String resourcePath, boolean saveResource) {
        File configFile = this.getFile(new File(resourcePath).getName());
        if (!configFile.exists()) {
            if (!saveResource) return null;
            this.saveResource(resourcePath, false);
        }
        return this.getConfig(configFile);
    }

    public Configuration getConfig(File configFile) {
        try {
            return YamlConfiguration.loadConfiguration(configFile);
        } catch (IOException e) {
            TutorMovesLogger.error("Something went wrong getting the config: " + configFile.getName() + ".");
            TutorMovesLogger.printStackTrace(e);
        }
        return null;
    }

    public void saveConfig(String fileName, Configuration config) {
        File file = this.getFile(fileName);
        try {
            YamlConfiguration.save(config, file);
        } catch (IOException e) {
            TutorMovesLogger.warn("Something went wrong saving the config: " + fileName + ".");
            TutorMovesLogger.printStackTrace(e);
        }
    }

    public void saveResource(String resourcePath, boolean overwrite) {
        File file = this.getFile(new File(resourcePath).getName());
        if (file.exists() && !overwrite) return;
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
             FileOutputStream outputStream = new FileOutputStream(file)) {

            if (in == null) {
                throw new IllegalArgumentException("Resource not found in JAR: " + resourcePath);
            }
            in.transferTo(outputStream);

        } catch (IOException e) {
            TutorMovesLogger.error("Something went wrong saving the resource: " + resourcePath + ".");
            TutorMovesLogger.printStackTrace(e);
        }
    }

    public static void saveNPCEntities() {
        NPCUtil.saveNPCEntities();
    }
}
