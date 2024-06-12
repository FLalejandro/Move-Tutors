package TutorMoves.commands;

import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import TutorMoves.guis.AllTutorScreen;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

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
                                        .executes(ctx -> openSpecificTutor(ctx, StringArgumentType.getString(ctx, "specific_tutor")))
                                )
                        )
        );
    }

    /**
     * Reloads the tutor moves configuration.
     *
     * @param ctx The command context.
     * @return 1 if successful, 0 otherwise.
     */
    private static int reloadTutorMoves(CommandContext<ServerCommandSource> ctx) {
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

        System.out.println("Executing /tutormoves tutor for player: " + (player != null ? player.getName().getString() : "null") + ", slot: " + slot);

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
     * @return 1 if successful, 0 otherwise.
     */
    private static int openSpecificTutor(CommandContext<ServerCommandSource> ctx, String specificTutor) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();

        System.out.println("Executing /tutormoves open for player: " + (player != null ? player.getName().getString() : "null") + ", specific tutor: " + specificTutor);

        if (player == null) {
            System.out.println("Player is null. Exiting command.");
            return 0;
        }

        // Implementation for opening a specific tutor GUI

        return 1;
    }
}
