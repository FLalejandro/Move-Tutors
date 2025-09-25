package me.novoro.tutormoves.config;

import me.novoro.tutormoves.api.configuration.Configuration;
import me.novoro.tutormoves.api.configuration.VersionedConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TutorsManager extends VersionedConfig {
    private static String permission;
    private static String currencyKey;
    private static double cost;
    private static List<String> moves;
    private static List<String> pokemonBlacklist;
    private static String guiTitle;
    private static int guiSize;
    private static String fillerItem;
    private static Map<String, Integer> moveOverrides;

    @Override
    protected void reload(Configuration settingsConfig) {
        super.reload(settingsConfig);
        TutorsManager.permission = settingsConfig.getString("SpecificTutor.permission");
        TutorsManager.currencyKey = settingsConfig.getString("SpecificTutor.currencyKey");
        TutorsManager.cost = settingsConfig.getDouble("SpecificTutor.cost");
        TutorsManager.moves = settingsConfig.getStringList("SpecificTutor.moves");
        TutorsManager.pokemonBlacklist = settingsConfig.getStringList("SpecificTutor.Blacklisted-Pokemon");
        TutorsManager.guiTitle = settingsConfig.getString("Specific-Tutor-GUI.title");
        TutorsManager.guiSize = settingsConfig.getInt("Specific-Tutor-GUI.size");
        TutorsManager.fillerItem = settingsConfig.getString("Specific-Tutor-GUI.filler-item");

        // Process move overrides
        Map<String, Integer> overrides = new HashMap<>();
        Configuration moveOverridesSection = settingsConfig.getSection("Specific-Tutor-Move-Overrides");

        if (moveOverridesSection != null) {
            for (String moveName : moveOverridesSection.getKeys()) {
                int moveCost = moveOverridesSection.getInt(moveName);
                overrides.put(moveName.toLowerCase(), moveCost);
            }
        }

        TutorsManager.moveOverrides = overrides;
    }

    public static String getPermission() {
        return permission;
    }

    public static String getCurrencyKey() {
        return currencyKey;
    }

    public static double getCost() {
        return cost;
    }

    public static List<String> getMoves() {
        return moves;
    }

    public static List<String> getPokemonBlacklist() {
        return pokemonBlacklist;
    }

    public static String getGuiTitle() {
        return guiTitle;
    }

    public static int getGuiSize() {
        return guiSize;
    }

    public static String getFillerItem() {
        return fillerItem;
    }

    public static Map<String, Integer> getMoveOverrides() {
        return moveOverrides;
    }

    // TODO: Check in pokemon and move blacklist
    private static boolean checkBlacklist(ItemStack item, List<String> blacklist) {
        if (blacklist.isEmpty()) return false;

        final String itemId = Registries.ITEM.getId(item.getItem()).toString();
        CustomModelDataComponent customModelDataComponent = item.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        int customModelData = customModelDataComponent != null ? customModelDataComponent.value() : 0;

        return blacklist.contains(itemId) || blacklist.contains(itemId + ":" + customModelData);
    }

    @Override
    public double getCurrentConfigVersion() {
        return 2.0;
    }

    @Override
    protected String getConfigFileName() {
        return "specifictutors.yml";
    }
}