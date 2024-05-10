package network.roanoke.ttms.events

import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.entity.Entity
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World
import network.roanoke.ttms.TTMs
import network.roanoke.ttms.gui.GUIs
import network.roanoke.ttms.utils.Utils

class UseNPCEvent: UseEntityCallback {
    override fun interact(
        player: PlayerEntity?,
        world: World?,
        hand: Hand?,
        entity: Entity?,
        hitResult: EntityHitResult?
    ): ActionResult {

        if (entity == null)
            return ActionResult.PASS

        if (player == null)
            return ActionResult.PASS

        if (hand != Hand.MAIN_HAND)
            return ActionResult.PASS

        if (entity !is VillagerEntity)
            return ActionResult.PASS

        if (TTMs.npcModePlayers.contains(player.uuid)) {
            if (TTMs.onCooldown)
                return ActionResult.PASS

            val type = TTMs.npcModePlayers[player.uuid]!!

            if (TTMs.trNpcs.contains(entity.uuid) || TTMs.tmNpcs.contains(entity.uuid)) {
                TTMs.onCooldown = true
                TTMs.trNpcs.remove(entity.uuid)
                TTMs.tmNpcs.remove(entity.uuid)
                player.sendMessage(Text.literal("§cNPC removed"))
                entity.isCustomNameVisible = false
                entity.isInvulnerable = false
            } else {
                TTMs.onCooldown = true
                if (type == "tr") {
                    TTMs.trNpcs.add(entity.uuid)
                } else {
                    TTMs.tmNpcs.add(entity.uuid)
                }
                player.sendMessage(Text.literal("§aNPC added - ${type.uppercase()}"))
                entity.isCustomNameVisible = true
                entity.isInvulnerable = true
            }
            TTMs.tmsConfig.saveNPCs()
        } else {
            if (TTMs.trNpcs.contains(entity.uuid) || TTMs.tmNpcs.contains(entity.uuid)) {
                if (TTMs.onCooldown)
                    return ActionResult.PASS

                val type = if (TTMs.trNpcs.contains(entity.uuid)) "TR" else "TM"

                GUIs.getShopStartGUI(Utils.getPlayerByUUID(player.uuid)!!, type).open()
                TTMs.onCooldown = true
                return ActionResult.FAIL
            }
        }
        return ActionResult.PASS
    }
}