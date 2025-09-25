package me.novoro.tutormoves.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.tutormoves.TutorMoves;
import me.novoro.tutormoves.utils.ColorUtil;
import net.minecraft.server.command.ServerCommandSource;

/**
 * TutorMoves' reload command.
 */
public final class TutorMovesReloadCommand extends CommandBase {
    public TutorMovesReloadCommand() {
        super("tutormoves", "tutormoves.reload", 4);
    }

    @Override
    public boolean bypassCommandCheck() {
        return true;
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(literal("reload")
                .executes(context -> {
                    TutorMoves.inst().reloadConfigs();
                    context.getSource().sendMessage(ColorUtil.parseColour(TutorMoves.MOD_PREFIX + "&aReloaded Configs!"));
                    return Command.SINGLE_SUCCESS;
                }));
    }
}
