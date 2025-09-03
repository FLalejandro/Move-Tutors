package me.novoro.TutorMoves.helper;

import me.novoro.TutorMoves.util.LangManager;
import com.cobblemon.mod.common.api.moves.BenchedMove;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.pokemon.moves.LearnsetQuery;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.Cobblemon;
import net.kyori.adventure.audience.Audience;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

public class MoveTeacher {

    public static boolean teachMove(ServerPlayerEntity player, int slot, MoveTemplate move) {
        PlayerPartyStore partyStore;
        partyStore = Cobblemon.INSTANCE.getStorage().getParty(player);
        Pokemon pokemon = partyStore.get(slot);

        if (pokemon == null) {
            LangManager.send((Audience) player, "Error-No-Pokemon", Map.of("{slot}", String.valueOf(slot + 1)));
            return false;
        }

        // Check if the move is a valid tutor move for the Pokémon
        if (!pokemon.getForm().getMoves().getTutorMoves().contains(move) && !pokemon.getForm().getMoves().getTmMoves().contains(move)) {
            LangManager.send((Audience) player, "Error-Not-Tutor", Map.of("{pokemon}", pokemon.getDisplayName(true).getString(), "{move}", move.getDisplayName().getString()));
            return false;
        }

        // Ensure the move can be learned by the Pokémon
        if (!LearnsetQuery.Companion.getANY().canLearn(move, pokemon.getForm().getMoves())) {
            LangManager.send((Audience) player, "Error-Cant-Learn", Map.of("{pokemon}", pokemon.getDisplayName(true).getString(), "{move}", move.getDisplayName().getString()));
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
            LangManager.send((Audience) player, "Error-Already-Knows", Map.of("{pokemon}", pokemon.getDisplayName(true).getString(), "{move}", move.getDisplayName().getString()));
            return false;
        }

        if (pokemon.getMoveSet().hasSpace()) {
            pokemon.getMoveSet().add(move.create());
        } else {
            pokemon.getBenchedMoves().add(new BenchedMove(move, 0));
        }

        LangManager.send((Audience) player, "Success-Learned", Map.of("{pokemon}", pokemon.getDisplayName(true).getString(), "{move}", move.getDisplayName().getString()));
        return true;
    }
}