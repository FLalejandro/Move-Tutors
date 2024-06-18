package TutorMoves.guis;

import TutorMoves.helper.SortingHelper;
import TutorMoves.util.EconUtil;
import TutorMoves.util.LangManager;
import TutorMoves.util.MoveUtil;
import TutorMoves.util.TutorYAMLReader;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.kyori.adventure.audience.Audience;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import dev.roanoke.rib.utils.GuiUtils;
import dev.roanoke.rib.utils.PaginatedSection;
import dev.roanoke.rib.utils.SlotRange;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import net.minecraft.util.Formatting;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SpecificTutorScreen {

    private enum SortOption {
        ALPHABETICAL,
        CATEGORY,
        TYPE
    }

    private static SortOption currentSortOption = SortOption.ALPHABETICAL;

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
        int guiSize = rows * 9;
        ScreenHandlerType<?> screenHandlerType = getScreenHandlerType(rows);
        SimpleGui gui = new SimpleGui(screenHandlerType, player, false);

        gui.setTitle(Text.literal(tutorConfig.getName()));

        Moves moves = Moves.INSTANCE;
        MoveUtil moveUtil = new MoveUtil(moves);

        BigDecimal price = new BigDecimal(tutorConfig.getCost());

        List<MoveTemplate> moveTemplates = tutorConfig.getMoves();

        switch (currentSortOption) {
            case CATEGORY:
                moveTemplates = SortingHelper.sortByCategory(moveTemplates);
                break;
            case TYPE:
                moveTemplates = SortingHelper.sortByType(moveTemplates);
                break;
            case ALPHABETICAL:
            default:
                moveTemplates = SortingHelper.sortAlphabetically(moveTemplates);
                break;
        }

        List<GuiElementBuilder> elements = moveTemplates.stream()
                .map(moveTemplate -> {
                    if (moveTemplate == null || moveTemplate.equals(MoveTemplate.Companion.dummy(moveTemplate.getName()))) {
                        return null;
                    }
                    ItemStack itemStack = moveUtil.getGemForMove(moveTemplate, price);
                    return GuiElementBuilder.from(itemStack).setCallback((x, y, z) -> {
                        try {
                            EconUtil.openConfirmationWindow(player, moveTemplate, slot - 1, gui, price).open();
                        } catch (NoPokemonStoreException e) {
                            throw new RuntimeException(e);
                        }
                    });
                })
                .filter(element -> element != null)
                .collect(Collectors.toList());

        PaginatedSection paginatedSection = new PaginatedSection(elements)
                .setSlotRanges(List.of(new SlotRange(0, guiSize - 10)))
                .setFillItem(GuiElementBuilder.from(Items.GRAY_STAINED_GLASS_PANE.getDefaultStack().setCustomName(Text.literal(""))));

        applyPaginationControls(gui, paginatedSection, rows);
        paginatedSection.applyToGui(gui);
        applySortingButtons(gui, rows, slot, tutorFileName);

        gui.open();
    }

    private static void applyPaginationControls(SimpleGui gui, PaginatedSection paginatedSection, int rows) {
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

        GuiUtils.fillGUI(gui);
    }

    private static void applySortingButtons(SimpleGui gui, int rows, int slot, String tutorFileName) {
        int sortSlotAlpha = (rows - 1) * 9 + 3;
        int sortSlotCategory = (rows - 1) * 9 + 4;
        int sortSlotType = (rows - 1) * 9 + 5;

        gui.setSlot(sortSlotAlpha, GuiElementBuilder.from(Items.PAPER.getDefaultStack().setCustomName(Text.literal("Alphabetical")))
                .setCallback((x, y, z) -> {
                    currentSortOption = SortOption.ALPHABETICAL;
                    open(gui.getPlayer(), slot, tutorFileName);
                }));

        gui.setSlot(sortSlotCategory, GuiElementBuilder.from(Items.NAME_TAG.getDefaultStack().setCustomName(Text.literal("Category")))
                .setCallback((x, y, z) -> {
                    currentSortOption = SortOption.CATEGORY;
                    open(gui.getPlayer(), slot, tutorFileName);
                }));

        gui.setSlot(sortSlotType, GuiElementBuilder.from(Items.GOLD_INGOT.getDefaultStack().setCustomName(Text.literal("Type")))
                .setCallback((x, y, z) -> {
                    currentSortOption = SortOption.TYPE;
                    open(gui.getPlayer(), slot, tutorFileName);
                }));
    }

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
