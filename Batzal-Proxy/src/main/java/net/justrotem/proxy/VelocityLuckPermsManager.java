package net.justrotem.data.hooks;

import com.velocitypowered.api.proxy.Player;
import net.justrotem.data.cache.LuckPermsManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.slf4j.Logger;

public class VelocityLuckPermsManager extends LuckPermsManager {

    public static void init(Logger log, LuckPerms lp, boolean debug) {
        if (lp != null) {
            try {
                initializeAPI(lp);
                if (debug) log.info("LuckPerms detected and initialized!");
            } catch (Exception e) {
                if (debug) log.warn("LuckPerms plugin is present but API could not be loaded.");
            }
        } else {
            if (debug) log.info("LuckPerms not found, skipping integration.");
        }
    }

    public static User getUser(Player player) {
        if (api == null) return null;
        return api.getPlayerAdapter(Player.class).getUser(player);
    }

    public static String getPrimaryGroup(Player player) {
        return getPrimaryGroup(player.getUniqueId());
    }
}
