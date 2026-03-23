package net.justrotem.data.cache;

import net.justrotem.data.integration.luckperms.LuckPermsService;
import net.justrotem.data.model.CooldownType;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-player cooldowns for enum-based categories.
 *
 * <p>This manager is generic and supports any enum category. Cooldowns are stored
 * by enum class, enum constant, and player UUID.</p>
 *
 * <p>Categories that implement {@link CooldownType} may also define a bypass
 * permission, allowing certain players to ignore the cooldown entirely.</p>
 *
 * <p>Data is stored in memory only and is not persisted across restarts.</p>
 *
 * <p>Thread-safe for concurrent access through the use of concurrent collections
 * on the outer storage and per-category player maps.</p>
 */
public final class CooldownManager {

    /**
     * Root cooldown storage.
     *
     * <p>Structure:
     * enum class -> (enum constant -> (player UUID -> expiry instant))</p>
     */
    private static final Map<Class<? extends Enum<?>>, Map<Enum<?>, Map<UUID, Instant>>> COOLDOWNS =
            new ConcurrentHashMap<>();

    private CooldownManager() {
    }

    /**
     * Starts or replaces a cooldown for a player in the given category.
     *
     * <p>If the player already has an active cooldown in this category, it will be
     * overwritten with a new expiry time based on the provided duration.</p>
     *
     * @param uuid     the player UUID
     * @param category the cooldown category
     * @param duration the cooldown duration; must be positive
     * @param <E>      the enum category type
     */
    public static <E extends Enum<E>> void startCooldown(UUID uuid, E category, Duration duration) {
        if (uuid == null || category == null || duration == null || duration.isNegative() || duration.isZero()) {
            return;
        }

        @SuppressWarnings("unchecked")
        Map<Enum<?>, Map<UUID, Instant>> map = COOLDOWNS.computeIfAbsent(
                category.getDeclaringClass(),
                cls -> (Map<Enum<?>, Map<UUID, Instant>>) new EnumMap<>(cls)
        );

        Map<UUID, Instant> playerMap = map.computeIfAbsent(category, ignored -> new ConcurrentHashMap<>());
        playerMap.put(uuid, Instant.now().plus(duration));
    }

    /**
     * Checks whether the player is currently allowed to use the given category.
     *
     * <p>This is the synchronous, cached-only variant. It is best used when the
     * relevant LuckPerms user data is already loaded and available locally.</p>
     *
     * <p>If the category defines a bypass permission and the player has it, this
     * method returns {@code true} regardless of the cooldown state.</p>
     *
     * @param uuid     the player UUID
     * @param category the cooldown category
     * @param <E>      the enum category type, which must also implement {@link CooldownType}
     * @return {@code true} if the player may use the category now, otherwise {@code false}
     */
    public static <E extends Enum<E> & CooldownType> boolean isReady(UUID uuid, E category) {
        if (uuid == null || category == null) {
            return true;
        }

        cleanupExpired(category);

        if (hasBypass(uuid, category)) {
            return true;
        }

        return isReadyInternal(uuid, category);
    }

    /**
     * Asynchronously checks whether the player is currently allowed to use the given category.
     *
     * <p>This is the preferred variant when permission accuracy matters and the
     * LuckPerms user may not already be loaded in cache.</p>
     *
     * <p>If the category does not define a bypass permission, the result is resolved
     * immediately using the current cooldown state only.</p>
     *
     * @param uuid     the player UUID
     * @param category the cooldown category
     * @param <E>      the enum category type, which must also implement {@link CooldownType}
     * @return a future containing {@code true} if the player may use the category now,
     *         otherwise {@code false}
     */
    public static <E extends Enum<E> & CooldownType> CompletableFuture<Boolean> isReadyAsync(UUID uuid, E category) {
        if (uuid == null || category == null) {
            return CompletableFuture.completedFuture(true);
        }

        cleanupExpired(category);

        String permission = category.getPermission();
        if (permission == null || permission.isBlank()) {
            return CompletableFuture.completedFuture(isReadyInternal(uuid, category));
        }

        return LuckPermsService.hasPermissionAsync(uuid, permission)
                .thenApply(hasBypass -> hasBypass || isReadyInternal(uuid, category));
    }

