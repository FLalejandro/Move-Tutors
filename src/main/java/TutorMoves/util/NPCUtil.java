package TutorMoves.util;

import TutorMoves.TutorMoves;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class NPCUtil {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File NPC_FILE = new File(TutorMoves.getConfigFolder(), "npc_entities.json");

    public static void saveNPCEntities() {
        try (FileWriter writer = new FileWriter(NPC_FILE)) {
            GSON.toJson(TutorMoves.npcEntities, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadNPCEntities() {
        if (NPC_FILE.exists()) {
            try (FileReader reader = new FileReader(NPC_FILE)) {
                Map<UUID, String> npcEntities = GSON.fromJson(reader, Map.class);
                TutorMoves.npcEntities.clear();
                TutorMoves.npcEntities.putAll(npcEntities);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
