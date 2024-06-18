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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TutorYAMLReader {

    public static class TutorConfig {
        private final String name;
        private final String permission;
        private final int size;
        private final int cost;  // Added cost parameter
        private final List<MoveTemplate> moves;

        public TutorConfig(String name, String permission, int size, int cost, List<MoveTemplate> moves) {
            this.name = name;
            this.permission = permission;
            this.size = size;
            this.cost = cost;
            this.moves = moves;
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
            return cost;  // Getter for cost
        }

        public List<MoveTemplate> getMoves() {
            return moves;
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

            String name = (String) obj.get("name");
            String permission = (String) obj.get("permission");
            int size = (int) obj.get("size");
            int cost = (int) obj.get("cost");  // Read cost from YAML
            List<String> moveNames = (List<String>) obj.get("moves");

            List<MoveTemplate> moves = new ArrayList<>();
            for (String moveName : moveNames) {
                MoveTemplate moveTemplate = Moves.INSTANCE.getByNameOrDummy(moveName);
                if (!moveTemplate.equals(MoveTemplate.Companion.dummy(moveName))) {
                    moves.add(moveTemplate);
                } else {
                    System.err.println("Move " + moveName + " not found.");
                }
            }

            return new TutorConfig(name, permission, size, cost, moves);  // Pass cost to TutorConfig
        }
    }

    public static void sendFeedback(ServerPlayerEntity player, String message) {
        player.sendMessage(Text.literal(message).formatted(Formatting.RED));
    }
}
