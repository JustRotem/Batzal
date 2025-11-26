package net.justrotem.data.hooks;

import net.justrotem.data.utils.Utility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class LuckPermsManager {

    private static LuckPerms api;
    private static JavaPlugin plugin = null;

    public static void init(JavaPlugin plugin) {
        Plugin lpPlugin = Bukkit.getPluginManager().getPlugin("LuckPerms");

        if (lpPlugin != null && lpPlugin.isEnabled()) {
            try {
                api = LuckPermsProvider.get(); // get LuckPerms API
                if (Utility.isDebug(plugin)) plugin.getLogger().info("LuckPerms detected and initialized!");
                LuckPermsManager.plugin = plugin;
            } catch (IllegalStateException e) {
                if (Utility.isDebug(plugin)) plugin.getLogger().warning("LuckPerms plugin is present but API could not be loaded.");
            }
        } else {
            if (Utility.isDebug(plugin)) plugin.getLogger().info("LuckPerms not found, skipping integration.");
        }

    }

    public static boolean isHooked() {
        return api != null;
    }

    public static Group getGroup(String name) {
        if (api == null) return null;

        for (Group group : api.getGroupManager().getLoadedGroups()) {
            if (group.getName().equalsIgnoreCase(name)) return group;
        }

        return api.getGroupManager().getGroup("default");
    }

    public static Component getGroupPrefix(String name) {
        if (api == null) return Component.text().build();

        String prefix = getLegacyGroupPrefix(name);
        if (prefix.isEmpty()) return Component.text().build();

        return color(prefix);
    }

    public static String getLegacyGroupPrefix(String name) {
        if (api == null) return "";

        String prefix = getGroup(name).getCachedData().getMetaData().getPrefix();
        if (prefix == null) return "";

        return prefix;
    }

    public static Component getGroupSuffix(String name) {
        if (api == null) return Component.text().build();

        String suffix = getLegacyGroupSuffix(name);
        if (suffix.isEmpty()) return Component.text().build();

        return color(suffix);
    }

    public static String getLegacyGroupSuffix(String name) {
        if (api == null) return "";

        String suffix = getGroup(name).getCachedData().getMetaData().getSuffix();
        if (suffix == null) return "";

        return suffix;
    }

    public static int getGroupWeight(String name) {
        if (api == null) return 0;

        Group group = getGroup(name);
        if (group == null) return 0;

        return group.getWeight().orElse(0);
    }

    public static Component getGroupDisplayName(String name) {
        if (api == null) return Component.text().build();

        String displayname = getGroup(name).getDisplayName();
        if (displayname == null) return color(getGroup(name).getName());

        return color(displayname);
    }

    public static User getUser(Player player) {
        if (api == null) return null;

        return api.getPlayerAdapter(Player.class).getUser(player);
    }

    public static User getUser(UUID uuid) {
        if (api == null) return null;

        UserManager userManager = api.getUserManager();
        CompletableFuture<User> userFuture = userManager.loadUser(uuid);
        return userFuture.join();
    }

    public static boolean isUserInherit(UUID uuid, String group) {
        if (api == null) return false;

        return api.getUserManager().loadUser(uuid)
                .thenApplyAsync(user -> {
                    Collection<Group> inheritedGroups = user.getInheritedGroups(user.getQueryOptions());
                    return inheritedGroups.stream().anyMatch(g -> g.getName().equalsIgnoreCase(group));
                }).join();
    }

    public static Component getPrefix(UUID uuid) {
        if (api == null) return Component.text().build();

        String prefix = getLegacyPrefix(uuid);
        if (prefix.isEmpty()) return Component.text().build();

        return color(prefix);
    }

    public static String getLegacyPrefix(UUID uuid) {
        if (api == null) return "";

        String prefix = getUser(uuid).getCachedData().getMetaData().getPrefix();
        if (prefix == null || prefix.isEmpty()) return "";

        return prefix;
    }

    public static Component getSuffix(UUID uuid) {
        if (api == null) return Component.text().build();

        String suffix = getLegacySuffix(uuid);
        if (suffix.isEmpty()) return Component.text().build();

        return color(suffix);
    }

    public static String getLegacySuffix(UUID uuid) {
        if (api == null) return "";

        String suffix = getUser(uuid).getCachedData().getMetaData().getPrefix();
        if (suffix == null || suffix.isEmpty()) return "";

        return suffix;
    }

    public static String getPrimaryGroup(UUID uuid) {
        if (api == null) return "default";

        User user = getUser(uuid);
        if (user == null) return "default";

        return user.getPrimaryGroup();
    }

    public static String getPrimaryGroup(Player player) {
        return getPrimaryGroup(player.getUniqueId());
    }

    public static void setPrefix(Player player, String prefix, int priority) {
        if (api == null) return;

        // Remove all old prefixes if you want a clean slate
        removePrefixes(player, priority);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Add new prefix with specified priority (higher number = higher priority)
            addPrefix(player, prefix, priority);
        }, 5L);
    }

    public static void addPrefix(Player player, String prefix, int priority) {
        if (api == null) return;

        // Get the LuckPerms user
        CompletableFuture<User> userFuture = api.getUserManager().loadUser(player.getUniqueId());

        userFuture.thenAccept(user -> {
            // Add new prefix with specified priority (higher number = higher priority)
            PrefixNode node = PrefixNode.builder(prefix, priority).build();
            user.data().add(node);

            // Save changes
            api.getUserManager().saveUser(user);
        });
    }

    public static void removePrefixes(Player player, String prefix, int priority) {
        if (api == null) return;

        // Get the LuckPerms user
        CompletableFuture<User> userFuture = api.getUserManager().loadUser(player.getUniqueId());

        userFuture.thenAccept(user -> {
            // Remove new prefix with specified priority (higher number = higher priority)
            PrefixNode node = PrefixNode.builder(prefix, priority).build();
            user.data().remove(node);

            // Save changes
            api.getUserManager().saveUser(user);
        });
    }

    public static void removePrefixes(Player player, int priority) {
        if (api == null) return;

        // Get the LuckPerms user
        CompletableFuture<User> userFuture = api.getUserManager().loadUser(player.getUniqueId());

        userFuture.thenAccept(user -> {
            for (Node node : user.data().toCollection()) {
                if (node instanceof PrefixNode prefixNode) {
                    if (prefixNode.getPriority() == priority) user.data().remove(node);
                }
            }

            // Save changes
            api.getUserManager().saveUser(user);
        });
    }

    public static boolean hasPermission(Group group, String permission) {
        if (api == null) return false;

        try {
            if (group == null) return false;

            return group.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    private static Component color(String text) {
        final MiniMessage mm = MiniMessage.miniMessage();

        // Convert & codes to MiniMessage-compatible tags
        String replaced = text
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&l", "<bold>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&m", "<strikethrough>")
                .replace("&r", "<reset>");
        return mm.deserialize(replaced);
    }
}
