package me.novoro.tutormoves.utils.economy;

import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.kyori.adventure.key.Key;
import net.minecraft.server.network.ServerPlayerEntity;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ImpactorUtil {

    public static Currency getCurrency(String currencyKey) {
        EconomyService economyService = EconomyService.instance();
        if (currencyKey.isEmpty()) {
            return economyService.currencies().primary();
        }

        Optional<Currency> currency = economyService.currencies().currency(Key.key(currencyKey));
        if (currency.isEmpty()) {
            System.err.println("Could not find a currency by the ID " + currencyKey + "! Valid currencies: " + economyService.currencies().registered());
            return economyService.currencies().primary();
        }

        return currency.get();
    }

    public static CompletableFuture<Account> getAccount(UUID uuid, String currencyKey) {
        Currency currency = getCurrency(currencyKey);
        return EconomyService.instance().account(currency, uuid);
    }

    public static double getBalance(ServerPlayerEntity player, String currencyKey) {
        return getAccount(player.getUuid(), currencyKey).thenCompose(Account::balanceAsync).join().doubleValue();
    }

    public static boolean withdraw(ServerPlayerEntity player, double amount, String currencyKey) {
        return getAccount(player.getUuid(), currencyKey).join().withdrawAsync(BigDecimal.valueOf(amount)).join().successful();
    }

}
