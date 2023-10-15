package network.roanoke.ttms.gui

import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.text.Text

class PaginatedSection(guiElements: List<GuiElementBuilder>) {
    var guiElements: List<GuiElementBuilder> = guiElements
    var fillItem = GuiElementBuilder.from(
        Items.GRAY_STAINED_GLASS_PANE.defaultStack.setCustomName(Text.literal(""))
    )

    var slotRanges: List<SlotRange> = emptyList<SlotRange>()
    var currentPage = 1

    fun setSlotRanges(slotRanges: List<SlotRange>): PaginatedSection {
        this.slotRanges = slotRanges
        return this
    }

    fun setFillItem(fillItem: GuiElementBuilder): PaginatedSection {
        this.fillItem = fillItem
        return this
    }

    fun getItemsPerPage(): Int {
        var itemsPerPage = 0
        for (range in slotRanges) {
            itemsPerPage += range.getEnd() - range.getStart() + 1
        }
        return itemsPerPage
    }

    fun applyToGui(gui: SimpleGui) {
        val itemsPerPage = getItemsPerPage()
        var startingIndex = (currentPage - 1) * itemsPerPage
        for (range in slotRanges) {
            for (slot in range.getStart()..range.getEnd()) {
                if (startingIndex < guiElements.size) {
                    gui.setSlot(slot, guiElements[startingIndex])
                } else {
                    gui.setSlot(slot, fillItem)
                }
                startingIndex++
            }
        }
    }

    fun incremementPage() {
        val totalPages = Math.ceil(guiElements.size.toDouble() / getItemsPerPage()).toInt()
        currentPage++
        if (currentPage > totalPages) {
            currentPage--
        }
    }

    fun decrementPage() {
        currentPage--
        if (currentPage < 1) {
            currentPage = 1
        }
    }
}