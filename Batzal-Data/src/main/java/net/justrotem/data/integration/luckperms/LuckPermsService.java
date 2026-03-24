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

/**
 * Wrapper service for interacting with the LuckPerms API.
 *
 * <p>This class provides:
 * <ul>
 *     <li>Cached and async user lookups</li>
 *     <li>Group and meta retrieval</li>
 *     <li>Permission checks</li>
 *     <li>Group and prefix management</li>
 * </ul>
 * </p>
 *
 * <p>Important:
 * <ul>
 *     <li>Sync methods are cache-only and do not block</li>
 *     <li>Async methods may load data from storage</li>
 * </ul>
 * </p>
 */
public final class LuckPermsService {

    private static LuckPerms api;

    private LuckPermsService() {
    }

    /**
     * Initializes the LuckPerms API reference.
     *
     * @param lpAPI the LuckPerms API instance
     */
    public static void initializeAPI(LuckPerms lpAPI) {
        api = Objects.requireNonNull(lpAPI, "LuckPerms API cannot be null");
    }

    /**
     * Checks whether the LuckPerms API has been initialized.
     *
     * @return true if hooked, otherwise false
     */
    public static boolean isHooked() {
        return api != null;
    }

    /**
     * Returns the initialized LuckPerms API instance.
     *
     * @return LuckPerms API
     * @throws IllegalStateException if the API has not been initialized
     */
    private static LuckPerms requireApi() {
        if (api == null) {
            throw new IllegalStateException("LuckPerms API is not initialized");
        }
        return api;
    }

    //<editor-fold desc="User">

    /**
     * Retrieves a cached LuckPerms user without loading from storage.
     *
     * @param uuid player UUID
     * @return optional cached user
     */
    public static Optional<User> getCachedUser(UUID uuid) {
        if (uuid == null || !isHooked()) {
            return Optional.empty();
        }

        return Optional.ofNullable(api.getUserManager().getUser(uuid));
    }

    /**
     * Retrieves a user, loading from storage if necessary.
     *
     * @param uuid player UUID
     * @return future containing user if found
     */
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
     *
     * <p>Returns only a currently loaded cached user and does not block.</p>
     *
     * @param uuid player UUID
     * @return cached user, or null if unavailable
     */
    public static User getUser(UUID uuid) {
        return getCachedUser(uuid).orElse(null);
    }

    //</editor-fold>

    //<editor-fold desc="Group">

    /**
     * Retrieves a group by name.
     *
     * <p>If the requested group is missing, this method falls back to the {@code default} group.</p>
     *
     * @param name group name
     * @return matching group, default group, or null if unavailable
     */
    public static Group getGroup(String name) {
        if (!isHooked()) return null;

        Group group = api.getGroupManager().getGroup(name);
        if (group != null) return group;

        return api.getGroupManager().getGroup("default");
    }

    /**
     * Retrieves the weight of a group.
     *
     * @param name group name
     * @return group weight, or 0 if unavailable
     */
    public static int getGroupWeight(String name) {
        Group group = getGroup(name);
        return group != null ? group.getWeight().orElse(0) : 0;
    }

    //</editor-fold>

    //<editor-fold desc="Group Meta">

    /**
     * Retrieves a group's prefix as a legacy string.
     *
     * @param name group name
     * @return legacy prefix, or empty string if none exists
     */
    public static String getLegacyGroupPrefix(String name) {
        return Optional.ofNullable(getGroup(name))
                .map(group -> group.getCachedData().getMetaData().getPrefix())
                .filter(prefix -> prefix != null && !prefix.isEmpty())
                .orElse("");
    }

    /**
     * Retrieves a group's prefix as a formatted component.
     *
     * @param name group name
     * @return formatted prefix component, or empty component if none exists
     */
    public static Component getGroupPrefix(String name) {
        String prefix = getLegacyGroupPrefix(name);
        return prefix.isEmpty() ? Component.empty() : TextFormatter.color(prefix);
    }

    /**
     * Retrieves a group's suffix as a legacy string.
     *
     * @param name group name
     * @return legacy suffix, or empty string if none exists
     */
    public static String getLegacyGroupSuffix(String name) {
        return Optional.ofNullable(getGroup(name))
                .map(group -> group.getCachedData().getMetaData().getSuffix())
                .filter(suffix -> suffix != null && !suffix.isEmpty())
                .orElse("");
    }

    /**
     * Retrieves a group's suffix as a formatted component.
     *
     * @param name group name
     * @return formatted suffix component, or empty component if none exists
     */
    public static Component getGroupSuffix(String name) {
        String suffix = getLegacyGroupSuffix(name);
        return suffix.isEmpty() ? Component.empty() : TextFormatter.color(suffix);
    }

