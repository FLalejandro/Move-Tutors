package TutorMoves.commands;

import TutorMoves.TutorMoves;
import TutorMoves.guis.AllTutorScreen;
import TutorMoves.guis.SelectionScreen;
import TutorMoves.guis.SpecificTutorScreen;
import TutorMoves.util.TutorYAMLReader;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TutorCommands {

    // Permission nodes for the tutor commands.
    public static final String RELOAD_PERMISSION_NODE = "tutormoves.reload";
    public static final String TUTOR_PERMISSION_NODE = "tutormoves.tutor";
    public static final String OPEN_PERMISSION_NODE = "tutormoves.open";
    public static final String MENU_PERMISSION_NODE = "tutormoves.menu";
    public static final String NPC_PERMISSION_NODE = "tutormoves.npc";

    /**
     * Registers the tutor commands.
     *
     * @param dispatcher The command dispatcher to register commands on.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("tutormoves")
                        .then(literal("reload")
                                .requires(Permissions.require(RELOAD_PERMISSION_NODE, 2))
                                .executes(TutorCommands::reloadTutorMoves)
                        )
                        .then(literal("tutor")
                                .requires(Permissions.require(TUTOR_PERMISSION_NODE, 2))
                                .then(argument("slot", IntegerArgumentType.integer(1, 6))
                                        .suggests(TutorCommands::suggestSlots)
                                        .executes(ctx -> {
                                            try {
                                                return tutorMove(ctx, IntegerArgumentType.getInteger(ctx, "slot"));
                                            } catch (NoPokemonStoreException e) {
                                                throw new RuntimeException(e);
                                            }
                                        })
                                )
                        )
                        .then(literal("open")
                                .requires(Permissions.require(OPEN_PERMISSION_NODE, 2))
                                .then(argument("specific_tutor", StringArgumentType.string())
                                        .suggests(TutorCommands::suggestTutors)
                                        .then(argument("slot", IntegerArgumentType.integer(1, 6))
                                                .suggests(TutorCommands::suggestSlots)
                                                .executes(ctx -> openSpecificTutor(ctx, StringArgumentType.getString(ctx, "specific_tutor"), IntegerArgumentType.getInteger(ctx, "slot")))
                                        )
                                )
                        )
                        .then(literal("menu")
                                .requires(Permissions.require(MENU_PERMISSION_NODE, 2))
                                .executes(TutorCommands::openSelectionMenu)
                        )
                        .then(literal("npc")
                                .requires(Permissions.require(NPC_PERMISSION_NODE, 2))
                                .then(argument("tutor_name", StringArgumentType.string())
                                        .suggests(TutorCommands::suggestTutors)
                                        .executes(ctx -> {
                                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                                            if (player != null) {
                                                TutorMoves.npcModePlayers.put(player.getUuid(), StringArgumentType.getString(ctx, "tutor_name"));
                                                player.sendMessage(Text.literal("Right-click on an entity to set it as an NPC for this tutor."));
                                            }
                                            return 1;
                                        })
                                )
                                .then(literal("off")
                                        .executes(ctx -> {
                                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                                            if (player != null) {
                                                if (TutorMoves.npcModePlayers.containsKey(player.getUuid())) {
                                                    TutorMoves.npcModePlayers.remove(player.getUuid());
                                                    player.sendMessage(Text.literal("NPC mode has been turned off."));
                                                } else {
                                                    player.sendMessage(Text.literal("You are not in NPC mode."));
                                                }
                                            }
                                            return 1;
                                        })
                                )
                        )
        );
    }

    /**
     * Suggests slot numbers between 1 and 6.
     *
     * @param context The command context.
     * @param builder The suggestions builder.
     * @return A CompletableFuture containing the suggestions.
     */
    private static CompletableFuture<Suggestions> suggestSlots(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        IntStream.rangeClosed(1, 6).forEach(builder::suggest);
        return builder.buildFuture();
    }

    /**
     * Suggests tutor file names from the tutors folder.
     *
     * @param context The command context.
     * @param builder The suggestions builder.
     * @return A CompletableFuture containing the suggestions.
     */
    private static CompletableFuture<Suggestions> suggestTutors(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        File tutorsFolder = new File("config/TutorMoves/tutors");
        if (tutorsFolder.exists() && tutorsFolder.isDirectory()) {
            for (File file : tutorsFolder.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".yml")) {
                    builder.suggest(file.getName().replace(".yml", ""));
                }
            }
        }
        builder.suggest("general");
        return builder.buildFuture();
    }

    /**
     * Reloads the tutor moves configuration.
     *
     * @param ctx The command context.
     * @return 1 if successful, 0 otherwise.
     */
    private static int reloadTutorMoves(CommandContext<ServerCommandSource> ctx) {
        TutorMoves.reloadConfigurations();
        ctx.getSource().sendMessage(Text.literal("Tutor moves configuration reloaded."));
        return 1;
    }

    /**
     * Opens the tutor move GUI for the Pokémon in the specified slot.
     *
     * @param ctx The command context.
     * @param slot The slot of the Pokémon.
     * @return 1 if successful, 0 otherwise.
     */
    private static int tutorMove(CommandContext<ServerCommandSource> ctx, int slot) throws NoPokemonStoreException {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            System.out.println("Player is null. Exiting command.");
            return 0;
        }

        // Open the GUI for tutor moves
        AllTutorScreen.open(player, slot);

        return 1;
    }

    /**
     * Opens the interface for a specific tutor.
     *
     * @param ctx The command context.
     * @param specificTutor The specific tutor to open.
     * @param slot The slot of the Pokémon.
     * @return 1 if successful, 0 otherwise.
     */
    public static int openSpecificTutor(CommandContext<ServerCommandSource> ctx, String specificTutor, int slot) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();

        return openSpecificTutor(player, specificTutor, slot);
    }

    public static int openSpecificTutor(ServerPlayerEntity player, String specificTutor, int slot) {
        if (player == null) {
            System.out.println("Player is null. Exiting command.");
            return 0;
        }

        // Fetch the tutor configuration from the YAML file
        TutorYAMLReader.TutorConfig tutorConfig;
        try {
            tutorConfig = TutorYAMLReader.readTutorFile(specificTutor);
        } catch (Exception e) {
            TutorYAMLReader.sendFeedback(player, "Error reading tutor file: " + specificTutor);
            return 0;
        }

        // Check if the player has the required permission
        String permission = tutorConfig.getPermission();
        if (!Permissions.check(player, permission)) {
            TutorYAMLReader.sendFeedback(player, "You do not have permission to open this tutor.");
            return 0;
        }

        // Open the specific tutor GUI
        SpecificTutorScreen.open(player, slot, specificTutor);

        return 1;
    }

    /**
     * Opens the selection menu GUI for the player.
     *
     * @param ctx The command context.
     * @return 1 if successful, 0 otherwise.
     */
    public static int openSelectionMenu(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();

        return openSelectionMenu(source);
    }

    public static int openSelectionMenu(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            System.out.println("Player is null. Exiting command.");
            return 0;
        }

        // Open the selection menu GUI
        SelectionScreen.open(player);

        return 1;
    }
}