    /**
     * Returns the remaining cooldown time in whole seconds.
     *
     * <p>If no cooldown exists, or it has already expired, this method returns {@code 0}.</p>
     *
     * @param uuid     the player UUID
     * @param category the cooldown category
     * @param <E>      the enum category type
     * @return remaining cooldown in seconds, never negative
     */
    public static <E extends Enum<E>> long getRemaining(UUID uuid, E category) {
        if (uuid == null || category == null) {
            return 0;
        }

        cleanupExpired(category);

        Map<Enum<?>, Map<UUID, Instant>> map = COOLDOWNS.get(category.getDeclaringClass());
        if (map == null) {
            return 0;
        }

        Map<UUID, Instant> playerMap = map.get(category);
        if (playerMap == null) {
            return 0;
        }

        Instant expiresAt = playerMap.get(uuid);
        if (expiresAt == null) {
            return 0;
        }

        long remaining = Duration.between(Instant.now(), expiresAt).getSeconds();
        return Math.max(remaining, 0);
    }

    /**
     * Clears a player's cooldown for the given category.
     *
     * <p>If no cooldown exists, this method does nothing.</p>
     *
     * @param uuid     the player UUID
     * @param category the cooldown category
     * @param <E>      the enum category type
     */
    public static <E extends Enum<E>> void clearCooldown(UUID uuid, E category) {
        if (uuid == null || category == null) {
            return;
        }

        Map<Enum<?>, Map<UUID, Instant>> map = COOLDOWNS.get(category.getDeclaringClass());
        if (map == null) {
            return;
        }

        Map<UUID, Instant> playerMap = map.get(category);
        if (playerMap != null) {
            playerMap.remove(uuid);
        }
    }

    /**
     * Removes expired cooldown entries for the given category.
     *
     * <p>This method only affects the specific enum constant provided.</p>
     *
     * @param category the cooldown category to clean
     * @param <E>      the enum category type
     */
    public static <E extends Enum<E>> void cleanupExpired(E category) {
        if (category == null) {
            return;
        }

        Map<Enum<?>, Map<UUID, Instant>> map = COOLDOWNS.get(category.getDeclaringClass());
        if (map == null) {
            return;
        }

        Map<UUID, Instant> playerMap = map.get(category);
        if (playerMap == null) {
            return;
        }

        Instant now = Instant.now();
        playerMap.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
    }

    /**
     * Checks whether the player has cooldown bypass permission for the given category.
     *
     * <p>This method uses the synchronous LuckPerms access path and should therefore
     * only be used when cached permission data is expected to be available.</p>
     *
     * @param uuid     the player UUID
     * @param category the cooldown category
     * @param <E>      the enum category type, which must also implement {@link CooldownType}
     * @return {@code true} if the player bypasses the cooldown, otherwise {@code false}
     */
    private static <E extends Enum<E> & CooldownType> boolean hasBypass(UUID uuid, E category) {
        String permission = category.getPermission();
        return permission != null
                && !permission.isBlank()
                && LuckPermsService.hasPermission(uuid, permission);
    }

    /**
     * Internal cooldown readiness check without permission bypass lookup.
     *
     * @param uuid     the player UUID
     * @param category the cooldown category
     * @param <E>      the enum category type
     * @return {@code true} if the player has no active cooldown, otherwise {@code false}
     */
    private static <E extends Enum<E>> boolean isReadyInternal(UUID uuid, E category) {
        Map<Enum<?>, Map<UUID, Instant>> map = COOLDOWNS.get(category.getDeclaringClass());
        if (map == null) {
            return true;
        }

        Map<UUID, Instant> playerMap = map.get(category);
        if (playerMap == null) {
            return true;
        }

        Instant expiresAt = playerMap.get(uuid);
        return expiresAt == null || Instant.now().isAfter(expiresAt);
    }
}