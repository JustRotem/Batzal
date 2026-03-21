package net.justrotem.data.hooks;

import net.justrotem.data.bukkit.BukkitUtility;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitLuckPermsManager extends LuckPermsManager {

    public static void init(JavaPlugin plugin) {
        Plugin lpPlugin = plugin.getServer().getPluginManager().getPlugin("LuckPerms");

        if (lpPlugin != null && lpPlugin.isEnabled()) {
            try {
                initializeAPI(LuckPermsProvider.get()); // get LuckPerms API
                if (BukkitUtility.isDebug(plugin)) plugin.getLogger().info("LuckPerms detected and initialized!");
            } catch (IllegalStateException e) {
                if (BukkitUtility.isDebug(plugin)) plugin.getLogger().warning("LuckPerms plugin is present but API could not be loaded.");
            }
        } else {
            if (BukkitUtility.isDebug(plugin)) plugin.getLogger().info("LuckPerms not found, skipping integration.");
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
