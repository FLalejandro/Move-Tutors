package TutorMoves.guis;

import TutorMoves.util.EconUtil;
import TutorMoves.util.JSONUtil;
import TutorMoves.util.MoveUtil;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import dev.roanoke.rib.utils.GuiUtils;
import dev.roanoke.rib.utils.PaginatedSection;
import dev.roanoke.rib.utils.SlotRange;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.stream.Collectors;

public class AllTutorScreen {

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
            player.sendMessage(Text.literal("No Pokémon found in slot " + slot).formatted(Formatting.RED));
            return;
        }

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);

        gui.setTitle(Text.literal("Tutor Moves"));

        // Create an instance of Moves and MoveUtil
        Moves moves = Moves.INSTANCE;
        MoveUtil moveUtil = new MoveUtil(moves);

        // Create GuiElementBuilder for each tutor move
        List<GuiElementBuilder> elements = tutorMoves.stream()
                .map(moveName -> {
                    MoveTemplate moveTemplate = moves.getByNameOrDummy(moveName);
                    if (moveTemplate == null) {
                        return null;
                    }
                    ItemStack itemStack = moveUtil.getGemForMove(moveTemplate);
                    return GuiElementBuilder.from(itemStack)
                            .setCallback((x, y, z) -> {
                                EconUtil.openConfirmationWindow(player, moveTemplate, slot - 1, gui).open();
                            });
                })
                .filter(element -> element != null)
                .collect(Collectors.toList());

        // Create PaginatedSection
        PaginatedSection paginatedSection = new PaginatedSection(elements)
                .setSlotRanges(List.of(new SlotRange(0, 44)))
                .setFillItem(GuiElementBuilder.from(Items.GRAY_STAINED_GLASS_PANE.getDefaultStack().setCustomName(Text.literal(""))));

        // Fill the GUI and add pagination controls
        applyPaginationControls(gui, paginatedSection);

        // Apply the initial page to the GUI
        paginatedSection.applyToGui(gui);

        gui.open();
    }

    /**
     * Applies pagination controls to the GUI.
     *
     * @param gui The GUI to apply the controls to.
     * @param paginatedSection The paginated section handling the tutor moves.
     */
    private static void applyPaginationControls(SimpleGui gui, PaginatedSection paginatedSection) {
        gui.setSlot(45, GuiElementBuilder.from(Items.ARROW.getDefaultStack().setCustomName(Text.literal("Previous Page")))
                .setCallback((x, y, z) -> {
                    paginatedSection.decrementPage();
                    paginatedSection.applyToGui(gui);
                }));

        gui.setSlot(53, GuiElementBuilder.from(Items.ARROW.getDefaultStack().setCustomName(Text.literal("Next Page")))
                .setCallback((x, y, z) -> {
                    paginatedSection.incremementPage();
                    paginatedSection.applyToGui(gui);
                }));

        GuiUtils.fillGUI(gui);
    }
}