    /**
     * Retrieves a group's display name as a legacy string.
     *
     * <p>If no custom display name exists, the raw group name is returned.</p>
     *
     * @param group group name
     * @return display name, or empty string if unavailable
     */
    public static String getLegacyGroupDisplayName(String group) {
        Group g = getGroup(group);
        if (g == null) return "";

        String display = g.getDisplayName();
        return display != null ? display : g.getName();
    }

    /**
     * Retrieves a group's display name as a formatted component.
     *
     * @param group group name
     * @return formatted display name component, or empty component if unavailable
     */
    public static Component getGroupDisplayName(String group) {
        String name = getLegacyGroupDisplayName(group);
        return name.isEmpty() ? Component.empty() : TextFormatter.color(name);
    }

    //</editor-fold>

    //<editor-fold desc="User Meta Async">

    /**
     * Retrieves a user's prefix asynchronously as a legacy string.
     *
     * @param uuid player UUID
     * @return future containing prefix, or empty string if unavailable
     */
    public static CompletableFuture<String> getLegacyPrefixAsync(UUID uuid) {
        return findUser(uuid).thenApply(optional ->
                optional.map(user -> user.getCachedData().getMetaData().getPrefix())
                        .filter(prefix -> prefix != null && !prefix.isEmpty())
                        .orElse("")
        );
    }

    /**
     * Retrieves a user's prefix asynchronously as a formatted component.
     *
     * @param uuid player UUID
     * @return future containing formatted prefix component
     */
    public static CompletableFuture<Component> getPrefixAsync(UUID uuid) {
        return getLegacyPrefixAsync(uuid)
                .thenApply(prefix -> prefix.isEmpty() ? Component.empty() : TextFormatter.color(prefix));
    }

    /**
     * Retrieves a user's suffix asynchronously as a legacy string.
     *
     * @param uuid player UUID
     * @return future containing suffix, or empty string if unavailable
     */
    public static CompletableFuture<String> getLegacySuffixAsync(UUID uuid) {
        return findUser(uuid).thenApply(optional ->
                optional.map(user -> user.getCachedData().getMetaData().getSuffix())
                        .filter(suffix -> suffix != null && !suffix.isEmpty())
                        .orElse("")
        );
    }

    /**
     * Retrieves a user's suffix asynchronously as a formatted component.
     *
     * @param uuid player UUID
     * @return future containing formatted suffix component
     */
    public static CompletableFuture<Component> getSuffixAsync(UUID uuid) {
        return getLegacySuffixAsync(uuid)
                .thenApply(suffix -> suffix.isEmpty() ? Component.empty() : TextFormatter.color(suffix));
    }

    /**
     * Retrieves a user's primary group asynchronously.
     *
     * @param uuid player UUID
     * @return future containing primary group, or {@code default} if unavailable
     */
    public static CompletableFuture<String> getPrimaryGroupAsync(UUID uuid) {
        return findUser(uuid).thenApply(optional ->
                optional.map(User::getPrimaryGroup).orElse("default")
        );
    }

    /**
     * Retrieves the display name of a user's primary group asynchronously.
     *
     * @param uuid player UUID
     * @return future containing group display name as a legacy string
     */
    public static CompletableFuture<String> getLegacyGroupDisplayNameAsync(UUID uuid) {
        return getPrimaryGroupAsync(uuid).thenApply(LuckPermsService::getLegacyGroupDisplayName);
    }

    //</editor-fold>

    //<editor-fold desc="User Meta Sync Cached Only">

    /**
     * Retrieves a user's prefix from the LuckPerms cache only.
     *
     * <p>This method does not block and does not load from storage.</p>
     *
     * @param uuid player UUID
     * @return cached prefix, or empty string if unavailable
     */
    public static String getLegacyPrefix(UUID uuid) {
        return getCachedUser(uuid)
                .map(user -> user.getCachedData().getMetaData().getPrefix())
                .filter(prefix -> prefix != null && !prefix.isEmpty())
                .orElse("");
    }

    /**
     * Retrieves a user's prefix from cache as a formatted component.
     *
     * @param uuid player UUID
     * @return formatted prefix component, or empty component if unavailable
     */
    public static Component getPrefix(UUID uuid) {
        String prefix = getLegacyPrefix(uuid);
        return prefix.isEmpty() ? Component.empty() : TextFormatter.color(prefix);
    }

