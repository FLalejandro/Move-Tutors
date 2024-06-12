package TutorMoves.guis;

import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import dev.roanoke.rib.utils.GuiUtils;
import dev.roanoke.rib.utils.PaginatedSection;
import dev.roanoke.rib.utils.SlotRange;
import TutorMoves.helper.MoveTeacher;
import TutorMoves.util.JSONUtil;
import TutorMoves.util.MoveUtil;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.moves.MoveTemplate;

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
        System.out.println("Opening tutor move GUI for player: " + player.getName().getString() + ", slot: " + slot);

        // Fetch tutor moves for the Pokémon in the specified slot
        List<String> tutorMoves = JSONUtil.getTutorMovesForSlot(player, slot);

        if (tutorMoves.isEmpty()) {
            player.sendMessage(Text.literal("No Pokémon found in slot " + slot).formatted(Formatting.RED));
            return;
        }

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);

        gui.setTitle(Text.literal("Tutor Moves"));
        System.out.println("GUI title set");

        System.out.println("Tutor moves fetched: " + tutorMoves);

        // Create an instance of Moves and MoveUtil
        Moves moves = Moves.INSTANCE;
        MoveUtil moveUtil = new MoveUtil(moves);

        // Create GuiElementBuilder for each tutor move
        List<GuiElementBuilder> elements = tutorMoves.stream()
                .map(moveName -> {
                    System.out.println("Processing move: " + moveName);
                    MoveTemplate moveTemplate = moves.getByNameOrDummy(moveName);
                    if (moveTemplate == null) {
                        System.out.println("Move template is null for move: " + moveName);
                        return null;
                    }
                    ItemStack itemStack = moveUtil.getGemForMove(moveTemplate);
                    return GuiElementBuilder.from(itemStack).setCallback((x, y, z) -> {
                        player.sendMessage(Text.literal("Selected move: " + moveTemplate.getDisplayName().getString()));
                        MoveTeacher.teachMove(player, slot - 1, moveTemplate);  // Adjusting slot index to 0-based
                    });
                })
                .filter(element -> element != null)
                .collect(Collectors.toList());

        System.out.println("Gui elements created: " + elements.size());

        // Create PaginatedSection
        PaginatedSection paginatedSection = new PaginatedSection(elements)
                .setSlotRanges(List.of(new SlotRange(0, 44))) // Setting the main area for tutor moves
                .setFillItem(GuiElementBuilder.from(Items.GRAY_STAINED_GLASS_PANE.getDefaultStack().setCustomName(Text.literal(""))));

        // Fill the GUI and add pagination controls
        applyPaginationControls(gui, paginatedSection);
        System.out.println("Pagination controls applied");

        // Apply the initial page to the GUI
        paginatedSection.applyToGui(gui);
        System.out.println("Initial page applied to GUI");

        System.out.println("Opening GUI for player: " + player.getName().getString());
        boolean isOpened = gui.open();
        System.out.println("GUI open call completed, success: " + isOpened);
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
        System.out.println("Previous page control set");

        gui.setSlot(53, GuiElementBuilder.from(Items.ARROW.getDefaultStack().setCustomName(Text.literal("Next Page")))
                .setCallback((x, y, z) -> {
                    paginatedSection.incremementPage();
                    paginatedSection.applyToGui(gui);
                }));
        System.out.println("Next page control set");

        GuiUtils.fillGUI(gui); // Fill the rest of the GUI with placeholder items
        System.out.println("GUI filled with placeholder items");
    }
}
