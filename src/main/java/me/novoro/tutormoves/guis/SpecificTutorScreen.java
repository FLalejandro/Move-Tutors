package me.novoro.tutormoves.guis;

import me.novoro.tutormoves.config.MoveOptions;
import me.novoro.tutormoves.config.TutorYAMLReader;
import me.novoro.tutormoves.helper.SortingHelper;
import me.novoro.tutormoves.config.LangManager;
import me.novoro.tutormoves.utils.*;
import me.novoro.tutormoves.guis.util.*;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.novoro.tutormoves.utils.economy.EconUtil;
import me.novoro.tutormoves.utils.economy.ItemEconUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpecificTutorScreen {

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

        // Check for blacklisted Pokémon
        if (TutorYAMLReader.getTutorBlacklistedPokemon(tutorFileName).contains(pokemon.getSpecies().getName().toLowerCase())) {
            LangManager.sendLang(player, "Blacklisted-Pokemon", Map.of("{pokemon}", pokemon.getSpecies().getName()));
            return;
        }

        // Fetch GUI settings from the configuration
        int rows = TutorYAMLReader.getTutorSize(tutorFileName);
        String currencyKey = TutorYAMLReader.getTutorCurrencyKey(tutorFileName);
        BigDecimal defaultPrice = BigDecimal.valueOf(TutorYAMLReader.getTutorCost(tutorFileName));
        String guiTitle = TutorYAMLReader.getTutorTitle(tutorFileName);
        String fillerItem = TutorYAMLReader.getTutorFillerItem(tutorFileName);

        // Fetch tutor-specific settings
        MoveOptions effectiveOptions = TutorYAMLReader.getEffectiveMoveOptions(tutorFileName);
        List<String> blacklistedMoves = TutorYAMLReader.getTutorBlacklistedMoves(tutorFileName);

        // Create the GUI
        SimpleGui gui = GuiUtil.createGui(player, rows, ColorUtil.parseColourToText(guiTitle));

        Moves moves = Moves.INSTANCE;
        ItemBuilder moveUtil = new ItemBuilder(moves);

        // Fetch the move overrides from the tutor configuration
        Map<String, Integer> moveOverrides = TutorYAMLReader.getTutorMoveOverrides(tutorFileName);

        List<MoveTemplate> moveTemplates;

        if (TutorYAMLReader.isGeneralMode(tutorFileName)) {
            List<String> moveNames;
            try {
                moveNames = PokemonUtil.getMovesForSlot(player, slot, effectiveOptions, blacklistedMoves);
            } catch (NoPokemonStoreException e) {
                player.sendMessage(Text.literal("No Pokémon found in slot " + slot).formatted(Formatting.RED));
                return;
            }
            moveTemplates = moveNames.stream()
                    .map(name -> Moves.INSTANCE.getByName(name))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            moveTemplates = new ArrayList<>(TutorYAMLReader.getTutorMoves(tutorFileName));
            for (String type : TutorYAMLReader.getTutorTypeFilters(tutorFileName)) {
                moveTemplates.addAll(PokemonUtil.getLearnableMovesOfType(pokemon, type, effectiveOptions));
            }
            moveTemplates = moveTemplates.stream()
                    .distinct()
                    .filter(move -> PokemonUtil.isMoveLearnable(pokemon, move, effectiveOptions))
                    .filter(move -> !blacklistedMoves.contains(move.getName().toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (moveTemplates.isEmpty()) {
            LangManager.sendLang(player, "Error-No-Tutor-Moves", Map.of("{pokemon}", pokemon.getSpecies().getName()));
            return;
        }

        moveTemplates = SortingHelper.sortByOption(moveTemplates, currentSortOption);

        List<GuiElementBuilder> elements = moveTemplates.stream()
                .map(moveTemplate -> {
                    if (moveTemplate == null || moveTemplate.equals(MoveTemplate.Companion.dummy(moveTemplate.getName()))) {
                        return null;
                    }

                    // Determine the price of the move, considering overrides
                    String moveName = moveTemplate.getName().toLowerCase();
                    BigDecimal price = ItemBuilder.getMoveOverrideCost(moveName, defaultPrice, moveOverrides);

                    GuiElementBuilder elementBuilder = moveUtil.getGemForMove(moveTemplate, price, currencyKey);
                    return elementBuilder.setCallback((x, y, z) -> {
                        try {
                            if (currencyKey.startsWith("ITEMS:")) {
                                List<ItemStack> requiredItems = ItemEconUtil.getRequiredItemsFromConfig(currencyKey, (int) TutorYAMLReader.getTutorCost(tutorFileName), moveOverrides, moveName);
                                ItemEconUtil.openConfirmationWindow(player, moveTemplate, slot - 1, gui, requiredItems, effectiveOptions).open();
                            } else {
                                EconUtil.openConfirmationWindow(player, moveTemplate, slot - 1, gui, price, currencyKey, effectiveOptions).open();
                            }
                        } catch (NoPokemonStoreException e) {
                            throw new RuntimeException(e);
                        }
                    });
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Item fillerItemInstance = Registries.ITEM.get(Identifier.of(fillerItem));

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

}
