package me.novoro.tutormoves.config;

import me.novoro.tutormoves.api.configuration.Configuration;
import me.novoro.tutormoves.api.configuration.VersionedConfig;
import me.novoro.tutormoves.utils.ItemBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

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
    private static String selectionGuiTitle;
    private static String confirmationGuiTitle;
    private static Map<String, Integer> moveOverrides;
    private static boolean tutorMovesEnabled;
    private static boolean eggMovesEnabled;
    private static boolean tmMovesEnabled;
    private static boolean evolutionMoves;
    private static boolean levelUpMoves;
    private static boolean formChangeMoves;
    private static boolean legacyMoves;
    private static boolean specialMoves;

    private static ItemStack previousPageItem;
    private static ItemStack nextPageItem;
    private static ItemStack exitItem;
    private static ItemStack alphabeticalSortItem;
    private static ItemStack typeSortItem;
    private static ItemStack categorySortItem;
    private static ItemStack generalGuiFillerItem;
    private static ItemStack selectionGuiFillerItem;
    private static ItemStack confirmationGuiFillerItem;
    private static ItemStack confirmItem;
    private static ItemStack cancelItem;


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
        ConfigManager.confirmationGuiTitle = settingsConfig.getString("Confirmation-GUI.title");
        ConfigManager.selectionGuiTitle = settingsConfig.getString("Selection-GUI.title");
        ConfigManager.previousPageItem = parseItem(settingsConfig.getString("General-Tutor-GUI.previous-page-item"));
        ConfigManager.nextPageItem = parseItem(settingsConfig.getString("General-Tutor-GUI.next-page-item"));
        ConfigManager.exitItem = parseItem(settingsConfig.getString("General-Tutor-GUI.exit-item"));
        ConfigManager.alphabeticalSortItem = parseItem(settingsConfig.getString("General-Tutor-GUI.alphabetical-sort-item"));
        ConfigManager.typeSortItem = parseItem(settingsConfig.getString("General-Tutor-GUI.type-sort-item"));
        ConfigManager.categorySortItem = parseItem(settingsConfig.getString("General-Tutor-GUI.category-sort-item"));
        ConfigManager.generalGuiFillerItem = parseItem(settingsConfig.getString("General-Tutor-GUI.filler-item"));
        ConfigManager.selectionGuiFillerItem = parseItem(settingsConfig.getString("Selection-GUI.filler-item"));
        ConfigManager.confirmationGuiFillerItem = parseItem(settingsConfig.getString("Confirmation-GUI.filler-item"));
        ConfigManager.confirmItem = parseItem(settingsConfig.getString("Confirmation-GUI.confirm-item"));
        ConfigManager.cancelItem = parseItem(settingsConfig.getString("Confirmation-GUI.cancel-item"));
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
    public static ItemStack getPreviousPageItem() {
        return previousPageItem;
    }
    public static ItemStack getNextPageItem() {
        return nextPageItem;
    }
    public static ItemStack getExitItem() {
        return exitItem;
    }
    public static ItemStack getAlphabeticalSortItem() {
        return alphabeticalSortItem;
    }
    public static ItemStack getTypeSortItem() {
        return typeSortItem;
    }
    public static ItemStack getCategorySortItem() {
        return categorySortItem;
    }
    public static ItemStack getGeneralFillerItem() {
        return generalGuiFillerItem;
    }
    public static String getSelectionGuiTitle() {
        return selectionGuiTitle;
    }
    public static ItemStack getSelectionFillerItem() {
        return selectionGuiFillerItem;
    }
    public static String getConfirmationGuiTitle() {
        return confirmationGuiTitle;
    }
    public static ItemStack getConfirmationFillerItem() {
        return confirmationGuiFillerItem;
    }
    public static ItemStack getConfirmItem() {
        return confirmItem;
    }
    public static ItemStack getCancelItem() {
        return cancelItem;
    }
    public static Map<String, Integer> getMoveOverrides() {
        return moveOverrides;
    }

    /**
     * Simple item parser that handles custom model data
     * Format: "minecraft:stone" or "minecraft:stone:123"
     */
    private static ItemStack parseItem(String itemString) {
        if (itemString == null || itemString.isEmpty()) {
            return new ItemStack(Registries.ITEM.get(Identifier.of("minecraft:air")));
        }

        String[] parts = itemString.split(":");
        String itemId;
        int customModelData = 0;

        if (parts.length >= 3) {
            try {
                itemId = parts[0] + ":" + parts[1];
                customModelData = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                itemId = itemString;
            }
        } else {
            itemId = itemString;
        }

        ItemStack stack = new ItemStack(Registries.ITEM.get(Identifier.of(itemId)));
        if (customModelData != 0) {
            stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(customModelData));
        }

        return stack;
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