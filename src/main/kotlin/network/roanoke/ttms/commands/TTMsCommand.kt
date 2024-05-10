package network.roanoke.ttms.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandSource
import net.minecraft.item.ItemStack
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import network.roanoke.ttms.TTMs
import network.roanoke.ttms.items.TMs
import network.roanoke.ttms.utils.Utils
import java.util.concurrent.CompletableFuture

class TTMsCommand() {

    init {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _, _ ->
            dispatcher.register(
                CommandManager.literal("ttms")
                    .then(CommandManager.literal("reload").requires { it.hasPermissionLevel(2) }.executes(reloadConfig()))
                    .then(CommandManager.literal("npc").requires { it.hasPermissionLevel(2) }.executes(toggleNpcMode()))
                    .then(CommandManager.literal("give").requires { it.hasPermissionLevel(2) }
                        .then(
                            argument("player", StringArgumentType.string())
                                .suggests(this::suggestPlayers)
                                .then(
                                    argument("trtm", StringArgumentType.string())
                                        .suggests(this::suggestItem)
                                        .then(
                                            argument("move", StringArgumentType.string())
                                                .suggests(this::suggestMove)
                                                .executes(giveTm())
                                        )
                                )
                        )
                    )
            )
        })
    }

    private fun reloadConfig(): Command<ServerCommandSource> {
        return Command {
            val source = it.source

            TTMs.tmsConfig.loadTMs()
            TTMs.tmsConfig.loadCustomModelData()
            TTMs.tmsConfig.loadedAllTypes()
            source.sendFeedback({Text.literal("Reloaded TTMs Config")}, true)

            1
        }
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

    private fun suggestMove(
        ctx: CommandContext<ServerCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        TTMs.tmsConfig.moveData.forEach {
            builder.suggest(it.move)
        }
        return builder.buildFuture()
    }

    private fun suggestItem(
        ctx: CommandContext<ServerCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        builder.suggest("tm")
        builder.suggest("tr")
        return builder.buildFuture()
    }

    private fun suggestPlayers(
        ctx: CommandContext<ServerCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val playerNames = Utils.getAllPlayerNames()
        return CommandSource.suggestMatching(playerNames!!, builder)
    }

}