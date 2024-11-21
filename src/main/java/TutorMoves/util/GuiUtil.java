package TutorMoves.util;

import TutorMoves.guis.util.*;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Locale;

import static net.minecraft.item.Items.BLACK_STAINED_GLASS_PANE;

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

        // Parse the title string to apply colors and styles using MiniMessage
        Text parsedTitle = parseFormattedTitle(title);
        gui.setTitle(parsedTitle);
        return gui;
    }

    private static MutableText parseFormattedTitle(String title) {
        for (char c : "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".toCharArray()) {
            String legacyCode = "§" + c;
            String replacement = ColorUtil.getLegacyReplacement(String.valueOf(c));
            title = title.replace(legacyCode, replacement);
        }
        return Text.literal(title);
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

        gui.setSlot(controlSlotPrevious, GuiElementBuilder.from(createArrowItem("Previous Page"))
                .setCallback((x, y, z) -> {
                    paginatedSection.decrementPage();
                    paginatedSection.applyToGui(gui);
                }).build());

        gui.setSlot(controlSlotNext, GuiElementBuilder.from(createArrowItem("Next Page"))
                .setCallback((x, y, z) -> {
                    paginatedSection.incrementPage();
                    paginatedSection.applyToGui(gui);
                }).build());
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

        gui.setSlot(sortSlotAlpha, GuiElementBuilder.from(createItem(Items.PAPER.getDefaultStack(), "Alphabetical"))
                .setCallback((x, y, z) -> alphabeticalCallback.run()).build());

        gui.setSlot(sortSlotCategory, GuiElementBuilder.from(createItem(Items.NAME_TAG.getDefaultStack(), "Category"))
                .setCallback((x, y, z) -> categoryCallback.run()).build());

        gui.setSlot(sortSlotType, GuiElementBuilder.from(createItem(Items.GOLD_INGOT.getDefaultStack(), "Type"))
                .setCallback((x, y, z) -> typeCallback.run()).build());
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

        gui.setSlot(backButtonSlot, GuiElementBuilder.from(createItem(Items.BARRIER.getDefaultStack(), "Back"))
                .setCallback((x, y, z) -> backCallback.run()).build());
    }

    /**
     * Creates an ItemStack with a custom name using Components for Minecraft 1.21.1.
     *
     * @param item The base item to modify.
     * @param name The custom name to apply to the item.
     * @return The modified ItemStack with the custom name.
     */
    private static ItemStack createItem(ItemStack item, String name) {
        ItemStack stack = new ItemStack(item.getItem());
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
        return stack;
    }

    /**
     * Creates an arrow ItemStack with a custom name using NBT components.
     *
     * @param name The custom name to apply to the arrow.
     * @return The modified ItemStack.
     */
    private static ItemStack createArrowItem(String name) {
        return createItem(Items.ARROW.getDefaultStack(), name);
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

    public static void fillGUI(SimpleGui gui) {
        int freeslot = gui.getFirstEmptySlot();
        while (freeslot != -1) {
            gui.setSlot(freeslot, GuiElementBuilder.from(BLACK_STAINED_GLASS_PANE.getDefaultStack()).setName(Text.literal("")));
            freeslot = gui.getFirstEmptySlot();
        }
    }
}
