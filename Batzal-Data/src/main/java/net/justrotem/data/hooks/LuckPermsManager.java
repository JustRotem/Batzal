package net.justrotem.data.hooks;

import net.justrotem.data.utils.TextUtility;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PrefixNode;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LuckPermsManager {

    protected static LuckPerms api;

    /**
     * api be default will be null. you need to initialize before using
     * @param lpAPI   the LuckPerms api
     */
    public static void initializeAPI(LuckPerms lpAPI) {
        api = lpAPI;
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
        if (api == null) return Component.empty();

        String prefix = getLegacyGroupPrefix(name);
        if (prefix.isEmpty()) return Component.empty();

        return TextUtility.color(prefix);
    }

    public static String getLegacyGroupPrefix(String name) {
        if (api == null) return "";

        String prefix;
        try {
            prefix = Objects.requireNonNull(getGroup(name)).getCachedData().getMetaData().getPrefix();
            if (prefix == null) return "";
        } catch (NullPointerException e) {
            return "";
        }

        return prefix;
    }

    public static Component getGroupSuffix(String name) {
        if (api == null) return Component.empty();

        String suffix = getLegacyGroupSuffix(name);
        if (suffix.isEmpty()) return Component.empty();

        return TextUtility.color(suffix);
    }

    public static String getLegacyGroupSuffix(String name) {
        if (api == null) return "";

        String suffix;
        try {
            suffix = Objects.requireNonNull(getGroup(name)).getCachedData().getMetaData().getSuffix();
            if (suffix == null) return "";
        } catch (Exception e) {
            return "";
        }

        return suffix;
    }

    public static int getGroupWeight(String name) {
        if (api == null) return 0;

        Group group = getGroup(name);
        if (group == null) return 0;

        return group.getWeight().orElse(0);
    }

    public static Component getGroupDisplayName(String group) {
        if (api == null) return Component.empty();

        String displayname;
        try {
            displayname = Objects.requireNonNull(getGroup(group)).getDisplayName();
            if (displayname == null) return TextUtility.color(Objects.requireNonNull(getGroup(group)).getName());
        } catch (Exception e) {
            return null;
        }

        return TextUtility.color(displayname);
    }

    public static String getLegacyGroupDisplayName(String group) {
        if (api == null) return "";

        String displayname;
        try {
            displayname = Objects.requireNonNull(getGroup(group)).getDisplayName();
            if (displayname == null) return Objects.requireNonNull(getGroup(group)).getName();
        } catch (Exception e) {
            return null;
        }

        return displayname;
    }

    public static String getLegacyGroupDisplayName(UUID uuid) {
        return getLegacyGroupDisplayName(getPrimaryGroup(uuid));
    }

    public static CompletableFuture<User> loadUser(UUID uuid) {
        if (api == null) return null;

        UserManager userManager = api.getUserManager();
        return userManager.loadUser(uuid);
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
        if (api == null) return Component.empty();

        String prefix = getLegacyPrefix(uuid);
        if (prefix.isEmpty()) return Component.empty();

        return TextUtility.color(prefix);
    }

    public static String getLegacyPrefix(UUID uuid) {
        if (api == null) return "";

        String prefix;
        try {
            prefix = Objects.requireNonNull(loadUser(uuid)).join().getCachedData().getMetaData().getPrefix();
            if (prefix == null || prefix.isEmpty()) return "";
        } catch (Exception e) {
            return "";
        }

        return prefix;
    }

    public static Component getSuffix(UUID uuid) {
        if (api == null) return Component.empty();

        String suffix = getLegacySuffix(uuid);
        if (suffix.isEmpty()) return Component.empty();

        return TextUtility.color(suffix);
    }

    public static String getLegacySuffix(UUID uuid) {
        if (api == null) return "";

        String suffix;
        try {
            suffix = Objects.requireNonNull(loadUser(uuid)).join().getCachedData().getMetaData().getPrefix();
            if (suffix == null || suffix.isEmpty()) return "";
        } catch (Exception e) {
            return "";
        }

        return suffix;
    }

    public static String getPrimaryGroup(UUID uuid) {
        if (api == null) return "default";

        try {
            User user = Objects.requireNonNull(loadUser(uuid)).join();

            return user.getPrimaryGroup();
        } catch (NullPointerException ignored) {
        }
        return "default";
    }

    public static void setPrefix(UUID uuid, String prefix, int priority) {
        if (api == null) return;

        // Remove all old prefixes if you want a clean slate
        removePrefixes(uuid, priority);

        addPrefix(uuid, prefix, priority);
    }

    public static void addPrefix(UUID uuid, String prefix, int priority) {
        if (api == null) return;

        try {
            Objects.requireNonNull(loadUser(uuid)).thenAccept(user -> {
                // Add new prefix with specified priority (higher number = higher priority)
                PrefixNode node = PrefixNode.builder(prefix, priority).build();
                user.data().add(node);

                // Save changes
                api.getUserManager().saveUser(user);
            });
        } catch (NullPointerException ignored) {
        }
    }

    public static void removePrefixes(UUID uuid, String prefix, int priority) {
        if (api == null) return;

        try {
            Objects.requireNonNull(loadUser(uuid)).thenAccept(user -> {
                // Remove new prefix with specified priority (higher number = higher priority)
                PrefixNode node = PrefixNode.builder(prefix, priority).build();
                user.data().remove(node);

                // Save changes
                api.getUserManager().saveUser(user);
            });
        } catch (NullPointerException ignored) {
        }
    }

    public static void removePrefixes(UUID uuid, int priority) {
        if (api == null) return;

        try {
            Objects.requireNonNull(loadUser(uuid)).thenAccept(user -> {
                for (Node node : user.data().toCollection()) {
                    if (node instanceof PrefixNode prefixNode) {
                        if (prefixNode.getPriority() == priority) user.data().remove(node);
                    }
                }

                // Save changes
                api.getUserManager().saveUser(user);
            });
        } catch (NullPointerException ignored) {
        }
    }

    public void setPrimaryGroup(UUID uuid, String group) {
        if (api == null) return;

        try {
            Objects.requireNonNull(loadUser(uuid)).thenAccept(user -> {
                user.setPrimaryGroup(group);
                api.getUserManager().saveUser(user);
            });
        } catch (NullPointerException ignored) {
        }
    }

    public void addGroup(UUID uuid, String group) {
        if (api == null) return;

        try {
            Objects.requireNonNull(loadUser(uuid)).thenAccept(user -> {
                Group g = getGroup(group);
                if (g == null) return;

                Node node = InheritanceNode.builder(g).build();
                user.data().add(node);

                api.getUserManager().saveUser(user);
            });
        } catch (NullPointerException ignored) {
        }
    }

    public void addGroupTemporary(UUID uuid, String group, Duration duration) {
        if (api == null) return;

        try {
            Objects.requireNonNull(loadUser(uuid)).thenAccept(user -> {
                Node node = InheritanceNode.builder(group).expiry(duration).build();

                user.data().add(node);
                api.getUserManager().saveUser(user);
            });
        } catch (NullPointerException ignored) {
        }
    }

    public void removeGroup(UUID uuid, String group) {
        if (api == null) return;

        try {
            Objects.requireNonNull(loadUser(uuid)).thenAccept(user -> {
                Node node = InheritanceNode.builder(group).build();
                user.data().remove(node);
                api.getUserManager().saveUser(user);
            });
        } catch (NullPointerException ignored) {
        }
    }

    public void clearGroups(UUID uuid) {
        if (api == null) return;

        try {
            Objects.requireNonNull(loadUser(uuid)).thenAccept(user -> {
                // remove any inheritance node
                user.data().clear(node -> node instanceof InheritanceNode);
                api.getUserManager().saveUser(user);
            });
        } catch (NullPointerException ignored) {
        }
    }

    public static boolean hasPermission(Group group, String permission) {
        if (api == null) return false;

        try {
            if (group == null) return false;
            if (permission == null || permission.isEmpty()) return true;

            return group.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasPermission(UUID uuid, String permission) {
        if (api == null) return false;

        try {
            if (uuid == null) return false;
            if (permission == null || permission.isEmpty()) return true;

            return Objects.requireNonNull(loadUser(uuid)).join().getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        } catch (NullPointerException ignored) {
        }

        return false;
    }
}
