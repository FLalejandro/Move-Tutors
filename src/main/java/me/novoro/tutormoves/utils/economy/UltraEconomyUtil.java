package me.novoro.tutormoves.utils.economy;

import com.kingpixel.ultraeconomy.api.UltraEconomyApi;

import java.math.BigDecimal;
import java.util.UUID;

public class UltraEconomyUtil {

    private static String parseCurrencyType(String currencyKey) {
        return currencyKey.substring("ultraeconomy:".length());
    }

    public static double getBalance(UUID playerUUID, String currencyKey) {
        return UltraEconomyApi.getBalance(playerUUID, parseCurrencyType(currencyKey)).doubleValue();
    }

    public static boolean withdraw(UUID playerUUID, double amount, String currencyKey) {
        UltraEconomyApi.withdraw(playerUUID, parseCurrencyType(currencyKey), BigDecimal.valueOf(amount));
        return true;
    }
}
