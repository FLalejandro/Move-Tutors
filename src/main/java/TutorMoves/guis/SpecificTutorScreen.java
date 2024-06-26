package TutorMoves.guis;

import TutorMoves.helper.SortingHelper;
import TutorMoves.util.EconUtil;
import TutorMoves.util.GuiUtil;
import TutorMoves.util.LangManager;
import TutorMoves.util.MoveUtil;
import TutorMoves.util.TutorYAMLReader;
import TutorMoves.util.ItemEconUtil;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
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
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import net.minecraft.util.Identifier;
import net.minecraft.util.Formatting;
import net.minecraft.registry.Registries;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        try {
            partyStore = Cobblemon.INSTANCE.getStorage().getParty(player.getUuid());
        } catch (NoPokemonStoreException e) {
            player.sendMessage(Text.literal("No Pokémon found in slot " + slot).formatted(Formatting.RED));
            return;
        }

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
        SimpleGui gui = GuiUtil.createGui(player, rows, guiTitle);

        Moves moves = Moves.INSTANCE;
        MoveUtil moveUtil = new MoveUtil(moves);

        BigDecimal price = new BigDecimal(tutorConfig.getCost());
        String currencyKey = tutorConfig.getCurrencyKey();

        List<MoveTemplate> moveTemplates = tutorConfig.getMoves();

        moveTemplates = SortingHelper.sortByOption(moveTemplates, currentSortOption);

        List<GuiElementBuilder> elements = moveTemplates.stream()
                .map(moveTemplate -> {
                    if (moveTemplate == null || moveTemplate.equals(MoveTemplate.Companion.dummy(moveTemplate.getName()))) {
                        return null;
                    }
                    ItemStack itemStack = moveUtil.getGemForMove(moveTemplate, price, currencyKey);
                    return GuiElementBuilder.from(itemStack).setCallback((x, y, z) -> {
                        try {
                            if (currencyKey.startsWith("ITEMS:")) {
                                List<ItemStack> requiredItems = ItemEconUtil.getRequiredItemsFromConfig(currencyKey, tutorConfig.getCost());
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

        Item fillerItemInstance = Registries.ITEM.get(new Identifier(fillerItem));
        if (fillerItemInstance == Items.AIR) {
            fillerItemInstance = Items.GRAY_STAINED_GLASS_PANE;
        }
        ItemStack fillerStack = new ItemStack(fillerItemInstance);

        PaginatedSection paginatedSection = new PaginatedSection(elements)
                .setSlotRanges(List.of(new SlotRange(0, rows * 9 - 10)))
                .setFillItem(GuiElementBuilder.from(fillerStack.setCustomName(Text.literal(""))));

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

        gui.open();
    }

}
