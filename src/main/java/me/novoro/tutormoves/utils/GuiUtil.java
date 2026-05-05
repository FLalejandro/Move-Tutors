package me.novoro.tutormoves.utils;

import me.novoro.tutormoves.config.ConfigManager;
import me.novoro.tutormoves.guis.util.*;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class GuiUtil {

    /**
     * Creates a SimpleGui with the given number of rows and title.
     *
     * @param player The player for whom the GUI is being created.
     * @param rows The number of rows in the GUI.
     * @param title The title of the GUI as a Text component.
     * @return The created SimpleGui.
     */
    public static SimpleGui createGui(ServerPlayerEntity player, int rows, Text title) {
        ScreenHandlerType<?> screenHandlerType = getScreenHandlerType(rows);
        SimpleGui gui = new SimpleGui(screenHandlerType, player, false);
        gui.setTitle(title);
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
        int controlSlotPrevious = ConfigManager.getPreviousPageSlot() >= 0
                ? ConfigManager.getPreviousPageSlot()
                : (rows - 1) * 9;
        int controlSlotNext = ConfigManager.getNextPageSlot() >= 0
                ? ConfigManager.getNextPageSlot()
                : (rows - 1) * 9 + 8;

        gui.setSlot(controlSlotPrevious, GuiElementBuilder.from(createItem(ConfigManager.getPreviousPageItem(), ConfigManager.getPreviousPageName()))
                .setCallback((x, y, z) -> {
                    paginatedSection.decrementPage();
                    paginatedSection.applyToGui(gui);
                }).build());

        gui.setSlot(controlSlotNext, GuiElementBuilder.from(createItem(ConfigManager.getNextPageItem(), ConfigManager.getNextPageName()))
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
        int sortSlotAlpha = ConfigManager.getAlphabeticalSortSlot() >= 0
                ? ConfigManager.getAlphabeticalSortSlot()
                : (rows - 1) * 9 + 3;
        int sortSlotCategory = ConfigManager.getCategorySortSlot() >= 0
                ? ConfigManager.getCategorySortSlot()
                : (rows - 1) * 9 + 4;
        int sortSlotType = ConfigManager.getTypeSortSlot() >= 0
                ? ConfigManager.getTypeSortSlot()
                : (rows - 1) * 9 + 5;

        gui.setSlot(sortSlotAlpha, GuiElementBuilder.from(createItem(ConfigManager.getAlphabeticalSortItem(), ConfigManager.getAlphabeticalSortName()))
                .setCallback((x, y, z) -> alphabeticalCallback.run()).build());

        gui.setSlot(sortSlotCategory, GuiElementBuilder.from(createItem(ConfigManager.getCategorySortItem(), ConfigManager.getCategorySortName()))
                .setCallback((x, y, z) -> categoryCallback.run()).build());

        gui.setSlot(sortSlotType, GuiElementBuilder.from(createItem(ConfigManager.getTypeSortItem(), ConfigManager.getTypeSortName()))
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
        int backButtonSlot = ConfigManager.getExitSlot() >= 0
                ? ConfigManager.getExitSlot()
                : (rows - 1) * 9 + 1;
        gui.setSlot(backButtonSlot, GuiElementBuilder.from(createItem(ConfigManager.getExitItem(), ConfigManager.getExitName()))
                .setCallback((x, y, z) -> backCallback.run()).build());
    }

    /**
     * Creates an ItemStack with a custom name using Components for Minecraft 1.21.1.
     *
     * @param item The base item to modify.
     * @param name The custom name to apply to the item (supports color codes).
     * @return The modified ItemStack with the custom name.
     */
    private static ItemStack createItem(ItemStack item, String name) {
        ItemStack stack = item.copy();
        stack.set(DataComponentTypes.CUSTOM_NAME, ColorUtil.parseColourToText(name));
        return stack;
    }

    /**
     * Returns the appropriate ScreenHandlerType based on the number of rows.
     *
     * @param rows The number of rows in the GUI.
     * @return The ScreenHandlerType corresponding to the number of rows.
     */
    private static ScreenHandlerType<?> getScreenHandlerType(int rows) {
        return switch (rows) {
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            case 6 -> ScreenHandlerType.GENERIC_9X6;
            default -> throw new IllegalArgumentException("Invalid number of rows: " + rows);
        };
    }

    public static void fillGUI(SimpleGui gui) {
        int freeslot = gui.getFirstEmptySlot();
        while (freeslot != -1) {
            gui.setSlot(freeslot, GuiElementBuilder.from(ConfigManager.getConfirmationFillerItem()).setName(Text.literal("")));
            freeslot = gui.getFirstEmptySlot();
        }
    }

}