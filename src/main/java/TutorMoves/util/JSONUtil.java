package TutorMoves.util;

import TutorMoves.TutorMoves;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
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
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JSONUtil {

    private static final String SPECIES_DIRECTORY = "/data/cobblemon/species/";

    /**
     * Fetches the Pokémon in the specified slot for the given player.
     *
     * @param player The player whose Pokémon slot is being queried.
     * @param slot   The slot number.
     * @return The Pokémon in the specified slot.
     */
    public static Pokemon getPokemonInSlot(ServerPlayerEntity player, int slot) throws NoPokemonStoreException {
        slot = slot - 1;
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
            String alphabeticSpeciesName = makeAlphabetic(speciesName.toLowerCase());
            Path speciesDir = Paths.get(JSONUtil.class.getResource(SPECIES_DIRECTORY).toURI());
            if (Files.exists(speciesDir)) {
                for (Path generationDir : Files.newDirectoryStream(speciesDir)) {
                    Path jsonFile = generationDir.resolve(alphabeticSpeciesName + ".json");
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
            String formName = pokemon.getForm().getName().toLowerCase();

            Path jsonFile = findSpeciesJsonFile(speciesName);
            if (jsonFile != null) {
                try (InputStream inputStream = Files.newInputStream(jsonFile)) {
                    JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                    JsonArray baseMovesArray = jsonObject.getAsJsonArray("moves");

                    // Add base moves
                    for (int i = 0; i < baseMovesArray.size(); i++) {
                        String move = baseMovesArray.get(i).getAsString();
                        if (move.startsWith("tutor:") && !isMoveBlacklisted(move.substring(6).toLowerCase())) {
                            tutorMoves.add(move.substring(6));
                        }
                    }

                    // Add form-specific moves
                    if (jsonObject.has("forms")) {
                        JsonArray formsArray = jsonObject.getAsJsonArray("forms");
                        for (int i = 0; i < formsArray.size(); i++) {
                            JsonObject formObject = formsArray.get(i).getAsJsonObject();
                            if (formObject.get("name").getAsString().toLowerCase().equals(formName)) {
                                JsonArray formMovesArray = formObject.getAsJsonArray("moves");
                                tutorMoves.clear(); // Clear base moves if form-specific moves are found
                                for (int j = 0; j < formMovesArray.size(); j++) {
                                    String move = formMovesArray.get(j).getAsString();
                                    if (move.startsWith("tutor:") && !isMoveBlacklisted(move.substring(6).toLowerCase())) {
                                        tutorMoves.add(move.substring(6));
                                    }
                                }
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return tutorMoves;
    }

    /**
     * Checks if a move is blacklisted in the configuration.
     *
     * @param move The name of the move.
     * @return True if the move is blacklisted, false otherwise.
     */
    private static boolean isMoveBlacklisted(String move) {
        List<String> blacklistedMoves = TutorMoves.getMainConfig().getStringList("TutorMoves.Blacklisted-Moves").stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        return blacklistedMoves.contains(move);
    }

    /**
     * Checks if a move is a tutor move for X pokemon.
     *
     * @param move The name of the move.
     * @return True if the move is blacklisted, false otherwise.
     */
    public static boolean isTutorMoveForPokemon(Pokemon pokemon, String move) {
        try {
            String speciesName = pokemon.getSpecies().getName().toLowerCase();
            String formName = pokemon.getForm().getName().toLowerCase();
            Path jsonFile = findSpeciesJsonFile(speciesName);

            if (jsonFile != null) {
                try (InputStream inputStream = Files.newInputStream(jsonFile)) {
                    JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                    JsonArray baseMovesArray = jsonObject.getAsJsonArray("moves");

                    for (int i = 0; i < baseMovesArray.size(); i++) {
                        String moveName = baseMovesArray.get(i).getAsString();
                        if (moveName.startsWith("tutor:") && moveName.substring(6).equalsIgnoreCase(move) && !isMoveBlacklisted(move)) {
                            return true;
                        }
                    }

                    if (jsonObject.has("forms")) {
                        JsonArray formsArray = jsonObject.getAsJsonArray("forms");
                        for (int i = 0; i < formsArray.size(); i++) {
                            JsonObject formObject = formsArray.get(i).getAsJsonObject();
                            if (formObject.get("name").getAsString().toLowerCase().equals(formName)) {
                                JsonArray formMovesArray = formObject.getAsJsonArray("moves");
                                for (int j = 0; j < formMovesArray.size(); j++) {
                                    String moveName = formMovesArray.get(j).getAsString();
                                    if (moveName.startsWith("tutor:") && moveName.substring(6).equalsIgnoreCase(move) && !isMoveBlacklisted(move)) {
                                        return true;
                                    }
                                }
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Converts a string to its alphabetic equivalent by removing non-alphabetic characters
     * and converting accented characters to their non-accented versions.
     *
     * @param input The input string.
     * @return The alphabetic version of the input string.
     */
    public static String makeAlphabetic(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("[^\\p{IsAlphabetic}]", "");
    }
}
