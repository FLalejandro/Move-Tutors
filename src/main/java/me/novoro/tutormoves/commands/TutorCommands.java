package me.novoro.tutormoves.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.tutormoves.TutorMoves;
import me.novoro.tutormoves.config.LangManager;
import me.novoro.tutormoves.config.TutorYAMLReader;
import me.novoro.tutormoves.guis.SelectionScreen;
import me.novoro.tutormoves.utils.ColorUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static me.novoro.tutormoves.TutorMoves.npcModePlayers;

/**
 * Combined command class for all TutorMoves commands
 */
public class TutorCommands extends CommandBase {
    public TutorCommands() {
        super("tutormoves", "tutormoves.base", 2);
    }

    @Override
    public boolean bypassCommandCheck() {
        return true;
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command
                .then(literal("reload")
                        .requires(source -> this.permission(source, "tutormoves.reload", 4))
                        .executes(context -> {
                            TutorMoves.inst().reloadConfigs();
                            context.getSource().sendMessage(ColorUtil.parseColour(TutorMoves.MOD_PREFIX + "&aReloaded Configs!"));
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(literal("npc")
                        .requires(source -> this.permission(source, "tutormoves.npc", 2))
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
                        ))
                .then(literal("menu")
                        .requires(source -> this.permission(source, "tutormoves.menu", 2))
                        .executes(context -> {
                            openSelectionMenu(context.getSource().getPlayerOrThrow(), Optional.empty());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(argument("tutorName", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("general");
                                    TutorYAMLReader.getTutorNames().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String tutorName = StringArgumentType.getString(context, "tutorName");
                                    openSelectionMenu(context.getSource().getPlayerOrThrow(), Optional.of(tutorName));
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(argument("target", EntityArgumentType.players())
                                        .requires(source -> this.permission(source, "tutormoves.menuothers", 2))
                                        .executes(context -> {
                                            String tutorName = StringArgumentType.getString(context, "tutorName");
                                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
                                            players.forEach(player -> openSelectionMenu(player, Optional.of(tutorName)));
                                            if (players.size() == 1) {
                                                ServerPlayerEntity firstPlayer = players.iterator().next();
                                                LangManager.sendLang(context.getSource(), "Menu-Other-Message", Map.of("{player}", firstPlayer.getName().getString(), "{tutor}", tutorName));
                                            } else {
                                                LangManager.sendLang(context.getSource(), "Menu-All-Message", Map.of("{amount}", String.valueOf(players.size()), "{tutor}", tutorName));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                );
    }

    /**
     * Toggles NPC Mode for the target player.
     */
    private static boolean toggleNPCMode(ServerPlayerEntity target, String tutorName) {
        UUID playerId = target.getUuid();
        String currentTutor = npcModePlayers.get(playerId);

        if (currentTutor != null && currentTutor.equalsIgnoreCase(tutorName)) {
            npcModePlayers.remove(playerId);
            return false;
        } else {
            npcModePlayers.put(playerId, tutorName);
            return true;
        }
    }

    /**
     * Opens the selection menu GUI for the player.
     */
    private static void openSelectionMenu(ServerPlayerEntity target, Optional<String> specificTutorName) {
        if (specificTutorName.isPresent() && specificTutorName.get().equalsIgnoreCase("general")) {
            specificTutorName = Optional.empty();
        }

        SelectionScreen.open(target, specificTutorName);

        LangManager.sendLang(target, "Menu-Self-Message",
                Map.of("{tutor}", specificTutorName.orElse("General Tutor")));
    }
}