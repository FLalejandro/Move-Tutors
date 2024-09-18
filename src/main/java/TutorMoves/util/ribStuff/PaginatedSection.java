package TutorMoves.util.ribStuff;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;

import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;

import static net.minecraft.item.Items.GRAY_STAINED_GLASS_PANE;

public class PaginatedSection {
    List<GuiElementBuilder> guiElements;
    GuiElementBuilder fillItem = GuiElementBuilder.from(
            GRAY_STAINED_GLASS_PANE.getDefaultStack().setCustomName(Text.literal("")));

    List<SlotRange> slotRanges = Collections.emptyList();
    int currentPage = 1;

    public PaginatedSection(List<GuiElementBuilder> guiElements) {
        this.guiElements = guiElements;
    }

    public PaginatedSection setSlotRanges(List<SlotRange> slotRanges) {
        this.slotRanges = slotRanges;
        return this;
    }

    public PaginatedSection setFillItem(GuiElementBuilder fillItem) {
        this.fillItem = fillItem;
        return this;
    }

    public int getItemsPerPage() {
        int itemsPerPage = 0;
        for (SlotRange range : this.slotRanges) {
            itemsPerPage += (range.getEnd() - range.getStart() + 1);
        }
        return itemsPerPage;
    }

    public void applyToGui(SimpleGui gui) {

        int itemsPerPage = getItemsPerPage();

        int startingIndex = (this.currentPage - 1) * itemsPerPage;

        for (SlotRange range: this.slotRanges) {
            for (int slot = range.getStart(); slot <= range.getEnd(); slot++) {
                if (startingIndex < this.guiElements.size()) {
                    gui.setSlot(slot, guiElements.get(startingIndex));
                } else {
                    gui.setSlot(slot, fillItem);
                }
                startingIndex++;
            }
        }

    }

    public void setGuiElements(List<GuiElementBuilder> guiElements) {
        this.guiElements = guiElements;
        this.currentPage = 1;
    }

    public void incremementPage() {
        int totalPages = (int) Math.ceil((double) this.guiElements.size() / this.getItemsPerPage());
        this.currentPage++;
        if (this.currentPage > totalPages) {
            this.currentPage--;
        }
    }

    public void decrementPage() {
        this.currentPage--;
        if (this.currentPage < 1) {
            this.currentPage = 1;
        }
    }

}