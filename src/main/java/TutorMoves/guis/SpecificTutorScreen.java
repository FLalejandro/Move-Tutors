package TutorMoves.guis;

import TutorMoves.util.EconUtil;
import TutorMoves.util.MoveUtil;
import TutorMoves.util.TutorYAMLReader;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
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
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.moves.MoveTemplate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SpecificTutorScreen {

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

        // Notify if any moves are not found
        List<String> missingMoves = tutorConfig.getMoves().stream()
                .filter(moveTemplate -> Objects.equals(moveTemplate, MoveTemplate.Companion.dummy(moveTemplate.getName())))
                .map(MoveTemplate::getName)
                .collect(Collectors.toList());

        if (!missingMoves.isEmpty()) {
            TutorYAMLReader.sendFeedback(player, "Moves not found: " + String.join(", ", missingMoves));
        }

        // Calculate the GUI size based on the number of rows
        int rows = tutorConfig.getSize();
        int guiSize = rows * 9;
        ScreenHandlerType<?> screenHandlerType = getScreenHandlerType(rows);
        SimpleGui gui = new SimpleGui(screenHandlerType, player, false);

        gui.setTitle(Text.literal(tutorConfig.getName()));

        // Create an instance of Moves and MoveUtil
        Moves moves = Moves.INSTANCE;
        MoveUtil moveUtil = new MoveUtil(moves);

        // Create GuiElementBuilder for each tutor move
        List<GuiElementBuilder> elements = tutorConfig.getMoves().stream()
                .map(moveTemplate -> {
                    if (moveTemplate == null || moveTemplate.equals(MoveTemplate.Companion.dummy(moveTemplate.getName()))) {
                        return null;
                    }
                    ItemStack itemStack = moveUtil.getGemForMove(moveTemplate);
                    return GuiElementBuilder.from(itemStack).setCallback((x, y, z) -> {
                        EconUtil.openConfirmationWindow(player, moveTemplate, slot - 1, gui).open();
                    });
                })
                .filter(element -> element != null)
                .collect(Collectors.toList());

        // Create PaginatedSection
        PaginatedSection paginatedSection = new PaginatedSection(elements)
                .setSlotRanges(List.of(new SlotRange(0, guiSize - 10)))  // Adjusted to fill available slots
                .setFillItem(GuiElementBuilder.from(Items.GRAY_STAINED_GLASS_PANE.getDefaultStack().setCustomName(Text.literal(""))));

        // Fill the GUI and add pagination controls
        applyPaginationControls(gui, paginatedSection, rows);

        // Apply the initial page to the GUI
        paginatedSection.applyToGui(gui);

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
        System.out.println("Previous page control set");

        gui.setSlot(controlSlotNext, GuiElementBuilder.from(Items.ARROW.getDefaultStack().setCustomName(Text.literal("Next Page")))
                .setCallback((x, y, z) -> {
                    paginatedSection.incremementPage();
                    paginatedSection.applyToGui(gui);
                }));
        System.out.println("Next page control set");

        GuiUtils.fillGUI(gui); // Fill the rest of the GUI with placeholder items
        System.out.println("GUI filled with placeholder items");
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
