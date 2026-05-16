package me.novoro.tutormoves.helper;

import me.novoro.tutormoves.config.ConfigManager;
import me.novoro.tutormoves.config.LangManager;
import me.novoro.tutormoves.config.MoveOptions;
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
        return teachMove(player, slot, move, MoveOptions.fromGlobal());
    }

    public static boolean teachMove(ServerPlayerEntity player, int slot, MoveTemplate move, MoveOptions options) {
        PlayerPartyStore partyStore;
        partyStore = Cobblemon.INSTANCE.getStorage().getParty(player);
        Pokemon pokemon = partyStore.get(slot);

        if (pokemon == null) {
            LangManager.sendLang(player, "Error-No-Pokemon", Map.of("{slot}", String.valueOf(slot + 1)));
            return false;
        }

        // Check if the move is a valid tutor move for the Pokémon
        if (!isValidMove(pokemon, move, options)) {
            LangManager.sendLang(player, "Error-Not-Tutor", Map.of("{pokemon}", pokemon.getDisplayName(true).getString(), "{move}", move.getDisplayName().getString()));
            return false;
        }

        // Ensure the move can be learned by the Pokémon
        if (!LearnsetQuery.Companion.getANY().canLearn(move, pokemon.getForm().getMoves())) {
            LangManager.sendLang(player, "Error-Cant-Learn", Map.of("{pokemon}", pokemon.getDisplayName(true).getString(), "{move}", move.getDisplayName().getString()));
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
            LangManager.sendLang(player, "Error-Already-Knows", Map.of("{pokemon}", pokemon.getDisplayName(true).getString(), "{move}", move.getDisplayName().getString()));
            return false;
        }

        if (pokemon.getMoveSet().hasSpace()) {
            pokemon.getMoveSet().add(move.create());
        } else {
            pokemon.getBenchedMoves().add(new BenchedMove(move, 0));
        }

        LangManager.sendLang(player, "Success-Learned", Map.of("{pokemon}", pokemon.getDisplayName(true).getString(), "{move}", move.getDisplayName().getString()));
        return true;
    }

    // Checks if the move can be learned by the Pokémon based on enabled move types
    // This prevents pokemon like Corviknight from learning Roost as a TM Move when it's actually an Egg Move
    private static boolean isValidMove(Pokemon pokemon, MoveTemplate move, MoveOptions options) {
        if (options.tutorMoves() &&
                pokemon.getForm().getMoves().getTutorMoves().contains(move)) {
            return true;
        }

        if (options.eggMoves() &&
                pokemon.getForm().getMoves().getEggMoves().contains(move)) {
            return true;
        }

        if (options.tmMoves() &&
                pokemon.getForm().getMoves().getTmMoves().contains(move)) {
            return true;
        }

        if (options.evolutionMoves() &&
                pokemon.getForm().getMoves().getEvolutionMoves().contains(move)) {
            return true;
        }

        if (options.levelUpMoves() &&
                pokemon.getForm().getMoves().getLevelUpMoves().containsValue(move)) {
            return true;
        }

        if (options.formChangeMoves() &&
                pokemon.getForm().getMoves().getFormChangeMoves().contains(move)) {
            return true;
        }

        if (options.legacyMoves() &&
                pokemon.getForm().getMoves().getLegacyMoves().contains(move)) {
            return true;
        }

        if (options.specialMoves() &&
                pokemon.getForm().getMoves().getSpecialMoves().contains(move)) {
            return true;
        }

        return false;
    }
}