package me.novoro.tutormoves.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.tutormoves.config.LangManager;
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
                    MenuCommand.openSelectionMenu(context.getSource().getPlayerOrThrow());
                    return Command.SINGLE_SUCCESS;
        }).then(argument("target", EntityArgumentType.players())
                .requires(source -> this.permission(source, "tutormoves.menuothers", 4))
                .executes(context -> {
                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
                    players.forEach(MenuCommand::openSelectionMenu);
                    if (players.size() == 1) {
                        ServerPlayerEntity firstPlayer = players.iterator().next();
                        LangManager.sendLang(context.getSource(), "Menu-Other-Message", Map.of("{player}", firstPlayer.getName().getString()));
                    } else LangManager.sendLang(context.getSource(), "Menu-All-Message", Map.of("{amount}", String.valueOf(players.size())));

                    return Command.SINGLE_SUCCESS;
                })
        ));
    }

    /**
     * Opens the selection menu GUI for the player.
     *
     * @param target The target players.
     */
    public static void openSelectionMenu(ServerPlayerEntity target) {
        SelectionScreen.open(target, Optional.empty());
        LangManager.sendLang(target, "Menu-Self-Message");
    }
}