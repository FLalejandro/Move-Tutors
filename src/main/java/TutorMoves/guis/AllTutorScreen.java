package TutorMoves.guis;

import TutorMoves.TutorMoves;
import TutorMoves.helper.SortingHelper;
import TutorMoves.util.EconUtil;
import TutorMoves.util.JSONUtil;
import TutorMoves.util.LangManager;
import TutorMoves.util.MoveUtil;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AllTutorScreen {

    private enum SortOption {
        ALPHABETICAL,
        CATEGORY,
        TYPE
    }

    private static SortOption currentSortOption = SortOption.ALPHABETICAL;

    /**
     * Opens the tutor move GUI for the player.
     *
     * @param player The player to open the GUI for.
     * @param slot The slot of the Pokémon (1-based index).
     */
    public static void open(ServerPlayerEntity player, int slot) throws NoPokemonStoreException {

        // Fetch tutor moves for the Pokémon in the specified slot
        List<String> tutorMoves = JSONUtil.getTutorMovesForSlot(player, slot);

        if (tutorMoves.isEmpty()) {
            LangManager.send((Audience) player, "Error-No-Pokemon", Map.of("{slot}", String.valueOf(slot)));
            return;
        }

        // Fetch the size and price from the configuration
        int rows = TutorMoves.getMainConfig().getInt("TutorMoves.size");
        BigDecimal price = new BigDecimal(TutorMoves.getMainConfig().getInt("TutorMoves.cost"));

        // Calculate the GUI size based on the number of rows
        int guiSize = rows * 9;
        ScreenHandlerType<?> screenHandlerType = getScreenHandlerType(rows);
        SimpleGui gui = new SimpleGui(screenHandlerType, player, false);

        gui.setTitle(Text.literal("Tutor Moves"));

        // Create an instance of Moves and MoveUtil
        Moves moves = Moves.INSTANCE;
        MoveUtil moveUtil = new MoveUtil(moves);

        // Create MoveTemplate list and apply sorting
        List<MoveTemplate> moveTemplates = tutorMoves.stream()
                .map(moves::getByNameOrDummy)
                .collect(Collectors.toList());

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

        // Create GuiElementBuilder for each tutor move
        List<GuiElementBuilder> elements = moveTemplates.stream()
                .map(moveTemplate -> {
                    if (moveTemplate == null) {
                        return null;
                    }
                    ItemStack itemStack = moveUtil.getGemForMove(moveTemplate, price);
                    return GuiElementBuilder.from(itemStack)
                            .setCallback((x, y, z) -> {
                                EconUtil.openConfirmationWindow(player, moveTemplate, slot - 1, gui, price).open();
                            });
                })
                .filter(element -> element != null)
                .collect(Collectors.toList());

        // Create PaginatedSection
        PaginatedSection paginatedSection = new PaginatedSection(elements)
                .setSlotRanges(List.of(new SlotRange(0, guiSize - 10)))
                .setFillItem(GuiElementBuilder.from(Items.GRAY_STAINED_GLASS_PANE.getDefaultStack().setCustomName(Text.literal(""))));

        // Fill the GUI and add pagination controls
        applyPaginationControls(gui, paginatedSection, rows);

        // Apply the initial page to the GUI
        paginatedSection.applyToGui(gui);

        // Add sorting buttons
        applySortingButtons(gui, rows, slot);

        gui.open();
    }

    /**
     * Applies pagination controls to the GUI.
     *
     * @param gui The GUI to apply the controls to.
     * @param paginatedSection The paginated section handling the tutor moves.
     * @param rows The number of rows in the GUI.
     */
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

    /**
     * Applies sorting buttons to the GUI.
     *
     * @param gui The GUI to apply the sorting buttons to.
     * @param rows The number of rows in the GUI.
     * @param slot The slot of the Pokémon (1-based index).
     */
    private static void applySortingButtons(SimpleGui gui, int rows, int slot) {
        int sortSlotAlpha = (rows - 1) * 9 + 3;
        int sortSlotCategory = (rows - 1) * 9 + 4;
        int sortSlotType = (rows - 1) * 9 + 5;

        gui.setSlot(sortSlotAlpha, GuiElementBuilder.from(Items.PAPER.getDefaultStack().setCustomName(Text.literal("Alphabetical")))
                .setCallback((x, y, z) -> {
                    currentSortOption = SortOption.ALPHABETICAL;
                    try {
                        open(gui.getPlayer(), slot);
                    } catch (NoPokemonStoreException e) {
                        throw new RuntimeException(e);
                    }
                }));

        gui.setSlot(sortSlotCategory, GuiElementBuilder.from(Items.NAME_TAG.getDefaultStack().setCustomName(Text.literal("Category")))
                .setCallback((x, y, z) -> {
                    currentSortOption = SortOption.CATEGORY;
                    try {
                        open(gui.getPlayer(), slot);
                    } catch (NoPokemonStoreException e) {
                        throw new RuntimeException(e);
                    }
                }));

        gui.setSlot(sortSlotType, GuiElementBuilder.from(Items.GOLD_INGOT.getDefaultStack().setCustomName(Text.literal("Type")))
                .setCallback((x, y, z) -> {
                    currentSortOption = SortOption.TYPE;
                    try {
                        open(gui.getPlayer(), slot);
                    } catch (NoPokemonStoreException e) {
                        throw new RuntimeException(e);
                    }
                }));
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
}
