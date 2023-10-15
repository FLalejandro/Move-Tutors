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
                ElementalTypes.NORMAL -> 2420
                ElementalTypes.FIRE -> 2520
                ElementalTypes.WATER -> 2620
                ElementalTypes.GRASS -> 2720
                ElementalTypes.ELECTRIC -> 2820
                ElementalTypes.ICE -> 2920
                ElementalTypes.FIGHTING -> 21020
                ElementalTypes.POISON -> 21120
                ElementalTypes.GROUND -> 21220
                ElementalTypes.FLYING -> 21320
                ElementalTypes.PSYCHIC -> 21420
                ElementalTypes.BUG -> 21520
                ElementalTypes.ROCK -> 21620
                ElementalTypes.GHOST -> 21720
                ElementalTypes.DRAGON -> 21820
                ElementalTypes.DARK -> 21920
                ElementalTypes.STEEL -> 22020
                ElementalTypes.FAIRY -> 22120
                else -> 2420
            }

            nbt.putInt("tm_id", id)
            nbt.putInt("CustomModelData", modelData)
            nbt.putString("tm_move", move.name)
            nbt.putBoolean("is_tr", false)
            item.nbt = nbt

            return item
        }

        fun getTR(name: String): ItemStack {
            var tm = getTM(name)
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