package TutorMoves.util;

import TutorMoves.TutorMoves;
import TutorMoves.helper.MoveTeacher;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import gg.levely.cobblestory.economy.api.Economy;
import net.kyori.adventure.audience.Audience;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class EconUtil {

    /**
     * Processes the purchase of a move using currency, checking which economy is loaded.
     */
    public static boolean purchaseMove(ServerPlayerEntity player,
                                       MoveTemplate move,
                                       int slot,
                                       BigDecimal price,
                                       String currencyKey) {
        double priceValue = price.doubleValue();
        double playerBalance;
        UUID playerUUID = player.getUuid();

        // Get player balance depending on what's available
        if (TutorMoves.isCobbleEconomyAvailable) {
            playerBalance = CobbleEconomy.getBalance(playerUUID, currencyKey);
        } else if (TutorMoves.isImpactorAvailable) {
            playerBalance = ImpactorUtil.getBalance(player, currencyKey);
        } else if (TutorMoves.isPebblesAvailable) {
            playerBalance = PebblesEconomy.getBalance(playerUUID);
        } else {
            return false;
        }

        if (playerBalance >= priceValue) {
            boolean canTeach = MoveTeacher.teachMove(player, slot, move);
            if (canTeach) {
                boolean withdrawalSuccess;
                if (TutorMoves.isCobbleEconomyAvailable) {
                    withdrawalSuccess = CobbleEconomy.withdraw(playerUUID, priceValue, currencyKey);
                } else if (TutorMoves.isImpactorAvailable) {
                    withdrawalSuccess = ImpactorUtil.withdraw(player, priceValue, currencyKey);
                } else {
                    PebblesEconomy.withdraw(playerUUID, priceValue);
                    withdrawalSuccess = true;
                }

                if (withdrawalSuccess) {
                    LangManager.send((Audience) player, "Successful-Tutor", Map.of(
                            "{pokemon}", player.getName().getString(),
                            "{move}", move.getDisplayName().getString()
                    ));
                    return true;
                }
            }
        } else {
            LangManager.send((Audience) player, "Insufficient-Funds");
        }

        return false;
    }

    /**
     * Opens a confirmation window to handle the purchase of a move using currency.
     */
    public static SimpleGui openConfirmationWindow(ServerPlayerEntity player,
                                                   MoveTemplate moveTemplate,
                                                   int slot,
                                                   SimpleGui oldGui,
                                                   BigDecimal price,
                                                   String currencyKey) throws NoPokemonStoreException {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Confirm Purchase"));

        MoveUtil moveUtil = new MoveUtil(Moves.INSTANCE);
        GuiElementBuilder elementBuilder = moveUtil.getGemForMove(moveTemplate, price, currencyKey);

        gui.setSlot(13, elementBuilder.build());

        gui.setSlot(11, GuiElementBuilder.from(Items.GREEN_WOOL.getDefaultStack())
                .setName(Text.literal("§aConfirm"))
                .setCallback((x, y, z) -> {
                    if (purchaseMove(player, moveTemplate, slot, price, currencyKey)) {
                        LangManager.send((Audience) player, "Successful-Tutor", Map.of(
                                "{pokemon}", player.getName().getString(),
                                "{move}", moveTemplate.getDisplayName().getString()
                        ));
                    }
                    oldGui.open();
                }));

        gui.setSlot(15, GuiElementBuilder.from(Items.RED_WOOL.getDefaultStack())
                .setName(Text.literal("§cCancel"))
                .setCallback((x, y, z) -> oldGui.open()));

        GuiUtil.fillGUI(gui);
        return gui;
    }
}
