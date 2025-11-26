package net.justrotem.data.utils;

import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    // category enum class → (player UUID → expiry time)
    private static final Map<Class<? extends Enum<?>>, Map<Enum<?>, Map<UUID, Instant>>> cooldowns = new HashMap<>();

    /**
     * Start a cooldown for a player in a given enum category.
     *
     * @param player   The player.
     * @param category Any enum constant representing the category.
     * @param duration Duration of the cooldown.
     */
    public static <E extends Enum<E>> void startCooldown(Player player, E category, Duration duration) {
        @SuppressWarnings("unchecked")
        Map<Enum<?>, Map<UUID, Instant>> map = cooldowns.computeIfAbsent(
                category.getDeclaringClass(),
                cls -> (Map<Enum<?>, Map<UUID, Instant>>) new EnumMap(cls)
        );
        Map<UUID, Instant> playerMap = map.computeIfAbsent(category, c -> new ConcurrentHashMap<>());

        playerMap.put(player.getUniqueId(), Instant.now().plus(duration));
    }

    /**
     * Check if a player's cooldown has expired.
     */
    public static <E extends Enum<E> & CooldownType> boolean isReady(Player player, E category) {
        cleanupExpired(category);

        String perm = category.getPermission();
        if (perm != null && !perm.isEmpty() && player.hasPermission(perm)) return true;

        Map<Enum<?>, Map<UUID, Instant>> map = cooldowns.get(category.getDeclaringClass());
        if (map == null) return true;

        Map<UUID, Instant> playerMap = map.get(category);
        if (playerMap == null) return true;

        Instant expiresAt = playerMap.get(player.getUniqueId());
        return expiresAt == null || Instant.now().isAfter(expiresAt);
    }

    /**
     * Get remaining cooldown time in seconds.
     */
    public static <E extends Enum<E>> long getRemaining(Player player, E category) {
        cleanupExpired(category);

        Map<Enum<?>, Map<UUID, Instant>> map = cooldowns.get(category.getDeclaringClass());
        if (map == null) return 0;

        Map<UUID, Instant> playerMap = map.get(category);
        if (playerMap == null) return 0;

        Instant expiresAt = playerMap.get(player.getUniqueId());
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
