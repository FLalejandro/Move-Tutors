package me.novoro.tutormoves.config;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import me.novoro.tutormoves.api.configuration.Configuration;
import me.novoro.tutormoves.api.configuration.YamlConfiguration;
import me.novoro.tutormoves.utils.TutorMovesLogger;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public final class TutorYAMLReader {
    private static final Map<String, TutorConfig> tutors = new HashMap<>();

    public record TutorConfig(
            String name,
            String permission,
            String currencyKey,
            int size,
            int cost,
            List<MoveTemplate> moves,
            List<String> blacklistedPokemon,
            String fillerItem,
            Map<String, Integer> moveOverrides
    ) {}

    public static TutorConfig readTutorFile(String tutorFileName) throws Exception {
        File file = Paths.get("config/TutorMoves/tutors", tutorFileName + ".yml").toFile();
        //TutorMovesLogger.info("Reading tutor config: " + file.getAbsolutePath());
        if (!file.exists()) {
            throw new IllegalArgumentException("Tutor file " + tutorFileName + " does not exist.");
        }

        Configuration config = YamlConfiguration.loadConfiguration(file);

        Configuration specific = config.getSection("Specific-Tutor");
        Configuration gui = config.getSection("Specific-Tutor-GUI");

        if (specific == null || gui == null) {
            throw new IllegalArgumentException("Missing Specific-Tutor or Specific-Tutor-GUI in " + tutorFileName);
        }

       // TutorMovesLogger.info("Parsing tutor sections for: " + tutorFileName);

        String permission = specific.getString("permission");
        String currencyKey = specific.getString("currencyKey");
        int cost = specific.getInt("cost");
        List<String> blacklistedPokemon = specific.getStringList("Blacklisted-Pokemon");
        List<MoveTemplate> moves = specific.getMoveTemplateList("moves");

        String fillerItem = gui.getString("filler-item");
        int size = gui.getInt("size");

        // Move overrides parsing (same pattern as ConfigManager)
        Map<String, Integer> overrides = new HashMap<>();
        List<?> rawOverrides = specific.getList("Move-Overrides");
        if (rawOverrides != null) {
            for (Object obj : rawOverrides) {
                if (obj instanceof Map<?, ?> entry) {
                    for (Map.Entry<?, ?> e : entry.entrySet()) {
                        if (e.getKey() instanceof String key) {
                            Object val = e.getValue();
                            if (val instanceof Number n) {
                                overrides.put(key.toLowerCase(), n.intValue());
                            } else if (val instanceof String s) {
                                try {
                                    overrides.put(key.toLowerCase(), Integer.parseInt(s));
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    }
                }
            }
        }

        TutorConfig tutorConfig = new TutorConfig(
                tutorFileName, permission, currencyKey, size, cost, moves, blacklistedPokemon, fillerItem, overrides
        );

        tutors.put(tutorFileName, tutorConfig);
        return tutorConfig;
    }

    public static void loadTutorFile(File file) {
        try {
            String tutorName = file.getName().replace(".yml", "");
            TutorConfig config = readTutorFile(tutorName);
            tutors.put(tutorName.toLowerCase(), config);
            TutorMovesLogger.info("Registered tutor: " + tutorName);
        } catch (Exception e) {
            TutorMovesLogger.error("Failed to parse tutor file: " + file.getName());
            TutorMovesLogger.printStackTrace(e);
        }
    }

    public static Set<String> getTutorNames() {
        return tutors.keySet();
    }

    public static TutorConfig getTutor(String tutorName) {
        return tutors.get(tutorName.toLowerCase());
    }

    public static String getTutorName(String tutorName) {
        TutorConfig config = getTutor(tutorName);
        return config != null ? config.name() : tutorName;
    }

    public static String getTutorPermission(String tutorName) {
        TutorConfig config = getTutor(tutorName);
        return config != null ? config.permission() : "tutormoves.tutor." + tutorName.toLowerCase();
    }

    public static String getTutorCurrencyKey(String tutorName) {
        TutorConfig config = getTutor(tutorName);
        return config != null ? config.currencyKey() : "minecraft:diamond";
    }

    public static int getTutorSize(String tutorName) {
        TutorConfig config = getTutor(tutorName);
        return config != null ? config.size() : 54;
    }

    public static int getTutorCost(String tutorName) {
        TutorConfig config = getTutor(tutorName);
        return config != null ? config.cost() : 1000;
    }

    public static List<MoveTemplate> getTutorMoves(String tutorName) {
        TutorConfig config = getTutor(tutorName);
        return config != null ? Collections.unmodifiableList(config.moves()) : Collections.emptyList();
    }

    public static Map<String, Integer> getTutorMoveOverrides(String tutorName) {
        TutorConfig config = getTutor(tutorName);
        return config != null ? Collections.unmodifiableMap(config.moveOverrides()) : Collections.emptyMap();
    }

    public static int getMoveCost(String tutorName, String moveName) {
        Map<String, Integer> overrides = getTutorMoveOverrides(tutorName);
        String lowerMoveName = moveName.toLowerCase();

        if (overrides.containsKey(lowerMoveName)) {
            return overrides.get(lowerMoveName);
        }

        return getTutorCost(tutorName);
    }

    public static List<String> getTutorBlacklistedPokemon(String tutorName) {
        TutorConfig config = getTutor(tutorName);
        return config != null ? Collections.unmodifiableList(config.blacklistedPokemon()) : Collections.emptyList();
    }

    public static boolean isPokemonBlacklisted(String tutorName, String pokemonName) {
        List<String> blacklisted = getTutorBlacklistedPokemon(tutorName);
        return blacklisted.contains(pokemonName.toLowerCase());
    }

    public static String getTutorFillerItem(String tutorName) {
        TutorConfig config = getTutor(tutorName);
        return config != null ? config.fillerItem() : "minecraft:black_stained_glass_pane";
    }
}
