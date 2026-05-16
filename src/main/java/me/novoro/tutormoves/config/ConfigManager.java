package me.novoro.tutormoves.config;

import me.novoro.tutormoves.api.configuration.Configuration;
import me.novoro.tutormoves.api.configuration.VersionedConfig;
import me.novoro.tutormoves.utils.ItemBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class ConfigManager extends VersionedConfig {
    private static String currencyKey;
    private static String currencyName;
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

    // Tutor GUI element names
    private static String previousPageName;
    private static String nextPageName;
    private static String exitName;
    private static String alphabeticalSortName;
    private static String categorySortName;
    private static String typeSortName;

    // Tutor GUI element slots
    private static int previousPageSlot;
    private static int nextPageSlot;
    private static int exitSlot;
    private static int alphabeticalSortSlot;
    private static int categorySortSlot;
    private static int typeSortSlot;

    // Confirmation GUI element names
    private static String confirmName;
    private static String cancelName;

    // Confirmation GUI element slots
    private static int confirmSlot;
    private static int cancelSlot;
    private static int displaySlot;

    @Override
    protected void reload(Configuration settingsConfig) {
        super.reload(settingsConfig);
        ConfigManager.currencyKey = settingsConfig.getString("TutorMoves.currencyKey");
        ConfigManager.currencyName = settingsConfig.getString("TutorMoves.currencyName");
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

        // Tutor GUI element names (MiniMessage supported)
        ConfigManager.previousPageName = settingsConfig.getString("General-Tutor-GUI.previous-page-name", "Previous Page");
        ConfigManager.nextPageName = settingsConfig.getString("General-Tutor-GUI.next-page-name", "Next Page");
        ConfigManager.exitName = settingsConfig.getString("General-Tutor-GUI.exit-name", "Back");
        ConfigManager.alphabeticalSortName = settingsConfig.getString("General-Tutor-GUI.alphabetical-sort-name", "Alphabetical");
        ConfigManager.categorySortName = settingsConfig.getString("General-Tutor-GUI.category-sort-name", "Category");
        ConfigManager.typeSortName = settingsConfig.getString("General-Tutor-GUI.type-sort-name", "Type");

        // Tutor GUI absolute slot positions (-1 = auto-calculate based on GUI size)
        ConfigManager.previousPageSlot = settingsConfig.getInt("General-Tutor-GUI.previous-page-slot", -1);
        ConfigManager.nextPageSlot = settingsConfig.getInt("General-Tutor-GUI.next-page-slot", -1);
        ConfigManager.exitSlot = settingsConfig.getInt("General-Tutor-GUI.exit-slot", -1);
        ConfigManager.alphabeticalSortSlot = settingsConfig.getInt("General-Tutor-GUI.alphabetical-sort-slot", -1);
        ConfigManager.categorySortSlot = settingsConfig.getInt("General-Tutor-GUI.category-sort-slot", -1);
        ConfigManager.typeSortSlot = settingsConfig.getInt("General-Tutor-GUI.type-sort-slot", -1);

        // Confirmation GUI element names (MiniMessage supported)
        ConfigManager.confirmName = settingsConfig.getString("Confirmation-GUI.confirm-name", "<green>Confirm");
        ConfigManager.cancelName = settingsConfig.getString("Confirmation-GUI.cancel-name", "<red>Cancel");

        // Confirmation GUI absolute slot positions (-1 = use defaults: confirm=11, display=13, cancel=15)
        ConfigManager.confirmSlot = settingsConfig.getInt("Confirmation-GUI.confirm-slot", -1);
        ConfigManager.cancelSlot = settingsConfig.getInt("Confirmation-GUI.cancel-slot", -1);
        ConfigManager.displaySlot = settingsConfig.getInt("Confirmation-GUI.display-slot", -1);

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
    public static String getCurrencyName() {
        return currencyName;
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

    public static String getPreviousPageName() {
        return previousPageName;
    }

    public static String getNextPageName() {
        return nextPageName;
    }

    public static String getExitName() {
        return exitName;
    }

    public static String getAlphabeticalSortName() {
        return alphabeticalSortName;
    }

    public static String getCategorySortName() {
        return categorySortName;
    }

    public static String getTypeSortName() {
        return typeSortName;
    }

    public static int getPreviousPageSlot() {
        return previousPageSlot;
    }

    public static int getNextPageSlot() {
        return nextPageSlot;
    }

    public static int getExitSlot() {
        return exitSlot;
    }

    public static int getAlphabeticalSortSlot() {
        return alphabeticalSortSlot;
    }

    public static int getCategorySortSlot() {
        return categorySortSlot;
    }

    public static int getTypeSortSlot() {
        return typeSortSlot;
    }

    public static String getConfirmName() {
        return confirmName;
    }

    public static String getCancelName() {
        return cancelName;
    }

    public static int getConfirmSlot() {
        return confirmSlot;
    }

    public static int getCancelSlot() {
        return cancelSlot;
    }

    public static int getDisplaySlot() {
        return displaySlot;
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
        stack.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);

        return stack;
    }

    @Override
    public double getCurrentConfigVersion() {
        return 2.4;
    }

    @Override
    protected String getConfigFileName() {
        return "config.yml";
    }
}