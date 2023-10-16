package network.roanoke.ttms.events

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.moves.BenchedMove
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World
import network.roanoke.ttms.TTMs
import network.roanoke.ttms.utils.Utils

class UseTMEvent: UseEntityCallback {
    override fun interact(
        player: PlayerEntity?,
        world: World?,
        hand: Hand?,
        entity: Entity?,
        hitResult: EntityHitResult?
    ): ActionResult {

        if (entity !is PokemonEntity)
            return ActionResult.PASS

        if (player == null)
            return ActionResult.PASS

        if (!entity.isOwner(player))
            return ActionResult.PASS

        if (hand != Hand.MAIN_HAND)
            return ActionResult.PASS

        if (player.isSneaking)
            return ActionResult.PASS

        if (player.mainHandStack.isEmpty)
            return ActionResult.PASS

        if (!Utils.isTM(player.mainHandStack))
            return ActionResult.PASS

        if (TTMs.onCooldown)
            return ActionResult.PASS

        var moveName = player.mainHandStack.orCreateNbt.getString("tm_move")

        val moveTemplate = Moves.getByName(moveName)

        val move = BenchedMove(moveTemplate!!, 0)

        entity.pokemon.benchedMoves.forEach {
            if (it.moveTemplate.name.lowercase() == moveName) {
                player.sendMessage(Text.literal("§c${entity.pokemon.species.name} already knows ").append(moveTemplate.displayName))
                TTMs.onCooldown = true
                return ActionResult.PASS
            }
        }

        entity.pokemon.moveSet.forEach {
            if (it.name.lowercase() == moveName) {
                player.sendMessage(Text.literal("§c${entity.pokemon.species.name} already knows ").append(moveTemplate.displayName))
                TTMs.onCooldown = true
                return ActionResult.PASS
            }
        }

        var canLearn = false
        entity.pokemon.species.moves.tmMoves.forEach {
            if (it.name.lowercase() == moveName)
                canLearn = true
        }

        if (canLearn) {
            entity.pokemon.benchedMoves.add(move)
            player.sendMessage(Text.literal("§a${entity.pokemon.species.name} learned ").append(moveTemplate.displayName).append("§a!"))
            player.world.playSound(null, player.steppingPos, CobblemonSounds.MEDICINE_PILLS_USE, SoundCategory.NEUTRAL, 1f, 1f)
        } else {
            player.sendMessage(Text.literal("§c${entity.pokemon.species.name} cannot learn ").append(moveTemplate.displayName).append("§c."))
            TTMs.onCooldown = true
            return ActionResult.PASS
        }

        if (player.mainHandStack.orCreateNbt.getBoolean("is_tr")) {
            player.mainHandStack.decrement(1)
        }

        TTMs.onCooldown = true
        return ActionResult.SUCCESS
    }
}