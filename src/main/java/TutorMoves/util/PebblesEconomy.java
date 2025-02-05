package TutorMoves.util;

import tech.sethi.pebbleseconomy.Economy;
import tech.sethi.pebbleseconomy.PebblesEconomyInitializer;
import java.util.UUID;

public class PebblesEconomy {

    private static final Economy pEconomy = PebblesEconomyInitializer.INSTANCE.getEconomy();

    public static double getBalance(UUID playerUUID) {
        return pEconomy.getBalance(playerUUID);
    }

    public static void withdraw(UUID playerUUID, double amount) {
        pEconomy.withdraw(playerUUID, amount);
    }

    public static void deposit(UUID playerUUID, double amount) {
        pEconomy.deposit(playerUUID, amount);
    }
}
