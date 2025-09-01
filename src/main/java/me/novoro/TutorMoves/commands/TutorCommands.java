package me.novoro.TutorMoves.commands;

import me.novoro.TutorMoves.TutorMoves;
import me.novoro.TutorMoves.guis.AllTutorScreen;
import me.novoro.TutorMoves.guis.SelectionScreen;
import me.novoro.TutorMoves.util.TutorYAMLReader;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static me.novoro.TutorMoves.TutorMoves.npcModePlayers;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TutorCommands {

    // Permission nodes for the tutor commands.
    public static final String RELOAD_PERMISSION_NODE = "tutormoves.reload";
    public static final String TUTOR_PERMISSION_NODE = "tutormoves.tutor";
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
                                            UUID playerId = player.getUuid();
                                            String tutorName = StringArgumentType.getString(ctx, "tutor_name");

                                            if (npcModePlayers.containsKey(playerId) && npcModePlayers.get(playerId).equals(tutorName)) {
                                                npcModePlayers.remove(playerId);
                                                player.sendMessage(Text.literal("NPC mode disabled."));
                                            } else {
                                                npcModePlayers.put(playerId, tutorName);
                                                player.sendMessage(Text.literal("NPC mode enabled for " + tutorName + ". Right-click an entity to toggle as NPC."));
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
        File tutorsFolder = new File("config/me.novoro.TutorMoves/tutors");
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

        TutorYAMLReader.TutorConfig tutorConfig;
        try {
            tutorConfig = TutorYAMLReader.readTutorFile(specificTutor);
        } catch (Exception e) {
            TutorYAMLReader.sendFeedback(player, "Error reading tutor file: " + specificTutor);
            return 0;
        }

        if (!Permissions.check(player, tutorConfig.getPermission())) {
            TutorYAMLReader.sendFeedback(player, "You do not have permission to open this tutor.");
            return 0;
        }

        // Open the selection screen for a specific tutor
        SelectionScreen.open(player, Optional.of(specificTutor));

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

        return openSelectionMenu(source);
    }

    public static int openSelectionMenu(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            System.out.println("Player is null. Exiting command.");
            return 0;
        }

        // Open the selection menu GUI
        SelectionScreen.open(player, Optional.empty());

        return 1;
    }
}
