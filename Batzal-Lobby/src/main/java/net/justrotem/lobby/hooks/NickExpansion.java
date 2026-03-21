package net.justrotem.lobby.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.justrotem.lobby.nick.NickManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NickExpansion extends PlaceholderExpansion {

    public static void init(JavaPlugin plugin) {
        Plugin papiPlugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");

        if (papiPlugin != null && papiPlugin.isEnabled()) {
            try {
                new NickExpansion().register();
                plugin.getLogger().info("PlaceholderAPI detected and initialized!");
            } catch (IllegalStateException e) {
                plugin.getLogger().warning("PlaceholderAPI plugin is present but API could not be loaded.");
            }
        } else {
            plugin.getLogger().info("PlaceholderAPI not found, skipping integration.");
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "batzal";
    }

    @Override
    public @NotNull String getAuthor() {
        return "JustRotem";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("rank_order")) {
            String rank;
            if (NickManager.isLobbyNicked(player) || NickManager.hasDifferentName(player)) rank = NickManager.getRank(player);
            else rank = LuckPermsManager.getPrimaryGroup(player.getUniqueId());

            return String.valueOf(LuckPermsManager.getGroupWeight(rank));
        }

        return null;
    }
}
