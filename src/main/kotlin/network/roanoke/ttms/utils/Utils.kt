package network.roanoke.ttms.utils

import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import network.roanoke.ttms.TTMs
import java.util.*

class Utils {

    companion object {
        fun broadcast(message: String) {
            val server = TTMs.serverInstance

            for (player in server.playerManager.playerList) {
                if (player is ServerPlayerEntity) {
                    player.sendMessage(Text.literal(message), false)
                }
            }
        }

        fun getPlayerByUUID(uuid: UUID): ServerPlayerEntity? {
            val server = TTMs.serverInstance
            val playerManager = server.playerManager

            return playerManager.getPlayer(uuid)
        }

        fun getPlayerByName(name: String): ServerPlayerEntity? {
            val server = TTMs.serverInstance
            val playerManager = server.playerManager

            return playerManager.getPlayer(name)
        }

        fun getAllPlayerNames(): Array<out String>? {
            val server = TTMs.serverInstance
            val playerManager = server.playerManager

            return playerManager.playerNames
        }

        fun isTM(item: ItemStack): Boolean {
            return item.orCreateNbt.get("id").toString().contains("ttms:tm_")
        }

    }


}