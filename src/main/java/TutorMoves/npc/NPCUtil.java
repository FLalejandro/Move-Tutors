package TutorMoves.npc;

import TutorMoves.TutorMoves;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NPCUtil {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File NPC_FILE = new File(TutorMoves.getConfigFolder(), "npc_entities.json");


    /**
     * Saves the current NPC entities to the JSON file.
     */
    public static void saveNPCEntities() {
        try (FileWriter writer = new FileWriter(NPC_FILE)) {
            GSON.toJson(TutorMoves.npcEntities, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the NPC entities from the JSON file, handling duplicates and converting string keys to UUIDs.
     */
    public static void loadNPCEntities() {
        if (NPC_FILE.exists()) {
            try (FileReader reader = new FileReader(NPC_FILE)) {
                // Read the NPC entities from the JSON file into a map with string keys and values
                Map<String, String> npcEntities = GSON.fromJson(reader, Map.class);
                if (npcEntities != null) {
                    // Create a new map to store unique NPC entities with UUID keys
                    Map<UUID, String> uniqueNpcEntities = new HashMap<>();
                    for (Map.Entry<String, String> entry : npcEntities.entrySet()) {
                        UUID key = UUID.fromString(entry.getKey());
                        String value = entry.getValue();
                        if (uniqueNpcEntities.containsKey(key)) {
                            // Log a warning about the duplicate entry
                            TutorMoves.LOGGER.warn("Duplicate NPC UUID found: " + key + ". Keeping the first occurrence.");
                        } else {
                            uniqueNpcEntities.put(key, value);
                        }
                    }
                    // Clear the existing map and put all unique NPC entities into it
                    TutorMoves.npcEntities.clear();
                    TutorMoves.npcEntities.putAll(uniqueNpcEntities);

                    // Save the sanitized map back to the file
                    saveNPCEntities();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
