package network.roanoke.ttms.items

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.text.Text
import network.roanoke.ttms.TTMs


class TMs {

    companion object {
        fun getTM(name: String): ItemStack {
            val item = Items.POPPED_CHORUS_FRUIT.defaultStack
            val nbt = item.orCreateNbt
            //val displayNbt = item.getOrCreateSubNbt("display")

            var id = -1
            var type: ElementalType = ElementalTypes.NORMAL

             TTMs.tmsConfig.tmsData.gens.forEach { gens ->
                gens.moves.forEach { move ->
                    if (move.name.replace(" ", "").lowercase() == name.lowercase()) {
                        item.setCustomName(Text.literal("§fTM${move.id}: ${move.name}"))
                        id = move.id
                        type = ElementalTypes.getOrException(move.type)
                    }
                }
            }

            if (id == -1)
                return ItemStack.EMPTY

            /*val loreList = mutableListOf<Text>()
            loreList.add(Text.literal("§7Use this to reduce a pokemon's IV stat to 0"))

            val nbtLore = NbtList()

            for (line in loreList)
                nbtLore.add(NbtString.of(Text.Serializer.toJson(line)))

            displayNbt.put("Lore", nbtLore)

            nbt.put("display", displayNbt)*/


            nbt.putString("id", "ttms:tm_$id")
            nbt.putInt("CustomModelData", 99999)
            nbt.putString("type", type.name.lowercase())
            nbt.putInt("tm_id", id)
            item.nbt = nbt

            return item
        }


    }

}