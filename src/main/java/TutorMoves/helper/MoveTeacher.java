package TutorMoves.helper;

import com.cobblemon.mod.common.api.moves.BenchedMove;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.pokemon.moves.LearnsetQuery;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.Cobblemon;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


public class MoveTeacher {

    public static void teachMove(ServerPlayerEntity player, int slot, MoveTemplate move) {
        PlayerPartyStore partyStore;
        try {
            partyStore = Cobblemon.INSTANCE.getStorage().getParty(player.getUuid());
        } catch (NoPokemonStoreException e) {
            player.sendMessage(Text.literal("No Pokémon found in slot " + (slot + 1)).formatted(Formatting.RED));
            return;
        }
        Pokemon pokemon = partyStore.get(slot);

        if (pokemon == null) {
            player.sendMessage(Text.literal("No Pokémon found in slot " + (slot + 1)).formatted(Formatting.RED));
            return;
        }

        if (!LearnsetQuery.Companion.getANY().canLearn(move, pokemon.getForm().getMoves())) {
            player.sendMessage(Text.literal(pokemon.getDisplayName().getString() + " can't learn " + move.getDisplayName().getString()).formatted(Formatting.RED));
            return;
        }

        if (pokemon.getMoveSet().getMoves().stream().anyMatch(m -> m.getTemplate() == move) || pokemon.getBenchedMoves().iterator().hasNext() && pokemon.getBenchedMoves().iterator().next().getMoveTemplate() == move) {
            player.sendMessage(Text.literal(pokemon.getDisplayName().getString() + " already knows " + move.getDisplayName().getString()).formatted(Formatting.RED));
            return;
        }

        if (pokemon.getMoveSet().hasSpace()) {
            pokemon.getMoveSet().add(move.create());
        } else {
            pokemon.getBenchedMoves().add(new BenchedMove(move, 0));
        }

        player.sendMessage(Text.literal(pokemon.getDisplayName().getString() + " learned " + move.getDisplayName().getString()).formatted(Formatting.GREEN));
    }
}
