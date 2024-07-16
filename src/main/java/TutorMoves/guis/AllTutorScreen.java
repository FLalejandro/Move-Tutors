package TutorMoves.guis;

import TutorMoves.TutorMoves;
import TutorMoves.helper.SortingHelper;
import TutorMoves.util.*;
import TutorMoves.util.PokemonUtil;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.pokemon.Pokemon;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.kyori.adventure.audience.Audience;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import dev.roanoke.rib.utils.PaginatedSection;
import dev.roanoke.rib.utils.SlotRange;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AllTutorScreen {

    private static SortingHelper.SortOption currentSortOption = SortingHelper.SortOption.ALPHABETICAL;

    /**
     * Opens the tutor move GUI for the player.
     *
     * @param player The player to open the GUI for.
     * @param slot The slot of the Pokémon (1-based index).
     */
    public static void open(ServerPlayerEntity player, int slot) throws NoPokemonStoreException {

        // Fetch tutor moves for the Pokémon in the specified slot
        List<String> tutorMoves = PokemonUtil.getTutorMovesForSlot(player, slot);

        // Fetch the Pokémon in the specified slot
        Pokemon pokemon = PokemonUtil.getPokemonInSlot(player, slot);

        if (tutorMoves.isEmpty()) {
            LangManager.send((Audience) player, "Error-No-Tutor-Moves", Map.of("{pokemon}", pokemon.getSpecies().getName()));
            return;
        }

        // Check for blacklisted Pokémon
        List<String> blacklistedPokemon = TutorMoves.getMainConfig().getStringList("TutorMoves.Blacklisted-Pokemon");
        if (blacklistedPokemon.contains(pokemon.getSpecies().getName().toLowerCase())) {
            LangManager.send((Audience) player, "Blacklisted-Pokemon", Map.of("{pokemon}", pokemon.getSpecies().getName()));
            return;
        }

        // Fetch GUI settings from the configuration
        int rows = TutorMoves.getMainConfig().getInt("GUI.size");
        if (rows < 2 || rows > 6) {
            rows = 6; // Default value if configuration is invalid
        }
        String currencyKey = TutorMoves.getMainConfig().getString("TutorMoves.currencyKey");
        BigDecimal price = new BigDecimal(TutorMoves.getMainConfig().getInt("TutorMoves.cost"));
        String guiTitle = TutorMoves.getMainConfig().getString("GUI.title");
        String fillerItem = TutorMoves.getMainConfig().getString("GUI.filler-item");

        // Create the GUI
        SimpleGui gui = GuiUtil.createGui(player, rows, guiTitle);

        // Create an instance of Moves and MoveUtil
        Moves moves = Moves.INSTANCE;
        MoveUtil moveUtil = new MoveUtil(moves);

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
                    ItemStack itemStack = moveUtil.getGemForMove(moveTemplate, price, currencyKey);
                    return GuiElementBuilder.from(itemStack)
                            .setCallback((x, y, z) -> {
                                try {
                                    if (currencyKey.startsWith("ITEMS:")) {
                                        List<ItemStack> requiredItems = ItemEconUtil.getRequiredItemsFromConfig(currencyKey, TutorMoves.getMainConfig().getInt("TutorMoves.cost"));
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

        // Get filler item from the configuration
        Item fillerItemInstance = Registries.ITEM.get(new Identifier(fillerItem));
        if (fillerItemInstance == Items.AIR) {
            fillerItemInstance = Items.GRAY_STAINED_GLASS_PANE; // Default fallback item
        }
        ItemStack fillerStack = new ItemStack(fillerItemInstance);

        // Create PaginatedSection with configurable filler item
        PaginatedSection paginatedSection = new PaginatedSection(elements)
                .setSlotRanges(List.of(new SlotRange(0, rows * 9 - 10)))
                .setFillItem(GuiElementBuilder.from(fillerStack.setCustomName(Text.literal(""))));

        // Fill the GUI and add pagination controls
        GuiUtil.applyPaginationControls(gui, paginatedSection, rows);

        // Apply the initial page to the GUI
        paginatedSection.applyToGui(gui);

        // Add sorting buttons
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
}
