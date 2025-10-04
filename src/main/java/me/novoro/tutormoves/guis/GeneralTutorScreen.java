package me.novoro.tutormoves.guis;

import me.novoro.tutormoves.config.ConfigManager;
import me.novoro.tutormoves.guis.util.PaginatedSection;
import me.novoro.tutormoves.guis.util.SlotRange;
import me.novoro.tutormoves.helper.SortingHelper;
import me.novoro.tutormoves.config.LangManager;
import me.novoro.tutormoves.utils.*;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.pokemon.Pokemon;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.novoro.tutormoves.utils.economy.EconUtil;
import me.novoro.tutormoves.utils.economy.ItemEconUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class GeneralTutorScreen {

    private static SortingHelper.SortOption currentSortOption = SortingHelper.SortOption.ALPHABETICAL;

    /**
     * Opens the tutor move GUI for the player.
     *
     * @param player The player to open the GUI for.
     * @param slot The slot of the Pokémon (1-based index).
     */
    public static void open(ServerPlayerEntity player, int slot) throws NoPokemonStoreException {

        // Fetch tutor moves for the Pokémon in the specified slot
        List<String> tutorMoves = PokemonUtil.getMovesForSlot(player, slot);

        // Fetch the Pokémon in the specified slot
        Pokemon pokemon = PokemonUtil.getPokemonInSlot(player, slot);

        if (tutorMoves.isEmpty()) {
            LangManager.sendLang(player, "Error-No-Tutor-Moves", Map.of("{pokemon}", pokemon.getSpecies().getName()));
            return;
        }

        // Check for blacklisted Pokémon
        if (ConfigManager.getPokemonBlacklist().contains(pokemon.getSpecies().getName().toLowerCase())) {
            LangManager.sendLang(player, "Blacklisted-Pokemon", Map.of("{pokemon}", pokemon.getSpecies().getName()));
            return;
        }

        // Fetch GUI settings from the configuration
        int rows = ConfigManager.getGeneralGuiSize();
        if (rows < 2 || rows > 6) {
            rows = 6; // Default value if configuration is invalid
        }
        String currencyKey = ConfigManager.getCurrencyKey();
        BigDecimal defaultPrice = BigDecimal.valueOf(ConfigManager.getCost());
        String guiTitle = ConfigManager.getGeneralGuiTitle();
        String fillerItem = ConfigManager.getGeneralFillerItem();

        // Fetch the move overrides from the configuration
        Map<String, Integer> moveOverrides = getStringIntegerMap();

        // Create the GUI
        SimpleGui gui = GuiUtil.createGui(player, rows, ColorUtil.parseColourToText(guiTitle));

        // Create an instance of Moves and ItemBuilder
        Moves moves = Moves.INSTANCE;
        ItemBuilder moveUtil = new ItemBuilder(moves);

        // Create MoveTemplate list and apply sorting
        List<MoveTemplate> moveTemplates = tutorMoves.stream()
                .map(moves::getByNameOrDummy)
                .collect(Collectors.toList());

        moveTemplates = SortingHelper.sortByOption(moveTemplates, currentSortOption);

        // Create GuiElementBuilder for each tutor move
        List<GuiElementBuilder> elements = moveTemplates.stream()
                .map(moveTemplate -> {
                    if (moveTemplate == null) {
                        return null;
                    }

                    // Determine the price of the move, considering overrides
                    String moveName = moveTemplate.getName().toLowerCase();
                    BigDecimal price = ItemBuilder.getMoveOverrideCost(moveName, defaultPrice, moveOverrides);

                    GuiElementBuilder elementBuilder = moveUtil.getGemForMove(moveTemplate, price, currencyKey);
                    return elementBuilder.setCallback((x, y, z) -> {
                                try {
                                    if (currencyKey.startsWith("ITEMS:")) {
                                        List<ItemStack> requiredItems = ItemEconUtil.getRequiredItemsFromConfig(currencyKey, defaultPrice.intValue(), moveOverrides, moveName);
                                        ItemEconUtil.openConfirmationWindow(player, moveTemplate, slot - 1, gui, requiredItems).open();
                                    } else {
                                        EconUtil.openConfirmationWindow(player, moveTemplate, slot - 1, gui, price, currencyKey).open();
                                    }
                                } catch (NoPokemonStoreException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Get filler item from the configuration
        Item fillerItemInstance = Registries.ITEM.get(Identifier.of((fillerItem)));

        ItemStack fillerStack = new ItemStack(fillerItemInstance);

        // Create PaginatedSection with configurable filler item
        PaginatedSection paginatedSection = new PaginatedSection(elements)
                .setSlotRanges(List.of(new SlotRange(0, rows * 9 - 10)))
                .setFillItem(GuiElementBuilder.from(fillerStack));


        // Fill the GUI and add pagination controls + sorting buttons
        GuiUtil.applyPaginationControls(gui, paginatedSection, rows);
        paginatedSection.applyToGui(gui);
        GuiUtil.applySortingButtons(gui, rows,
                () -> {
                    currentSortOption = SortingHelper.SortOption.ALPHABETICAL;
                    try {
                        open(player, slot);
                    } catch (NoPokemonStoreException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                    currentSortOption = SortingHelper.SortOption.CATEGORY;
                    try {
                        open(player, slot);
                    } catch (NoPokemonStoreException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                    currentSortOption = SortingHelper.SortOption.TYPE;
                    try {
                        open(player, slot);
                    } catch (NoPokemonStoreException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        // Add back button
        GuiUtil.applyBackButton(gui, rows, () -> SelectionScreen.open(player, Optional.empty()));

        gui.open();
    }

    private static @NotNull Map<String, Integer> getStringIntegerMap() {
        Map<String, Integer> overrides = ConfigManager.getMoveOverrides();
        Map<String, Integer> normalized = new HashMap<>();

        if (overrides != null) {
            for (Map.Entry<String, Integer> entry : overrides.entrySet()) {
                String moveName = entry.getKey().toLowerCase();
                Integer cost = entry.getValue();
                normalized.put(moveName, cost);
            }
        }
        return normalized;
    }
}
