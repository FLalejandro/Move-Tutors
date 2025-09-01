package me.novoro.TutorMoves.events;

import me.novoro.TutorMoves.TutorMoves;
import me.novoro.TutorMoves.guis.SelectionScreen;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EntityInteractEvent implements UseEntityCallback {

    private static final Map<UUID, Long> lastInteractionTime = new HashMap<>();
    private static final long COOLDOWN = 500; // Cooldown in milliseconds

    @Override
    public ActionResult interact(PlayerEntity player, net.minecraft.world.World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        if (!(player instanceof ServerPlayerEntity) || hand != Hand.MAIN_HAND) {
            return ActionResult.PASS; // Ensure it's a server player using the main hand
        }

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        UUID playerUUID = serverPlayer.getUuid();
        UUID entityUUID = entity.getUuid();

        // Check for cooldown
        if (!isCooldownElapsed(playerUUID)) {
            return ActionResult.PASS;
        }

        // Check if the player is in NPC mode
        if (TutorMoves.npcModePlayers.containsKey(playerUUID)) {
            String tutor = TutorMoves.npcModePlayers.get(playerUUID);

            // Toggle the NPC state
            boolean isNPCNow = toggleNPCEntity(entityUUID, tutor);
            if (isNPCNow) {
                serverPlayer.sendMessage(Text.literal("NPC tutor added for " + tutor).formatted(Formatting.GREEN));
            } else {
                serverPlayer.sendMessage(Text.literal("NPC tutor removed.").formatted(Formatting.RED));
            }

            lastInteractionTime.put(playerUUID, System.currentTimeMillis());
            TutorMoves.saveNPCEntities();
            return ActionResult.SUCCESS;
        } else {
            // Handle interaction outside of NPC mode
            return handleNonNPCModeInteraction(serverPlayer, entityUUID);
        }
    }

    private boolean toggleNPCEntity(UUID entityUUID, String tutor) {
        if (TutorMoves.npcEntities.containsKey(entityUUID)) {
            TutorMoves.npcEntities.remove(entityUUID);
            return false; // NPC removed
        } else {
            TutorMoves.npcEntities.put(entityUUID, tutor);
            return true; // NPC added
        }
    }

    private ActionResult handleNonNPCModeInteraction(ServerPlayerEntity player, UUID entityUUID) {
        if (TutorMoves.npcEntities.containsKey(entityUUID)) {
            String tutor = TutorMoves.npcEntities.get(entityUUID);
            if ("general".equalsIgnoreCase(tutor)) {
                SelectionScreen.open(player, Optional.empty());
            } else {
                SelectionScreen.open(player, Optional.of(tutor));
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    private boolean isCooldownElapsed(UUID playerUUID) {
        Long lastTime = lastInteractionTime.get(playerUUID);
        long currentTime = System.currentTimeMillis();
        return lastTime == null || (currentTime - lastTime > COOLDOWN);
    }
}
