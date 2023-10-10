package network.roanoke.ttms.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.item.ItemStack
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import network.roanoke.ttms.items.TMs
import network.roanoke.ttms.utils.Utils

class TTMsCommand() {

    init {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _, _ ->
            dispatcher.register(
                CommandManager.literal("ttms")
                    .then(CommandManager.literal("give").requires { it.hasPermissionLevel(2) }
                        .then(
                            argument("player", StringArgumentType.string())
                                .suggests(playerSuggestionProvider())
                                .then(
                                    argument("tm", StringArgumentType.string())
                                        .executes(giveCap())
                                )
                        )
                    )
            )
        })
    }

    private fun giveCap(): Command<ServerCommandSource> {
        return Command {
            val source = it.source

            val player = StringArgumentType.getString(it, "player")
            val name = StringArgumentType.getString(it, "tm")

            val p = Utils.getPlayerByName(player)

            val tm = TMs.getTM(name)
            if (tm == ItemStack.EMPTY) {
                source.player?.sendMessage(Text.literal("§cInvalid TM Name: $name"))
                return@Command 1
            }

            p?.inventory!!.insertStack(tm)

            p.sendMessage(Text.literal("§aYou received TM $name"))
            source.player?.sendMessage(
                Text.literal("§7Gave $player TM $name")
            )
            1
        }
    }

    private fun playerSuggestionProvider(): SuggestionProvider<ServerCommandSource>? {
        return SuggestionProvider { _, builder ->
            val playerNames = Utils.getAllPlayerNames()

            playerNames!!.forEach {
                builder.suggest(it)
            }

            builder.buildFuture()
        }
    }

}