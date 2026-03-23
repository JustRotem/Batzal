package net.justrotem.data.cache;

import net.justrotem.data.model.PlayerData;
import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache for {@link PlayerData} with TTL-based expiration and auxiliary indexes.
 *
 * <p>This cache is designed to:
 * <ul>
 *     <li>Provide fast access to player data by UUID</li>
 *     <li>Support name-based lookup via a normalized index</li>
 *     <li>Cache missing players to avoid repeated lookups</li>
 *     <li>Store additional ephemeral data such as last sent messages</li>
 * </ul>
 *
 * <p>Entries expire after a fixed time-to-live (TTL) since last access.</p>
 *
 * <p>Thread-safe: uses {@link ConcurrentHashMap} for all internal storage.</p>
 *
 * <p>This cache is not persistent and will be cleared on server restart.</p>
 */
public final class PlayerCache {

    /**
     * Time-to-live for each cache entry.
     */
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);

    /**
     * TTL in milliseconds, used for fast expiration checks.
     */
    private static final long CACHE_TTL_MILLIS = CACHE_TTL.toMillis();

    /**
     * Main cache storage.
     *
     * <p>UUID -> CacheEntry</p>
     */
    private static final Map<UUID, CacheEntry> CACHE = new ConcurrentHashMap<>();

    /**
     * Case-insensitive player name index.
     *
     * <p>Normalized name -> UUID</p>
     */
    private static final Map<String, UUID> NAME_INDEX = new ConcurrentHashMap<>();

    /**
     * Stores the last message associated with a player.
     *
     * <p>Useful for features such as /reply.</p>
     */
    private static final Map<UUID, Component> LAST_MESSAGES = new ConcurrentHashMap<>();

    private PlayerCache() {
    }

    /**
     * Represents a cached entry together with its last access time.
     *
     * @param data       cached player data, or empty if the player is cached as missing
     * @param lastAccess last access timestamp in milliseconds
     */
    public record CacheEntry(Optional<PlayerData> data, long lastAccess) {

        /**
         * Creates a new entry with the same data and a refreshed access timestamp.
         *
         * @return refreshed cache entry
         */
        public CacheEntry touch() {
            return new CacheEntry(this.data, System.currentTimeMillis());
        }

        /**
         * Checks whether this entry has expired.
         *
         * @param now current time in milliseconds
         * @return true if expired, otherwise false
         */
        public boolean isExpired(long now) {
            return now - this.lastAccess > CACHE_TTL_MILLIS;
        }
    }

    /**
     * Retrieves cached player data by UUID.
     *
     * <p>If the entry exists but has expired, it is removed and treated as missing.</p>
     *
     * @param uuid the player UUID
     * @return cached player data if present and still valid, otherwise empty
     */
    public static Optional<PlayerData> get(UUID uuid) {
        if (uuid == null) {
            return Optional.empty();
        }

        CacheEntry entry = CACHE.get(uuid);
        if (entry == null) {
            return Optional.empty();
        }

        if (entry.isExpired(System.currentTimeMillis())) {
            remove(uuid);
            return Optional.empty();
        }

        CACHE.put(uuid, entry.touch());
        return entry.data();
    }

    /**
     * Inserts or updates player data in the cache.
     *
     * <p>If a cached entry already exists and contains player data, the existing
     * object is updated using {@code copyFrom(...)} in order to preserve object references.</p>
     *
     * @param playerData the player data to cache
     */
    public static void put(PlayerData playerData) {
        if (playerData == null) {
            return;
        }

        UUID uuid = playerData.getUniqueId();
        if (uuid == null) {
            return;
        }

        long now = System.currentTimeMillis();

        CACHE.compute(uuid, (ignored, existing) -> {
            if (existing != null && existing.data().isPresent()) {
                PlayerData cached = existing.data().get();
                cached.copyFrom(playerData);
                indexName(cached);
                return new CacheEntry(Optional.of(cached), now);
            }

            indexName(playerData);
            return new CacheEntry(Optional.of(playerData), now);
        });
    }

    /**
     * Stores a missing-entry marker for a UUID.
     *
     * <p>This is useful when a lookup was already attempted and no player data exists,
     * preventing repeated expensive lookups for the same UUID.</p>
     *
     * @param uuid the player UUID
     */
    public static void putMissing(UUID uuid) {
        if (uuid == null) {
            return;
        }

        CACHE.put(uuid, new CacheEntry(Optional.empty(), System.currentTimeMillis()));
    }

    /**
     * Checks whether valid player data exists in the cache for the given UUID.
     *
     * @param uuid the player UUID
     * @return true if cached player data exists and is not expired
     */
    public static boolean contains(UUID uuid) {
        return get(uuid).isPresent();
    }

    /**
     * Removes all cached data related to a player.
     *
     * <p>This includes the main cache entry, name index entries, and last message entry.</p>
     *
     * @param uuid the player UUID
     */
    public static void remove(UUID uuid) {
        if (uuid == null) {
            return;
        }

        CACHE.remove(uuid);
        LAST_MESSAGES.remove(uuid);
        removeNameIndex(uuid);
    }

    /**
     * Clears the entire cache and all auxiliary indexes.
     *
     * <p>Use with caution.</p>
     */
    public static void clear() {
        CACHE.clear();
        NAME_INDEX.clear();
        LAST_MESSAGES.clear();
    }

    /**
     * Returns the number of cached entries that currently contain actual player data.
     *
     * @return number of cached PlayerData entries
     */
    public static int size() {
        return (int) CACHE.values().stream()
                .map(CacheEntry::data)
                .flatMap(Optional::stream)
                .count();
    }

    /**
     * Returns all cached player data values.
     *
     * @return immutable snapshot list of all currently cached player data
     */
    public static List<PlayerData> values() {
        return CACHE.values().stream()
                .map(CacheEntry::data)
                .flatMap(Optional::stream)
                .toList();
    }

    /**
     * Removes all expired entries from the cache.
     *
     * <p>This method is a good candidate for periodic cleanup via scheduler.</p>
     */
    public static void evictExpired() {
        long now = System.currentTimeMillis();
        CACHE.forEach((uuid, entry) -> {
            if (entry.isExpired(now)) {
                remove(uuid);
            }
        });
    }

    /**
     * Looks up a player's UUID by name.
     *
     * <p>Name matching is case-insensitive and uses the normalized internal name index.</p>
     *
     * @param name the player name
     * @return optional UUID if present in the index, otherwise empty
     */
    public static Optional<UUID> getUniqueId(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(NAME_INDEX.get(normalizeName(name)));
    }

    /**
     * @deprecated Use {@link #getUniqueId(String)} instead, which returns an {@link Optional}
     * for safer null handling.
     *
     * <p>This method is kept for backward compatibility and may be removed in a future version.</p>
     */
    @Deprecated
    public static UUID getUniqueIdOrNull(String name) {
        return getUniqueId(name).orElse(null);
    }

    /**
     * Updates the name index for the given player.
     *
     * <p>If the player has no valid name, this method does nothing.</p>
     *
     * @param playerData the player data to index
     */
    public static void indexName(PlayerData playerData) {
        if (playerData == null) {
            return;
        }

        UUID uuid = playerData.getUniqueId();
        String name = playerData.getName();

        if (uuid == null || name == null || name.isBlank()) {
            return;
        }

        NAME_INDEX.put(normalizeName(name), uuid);
    }

    /**
     * Retrieves the last message associated with a player.
     *
     * @param uuid the player UUID
     * @return the cached message component, or null if none exists
     */
    public static Component getLastMessage(UUID uuid) {
        return uuid == null ? null : LAST_MESSAGES.get(uuid);
    }

    /**
     * Stores the last message associated with a player.
     *
     * @param uuid      the player UUID
     * @param component the message component
     */
    public static void setLastMessage(UUID uuid, Component component) {
        if (uuid == null || component == null) {
            return;
        }

        LAST_MESSAGES.put(uuid, component);
    }

    /**
     * Removes the last message associated with a player.
     *
     * @param uuid the player UUID
     */
    public static void removeLastMessage(UUID uuid) {
        if (uuid != null) {
            LAST_MESSAGES.remove(uuid);
        }
    }

    /**
     * Removes all name index entries that currently point to the given UUID.
     *
     * @param uuid the player UUID
     */
    private static void removeNameIndex(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        NAME_INDEX.entrySet().removeIf(entry -> entry.getValue().equals(uuid));
    }

    /**
     * Normalizes a player name for case-insensitive indexing.
     *
     * @param name raw player name
     * @return normalized lowercase name
     */
    private static String normalizeName(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}