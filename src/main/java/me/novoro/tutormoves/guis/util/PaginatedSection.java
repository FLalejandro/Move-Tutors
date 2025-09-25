/*
Courtesy of GuitarXpress
 */

package me.novoro.tutormoves.guis.util;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.List;

public class PaginatedSection {
    private List<GuiElementBuilder> guiElements;
    private GuiElementBuilder fillItem = GuiElementBuilder.from(Items.GRAY_STAINED_GLASS_PANE.getDefaultStack())
            .setName(Text.of(""));
    private List<SlotRange> slotRanges = List.of();
    private int currentPage = 1;
    private int totalPages = 0;

    public PaginatedSection(List<GuiElementBuilder> guiElements) {
        this.guiElements = guiElements;
    }

    public PaginatedSection setSlotRanges(List<SlotRange> slotRanges) {
        this.slotRanges = slotRanges;
        this.totalPages = (int) Math.ceil((double) guiElements.size() / getItemsPerPage());
        return this;
    }

    public PaginatedSection setFillItem(GuiElementBuilder fillItem) {
        this.fillItem = fillItem;
        return this;
    }

    public int getItemsPerPage() {
        int itemsPerPage = 0;
        for (SlotRange range : slotRanges) {
            itemsPerPage += range.getEnd() - range.getStart() + 1;
        }
        return itemsPerPage;
    }

    public void applyToGui(SimpleGui gui) {
        int itemsPerPage = getItemsPerPage();
        int startingIndex = (currentPage - 1) * itemsPerPage;

        for (SlotRange range : slotRanges) {
            for (int slot = range.getStart(); slot <= range.getEnd(); slot++) {
                if (startingIndex < guiElements.size()) {
                    gui.setSlot(slot, guiElements.get(startingIndex));
                } else {
                    gui.setSlot(slot, fillItem);
                }
                startingIndex++;
            }
        }
    }

    public void incrementPage() {
        currentPage++;
        if (currentPage > totalPages) {
            currentPage = 1;
        }
    }

    public void decrementPage() {
        currentPage--;
        if (currentPage < 1) {
            currentPage = totalPages;
        }
    }
}

