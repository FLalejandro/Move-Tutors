package me.novoro.tutormoves.utils.economy;

import me.novoro.tutormoves.TutorMoves;
import me.novoro.tutormoves.config.ConfigManager;
import me.novoro.tutormoves.config.LangManager;
import me.novoro.tutormoves.config.MoveOptions;
import me.novoro.tutormoves.helper.MoveTeacher;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.novoro.tutormoves.utils.ColorUtil;
import me.novoro.tutormoves.utils.GuiUtil;
import me.novoro.tutormoves.utils.ItemBuilder;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

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
        return purchaseMove(player, move, slot, price, currencyKey, MoveOptions.fromGlobal());
    }

    public static boolean purchaseMove(ServerPlayerEntity player,
                                       MoveTemplate move,
                                       int slot,
                                       BigDecimal price,
                                       String currencyKey,
                                       MoveOptions options) {
        double priceValue = price.doubleValue();
        double playerBalance;
        UUID playerUUID = player.getUuid();

        // Get player balance depending on what's available
        if (TutorMoves.isImpactorAvailable) {
            playerBalance = ImpactorUtil.getBalance(player, currencyKey);
        } else if (TutorMoves.isPebblesAvailable) {
            playerBalance = PebblesEconomy.getBalance(playerUUID);
        } else {
            return false;
        }

        if (playerBalance >= priceValue) {
            boolean canTeach = MoveTeacher.teachMove(player, slot, move, options);
            if (canTeach) {
                boolean withdrawalSuccess;
                if (TutorMoves.isImpactorAvailable) {
                    withdrawalSuccess = ImpactorUtil.withdraw(player, priceValue, currencyKey);
                } else {
                    PebblesEconomy.withdraw(playerUUID, priceValue);
                    withdrawalSuccess = true;
                }

                if (withdrawalSuccess) {
                    LangManager.sendLang(player, "Successful-Tutor", Map.of(
                            "{pokemon}", player.getName().getString(),
                            "{move}", move.getDisplayName().getString()
                    ));
                    return true;
                }
            }
        } else {
            LangManager.sendLang(player, "Insufficient-Funds");
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
                                                   String currencyKey,
                                                   String currencyName) throws NoPokemonStoreException {
        return openConfirmationWindow(player, moveTemplate, slot, oldGui, price, currencyKey, currencyName, MoveOptions.fromGlobal());
    }

    public static SimpleGui openConfirmationWindow(ServerPlayerEntity player,
                                                   MoveTemplate moveTemplate,
                                                   int slot,
                                                   SimpleGui oldGui,
                                                   BigDecimal price,
                                                   String currencyKey,
                                                   String currencyName,
                                                   MoveOptions options) throws NoPokemonStoreException {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        String guiTitle = ConfigManager.getConfirmationGuiTitle();
        gui.setTitle(ColorUtil.parseColourToText(guiTitle));

        ItemBuilder moveUtil = new ItemBuilder(Moves.INSTANCE);
        GuiElementBuilder elementBuilder = moveUtil.getGemForMove(moveTemplate, price, currencyKey, currencyName);

        int displaySlot = ConfigManager.getDisplaySlot() >= 0 ? ConfigManager.getDisplaySlot() : 13;
        int confirmSlot = ConfigManager.getConfirmSlot() >= 0 ? ConfigManager.getConfirmSlot() : 11;
        int cancelSlot = ConfigManager.getCancelSlot() >= 0 ? ConfigManager.getCancelSlot() : 15;

        gui.setSlot(displaySlot, elementBuilder.build());

        gui.setSlot(confirmSlot, GuiElementBuilder.from(ConfigManager.getConfirmItem())
                .setName(ColorUtil.parseColourToText(ConfigManager.getConfirmName()))
                .setCallback((x, y, z) -> {
                    if (purchaseMove(player, moveTemplate, slot, price, currencyKey, options)) {
                        LangManager.sendLang(player, "Successful-Tutor", Map.of(
                                "{pokemon}", player.getName().getString(),
                                "{move}", moveTemplate.getDisplayName().getString()
                        ));
                    }
                    oldGui.open();
                }));

        gui.setSlot(cancelSlot, GuiElementBuilder.from(ConfigManager.getCancelItem())
                .setName(ColorUtil.parseColourToText(ConfigManager.getCancelName()))
                .setCallback((x, y, z) -> oldGui.open()));

        GuiUtil.fillGUI(gui);
        return gui;
    }
}
