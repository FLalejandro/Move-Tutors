package TutorMoves.util;

import TutorMoves.TutorMoves;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.network.ServerPlayerEntity;

import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PokemonUtil {

    /**
     * Fetches the Pokémon in the specified slot for the given player.
     *
     * @param player The player whose Pokémon slot is being queried.
     * @param slot   The slot number.
     * @return The Pokémon in the specified slot.
     */
    public static Pokemon getPokemonInSlot(ServerPlayerEntity player, int slot) throws NoPokemonStoreException {
        slot = slot - 1;
        PlayerPartyStore partyStore = Cobblemon.INSTANCE.getStorage().getParty(player);
        return partyStore.get(slot);
    }

    /**
     * Fetches the tutor moves for the Pokémon in the specified slot for the given player.
     * This method uses the Cobblemon API to directly retrieve tutor moves.
     *
     * @param player The player whose Pokémon slot is being queried.
     * @param slot   The slot number.
     * @return A list of tutor moves for the Pokémon in the specified slot.
     */
    public static List<String> getTutorMovesForSlot(ServerPlayerEntity player, int slot) throws NoPokemonStoreException {
        Pokemon pokemon = getPokemonInSlot(player, slot);
        if (pokemon != null) {
            List<MoveTemplate> tutorMoves = pokemon.getForm().getMoves().getTutorMoves();
            List<MoveTemplate> eggMoves = pokemon.getForm().getMoves().getEggMoves();

            return Stream.concat(tutorMoves.stream(), eggMoves.stream())
                    .filter(move -> !isMoveBlacklisted(move.getName().toLowerCase()))
                    .map(MoveTemplate::getName)
                    .distinct()
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    /**
     * Checks if a move is blacklisted in the configuration.
     *
     * @param move The name of the move.
     * @return True if the move is blacklisted, false otherwise.
     */
    private static boolean isMoveBlacklisted(String move) {
        List<String> blacklistedMoves = TutorMoves.getMainConfig().getStringList("TutorMoves.Blacklisted-Moves")
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        return blacklistedMoves.contains(move);
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
        return normalized.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]", "");
    }
}
