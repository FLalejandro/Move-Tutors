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


public final class ConfigManager extends VersionedConfig {
    private static String currencyKey;
    private static double cost;
    private static List<String> moveBlacklist;
    private static List<String> pokemonBlacklist;
    private static String generalGuiTitle;
    private static int generalGuiSize;
    private static String generalGuiFillerItem;
    private static String selectionGuiTitle;
    private static String selectionGuiFillerItem;
    private static Map<String, Integer> moveOverrides;
    private static boolean tutorMovesEnabled;
    private static boolean eggMovesEnabled;
    private static boolean tmMovesEnabled;
    private static boolean evolutionMoves;
    private static boolean levelUpMoves;
    private static boolean formChangeMoves;
    private static boolean legacyMoves;
    private static boolean specialMoves;


    @Override
    protected void reload(Configuration settingsConfig) {
        super.reload(settingsConfig);
        ConfigManager.currencyKey = settingsConfig.getString("TutorMoves.currencyKey");
        ConfigManager.cost = settingsConfig.getDouble("TutorMoves.cost");
        ConfigManager.moveBlacklist = settingsConfig.getStringList("TutorMoves.Blacklisted-Moves");
        ConfigManager.pokemonBlacklist = settingsConfig.getStringList("TutorMoves.Blacklisted-Pokemon");
        ConfigManager.tutorMovesEnabled = settingsConfig.getBoolean("Move-Options.tutorMoves");
        ConfigManager.eggMovesEnabled = settingsConfig.getBoolean("Move-Options.eggMoves");
        ConfigManager.tmMovesEnabled = settingsConfig.getBoolean("Move-Options.tmMoves");
        ConfigManager.evolutionMoves = settingsConfig.getBoolean("Move-Options.evolutionMoves");
        ConfigManager.levelUpMoves = settingsConfig.getBoolean("Move-Options.levelUpMoves");
        ConfigManager.formChangeMoves = settingsConfig.getBoolean("Move-Options.formChangeMoves");
        ConfigManager.legacyMoves = settingsConfig.getBoolean("Move-Options.legacyMoves");
        ConfigManager.specialMoves = settingsConfig.getBoolean("Move-Options.specialMoves");
        ConfigManager.generalGuiTitle = settingsConfig.getString("General-Tutor-GUI.title");
        ConfigManager.generalGuiSize = settingsConfig.getInt("General-Tutor-GUI.size");
        ConfigManager.generalGuiFillerItem = settingsConfig.getString("General-Tutor-GUI.filler-item");
        ConfigManager.selectionGuiTitle = settingsConfig.getString("Selection-GUI.title");
        ConfigManager.selectionGuiFillerItem = settingsConfig.getString("Selection-GUI.filler-item");
        List<?> rawList = settingsConfig.getList("Move-Overrides");
        Map<String, Integer> overrides = new HashMap<>();

        if (rawList != null) {
            for (Object obj : rawList) {
                if (obj instanceof Map<?, ?> entry) {
                    for (Map.Entry<?, ?> e : entry.entrySet()) {
                        if (e.getKey() instanceof String key) {
                            String moveName = key.toLowerCase();
                            Object value = e.getValue();
                            if (value instanceof Number n) {
                                overrides.put(moveName, n.intValue());
                            } else if (value instanceof String s) {
                                try {
                                    overrides.put(moveName, Integer.parseInt(s));
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    }
                }
            }
        }
        ConfigManager.moveOverrides = overrides;

    }

    public static String getCurrencyKey() {
        return currencyKey;
    }
    public static double getCost() {
        return cost;
    }
    public static List<String> getMoveBlacklist() {
        return moveBlacklist;
    }
    public static List<String> getPokemonBlacklist() {
        return pokemonBlacklist;
    }
    public static boolean isTutorMovesEnabled() {
        return tutorMovesEnabled;
    }
    public static boolean isEggMovesEnabled() {
        return eggMovesEnabled;
    }
    public static boolean isTmMovesEnabled() {
        return tmMovesEnabled;
    }
    public static boolean isEvolutionMoves() {
        return evolutionMoves;
    }
    public static boolean isLevelUpMoves() {
        return levelUpMoves;
    }
    public static boolean isFormChangeMoves() {
        return formChangeMoves;
    }
    public static boolean isLegacyMoves() {
        return legacyMoves;
    }
    public static boolean isSpecialMoves() {
        return specialMoves;
    }
    public static String getGeneralGuiTitle() {
        return generalGuiTitle;
    }
    public static int getGeneralGuiSize() {
        return generalGuiSize;
    }
    public static String getGeneralFillerItem() {
        return generalGuiFillerItem;
    }
    public static String getSelectionGuiTitle() {
        return selectionGuiTitle;
    }
    public static String getSelectionFillerItem() {
        return selectionGuiFillerItem;
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
        return "config.yml";
    }
}