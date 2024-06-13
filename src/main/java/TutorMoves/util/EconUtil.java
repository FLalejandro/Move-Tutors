package TutorMoves.util;

import TutorMoves.helper.MoveTeacher;
import com.cobblemon.mod.common.api.moves.BenchedMove;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.moves.LearnsetQuery;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.Cobblemon;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.impactdev.impactor.api.economy.EconomyService;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import dev.roanoke.rib.utils.GuiUtils;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

public class EconUtil {

    /**
     * Processes the purchase of a move.
     *
     * @param player The player purchasing the move.
     * @param move The move being purchased.
     * @param slot The slot of the Pokémon.
     * @return True if the purchase was successful, false otherwise.
     */
    public static boolean purchaseMove(ServerPlayerEntity player, MoveTemplate move, int slot) {
        BigDecimal price = new BigDecimal("100"); // Example price
        try {
            var account = EconomyService.instance().account(player.getUuid()).get();

            PlayerPartyStore partyStore = Cobblemon.INSTANCE.getStorage().getParty(player.getUuid());
            Pokemon pokemon = partyStore.get(slot);

            if (pokemon == null) {
                player.sendMessage(Text.literal("No Pokémon found in slot " + (slot + 1)).formatted(Formatting.RED));
                return false;
            }

            if (!LearnsetQuery.Companion.getANY().canLearn(move, pokemon.getForm().getMoves())) {
                player.sendMessage(Text.literal(pokemon.getDisplayName().getString() + " can't learn " + move.getDisplayName().getString()).formatted(Formatting.RED));
                return false;
            }

            boolean knowsMove = pokemon.getMoveSet().getMoves().stream().anyMatch(m -> m.getTemplate() == move);

            if (!knowsMove) {
                for (BenchedMove benchedMove : pokemon.getBenchedMoves()) {
                    if (benchedMove.getMoveTemplate() == move) {
                        knowsMove = true;
                        break;
                    }
                }
            }

            if (knowsMove) {
                player.sendMessage(Text.literal(pokemon.getDisplayName().getString() + " already knows " + move.getDisplayName().getString()).formatted(Formatting.RED));
                return false;
            }

            if (account.balanceAsync().get().compareTo(price) >= 0) {
                account.withdrawAsync(price).get();
                if (pokemon.getMoveSet().hasSpace()) {
                    pokemon.getMoveSet().add(move.create());
                } else {
                    pokemon.getBenchedMoves().add(new BenchedMove(move, 0));
                }
                player.sendMessage(Text.literal(pokemon.getDisplayName().getString() + " learned " + move.getDisplayName().getString()).formatted(Formatting.GREEN));
                return true;
            } else {
                player.sendMessage(Text.literal("§cYou can't afford this move.").formatted(Formatting.RED));
            }

        } catch (ExecutionException | InterruptedException | NoPokemonStoreException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Opens a confirmation window to handle the purchase of a move.
     *
     * @param player The player who initiated the purchase.
     * @param moveTemplate The move template to purchase.
     * @param slot The slot of the Pokémon.
     * @param oldGui The previous GUI.
     * @return The confirmation GUI.
     */
    public static SimpleGui openConfirmationWindow(ServerPlayerEntity player, MoveTemplate moveTemplate, int slot, SimpleGui oldGui) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);

        gui.setTitle(Text.literal("Confirm Purchase"));

        MoveUtil moveUtil = new MoveUtil(Moves.INSTANCE);
        ItemStack itemStack = moveUtil.getGemForMove(moveTemplate);

        gui.setSlot(13, GuiElementBuilder.from(itemStack)
                .build());

        gui.setSlot(11, GuiElementBuilder.from(Items.GREEN_WOOL.getDefaultStack())
                .setName(Text.literal("§aConfirm"))
                .setCallback((x, y, z) -> {
                    if (purchaseMove(player, moveTemplate, slot)) {
                        player.sendMessage(Text.literal("§aSuccessfully purchased " + moveTemplate.getDisplayName().getString() + " for your Pokémon!"));
                    }
                    oldGui.open();
                }));

        gui.setSlot(15, GuiElementBuilder.from(Items.RED_WOOL.getDefaultStack())
                .setName(Text.literal("§cCancel"))
                .setCallback((x, y, z) -> oldGui.open()));

        GuiUtils.fillGUI(gui);
        return gui;
    }
}
