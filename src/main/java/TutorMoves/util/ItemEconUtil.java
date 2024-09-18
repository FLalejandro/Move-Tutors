package TutorMoves.util;

import TutorMoves.helper.MoveTeacher;
import TutorMoves.util.ribStuff.GuiUtils;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.kyori.adventure.audience.Audience;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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
        if (hasRequiredItems(player, requiredItems)) {
            if (MoveTeacher.teachMove(player, slot, move)) {
                removeRequiredItems(player, requiredItems);
                LangManager.send((Audience) player, "Successful-Tutor", Map.of(
                        "{pokemon}", player.getName().getString(),
                        "{move}", move.getDisplayName().getString()
                ));
                return true;
            }
        } else {
            String requiredItemNames = getRequiredItemNames(requiredItems);
            LangManager.send((Audience) player, "Insufficient-Items", Map.of("{itemcost}", requiredItemNames));
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
     * @return The confirmation GUI.
     */
    public static SimpleGui openConfirmationWindow(ServerPlayerEntity player, MoveTemplate moveTemplate, int slot, SimpleGui oldGui, List<ItemStack> requiredItems) throws NoPokemonStoreException {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Confirm Purchase"));

        MoveUtil moveUtil = new MoveUtil(Moves.INSTANCE);
        ItemStack itemStack = moveUtil.getGemForMove(moveTemplate, new BigDecimal(requiredItems.get(0).getCount()), requiredItems.get(0).getItem().toString());

        gui.setSlot(13, GuiElementBuilder.from(itemStack).build());

        gui.setSlot(11, GuiElementBuilder.from(Items.GREEN_WOOL.getDefaultStack())
                .setName(Text.literal("§aConfirm"))
                .setCallback((x, y, z) -> {
                    if (purchaseMove(player, moveTemplate, slot, requiredItems)) {
                        LangManager.send((Audience) player, "Successful-Tutor", Map.of("{pokemon}", player.getName().getString(),
                                "{move}", moveTemplate.getDisplayName().getString()));
                    }
                    oldGui.open();
                }));

        gui.setSlot(15, GuiElementBuilder.from(Items.RED_WOOL.getDefaultStack())
                .setName(Text.literal("§cCancel"))
                .setCallback((x, y, z) -> oldGui.open()));

        GuiUtils.fillGUI(gui);
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

        if (!item.getName().getString().equals(shopItem.getName().getString())) {
            return false;
        }

        if (item.hasNbt() && shopItem.hasNbt()) {
            return item.getNbt().equals(shopItem.getNbt());
        }

        return !item.hasNbt() && !shopItem.hasNbt();
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
        return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : sb.toString();
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

        // Correctly parse the item ID from the currency key
        String itemId = currencyKey.substring("ITEMS:".length());
        Item item = Registries.ITEM.get(new Identifier(itemId));
        return List.of(new ItemStack(item, overriddenCost));
    }
}
