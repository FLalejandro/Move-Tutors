package me.novoro.tutormoves.config;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

public class TutorYAMLReader {
    private static final Map<String, TutorConfig> tutors = new HashMap<>();

    public record TutorConfig(String name, String permission, String currencyKey, int size, int cost,
                              List<MoveTemplate> moves, List<String> blacklistedPokemon, String fillerItem,
                              Map<String, Integer> moveOverrides) {
    }

    public static Set<String> getTutorNames() {
        return tutors.keySet();
    }

    public static TutorConfig readTutorFile(String tutorFileName) throws Exception {
        File file = new File(Paths.get("config/TutorMoves/tutors", tutorFileName + ".yml").toUri());
        if (!file.exists()) {
            throw new IllegalArgumentException("Tutor file " + tutorFileName + " does not exist.");
        }

        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(file)) {
            Map<String, Object> obj = yaml.load(inputStream);
            Map<String, Object> specificTutor = (Map<String, Object>) obj.get("Specific-Tutor");
            Map<String, Object> gui = (Map<String, Object>) obj.get("Specific-Tutor-GUI");

            if (specificTutor == null || gui == null) {
                throw new IllegalArgumentException("SpecificTutor or GUI section not found in file " + tutorFileName + ".yml");
            }

            String name = (String) gui.get("title");
            String permission = (String) specificTutor.get("permission");
            String currencyKey = (String) specificTutor.get("currencyKey");
            int size = (int) gui.get("size");
            int cost = (int) specificTutor.get("cost");
            List<String> moveNames = (List<String>) specificTutor.get("moves");
            List<String> blacklistedPokemon = (List<String>) specificTutor.get("Blacklisted-Pokemon");
            String fillerItem = (String) gui.get("filler-item");

            List<MoveTemplate> moves = new ArrayList<>();
            for (String moveName : moveNames) {
                MoveTemplate moveTemplate = Moves.INSTANCE.getByNameOrDummy(moveName);
                if (!moveTemplate.equals(MoveTemplate.Companion.dummy(moveName))) {
                    moves.add(moveTemplate);
                }
            }

            // Parse the Move-Overrides section
            Map<String, Integer> moveOverrides = new HashMap<>();
            List<Map<String, Integer>> overridesList = (List<Map<String, Integer>>) specificTutor.get("Move-Overrides");
            if (overridesList != null) {
                for (Map<String, Integer> override : overridesList) {
                    moveOverrides.putAll(override);
                }
            }


            return new TutorConfig(name, permission, currencyKey, size, cost, moves, blacklistedPokemon, fillerItem, moveOverrides);
        }
    }
}
