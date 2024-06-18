package TutorMoves.util;

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
        private final int size;
        private final int cost;
        private final List<MoveTemplate> moves;
        private final Set<String> blacklistedPokemon;

        public TutorConfig(String name, String permission, int size, int cost, List<MoveTemplate> moves, Set<String> blacklistedPokemon) {
            this.name = name;
            this.permission = permission;
            this.size = size;
            this.cost = cost;
            this.moves = moves;
            this.blacklistedPokemon = blacklistedPokemon;
        }

        public String getName() {
            return name;
        }

        public String getPermission() {
            return permission;
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
    }

    public static TutorConfig readTutorFile(String tutorFileName) throws Exception {
        File file = new File(Paths.get("config/TutorMoves/tutors", tutorFileName + ".yml").toUri());
        if (!file.exists()) {
            throw new IllegalArgumentException("Tutor file " + tutorFileName + " does not exist.");
        }

        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(file)) {
            Map<String, Object> obj = yaml.load(inputStream);
            Map<String, Object> specificTutor = (Map<String, Object>) obj.get("SpecificTutor");

            if (specificTutor == null) {
                throw new IllegalArgumentException("SpecificTutor section not found in file " + tutorFileName + ".yml");
            }

            String name = (String) specificTutor.get("name");
            String permission = (String) specificTutor.get("permission");
            int size = (int) specificTutor.get("size");
            int cost = (int) specificTutor.get("cost");
            List<String> moveNames = (List<String>) specificTutor.get("moves");
            List<String> blacklistedPokemonList = (List<String>) specificTutor.get("Blacklisted-Pokemon");

            Set<String> blacklistedPokemon = new HashSet<>(blacklistedPokemonList);

            List<MoveTemplate> moves = new ArrayList<>();
            for (String moveName : moveNames) {
                MoveTemplate moveTemplate = Moves.INSTANCE.getByNameOrDummy(moveName);
                if (!moveTemplate.equals(MoveTemplate.Companion.dummy(moveName))) {
                    moves.add(moveTemplate);
                }
            }

            return new TutorConfig(name, permission, size, cost, moves, blacklistedPokemon);
        }
    }

    public static void sendFeedback(ServerPlayerEntity player, String message) {
        player.sendMessage(Text.literal(message).formatted(Formatting.RED));
    }
}
