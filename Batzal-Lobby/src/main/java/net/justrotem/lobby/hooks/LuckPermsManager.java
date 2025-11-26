package net.justrotem.lobby.hooks;

import net.justrotem.lobby.nick.NickManager;
import net.luckperms.api.model.group.Group;
import org.bukkit.entity.Player;

public class LuckPermsManager extends net.justrotem.data.hooks.LuckPermsManager {

    public static Group getDisplayedPrimaryGroup(Player player) {
        if (NickManager.isLobbyNicked(player)) return getGroup(NickManager.getRank(player));
        return getGroup(getPrimaryGroup(player));
    }

    /**
     *
     * @return if the player has the permission BASED ON THE PRIMARY GROUP
     */
    public static boolean hasPermission(Player player, String permission) {
        return LuckPermsManager.hasPermission(getDisplayedPrimaryGroup(player), permission);
    }
}
