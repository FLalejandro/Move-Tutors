package network.roanoke.ttms.gui

import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import network.roanoke.ttms.TTMs
import network.roanoke.ttms.items.TMs

class GUIs {
    companion object {

        fun fillGUI(gui: SimpleGui) {
            var freeslot = gui.firstEmptySlot
            while (freeslot != -1) {
                gui.setSlot(
                    freeslot,
                    GuiElementBuilder.from(Items.BLACK_STAINED_GLASS_PANE.defaultStack.setCustomName(Text.literal("")))
                )
                freeslot = gui.firstEmptySlot
            }
        }
        fun getTMShop(player: ServerPlayerEntity): SimpleGui {
            val gui = SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false)
            gui.title = Text.literal("TM Shop")


            val tmList: List<GuiElementBuilder> = TTMs.tmsConfig.moveData.map {
                GuiElementBuilder.from(TMs.getTR(it.move))
            }
            val paginatedSection = PaginatedSection(tmList).setSlotRanges(listOf(
                SlotRange(10, 16), SlotRange(19, 25), SlotRange(29, 33)
            ))

            paginatedSection.applyToGui(gui)

            gui.setSlot(28, GuiElementBuilder.from(
                Items.ARROW.defaultStack.setCustomName(
                    Text.literal("Back")
                )
            ).setCallback { x: Int, y: ClickType?, z: SlotActionType? ->
                paginatedSection.decrementPage()
                paginatedSection.applyToGui(gui)
            })

            gui.setSlot(34, GuiElementBuilder.from(
                Items.ARROW.defaultStack.setCustomName(
                    Text.literal("Forward")
                )
            ).setCallback { x: Int, y: ClickType?, z: SlotActionType? ->
                paginatedSection.incremementPage()
                paginatedSection.applyToGui(gui)
            })

            fillGUI(gui)

            return gui
        }
    }
}