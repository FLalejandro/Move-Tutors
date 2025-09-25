package me.novoro.tutormoves.helper;

import me.novoro.tutormoves.config.ConfigManager;
import me.novoro.tutormoves.config.LangManager;
import com.cobblemon.mod.common.api.moves.BenchedMove;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.pokemon.moves.LearnsetQuery;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.Cobblemon;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

public class MoveTeacher {

    public static boolean teachMove(ServerPlayerEntity player, int slot, MoveTemplate move) {
        PlayerPartyStore partyStore;
        partyStore = Cobblemon.INSTANCE.getStorage().getParty(player);
        Pokemon pokemon = partyStore.get(slot);

        if (pokemon == null) {
            LangManager.sendLang(player, "Error-No-Pokemon", Map.of("{slot}", String.valueOf(slot + 1)));
            return false;
        }

        // TODO: Change this so it revolves around a config setting for all types of moves
        // TODO: 1.6 and 1.7 have major differences
        // Check if the move is a valid tutor move for the Pokémon
        if (!isValidMove(pokemon, move)) {
            LangManager.sendLang(player, "Error-Not-Tutor", Map.of("{pokemon}", pokemon.getDisplayName().getString(), "{move}", move.getDisplayName().getString()));
            return false;
        }

        // Ensure the move can be learned by the Pokémon
        if (!LearnsetQuery.Companion.getANY().canLearn(move, pokemon.getForm().getMoves())) {
            LangManager.sendLang(player, "Error-Cant-Learn", Map.of("{pokemon}", pokemon.getDisplayName().getString(), "{move}", move.getDisplayName().getString()));
            return false;
        }

        boolean knowsMove = pokemon.getMoveSet().getMoves().stream().anyMatch(m -> m.getTemplate() == move);

        if (!knowsMove) {
            for (BenchedMove benchedMove : pokemon.getBenchedMoves()) {
                if (benchedMove.getMoveTemplate() == move) {
                    knowsMove = true;
                    break;
                }
            }
        }

        if (knowsMove) {
            LangManager.sendLang(player, "Error-Already-Knows", Map.of("{pokemon}", pokemon.getDisplayName().getString(), "{move}", move.getDisplayName().getString()));
            return false;
        }

        if (pokemon.getMoveSet().hasSpace()) {
            pokemon.getMoveSet().add(move.create());
        } else {
            pokemon.getBenchedMoves().add(new BenchedMove(move, 0));
        }

        LangManager.sendLang(player, "Success-Learned", Map.of("{pokemon}", pokemon.getDisplayName().getString(), "{move}", move.getDisplayName().getString()));
        return true;
    }

    // Checks if the move can be learned by the Pokémon based on enabled move types
    // This prevents pokemon like Corviknight from learning Roost as a TM Move when it's actually an Egg Move
    private static boolean isValidMove(Pokemon pokemon, MoveTemplate move) {
        // Check tutor moves if enabled
        if (ConfigManager.isTutorMovesEnabled() &&
                pokemon.getForm().getMoves().getTutorMoves().contains(move)) {
            return true;
        }

        // Check egg moves if enabled
        if (ConfigManager.isEggMovesEnabled() &&
                pokemon.getForm().getMoves().getEggMoves().contains(move)) {
            return true;
        }

        // Check TM moves if enabled
        if (ConfigManager.isTmMovesEnabled() &&
                pokemon.getForm().getMoves().getTmMoves().contains(move)) {
            return true;
        }

        // Check evolution moves if enabled
        if (ConfigManager.isEvolutionMoves() &&
                pokemon.getForm().getMoves().getEvolutionMoves().contains(move)) {
            return true;
        }

        // Check level-up moves if enabled
        if (ConfigManager.isLevelUpMoves() &&
                pokemon.getForm().getMoves().getLevelUpMoves().containsValue(move)) {
            return true;
        }

        // Check form change moves if enabled
        if (ConfigManager.isFormChangeMoves() &&
                pokemon.getForm().getMoves().getFormChangeMoves().contains(move)) {
            return true;
        }

        return false;
    }
}