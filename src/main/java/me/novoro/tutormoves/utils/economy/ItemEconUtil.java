package me.novoro.tutormoves.utils.economy;

import me.novoro.tutormoves.config.ConfigManager;
import me.novoro.tutormoves.config.MoveOptions;
import me.novoro.tutormoves.helper.MoveTeacher;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.novoro.tutormoves.utils.ColorUtil;
import me.novoro.tutormoves.utils.GuiUtil;
import me.novoro.tutormoves.config.LangManager;
import me.novoro.tutormoves.utils.ItemBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ItemEconUtil {

    /**
     * Checks if the player has the required items.
     *
     * @param player The player to check.
     * @param requiredItems The items required for the transaction.
     * @return True if the player has the required items, false otherwise.
     */
    public static boolean hasRequiredItems(ServerPlayerEntity player, List<ItemStack> requiredItems) {
        for (ItemStack requiredItem : requiredItems) {
            int count = 0;
            for (ItemStack inventoryItem : player.getInventory().main) {
                if (isSameItem(inventoryItem, requiredItem)) {
                    count += inventoryItem.getCount();
                }
            }
            if (count < requiredItem.getCount()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes the required items from the player's inventory.
     *
     * @param player The player to remove items from.
     * @param requiredItems The items to remove.
     */
    public static void removeRequiredItems(ServerPlayerEntity player, List<ItemStack> requiredItems) {
        for (ItemStack requiredItem : requiredItems) {
            int remaining = requiredItem.getCount();
            for (int slot = 0; slot < player.getInventory().size(); slot++) {
                ItemStack inventoryItem = player.getInventory().getStack(slot);
                if (isSameItem(inventoryItem, requiredItem) && remaining > 0) {
                    int toRemove = Math.min(inventoryItem.getCount(), remaining);
                    inventoryItem.decrement(toRemove);
                    remaining -= toRemove;
                    if (remaining <= 0) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Processes the purchase of a move using items.
     *
     * @param player The player purchasing the move.
     * @param move The move being purchased.
     * @param slot The slot of the Pokémon.
     * @param requiredItems The items required for the transaction.
     * @return True if the purchase was successful, false otherwise.
     */
    public static boolean purchaseMove(ServerPlayerEntity player, MoveTemplate move, int slot, List<ItemStack> requiredItems) {
        return purchaseMove(player, move, slot, requiredItems, MoveOptions.fromGlobal());
    }

    public static boolean purchaseMove(ServerPlayerEntity player, MoveTemplate move, int slot, List<ItemStack> requiredItems, MoveOptions options) {
        if (hasRequiredItems(player, requiredItems)) {
            if (MoveTeacher.teachMove(player, slot, move, options)) {
                removeRequiredItems(player, requiredItems);
                LangManager.sendLang(player, "Successful-Tutor", Map.of(
                        "{pokemon}", player.getName().getString(),
                        "{move}", move.getDisplayName().getString()
                ));
                return true;
            }
        } else {
            String requiredItemNames = getRequiredItemNames(requiredItems);
            LangManager.sendLang(player, "Insufficient-Items", Map.of("{itemcost}", requiredItemNames));
            return false;
        }
        return false;
    }

    /**
     * Opens a confirmation window to handle the purchase of a move using items.
     *
     * @param player The player who initiated the purchase.
     * @param moveTemplate The move template to purchase.
     * @param slot The slot of the Pokémon.
     * @param oldGui The previous GUI.
     * @param requiredItems The items required for the transaction.
     * @param currencyKey The key defining the currency item.
     * @return The confirmation GUI.
     */
    public static SimpleGui openConfirmationWindow(ServerPlayerEntity player, MoveTemplate moveTemplate, int slot, SimpleGui oldGui, List<ItemStack> requiredItems, String currencyKey, String currencyName) throws NoPokemonStoreException {
        return openConfirmationWindow(player, moveTemplate, slot, oldGui, requiredItems, MoveOptions.fromGlobal(), currencyKey, currencyName);
    }

    public static SimpleGui openConfirmationWindow(ServerPlayerEntity player, MoveTemplate moveTemplate, int slot, SimpleGui oldGui, List<ItemStack> requiredItems, MoveOptions options, String currencyKey, String currencyName) throws NoPokemonStoreException {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        String guiTitle = ConfigManager.getConfirmationGuiTitle();
        gui.setTitle(ColorUtil.parseColourToText(guiTitle));

        ItemBuilder moveUtil = new ItemBuilder(Moves.INSTANCE);
        ItemStack itemStack = moveUtil.getGemForMove(moveTemplate, new BigDecimal(requiredItems.getFirst().getCount()), currencyKey, currencyName).asStack();

        int displaySlot = ConfigManager.getDisplaySlot() >= 0 ? ConfigManager.getDisplaySlot() : 13;
        int confirmSlot = ConfigManager.getConfirmSlot() >= 0 ? ConfigManager.getConfirmSlot() : 11;
        int cancelSlot = ConfigManager.getCancelSlot() >= 0 ? ConfigManager.getCancelSlot() : 15;

        gui.setSlot(displaySlot, GuiElementBuilder.from(itemStack).build());

        gui.setSlot(confirmSlot, GuiElementBuilder.from(ConfigManager.getConfirmItem())
                .setName(ColorUtil.parseColourToText(ConfigManager.getConfirmName()))
                .setCallback((x, y, z) -> {
                    if (purchaseMove(player, moveTemplate, slot, requiredItems, options)) {
                        LangManager.sendLang(player, "Successful-Tutor", Map.of("{pokemon}", player.getName().getString(),
                                "{move}", moveTemplate.getDisplayName().getString()));
                    }
                    oldGui.open();
                }));

        gui.setSlot(cancelSlot, GuiElementBuilder.from(ConfigManager.getCancelItem())
                .setName(ColorUtil.parseColourToText(ConfigManager.getCancelName()))
                .setCallback((x, y, z) -> oldGui.open()));

        GuiUtil.fillGUI(gui);
        return gui;
    }

    /**
     * Determines if two items are the same based on item type and metadata.
     *
     * @param item The first item to compare.
     * @param shopItem The second item to compare.
     * @return True if the items are the same, false otherwise.
     */
    private static boolean isSameItem(ItemStack item, ItemStack shopItem) {
        if (item.getItem() != shopItem.getItem()) {
            return false;
        }

        // Compare custom model data specifically instead of all components
        if (shopItem.getComponents().contains(DataComponentTypes.CUSTOM_MODEL_DATA)) {
            if (!item.getComponents().contains(DataComponentTypes.CUSTOM_MODEL_DATA)) {
                return false;
            }
            return item.get(DataComponentTypes.CUSTOM_MODEL_DATA)
                    .equals(shopItem.get(DataComponentTypes.CUSTOM_MODEL_DATA));
        }

        // Shop item has no CMD requirement — any item of this type matches
        return true;
    }

    /**
     * Retrieves a formatted string of required item names and quantities.
     *
     * @param requiredItems The list of required items.
     * @return A formatted string of item names and quantities.
     */
    private static String getRequiredItemNames(List<ItemStack> requiredItems) {
        StringBuilder sb = new StringBuilder();
        for (ItemStack itemStack : requiredItems) {
            sb.append(itemStack.getCount())
                    .append(" ")
                    .append(itemStack.getName().getString())
                    .append(itemStack.getCount() > 1 ? "s" : "")
                    .append(", ");
        }
        return !sb.isEmpty() ? sb.substring(0, sb.length() - 2) : sb.toString();
    }

    /**
     * Fetches the required items from the configuration for item-based transactions.
     *
     * @param currencyKey The key defining the currency item.
     * @param cost The default cost if no override is found.
     * @param moveName The name of the move for which to fetch required items.
     * @param overrides The map of move-specific item cost overrides.
     * @return List of required ItemStacks.
     */
    public static List<ItemStack> getRequiredItemsFromConfig(String currencyKey, int cost, Map<String, Integer> overrides, String moveName) {
        // Check for move-specific overrides and return the required items accordingly
        int overriddenCost = overrides.getOrDefault(moveName.toLowerCase(), cost);

        // Correctly parse the item ID and optional custom model data from the currency key
        String remainder = currencyKey.substring("ITEMS:".length());
        String[] parts = remainder.split(":");
        String itemId;
        int customModelData = 0;
        if (parts.length >= 3) {
            itemId = parts[0] + ":" + parts[1];
            try {
                customModelData = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                itemId = remainder;
            }
        } else {
            itemId = remainder;
        }
        Item item = Registries.ITEM.get(Identifier.of(itemId));
        ItemStack stack = new ItemStack(item, overriddenCost);
        if (customModelData != 0) {
            stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(customModelData));
        }
        return List.of(stack);
    }
}
