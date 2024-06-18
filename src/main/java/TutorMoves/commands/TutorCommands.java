package TutorMoves.commands;

import TutorMoves.TutorMoves;
import TutorMoves.guis.AllTutorScreen;
import TutorMoves.guis.SpecificTutorScreen;
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
    private static int openSpecificTutor(CommandContext<ServerCommandSource> ctx, String specificTutor, int slot) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            System.out.println("Player is null. Exiting command.");
            return 0;
        }

        // Open the specific tutor GUI
        SpecificTutorScreen.open(player, slot, specificTutor);

        return 1;
    }
}
