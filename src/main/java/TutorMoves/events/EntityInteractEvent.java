package TutorMoves.events;

import TutorMoves.TutorMoves;
import TutorMoves.commands.TutorCommands;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

import java.util.UUID;

public class EntityInteractEvent implements UseEntityCallback {

    @Override
    public ActionResult interact(PlayerEntity player, net.minecraft.world.World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        if (!(player instanceof ServerPlayerEntity)) {
            return ActionResult.PASS;
        }

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        UUID playerUUID = serverPlayer.getUuid();

        if (TutorMoves.npcModePlayers.containsKey(playerUUID)) {
            String tutor = TutorMoves.npcModePlayers.remove(playerUUID);
            UUID entityUUID = entity.getUuid();

            if (TutorMoves.npcEntities.containsKey(entityUUID)) {
                TutorMoves.npcEntities.remove(entityUUID);
                serverPlayer.sendMessage(Text.literal("NPC tutor removed.").formatted(Formatting.RED));
            } else {
                TutorMoves.npcEntities.put(entityUUID, tutor);
                serverPlayer.sendMessage(Text.literal("NPC tutor added for " + tutor).formatted(Formatting.GREEN));
            }

            TutorMoves.saveNPCEntities();
            return ActionResult.SUCCESS;
        } else if (TutorMoves.npcEntities.containsKey(entity.getUuid())) {
            String tutor = TutorMoves.npcEntities.get(entity.getUuid());

            try {
                if ("general".equalsIgnoreCase(tutor)) {
                    TutorCommands.openSelectionMenu(serverPlayer.getCommandSource());
                } else {
                    TutorCommands.openSpecificTutor(serverPlayer, tutor, 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
