package TutorMoves.util;

import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JSONUtil {

    private static final String SPECIES_DIRECTORY = "/data/cobblemon/species/";

    /**
     * Fetches the Pokémon in the specified slot for the given player.
     *
     * @param player The player whose Pokémon slot is being queried.
     * @param slot   The slot number.
     * @return The Pokémon in the specified slot.
     */
    private static Pokemon getPokemonInSlot(ServerPlayerEntity player, int slot) throws NoPokemonStoreException {
        slot = slot-1;
        PlayerPartyStore partyStore = Cobblemon.INSTANCE.getStorage().getParty(player.getUuid());
        return partyStore.get(slot);
    }

    /**
     * Searches for the JSON file corresponding to the given species name in the species directory.
     *
     * @param speciesName The name of the Pokémon species.
     * @return The Path to the JSON file if found, null otherwise.
     */
    private static Path findSpeciesJsonFile(String speciesName) {
        try {
            Path speciesDir = Paths.get(JSONUtil.class.getResource(SPECIES_DIRECTORY).toURI());
            if (Files.exists(speciesDir)) {
                for (Path generationDir : Files.newDirectoryStream(speciesDir)) {
                    Path jsonFile = generationDir.resolve(speciesName + ".json");
                    if (Files.exists(jsonFile)) {
                        return jsonFile;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fetches the tutor moves for the Pokémon in the specified slot for the given player.
     *
     * @param player The player whose Pokémon slot is being queried.
     * @param slot   The slot number.
     * @return A list of tutor moves for the Pokémon in the specified slot.
     */
    public static List<String> getTutorMovesForSlot(ServerPlayerEntity player, int slot) throws NoPokemonStoreException {
        List<String> tutorMoves = new ArrayList<>();
        Pokemon pokemon = getPokemonInSlot(player, slot);

        if (pokemon != null) {
            String speciesName = pokemon.getSpecies().getName().toLowerCase();

            Path jsonFile = findSpeciesJsonFile(speciesName);
            if (jsonFile != null) {
                try (InputStream inputStream = Files.newInputStream(jsonFile)) {
                    JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                    JsonArray movesArray = jsonObject.getAsJsonArray("moves");

                    for (int i = 0; i < movesArray.size(); i++) {
                        String move = movesArray.get(i).getAsString();
                        if (move.startsWith("tutor:")) {
                            tutorMoves.add(move.substring(6));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("JSON file for species " + speciesName + " not found.");
            }
        } else {
            System.err.println("No Pokémon found in slot " + slot);
        }

        return tutorMoves;
    }
}
