package net.justrotem.data.cache;

import net.justrotem.data.integration.luckperms.LuckPermsService;
import net.justrotem.data.model.CooldownType;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    // category enum class → (player UUID → expiry time)
    private static final Map<Class<? extends Enum<?>>, Map<Enum<?>, Map<UUID, Instant>>> cooldowns = new HashMap<>();

    /**
     * Start a cooldown for a player in a given enum category.
     *
     * @param uuid   The unique id of the player.
     * @param category Any enum constant representing the category.
     * @param duration Duration of the cooldown.
     */
    public static <E extends Enum<E>> void startCooldown(UUID uuid, E category, Duration duration) {
        @SuppressWarnings("unchecked")
        Map<Enum<?>, Map<UUID, Instant>> map = cooldowns.computeIfAbsent(
                category.getDeclaringClass(),
                cls -> (Map<Enum<?>, Map<UUID, Instant>>) new EnumMap(cls)
        );
        Map<UUID, Instant> playerMap = map.computeIfAbsent(category, c -> new ConcurrentHashMap<>());

        playerMap.put(uuid, Instant.now().plus(duration));
    }

    /**
     * Check if a player's cooldown has expired.
     */
    public static <E extends Enum<E> & CooldownType> boolean isReady(UUID uuid, E category) {
        cleanupExpired(category);

        if (hasBypass(uuid, category)) return true;

        Map<Enum<?>, Map<UUID, Instant>> map = cooldowns.get(category.getDeclaringClass());
        if (map == null) return true;

        Map<UUID, Instant> playerMap = map.get(category);
        if (playerMap == null) return true;

        Instant expiresAt = playerMap.get(uuid);
        return expiresAt == null || Instant.now().isAfter(expiresAt);
    }

    private static <E extends Enum<E> & CooldownType> boolean hasBypass(UUID uuid, E category) {
        String perm = category.getPermission();
        return perm != null && !perm.isEmpty() && LuckPermsService.hasPermission(uuid, perm);
    }

    /**
     * Get remaining cooldown time in seconds.
     */
    public static <E extends Enum<E>> long getRemaining(UUID uuid, E category) {
        cleanupExpired(category);

        Map<Enum<?>, Map<UUID, Instant>> map = cooldowns.get(category.getDeclaringClass());
        if (map == null) return 0;

        Map<UUID, Instant> playerMap = map.get(category);
        if (playerMap == null) return 0;

        Instant expiresAt = playerMap.get(uuid);
        if (expiresAt == null) return 0;

        long remaining = Duration.between(Instant.now(), expiresAt).getSeconds();
        return Math.max(remaining, 0);
    }

    /**
     * Cleanup expired cooldowns for a given category.
     */
    private static <E extends Enum<E>> void cleanupExpired(E category) {
        Map<Enum<?>, Map<UUID, Instant>> map = cooldowns.get(category.getDeclaringClass());
        if (map == null) return;

        Map<UUID, Instant> playerMap = map.get(category);
        if (playerMap == null) return;

        Instant now = Instant.now();
        playerMap.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
    }
}
