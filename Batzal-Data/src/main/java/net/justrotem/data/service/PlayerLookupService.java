package net.justrotem.data.service;

import net.justrotem.data.cache.PlayerCache;
import net.justrotem.data.model.PlayerData;
import net.justrotem.data.storage.mysql.MySQL;
import net.justrotem.data.storage.mysql.PlayerDataManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Service layer for retrieving and managing {@link PlayerData}.
 *
 * <p>This class acts as a bridge between the cache ({@link PlayerCache})
 * and the persistent storage ({@link PlayerDataManager}).</p>
 *
 * <p>It provides both asynchronous and synchronous APIs:
 * <ul>
 *     <li>Async methods should be preferred when working off the main thread</li>
 *     <li>Sync methods internally block using {@code join()} and should be used carefully</li>
 * </ul>
 * </p>
 *
 * <p>All methods are static and thread-safe under normal usage assumptions.</p>
 */
public final class PlayerLookupService {

    private PlayerLookupService() {
    }

    /**
     * Underlying storage access.
     */
    private static final PlayerDataManager SQL = MySQL.getPlayerData();

    /**
     * Retrieves player data asynchronously.
     *
     * <p>This method first checks the cache. If not present, it fetches the data
     * from the database and updates the cache accordingly.</p>
     *
     * <p>If the player does not exist, a "missing" entry is cached to avoid repeated lookups.</p>
     *
     * @param uuid the player UUID
     * @return a future containing the player data if found, otherwise empty
     */
    public static CompletableFuture<Optional<PlayerData>> getAsync(UUID uuid) {
        if (uuid == null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        Optional<PlayerData> cached = PlayerCache.get(uuid);
        if (cached.isPresent()) {
            return CompletableFuture.completedFuture(cached);
        }

        return SQL.getData(uuid)
                .thenApply(optional -> {
                    if (optional.isPresent()) {
                        PlayerCache.put(optional.get());
                    } else {
                        PlayerCache.putMissing(uuid);
                    }
                    return optional;
                })
                .exceptionally(throwable -> Optional.empty());
    }

    /**
     * Retrieves player data synchronously.
     *
     * <p>This method blocks using {@link CompletableFuture#join()} and should NOT
     * be used in performance-sensitive or async contexts.</p>
     *
     * <p>If an exception occurs during retrieval, an empty result is returned.</p>
     *
     * @param uuid the player UUID
     * @return optional player data if found, otherwise empty
     */
    public static Optional<PlayerData> get(UUID uuid) {
        try {
            return getAsync(uuid).join();
        } catch (CancellationException | CompletionException e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves all player data asynchronously.
     *
     * <p>All retrieved entries are cached.</p>
     *
     * @return a future containing a list of all players
     */
    public static CompletableFuture<List<PlayerData>> getAllAsync() {
        return SQL.getAll()
                .thenApply(list -> {
                    list.forEach(PlayerCache::put);
                    return list;
                })
                .exceptionally(throwable -> List.of());
    }

    /**
     * Retrieves all player data synchronously.
     *
     * <p>This method blocks using {@link CompletableFuture#join()}.</p>
     *
     * @return list of all players (empty if an error occurs)
     */
    public static List<PlayerData> getAll() {
        try {
            return getAllAsync().join();
        } catch (CancellationException | CompletionException e) {
            return List.of();
        }
    }

    /**
     * Updates the cache with the given player data.
     *
     * <p>This does NOT persist the data to the database.</p>
     *
     * @param playerData the player data to update
     */
    public static void update(PlayerData playerData) {
        PlayerCache.put(playerData);
    }

    /**
     * Saves player data to the database and updates the cache.
     *
     * <p>The player will be marked as not dirty after saving.</p>
     *
     * @param playerData the player data to save
     */
    public static void save(PlayerData playerData) {
        if (playerData == null) {
            return;
        }

        SQL.update(playerData);
        playerData.setDirty(false);
        PlayerCache.put(playerData);
    }

    /**
     * Saves cached player data (if present) and removes it from the cache.
     *
     * @param uuid the player UUID
     */
    public static void saveAndRemove(UUID uuid) {
        if (uuid == null) {
            return;
        }

        Optional<PlayerData> cached = PlayerCache.get(uuid);
        cached.ifPresent(playerData -> {
            SQL.update(playerData);
            playerData.setDirty(false);
        });

        PlayerCache.remove(uuid);
    }

    /**
     * Saves all cached player data to the database.
     */
    public static void saveAll() {
        PlayerCache.values().forEach(playerData -> {
            SQL.update(playerData);
            playerData.setDirty(false);
        });
    }

    /**
     * Saves only players marked as dirty.
     */
    public static void saveDirty() {
        PlayerCache.values().stream()
                .filter(PlayerData::isDirty)
                .forEach(playerData -> {
                    SQL.update(playerData);
                    playerData.setDirty(false);
                });
    }

    /**
     * Saves dirty players and evicts expired cache entries.
     *
     * <p>This is useful for periodic maintenance tasks.</p>
     */
    public static void saveDirtyAndEvictExpired() {
        saveDirty();
        PlayerCache.evictExpired();
    }

    /**
     * Checks whether a player is registered (exists in storage).
     *
     * @param uuid the player UUID
     * @return true if the player exists, false otherwise
     */
    public static boolean isRegistered(UUID uuid) {
        return get(uuid).isPresent();
    }

    /**
     * Retrieves a player's name by UUID.
     *
     * @param uuid the player UUID
     * @return player name, or null if not found
     */
    public static String getName(UUID uuid) {
        return get(uuid).map(PlayerData::getName).orElse(null);
    }

    /**
     * Retrieves a player's UUID by name.
     *
     * <p>This uses the cache name index.</p>
     *
     * @param name the player name
     * @return UUID if found, otherwise null
     */
    public static UUID getUniqueId(String name) {
        return PlayerCache.getUniqueIdOrNull(name);
    }

    /**
     * Resolves a player's current name using a provided name.
     *
     * <p>This first resolves the UUID, then fetches the current name.</p>
     *
     * @param name input player name
     * @return resolved player name, or null if not found
     */
    public static String getName(String name) {
        UUID uuid = getUniqueId(name);
        return uuid != null ? getName(uuid) : null;
    }
}