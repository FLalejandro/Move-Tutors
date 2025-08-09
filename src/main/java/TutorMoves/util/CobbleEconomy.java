package TutorMoves.util;


import gg.levely.cobblestory.economy.api.Economy;
import gg.levely.cobblestory.economy.api.PlayerEconomy;
import gg.levely.cobblestory.economy.api.PlayerEconomyProvider;

import java.util.UUID;

public class CobbleEconomy {

    private static final PlayerEconomyProvider economyProvider = PlayerEconomyProvider.getInstance();

    public static double getBalance(UUID playerUUID, String currency) {
        PlayerEconomy playerEconomy = economyProvider.getEconomy(playerUUID);
        if  (playerEconomy == null) return 0;

        return playerEconomy.getBalance(Economy.fromName(currency)).doubleValue();
    }

    public static boolean withdraw(UUID playerUUID, double amount, String currency) {
        PlayerEconomy playerEconomy = economyProvider.getEconomy(playerUUID);
        if  (playerEconomy == null) return false;

        playerEconomy.subtractBalance(Economy.fromName(currency), amount);
        return true;
    }

    public static void deposit(UUID playerUUID, double amount, String currency) {
        PlayerEconomy playerEconomy = economyProvider.getEconomy(playerUUID);
        if  (playerEconomy == null) return;

        playerEconomy.addBalance(Economy.fromName(currency), amount);
    }
}
