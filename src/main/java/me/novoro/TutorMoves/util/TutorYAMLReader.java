package me.novoro.TutorMoves.util;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

public class TutorYAMLReader {

    public static class TutorConfig {
        private final String name;
        private final String permission;
        private final String currencyKey;
        private final int size;
        private final int cost;
        private final List<MoveTemplate> moves;
        private final Set<String> blacklistedPokemon;
        private final String fillerItem;
        private final Map<String, Integer> moveOverrides;

        public TutorConfig(String name, String permission, String currencyKey, int size, int cost, List<MoveTemplate> moves, Set<String> blacklistedPokemon, String fillerItem, Map<String, Integer> moveOverrides) {
            this.name = name;
            this.permission = permission;
            this.currencyKey = currencyKey;
            this.size = size;
            this.cost = cost;
            this.moves = moves;
            this.blacklistedPokemon = blacklistedPokemon;
            this.fillerItem = fillerItem;
            this.moveOverrides = moveOverrides;
        }

        public String getName() {
            return name;
        }

        public String getPermission() {
            return permission;
        }

        public String getCurrencyKey() {
            return currencyKey;
        }

        public int getSize() {
            return size;
        }

        public int getCost() {
            return cost;
        }

        public List<MoveTemplate> getMoves() {
            return moves;
        }

        public Set<String> getBlacklistedPokemon() {
            return blacklistedPokemon;
        }

        public String getFillerItem() {
            return fillerItem;
        }

        public Map<String, Integer> getMoveOverrides() {
            return moveOverrides;
        }
    }

    public static TutorConfig readTutorFile(String tutorFileName) throws Exception {
        File file = new File(Paths.get("config/me.novoro.TutorMoves/tutors", tutorFileName + ".yml").toUri());
        if (!file.exists()) {
            throw new IllegalArgumentException("Tutor file " + tutorFileName + " does not exist.");
        }

        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(file)) {
            Map<String, Object> obj = yaml.load(inputStream);
            Map<String, Object> specificTutor = (Map<String, Object>) obj.get("SpecificTutor");
            Map<String, Object> gui = (Map<String, Object>) obj.get("GUI");

            if (specificTutor == null || gui == null) {
                throw new IllegalArgumentException("SpecificTutor or GUI section not found in file " + tutorFileName + ".yml");
            }

            String name = (String) gui.get("title");
            String permission = (String) specificTutor.get("permission");
            String currencyKey = (String) specificTutor.get("currencyKey");
            int size = (int) gui.get("size");
            int cost = (int) specificTutor.get("cost");
            List<String> moveNames = (List<String>) specificTutor.get("moves");
            List<String> blacklistedPokemonList = (List<String>) specificTutor.get("Blacklisted-Pokemon");
            String fillerItem = (String) gui.get("filler-item");

            Set<String> blacklistedPokemon = new HashSet<>(blacklistedPokemonList);

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

    public static void sendFeedback(ServerPlayerEntity player, String message) {
        player.sendMessage(Text.literal(message).formatted(Formatting.RED));
    }
}
