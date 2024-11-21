package TutorMoves.guis;

import TutorMoves.helper.SortingHelper;
import TutorMoves.util.*;
import TutorMoves.guis.util.*;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.kyori.adventure.audience.Audience;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SpecificTutorScreen {

    private static final Logger LOGGER = Logger.getLogger(SpecificTutorScreen.class.getName());
    private static SortingHelper.SortOption currentSortOption = SortingHelper.SortOption.ALPHABETICAL;

    /**
     * Opens the specific tutor move GUI for the player.
     *
     * @param player The player to open the GUI for.
     * @param slot   The slot of the Pokémon (1-based index).
     * @param tutorFileName The name of the specific tutor file.
     */
    public static void open(ServerPlayerEntity player, int slot, String tutorFileName) {

        PlayerPartyStore partyStore;
        partyStore = Cobblemon.INSTANCE.getStorage().getParty(player);

        Pokemon pokemon = partyStore.get(slot - 1);

        if (pokemon == null) {
            player.sendMessage(Text.literal("No Pokémon found in slot " + slot).formatted(Formatting.RED));
            return;
        }

        // Fetch the tutor configuration from the YAML file
        TutorYAMLReader.TutorConfig tutorConfig;
        try {
            tutorConfig = TutorYAMLReader.readTutorFile(tutorFileName);
        } catch (Exception e) {
            TutorYAMLReader.sendFeedback(player, "Error reading tutor file: " + tutorFileName);
            return;
        }

        // Check for blacklisted Pokémon
        if (tutorConfig.getBlacklistedPokemon().contains(pokemon.getSpecies().getName().toLowerCase())) {
            LangManager.send((Audience) player, "Blacklisted-Pokemon", Map.of("{pokemon}", pokemon.getSpecies().getName()));
            return;
        }

        List<String> missingMoves = tutorConfig.getMoves().stream()
                .filter(moveTemplate -> Objects.equals(moveTemplate, MoveTemplate.Companion.dummy(moveTemplate.getName())))
                .map(MoveTemplate::getName)
                .collect(Collectors.toList());

        if (!missingMoves.isEmpty()) {
            TutorYAMLReader.sendFeedback(player, "Moves not found: " + String.join(", ", missingMoves));
        }

        int rows = tutorConfig.getSize();

        // Fetch GUI settings from the configuration
        String guiTitle = tutorConfig.getName();
        String fillerItem = tutorConfig.getFillerItem();

        // Create the GUI
        SimpleGui gui = GuiUtil.createGui(player, rows, tutorConfig.getName());

        Moves moves = Moves.INSTANCE;
        MoveUtil moveUtil = new MoveUtil(moves);

        BigDecimal defaultPrice = new BigDecimal(tutorConfig.getCost());
        String currencyKey = tutorConfig.getCurrencyKey();

        // Fetch the move overrides from the tutor configuration
        Map<String, Integer> moveOverrides = tutorConfig.getMoveOverrides();

        for (Map.Entry<String, Integer> entry : moveOverrides.entrySet()) {
            String moveName = entry.getKey().toLowerCase();
            Integer cost = entry.getValue();

        }

        List<MoveTemplate> moveTemplates = tutorConfig.getMoves();
        moveTemplates = SortingHelper.sortByOption(moveTemplates, currentSortOption);

        List<GuiElementBuilder> elements = moveTemplates.stream()
                .map(moveTemplate -> {
                    if (moveTemplate == null || moveTemplate.equals(MoveTemplate.Companion.dummy(moveTemplate.getName()))) {
                        return null;
                    }

                    // Determine the price of the move, considering overrides
                    String moveName = moveTemplate.getName().toLowerCase();
                    BigDecimal price = getMoveOverrideCost(moveName, defaultPrice, moveOverrides);

                    GuiElementBuilder elementBuilder = moveUtil.getGemForMove(moveTemplate, price, currencyKey);
                    return elementBuilder.setCallback((x, y, z) -> {
                        try {
                            if (currencyKey.startsWith("ITEMS:")) {
                                List<ItemStack> requiredItems = ItemEconUtil.getRequiredItemsFromConfig(currencyKey, tutorConfig.getCost(), moveOverrides, moveName);
                                ItemEconUtil.openConfirmationWindow(player, moveTemplate, slot - 1, gui, requiredItems).open();
                            } else {
                                EconUtil.openConfirmationWindow(player, moveTemplate, slot - 1, gui, price, currencyKey).open();
                            }
                        } catch (NoPokemonStoreException e) {
                            throw new RuntimeException(e);
                        }
                    });
                })
                .filter(element -> element != null)
                .collect(Collectors.toList());

        Item fillerItemInstance = Registries.ITEM.get(Identifier.of(fillerItem));
        if (fillerItemInstance == Items.AIR) {
            fillerItemInstance = Items.GRAY_STAINED_GLASS_PANE;
        }
        ItemStack fillerStack = new ItemStack(fillerItemInstance);

        PaginatedSection paginatedSection = new PaginatedSection(elements)
                .setSlotRanges(List.of(new SlotRange(0, rows * 9 - 10)))
                .setFillItem(GuiElementBuilder.from(fillerStack));

        GuiUtil.applyPaginationControls(gui, paginatedSection, rows);
        paginatedSection.applyToGui(gui);

        GuiUtil.applySortingButtons(gui, rows,
                () -> {
                    currentSortOption = SortingHelper.SortOption.ALPHABETICAL;
                    open(player, slot, tutorFileName);
                },
                () -> {
                    currentSortOption = SortingHelper.SortOption.CATEGORY;
                    open(player, slot, tutorFileName);
                },
                () -> {
                    currentSortOption = SortingHelper.SortOption.TYPE;
                    open(player, slot, tutorFileName);
                }
        );

        GuiUtil.applyBackButton(gui, rows, () -> SelectionScreen.open(player, Optional.of(tutorFileName)));

        gui.open();
    }

    /**
     * Retrieves the override cost for a specific move if it exists, otherwise returns the default cost.
     *
     * @param moveName The name of the move.
     * @param defaultCost The default cost for moves.
     * @param moveOverrides The map containing override costs for specific moves.
     * @return The override cost if defined, otherwise the default cost.
     */
    private static BigDecimal getMoveOverrideCost(String moveName, BigDecimal defaultCost, Map<String, Integer> moveOverrides) {
        return moveOverrides.containsKey(moveName) ? BigDecimal.valueOf(moveOverrides.get(moveName)) : defaultCost;
    }
}
