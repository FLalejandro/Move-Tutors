package me.novoro.tutormoves.utils;

import me.novoro.tutormoves.config.ConfigManager;
import me.novoro.tutormoves.config.MoveOptions;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.network.ServerPlayerEntity;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    public static List<String> getMovesForSlot(ServerPlayerEntity player, int slot) throws NoPokemonStoreException {
        return getMovesForSlot(player, slot, MoveOptions.fromGlobal(), ConfigManager.getMoveBlacklist());
    }

    public static List<String> getMovesForSlot(ServerPlayerEntity player, int slot, MoveOptions options, List<String> blacklistedMoves) throws NoPokemonStoreException {
        Pokemon pokemon = getPokemonInSlot(player, slot);
        if (pokemon == null) {
            return List.of();
        }

        Stream<MoveTemplate> moves = Stream.empty();

        if (options.tutorMoves()) {
            moves = Stream.concat(moves, pokemon.getForm().getMoves().getTutorMoves().stream());
        }
        if (options.eggMoves()) {
            moves = Stream.concat(moves, pokemon.getForm().getMoves().getEggMoves().stream());
        }
        if (options.tmMoves()) {
            moves = Stream.concat(moves, pokemon.getForm().getMoves().getTmMoves().stream());
        }
        if (options.evolutionMoves()) {
            moves = Stream.concat(moves, pokemon.getForm().getMoves().getEvolutionMoves().stream());
        }
        if (options.levelUpMoves()) {
            Map<?, ?> levelUpMoves = pokemon.getForm().getMoves().getLevelUpMoves();
            Stream<MoveTemplate> levelUpStream = levelUpMoves.values().stream()
                    .flatMap(value -> {
                        if (value instanceof List) {
                            return ((List<?>) value).stream()
                                    .filter(MoveTemplate.class::isInstance)
                                    .map(MoveTemplate.class::cast);
                        } else {
                            return Stream.empty();
                        }
                    });
            moves = Stream.concat(moves, levelUpStream);
        }
        if (options.formChangeMoves()) {
            moves = Stream.concat(moves, pokemon.getForm().getMoves().getFormChangeMoves().stream());
        }
        if (options.legacyMoves()) {
            moves = Stream.concat(moves, pokemon.getForm().getMoves().getLegacyMoves().stream());
        }
        if (options.specialMoves()) {
            moves = Stream.concat(moves, pokemon.getForm().getMoves().getSpecialMoves().stream());
        }

        List<String> normalizedBlacklist = blacklistedMoves.stream()
                .map(String::toLowerCase)
                .toList();

        return moves
                .filter(move -> !normalizedBlacklist.contains(move.getName().toLowerCase()))
                .map(MoveTemplate::getName)
                .distinct()
                .toList();
    }

    /**
     * Returns all learnable moves of the given type for the Pokémon using the provided MoveOptions.
     *
     * @param pokemon   The Pokémon to query.
     * @param typeName  The elemental type name to filter by.
     * @param options   The move options determining which learnsets to include.
     * @return A list of distinct MoveTemplates of the given type that the Pokémon can learn.
     */
    public static List<MoveTemplate> getLearnableMovesOfType(Pokemon pokemon, String typeName, MoveOptions options) {
        List<MoveTemplate> result = new ArrayList<>();

        if (options.tutorMoves()) {
            result.addAll(pokemon.getForm().getMoves().getTutorMoves());
        }
        if (options.eggMoves()) {
            result.addAll(pokemon.getForm().getMoves().getEggMoves());
        }
        if (options.tmMoves()) {
            result.addAll(pokemon.getForm().getMoves().getTmMoves());
        }
        if (options.evolutionMoves()) {
            result.addAll(pokemon.getForm().getMoves().getEvolutionMoves());
        }
        if (options.levelUpMoves()) {
            Map<?, ?> levelUpMoves = pokemon.getForm().getMoves().getLevelUpMoves();
            levelUpMoves.values().forEach(value -> {
                if (value instanceof List<?> list) {
                    for (Object obj : list) {
                        if (obj instanceof MoveTemplate mt) {
                            result.add(mt);
                        }
                    }
                }
            });
        }
        if (options.formChangeMoves()) {
            result.addAll(pokemon.getForm().getMoves().getFormChangeMoves());
        }
        if (options.legacyMoves()) {
            result.addAll(pokemon.getForm().getMoves().getLegacyMoves());
        }
        if (options.specialMoves()) {
            result.addAll(pokemon.getForm().getMoves().getSpecialMoves());
        }

        return result.stream()
                .filter(move -> move.getElementalType().getName().equalsIgnoreCase(typeName))
                .distinct()
                .toList();
    }

    /**
     * Checks whether the given move is learnable by the Pokémon under the provided MoveOptions.
     *
     * @param pokemon The Pokémon to check.
     * @param move    The move template to check.
     * @param options The move options determining which learnsets to include.
     * @return True if the move is in at least one enabled learnset category, false otherwise.
     */
    public static boolean isMoveLearnable(Pokemon pokemon, MoveTemplate move, MoveOptions options) {
        if (options.tutorMoves() && pokemon.getForm().getMoves().getTutorMoves().contains(move)) {
            return true;
        }
        if (options.eggMoves() && pokemon.getForm().getMoves().getEggMoves().contains(move)) {
            return true;
        }
        if (options.tmMoves() && pokemon.getForm().getMoves().getTmMoves().contains(move)) {
            return true;
        }
        if (options.evolutionMoves() && pokemon.getForm().getMoves().getEvolutionMoves().contains(move)) {
            return true;
        }
        if (options.levelUpMoves() && pokemon.getForm().getMoves().getLevelUpMoves().containsValue(move)) {
            return true;
        }
        if (options.formChangeMoves() && pokemon.getForm().getMoves().getFormChangeMoves().contains(move)) {
            return true;
        }
        if (options.legacyMoves() && pokemon.getForm().getMoves().getLegacyMoves().contains(move)) {
            return true;
        }
        if (options.specialMoves() && pokemon.getForm().getMoves().getSpecialMoves().contains(move)) {
            return true;
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
        return normalized.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]", "");
    }
}
