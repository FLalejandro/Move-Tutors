package network.roanoke.ttms.items

import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.types.ElementalTypes
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import network.roanoke.ttms.TTMs


class TMs {

    companion object {
        fun getTM(name: String): ItemStack {
            val item = Items.POPPED_CHORUS_FRUIT.defaultStack
            val nbt = item.orCreateNbt

            var id = -1

            val move = Moves.getByName(name)
            TTMs.tmsConfig.moveData.forEach {
               if (it.move == name) {
                   if (move != null) {
                       item.setCustomName(Text.literal("§fTM${it.number}: ").append(move.displayName))
                   }
                   id = it.number
               }
            }

            if (id == -1)
                return ItemStack.EMPTY


            val modelData = when (move!!.elementalType) {
                ElementalTypes.NORMAL -> TTMs.tmsConfig.customModelData["normal"] ?: 0
                ElementalTypes.FIRE -> TTMs.tmsConfig.customModelData["fire"] ?: 0
                ElementalTypes.WATER -> TTMs.tmsConfig.customModelData["water"] ?: 0
                ElementalTypes.GRASS -> TTMs.tmsConfig.customModelData["grass"] ?: 0
                ElementalTypes.ELECTRIC -> TTMs.tmsConfig.customModelData["electric"] ?: 0
                ElementalTypes.ICE -> TTMs.tmsConfig.customModelData["ice"] ?: 0
                ElementalTypes.FIGHTING -> TTMs.tmsConfig.customModelData["fighting"] ?: 0
                ElementalTypes.POISON -> TTMs.tmsConfig.customModelData["poison"] ?: 0
                ElementalTypes.GROUND -> TTMs.tmsConfig.customModelData["ground"] ?: 0
                ElementalTypes.FLYING -> TTMs.tmsConfig.customModelData["flying"] ?: 0
                ElementalTypes.PSYCHIC -> TTMs.tmsConfig.customModelData["psychic"] ?: 0
                ElementalTypes.BUG -> TTMs.tmsConfig.customModelData["bug"] ?: 0
                ElementalTypes.ROCK -> TTMs.tmsConfig.customModelData["rock"] ?: 0
                ElementalTypes.GHOST -> TTMs.tmsConfig.customModelData["ghost"] ?: 0
                ElementalTypes.DRAGON -> TTMs.tmsConfig.customModelData["dragon"] ?: 0
                ElementalTypes.DARK -> TTMs.tmsConfig.customModelData["dark"] ?: 0
                ElementalTypes.STEEL -> TTMs.tmsConfig.customModelData["steel"] ?: 0
                ElementalTypes.FAIRY -> TTMs.tmsConfig.customModelData["fairy"] ?: 0
                else -> TTMs.tmsConfig.customModelData["unknown"] ?: TTMs.tmsConfig.customModelData["normal"] ?: 0
            }

            nbt.putInt("tm_id", id)
            nbt.putInt("CustomModelData", modelData)
            nbt.putString("tm_move", move.name)
            nbt.putBoolean("is_tr", false)
            item.nbt = nbt

            return item
        }

        fun getTR(name: String): ItemStack {
            val tm = getTM(name)
            if (tm == ItemStack.EMPTY) return ItemStack.EMPTY

            val move = Moves.getByName(name)
            if (move != null) {
                tm.setCustomName(Text.literal("TR" + tm.orCreateNbt.getInt("tm_id") + ": ").append(move.displayName))
            }

            tm.orCreateNbt.putBoolean("is_tr", true)
            return tm
        }


    }

}