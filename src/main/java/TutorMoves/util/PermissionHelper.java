package TutorMoves.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Provides utility functions for checking permissions using LuckPerms.
 * Primarily used to validate if a player has certain privileges within the EEssentials mod.
 */
public class PermissionHelper {

    public LuckPerms luckperms = LuckPermsProvider.get();

    public User getLuckPermsUser(ServerPlayerEntity player) {
        return luckperms.getPlayerAdapter(
                ServerPlayerEntity.class
        ).getUser(player);
    }

}

