package me.novoro.tutormoves.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.novoro.tutormoves.config.LangManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
                        .suggests(NPCCommand::suggestTutors)
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
        boolean wasNPCMode = !npcModePlayers.containsKey(playerId) && !npcModePlayers.get(playerId).equals(tutorName);

        if (wasNPCMode) npcModePlayers.remove(playerId);
        else npcModePlayers.put(playerId, tutorName);

        return !wasNPCMode;
    }

    /**
     * Suggests tutor file names from the tutors folder.
     *
     * @param context The command context.
     * @param builder The suggestions builder.
     * @return A CompletableFuture containing the suggestions.
     */
    private static CompletableFuture<Suggestions> suggestTutors(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        File tutorsFolder = new File("configuration/tutormoves/tutors");
        if (tutorsFolder.exists() && tutorsFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(tutorsFolder.listFiles())) {
                if (file.isFile() && file.getName().endsWith(".yml")) {
                    builder.suggest(file.getName().replace(".yml", ""));
                }
            }
        }
        builder.suggest("general");
        return builder.buildFuture();
    }
}
