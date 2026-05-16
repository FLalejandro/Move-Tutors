package me.novoro.tutormoves.utils.economy;

import org.krripe.beconomy.api.BEconomy;
import org.krripe.beconomy.api.EconomyAPI;

import java.math.BigDecimal;
import java.util.UUID;

public class BEconomyUtil {

    private static EconomyAPI getApi() {
        if (!BEconomy.INSTANCE.isInitialized()) {
            return null;
        }
        return BEconomy.INSTANCE.getAPI();
    }

    private static String parseCurrencyType(String currencyKey) {
        String type = currencyKey.substring("beconomy:".length());
        EconomyAPI api = getApi();
        if (api == null) return type;
        return api.getCurrencyTypeOrFallback(type);
    }

    public static double getBalance(UUID playerUUID, String currencyKey) {
        EconomyAPI api = getApi();
        if (api == null) return 0.0;
        return api.getBalance(playerUUID, parseCurrencyType(currencyKey)).doubleValue();
    }

    public static boolean withdraw(UUID playerUUID, double amount, String currencyKey) {
        EconomyAPI api = getApi();
        if (api == null) return false;
        return api.decreaseBalance(playerUUID, parseCurrencyType(currencyKey), BigDecimal.valueOf(amount));
    }
}
