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
                ElementalTypes.NORMAL -> 420
                ElementalTypes.FIRE -> 520
                ElementalTypes.WATER -> 620
                ElementalTypes.GRASS -> 720
                ElementalTypes.ELECTRIC -> 820
                ElementalTypes.ICE -> 920
                ElementalTypes.FIGHTING -> 1020
                ElementalTypes.POISON -> 1120
                ElementalTypes.GROUND -> 1220
                ElementalTypes.FLYING -> 1320
                ElementalTypes.PSYCHIC -> 1420
                ElementalTypes.BUG -> 1520
                ElementalTypes.ROCK -> 1620
                ElementalTypes.GHOST -> 1720
                ElementalTypes.DRAGON -> 1820
                ElementalTypes.DARK -> 1920
                ElementalTypes.STEEL -> 2020
                ElementalTypes.FAIRY -> 2120
                else -> 420
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