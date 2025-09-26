package me.novoro.tutormoves.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.tutormoves.config.LangManager;
import me.novoro.tutormoves.config.TutorYAMLReader;
import me.novoro.tutormoves.guis.SelectionScreen;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Provides a command to open the General Tutor Menu
 */
public class MenuCommand extends CommandBase {
    public MenuCommand() {
        super("tutormoves", "tutormoves.menu", 2);
    }

    @Override
    public boolean bypassCommandCheck() {
        return true;
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(literal("menu")
                .executes(context -> {
                    openSelectionMenu(context.getSource().getPlayerOrThrow(), Optional.empty());
                    return Command.SINGLE_SUCCESS;
                }).then(argument("tutorName", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("general");
                            TutorYAMLReader.getTutorNames().forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String tutorName = StringArgumentType.getString(context, "tutorName");
                            openSelectionMenu(context.getSource().getPlayerOrThrow(), Optional.of(tutorName));
                            return Command.SINGLE_SUCCESS;
                        }).then(argument("target", EntityArgumentType.players())
                .requires(source -> this.permission(source, "tutormoves.menuothers", 2))
                .executes(context -> {
                    String tutorName = StringArgumentType.getString(context, "tutorName");
                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
                    players.forEach(player -> openSelectionMenu(player, Optional.of(tutorName)));
                    if (players.size() == 1) {
                        ServerPlayerEntity firstPlayer = players.iterator().next();
                        LangManager.sendLang(context.getSource(), "Menu-Other-Message", Map.of("{player}", firstPlayer.getName().getString(), "{tutor}", tutorName));
                    } else LangManager.sendLang(context.getSource(), "Menu-All-Message", Map.of("{amount}", String.valueOf(players.size()), "{tutor}", tutorName));

                    return Command.SINGLE_SUCCESS;
                }))
        ));
    }

    //TODO: add a parameter to specify which tutor to open (Uses Optional String)

    /**
     * Opens the selection menu GUI for the player.
     *
     * @param target The target players.
     */
    public static void openSelectionMenu(ServerPlayerEntity target, Optional<String> specificTutorName) {
        if (specificTutorName.isPresent() && specificTutorName.get().equalsIgnoreCase("general")) {
            specificTutorName = Optional.empty();
        }

        SelectionScreen.open(target, specificTutorName);

        LangManager.sendLang(target, "Menu-Self-Message",
                Map.of("{tutor}", specificTutorName.orElse("General Tutor")));
    }
}