package net.justrotem.data.integration.luckperms;

import net.justrotem.data.util.TextFormatter;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PrefixNode;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class LuckPermsService {

    private static LuckPerms api;

    private LuckPermsService() {
    }

    public static void initializeAPI(LuckPerms lpAPI) {
        api = Objects.requireNonNull(lpAPI, "LuckPerms API cannot be null");
    }

    public static boolean isHooked() {
        return api != null;
    }

    private static LuckPerms requireApi() {
        if (api == null) {
            throw new IllegalStateException("LuckPerms API is not initialized");
        }
        return api;
    }

    //<editor-fold desc="User">

    public static Optional<User> getCachedUser(UUID uuid) {
        if (uuid == null || !isHooked()) {
            return Optional.empty();
        }

        return Optional.ofNullable(api.getUserManager().getUser(uuid));
    }

    public static CompletableFuture<Optional<User>> findUser(UUID uuid) {
        if (uuid == null || !isHooked()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        User cached = api.getUserManager().getUser(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(Optional.of(cached));
        }

        return requireApi().getUserManager().loadUser(uuid)
                .thenApply(Optional::ofNullable)
                .exceptionally(throwable -> Optional.empty());
    }

    /**
     * Legacy sync wrapper.
     * מחזיר רק משתמש טעון כרגע, בלי לחסום.
     */
    public static User getUser(UUID uuid) {
        return getCachedUser(uuid).orElse(null);
    }

    //</editor-fold>

    //<editor-fold desc="Group">

    public static Group getGroup(String name) {
        if (!isHooked()) return null;

        Group group = api.getGroupManager().getGroup(name);
        if (group != null) return group;

        return api.getGroupManager().getGroup("default");
    }

    public static int getGroupWeight(String name) {
        Group group = getGroup(name);
        return group != null ? group.getWeight().orElse(0) : 0;
    }

    //</editor-fold>

    //<editor-fold desc="Group Meta">

    public static String getLegacyGroupPrefix(String name) {
        return Optional.ofNullable(getGroup(name))
                .map(group -> group.getCachedData().getMetaData().getPrefix())
                .filter(prefix -> prefix != null && !prefix.isEmpty())
                .orElse("");
    }

    public static Component getGroupPrefix(String name) {
        String prefix = getLegacyGroupPrefix(name);
        return prefix.isEmpty() ? Component.empty() : TextFormatter.color(prefix);
    }

    public static String getLegacyGroupSuffix(String name) {
        return Optional.ofNullable(getGroup(name))
                .map(group -> group.getCachedData().getMetaData().getSuffix())
                .filter(suffix -> suffix != null && !suffix.isEmpty())
                .orElse("");
    }

    public static Component getGroupSuffix(String name) {
        String suffix = getLegacyGroupSuffix(name);
        return suffix.isEmpty() ? Component.empty() : TextFormatter.color(suffix);
    }

    public static String getLegacyGroupDisplayName(String group) {
        Group g = getGroup(group);
        if (g == null) return "";

        String display = g.getDisplayName();
        return display != null ? display : g.getName();
    }

    public static Component getGroupDisplayName(String group) {
        String name = getLegacyGroupDisplayName(group);
        return name.isEmpty() ? Component.empty() : TextFormatter.color(name);
    }

    //</editor-fold>

    //<editor-fold desc="User Meta Async">

    public static CompletableFuture<String> getLegacyPrefixAsync(UUID uuid) {
        return findUser(uuid).thenApply(optional ->
                optional.map(user -> user.getCachedData().getMetaData().getPrefix())
                        .filter(prefix -> prefix != null && !prefix.isEmpty())
                        .orElse("")
        );
    }

    public static CompletableFuture<Component> getPrefixAsync(UUID uuid) {
        return getLegacyPrefixAsync(uuid)
                .thenApply(prefix -> prefix.isEmpty() ? Component.empty() : TextFormatter.color(prefix));
    }

    public static CompletableFuture<String> getLegacySuffixAsync(UUID uuid) {
        return findUser(uuid).thenApply(optional ->
                optional.map(user -> user.getCachedData().getMetaData().getSuffix())
                        .filter(suffix -> suffix != null && !suffix.isEmpty())
                        .orElse("")
        );
    }

    public static CompletableFuture<Component> getSuffixAsync(UUID uuid) {
        return getLegacySuffixAsync(uuid)
                .thenApply(suffix -> suffix.isEmpty() ? Component.empty() : TextFormatter.color(suffix));
    }

    public static CompletableFuture<String> getPrimaryGroupAsync(UUID uuid) {
        return findUser(uuid).thenApply(optional ->
                optional.map(User::getPrimaryGroup).orElse("default")
        );
    }

    public static CompletableFuture<String> getLegacyGroupDisplayNameAsync(UUID uuid) {
        return getPrimaryGroupAsync(uuid).thenApply(LuckPermsService::getLegacyGroupDisplayName);
    }

    //</editor-fold>

    //<editor-fold desc="User Meta Sync Cached Only">

    /**
     * Sync without blocking: returns only from LP cache.
     */
    public static String getLegacyPrefix(UUID uuid) {
        return getCachedUser(uuid)
                .map(user -> user.getCachedData().getMetaData().getPrefix())
                .filter(prefix -> prefix != null && !prefix.isEmpty())
                .orElse("");
    }

    public static Component getPrefix(UUID uuid) {
        String prefix = getLegacyPrefix(uuid);
        return prefix.isEmpty() ? Component.empty() : TextFormatter.color(prefix);
    }

    /**
     * Sync without blocking: returns only from LP cache.
     */
    public static String getLegacySuffix(UUID uuid) {
        return getCachedUser(uuid)
                .map(user -> user.getCachedData().getMetaData().getSuffix())
                .filter(suffix -> suffix != null && !suffix.isEmpty())
                .orElse("");
    }

    public static Component getSuffix(UUID uuid) {
        String suffix = getLegacySuffix(uuid);
        return suffix.isEmpty() ? Component.empty() : TextFormatter.color(suffix);
    }

    /**
     * Sync without blocking: returns only from LP cache.
     */
    public static String getPrimaryGroup(UUID uuid) {
        return getCachedUser(uuid)
                .map(User::getPrimaryGroup)
                .orElse("default");
    }

    public static String getLegacyGroupDisplayName(UUID uuid) {
        return getLegacyGroupDisplayName(getPrimaryGroup(uuid));
    }

    //</editor-fold>

    //<editor-fold desc="Permissions">

    public static boolean hasPermission(Group group, String permission) {
        if (group == null || permission == null || permission.isBlank()) {
            return false;
        }

        return group.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    /**
     * Sync without blocking: cached user only.
     */
    public static boolean hasPermission(UUID uuid, String permission) {
        if (permission == null || permission.isBlank()) {
            return false;
        }

        return getCachedUser(uuid)
                .map(user -> user.getCachedData().getPermissionData().checkPermission(permission).asBoolean())
                .orElse(false);
    }

    public static CompletableFuture<Boolean> hasPermissionAsync(UUID uuid, String permission) {
        if (permission == null || permission.isBlank()) {
            return CompletableFuture.completedFuture(false);
        }

        return findUser(uuid).thenApply(optional ->
                optional.map(user -> user.getCachedData().getPermissionData().checkPermission(permission).asBoolean())
                        .orElse(false)
        );
    }

    //</editor-fold>

    //<editor-fold desc="Groups Async">

    public static CompletableFuture<Boolean> isUserInheritAsync(UUID uuid, String group) {
        if (group == null || group.isBlank() || !isHooked()) {
            return CompletableFuture.completedFuture(false);
        }

        return findUser(uuid).thenApply(optional -> optional
                .map(user -> {
                    Collection<Group> groups = user.getInheritedGroups(user.getQueryOptions());
                    return groups.stream().anyMatch(g -> g.getName().equalsIgnoreCase(group));
                })
                .orElse(false));
    }

    /**
     * Sync cached-only fallback.
     */
    public static boolean isUserInherit(UUID uuid, String group) {
        if (group == null || group.isBlank()) {
            return false;
        }

        return getCachedUser(uuid)
                .map(user -> {
                    Collection<Group> groups = user.getInheritedGroups(user.getQueryOptions());
                    return groups.stream().anyMatch(g -> g.getName().equalsIgnoreCase(group));
                })
                .orElse(false);
    }

    public static CompletableFuture<Boolean> addGroup(UUID uuid, String group) {
        if (group == null || group.isBlank() || !isHooked()) {
            return CompletableFuture.completedFuture(false);
        }

        return findUser(uuid).thenCompose(optional -> {
            User user = optional.orElse(null);
            Group g = getGroup(group);

            if (user == null || g == null) {
                return CompletableFuture.completedFuture(false);
            }

            user.data().add(InheritanceNode.builder(g).build());
            return requireApi().getUserManager().saveUser(user).thenApply(ignored -> true);
        }).exceptionally(throwable -> false);
    }

    public static CompletableFuture<Boolean> addGroupTemporary(UUID uuid, String group, Duration duration) {
        if (group == null || group.isBlank() || duration == null || !isHooked()) {
            return CompletableFuture.completedFuture(false);
        }

        return findUser(uuid).thenCompose(optional -> {
            User user = optional.orElse(null);
            if (user == null) {
                return CompletableFuture.completedFuture(false);
            }

            user.data().add(InheritanceNode.builder(group).expiry(duration).build());
            return requireApi().getUserManager().saveUser(user).thenApply(ignored -> true);
        }).exceptionally(throwable -> false);
    }

    public static CompletableFuture<Boolean> removeGroup(UUID uuid, String group) {
        if (group == null || group.isBlank() || !isHooked()) {
            return CompletableFuture.completedFuture(false);
        }

        return findUser(uuid).thenCompose(optional -> {
            User user = optional.orElse(null);
            if (user == null) {
                return CompletableFuture.completedFuture(false);
            }

            user.data().remove(InheritanceNode.builder(group).build());
            return requireApi().getUserManager().saveUser(user).thenApply(ignored -> true);
        }).exceptionally(throwable -> false);
    }

    public static CompletableFuture<Boolean> clearGroups(UUID uuid) {
        if (!isHooked()) {
            return CompletableFuture.completedFuture(false);
        }

        return findUser(uuid).thenCompose(optional -> {
            User user = optional.orElse(null);
            if (user == null) {
                return CompletableFuture.completedFuture(false);
            }

            user.data().clear(node -> node instanceof InheritanceNode);
            return requireApi().getUserManager().saveUser(user).thenApply(ignored -> true);
        }).exceptionally(throwable -> false);
    }

    //</editor-fold>

    //<editor-fold desc="Prefix Management Async">

    public static CompletableFuture<Boolean> setPrefix(UUID uuid, String prefix, int priority) {
        if (prefix == null || prefix.isBlank() || !isHooked()) {
            return CompletableFuture.completedFuture(false);
        }

        return removePrefixes(uuid, priority)
                .thenCompose(success -> addPrefix(uuid, prefix, priority));
    }

    public static CompletableFuture<Boolean> addPrefix(UUID uuid, String prefix, int priority) {
        if (prefix == null || prefix.isBlank() || !isHooked()) {
            return CompletableFuture.completedFuture(false);
        }

        return findUser(uuid).thenCompose(optional -> {
            User user = optional.orElse(null);
            if (user == null) {
                return CompletableFuture.completedFuture(false);
            }

            user.data().add(PrefixNode.builder(prefix, priority).build());
            return requireApi().getUserManager().saveUser(user).thenApply(ignored -> true);
        }).exceptionally(throwable -> false);
    }

    public static CompletableFuture<Boolean> removePrefixes(UUID uuid, String prefix, int priority) {
        if (prefix == null || prefix.isBlank() || !isHooked()) {
            return CompletableFuture.completedFuture(false);
        }

        return findUser(uuid).thenCompose(optional -> {
            User user = optional.orElse(null);
            if (user == null) {
                return CompletableFuture.completedFuture(false);
            }

            user.data().remove(PrefixNode.builder(prefix, priority).build());
            return requireApi().getUserManager().saveUser(user).thenApply(ignored -> true);
        }).exceptionally(throwable -> false);
    }

    public static CompletableFuture<Boolean> removePrefixes(UUID uuid, int priority) {
        if (!isHooked()) {
            return CompletableFuture.completedFuture(false);
        }

        return findUser(uuid).thenCompose(optional -> {
            User user = optional.orElse(null);
            if (user == null) {
                return CompletableFuture.completedFuture(false);
            }

            for (Node node : user.data().toCollection()) {
                if (node instanceof PrefixNode prefixNode && prefixNode.getPriority() == priority) {
                    user.data().remove(node);
                }
            }

            return requireApi().getUserManager().saveUser(user).thenApply(ignored -> true);
        }).exceptionally(throwable -> false);
    }

    //</editor-fold>
}