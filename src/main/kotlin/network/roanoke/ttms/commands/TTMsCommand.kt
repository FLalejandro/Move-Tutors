package network.roanoke.ttms.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.item.ItemStack
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import network.roanoke.ttms.TTMs
import network.roanoke.ttms.items.TMs
import network.roanoke.ttms.utils.Utils

class TTMsCommand() {

    init {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _, _ ->
            dispatcher.register(
                CommandManager.literal("ttms")
                    .then(CommandManager.literal("npc").requires { it.hasPermissionLevel(2) }.executes(toggleNpcMode()))
                    .then(CommandManager.literal("give").requires { it.hasPermissionLevel(2) }
                        .then(
                            argument("player", StringArgumentType.string())
                                .suggests(playerSuggestionProvider())
                                .then(
                                    argument("trtm", StringArgumentType.string())
                                        .suggests(tmTrSuggestionProvider())
                                        .then(
                                            argument("move", StringArgumentType.string())
                                                .suggests(tmMovesSuggestionProvider())
                                                .executes(giveTm())
                                        )
                                )
                        )
                    )
            )
        })
    }

    private fun toggleNpcMode(): Command<ServerCommandSource> {
        return Command {
            val source = it.source

            if (source.player == null)
                return@Command 1

            if (TTMs.npcModePlayers.contains(source.player?.uuid)) {
                TTMs.npcModePlayers.remove(source.player?.uuid)
                source.player!!.sendMessage(Text.literal("§cNPC Mode disabled"))
            } else {
                TTMs.npcModePlayers.add(source.player?.uuid!!)
                source.player!!.sendMessage(Text.literal("§aNPC Mode enabled"))
            }

            1
        }
    }

    private fun giveTm(): Command<ServerCommandSource> {
        return Command {
            val source = it.source

            val player = StringArgumentType.getString(it, "player")
            val name = StringArgumentType.getString(it, "move")
            val tmortr = StringArgumentType.getString(it, "trtm")


            val p = Utils.getPlayerByName(player)

            var tm = TMs.getTM(name)
            if (tmortr == "tr") {
                tm = TMs.getTR(name)
            }
            if (tm == ItemStack.EMPTY) {
                source.player?.sendMessage(Text.literal("§cInvalid Move Name: $name"))
                return@Command 1
            }

            if (p?.inventory!!.emptySlot != -1) {
                p.inventory!!.insertStack(tm)
            } else {
                p.dropItem(tm, false)
            }

            p.sendMessage(Text.literal("§aYou received ${tmortr.uppercase()} $name"))
            source.player?.sendMessage(
                Text.literal("§7Gave $player ${tmortr.uppercase()} $name")
            )
            1
        }
    }

    private fun tmMovesSuggestionProvider(): SuggestionProvider<ServerCommandSource>? {
        return SuggestionProvider { _, builder ->

            TTMs.tmsConfig.moveData.forEach {
                builder.suggest(it.move)
            }

            builder.buildFuture()
        }
    }

    private fun tmTrSuggestionProvider(): SuggestionProvider<ServerCommandSource>? {
        return SuggestionProvider { _, builder ->

            builder.suggest("tr")
            builder.suggest("tm")

            builder.buildFuture()
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