    /**
     * Retrieves a user's suffix from the LuckPerms cache only.
     *
     * <p>This method does not block and does not load from storage.</p>
     *
     * @param uuid player UUID
     * @return cached suffix, or empty string if unavailable
     */
    public static String getLegacySuffix(UUID uuid) {
        return getCachedUser(uuid)
                .map(user -> user.getCachedData().getMetaData().getSuffix())
                .filter(suffix -> suffix != null && !suffix.isEmpty())
                .orElse("");
    }

    /**
     * Retrieves a user's suffix from cache as a formatted component.
     *
     * @param uuid player UUID
     * @return formatted suffix component, or empty component if unavailable
     */
    public static Component getSuffix(UUID uuid) {
        String suffix = getLegacySuffix(uuid);
        return suffix.isEmpty() ? Component.empty() : TextFormatter.color(suffix);
    }

    /**
     * Retrieves a user's primary group from the LuckPerms cache only.
     *
     * @param uuid player UUID
     * @return primary group, or {@code default} if unavailable
     */
    public static String getPrimaryGroup(UUID uuid) {
        return getCachedUser(uuid)
                .map(User::getPrimaryGroup)
                .orElse("default");
    }

    /**
     * Retrieves the display name of a user's cached primary group.
     *
     * @param uuid player UUID
     * @return group display name as a legacy string
     */
    public static String getLegacyGroupDisplayName(UUID uuid) {
        return getLegacyGroupDisplayName(getPrimaryGroup(uuid));
    }

    //</editor-fold>

    //<editor-fold desc="Permissions">

    /**
     * Checks whether a group has a permission.
     *
     * @param group group instance
     * @param permission permission node
     * @return true if granted, otherwise false
     */
    public static boolean hasPermission(Group group, String permission) {
        if (group == null || permission == null || permission.isBlank()) {
            return false;
        }

        return group.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    /**
     * Checks whether a cached user has a permission.
     *
     * <p>This method does not block and only checks already loaded user data.</p>
     *
     * @param uuid player UUID
     * @param permission permission node
     * @return true if granted, otherwise false
     */
    public static boolean hasPermission(UUID uuid, String permission) {
        if (permission == null || permission.isBlank()) {
            return false;
        }

        return getCachedUser(uuid)
                .map(user -> user.getCachedData().getPermissionData().checkPermission(permission).asBoolean())
                .orElse(false);
    }

    /**
     * Checks a permission asynchronously.
     *
     * @param uuid player UUID
     * @param permission permission node
     * @return future containing result
     */
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

    /**
     * Checks asynchronously whether a user inherits a specific group.
     *
     * @param uuid player UUID
     * @param group group name
     * @return future containing true if the user inherits the group
     */
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
     * Checks from cache whether a user inherits a specific group.
     *
     * <p>This method is non-blocking and cache-only.</p>
     *
     * @param uuid player UUID
     * @param group group name
     * @return true if the user inherits the group
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

    /**
     * Adds a user to a group.
     *
     * @param uuid player UUID
     * @param group group name
     * @return future indicating success
     */
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

    /**
     * Adds a user to a temporary group for a fixed duration.
     *
     * @param uuid player UUID
     * @param group group name
     * @param duration duration of the temporary membership
     * @return future indicating success
     */
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

    /**
     * Removes a user from a group.
     *
     * @param uuid player UUID
     * @param group group name
     * @return future indicating success
     */
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

    /**
     * Clears all inheritance groups from a user.
     *
     * @param uuid player UUID
     * @return future indicating success
     */
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

    /**
     * Replaces all prefixes at the given priority with a new prefix.
     *
     * @param uuid player UUID
     * @param prefix new prefix
     * @param priority prefix priority
     * @return future indicating success
     */
    public static CompletableFuture<Boolean> setPrefix(UUID uuid, String prefix, int priority) {
        if (prefix == null || prefix.isBlank() || !isHooked()) {
            return CompletableFuture.completedFuture(false);
        }

        return removePrefixes(uuid, priority)
                .thenCompose(success -> addPrefix(uuid, prefix, priority));
    }

    /**
     * Adds a prefix node to a user.
     *
     * @param uuid player UUID
     * @param prefix prefix text
     * @param priority prefix priority
     * @return future indicating success
     */
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

    /**
     * Removes a specific prefix node from a user.
     *
     * @param uuid player UUID
     * @param prefix prefix text
     * @param priority prefix priority
     * @return future indicating success
     */
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

    /**
     * Removes all prefix nodes from a user at the given priority.
     *
     * @param uuid player UUID
     * @param priority prefix priority
     * @return future indicating success
     */
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