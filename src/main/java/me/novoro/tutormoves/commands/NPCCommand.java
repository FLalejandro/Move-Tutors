package me.novoro.tutormoves.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.tutormoves.config.LangManager;
import me.novoro.tutormoves.config.TutorYAMLReader;
import me.novoro.tutormoves.utils.TutorMovesLogger;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

import static me.novoro.tutormoves.TutorMoves.npcModePlayers;

/**
 * Provides a command to open the General Tutor Menu
 */
public class NPCCommand extends CommandBase {
    public NPCCommand() {
        super("tutormoves", "tutormoves.npc", 2);
    }

    @Override
    public boolean bypassCommandCheck() {
        return true;
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(literal("npc")
                .then(argument("tutor_name", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            builder.suggest("general");
                            TutorYAMLReader.getTutorNames().forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            String tutorName = StringArgumentType.getString(context, "tutor_name");
                            boolean enabled = toggleNPCMode(player, tutorName);
                            if (enabled) LangManager.sendLang(context.getSource(), "NPC-Mode-Enabled");
                            else LangManager.sendLang(context.getSource(), "NPC-Mode-Disabled");
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

    /**
     * Toggles NPC Mode for the target player.
     *
     * @param target the target player.
     * @return true if NPC mode is enabled after toggling, false otherwise.
     */
    private static boolean toggleNPCMode(ServerPlayerEntity target, String tutorName) {
        UUID playerId = target.getUuid();
        String currentTutor = npcModePlayers.get(playerId);

        if (currentTutor != null && currentTutor.equalsIgnoreCase(tutorName)) {
            npcModePlayers.remove(playerId);
            //TutorMovesLogger.info("NPC mode disabled for " + target.getDisplayName() + " (" + tutorName + ")");
            return false;
        } else {
            npcModePlayers.put(playerId, tutorName);
            //TutorMovesLogger.info("NPC mode enabled for " + target.getDisplayName() + " (" + tutorName + ")");
            return true;
        }
    }


}
