package TutorMoves.util;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import dev.roanoke.rib.utils.PaginatedSection;
import dev.roanoke.rib.utils.SlotRange;

import java.util.List;

public class GuiUtil {

    /**
     * Creates a SimpleGui with the given number of rows and title.
     *
     * @param player The player for whom the GUI is being created.
     * @param rows The number of rows in the GUI.
     * @param title The title of the GUI.
     * @return The created SimpleGui.
     */
    public static SimpleGui createGui(ServerPlayerEntity player, int rows, String title) {
        ScreenHandlerType<?> screenHandlerType = getScreenHandlerType(rows);
        SimpleGui gui = new SimpleGui(screenHandlerType, player, false);
        gui.setTitle(Text.literal(title));
        return gui;
    }

    /**
     * Applies pagination controls (Previous and Next Page buttons) to the GUI.
     *
     * @param gui The GUI to which the controls are being applied.
     * @param paginatedSection The paginated section handling the tutor moves.
     * @param rows The number of rows in the GUI.
     */
    public static void applyPaginationControls(SimpleGui gui, PaginatedSection paginatedSection, int rows) {
        int controlSlotPrevious = (rows - 1) * 9;
        int controlSlotNext = controlSlotPrevious + 8;

        gui.setSlot(controlSlotPrevious, GuiElementBuilder.from(Items.ARROW.getDefaultStack().setCustomName(Text.literal("Previous Page")))
                .setCallback((x, y, z) -> {
                    paginatedSection.decrementPage();
                    paginatedSection.applyToGui(gui);
                }));

        gui.setSlot(controlSlotNext, GuiElementBuilder.from(Items.ARROW.getDefaultStack().setCustomName(Text.literal("Next Page")))
                .setCallback((x, y, z) -> {
                    paginatedSection.incremementPage();
                    paginatedSection.applyToGui(gui);
                }));
    }

    /**
     * Applies sorting buttons (Alphabetical, Category, Type) to the GUI.
     *
     * @param gui The GUI to which the sorting buttons are being applied.
     * @param rows The number of rows in the GUI.
     * @param alphabeticalCallback The callback for the alphabetical sorting button.
     * @param categoryCallback The callback for the category sorting button.
     * @param typeCallback The callback for the type sorting button.
     */
    public static void applySortingButtons(SimpleGui gui, int rows, Runnable alphabeticalCallback, Runnable categoryCallback, Runnable typeCallback) {
        int sortSlotAlpha = (rows - 1) * 9 + 3;
        int sortSlotCategory = (rows - 1) * 9 + 4;
        int sortSlotType = (rows - 1) * 9 + 5;

        gui.setSlot(sortSlotAlpha, GuiElementBuilder.from(Items.PAPER.getDefaultStack().setCustomName(Text.literal("Alphabetical")))
                .setCallback((x, y, z) -> alphabeticalCallback.run()));

        gui.setSlot(sortSlotCategory, GuiElementBuilder.from(Items.NAME_TAG.getDefaultStack().setCustomName(Text.literal("Category")))
                .setCallback((x, y, z) -> categoryCallback.run()));

        gui.setSlot(sortSlotType, GuiElementBuilder.from(Items.GOLD_INGOT.getDefaultStack().setCustomName(Text.literal("Type")))
                .setCallback((x, y, z) -> typeCallback.run()));
    }

    /**
     * Applies a back button to the GUI.
     *
     * @param gui The GUI to which the back button is being applied.
     * @param rows The number of rows in the GUI.
     * @param backCallback The callback for the back button.
     */
    public static void applyBackButton(SimpleGui gui, int rows, Runnable backCallback) {
        int backButtonSlot = (rows - 1) * 9 + 1;

        gui.setSlot(backButtonSlot, GuiElementBuilder.from(Items.BARRIER.getDefaultStack().setCustomName(Text.literal("Back")))
                .setCallback((x, y, z) -> backCallback.run()));
    }

    /**
     * Returns the appropriate ScreenHandlerType based on the number of rows.
     *
     * @param rows The number of rows in the GUI.
     * @return The ScreenHandlerType corresponding to the number of rows.
     */
    private static ScreenHandlerType<?> getScreenHandlerType(int rows) {
        switch (rows) {
            case 2:
                return ScreenHandlerType.GENERIC_9X2;
            case 3:
                return ScreenHandlerType.GENERIC_9X3;
            case 4:
                return ScreenHandlerType.GENERIC_9X4;
            case 5:
                return ScreenHandlerType.GENERIC_9X5;
            case 6:
                return ScreenHandlerType.GENERIC_9X6;
            default:
                throw new IllegalArgumentException("Invalid number of rows: " + rows);
        }
    }
}
