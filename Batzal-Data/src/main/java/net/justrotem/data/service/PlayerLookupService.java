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

public final class PlayerLookupService {

    private PlayerLookupService() {
    }

    private static final PlayerDataManager SQL = MySQL.getPlayerData();

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

    public static Optional<PlayerData> get(UUID uuid) {
        try {
            return getAsync(uuid).join();
        } catch (CancellationException | CompletionException e) {
            return Optional.empty();
        }
    }

    public static CompletableFuture<List<PlayerData>> getAllAsync() {
        return SQL.getAll()
                .thenApply(list -> {
                    list.forEach(PlayerCache::put);
                    return list;
                })
                .exceptionally(throwable -> List.of());
    }

    public static List<PlayerData> getAll() {
        try {
            return getAllAsync().join();
        } catch (CancellationException | CompletionException e) {
            return List.of();
        }
    }

    public static void update(PlayerData playerData) {
        PlayerCache.put(playerData);
    }

    public static void save(PlayerData playerData) {
        if (playerData == null) {
            return;
        }

        SQL.update(playerData);
        playerData.setDirty(false);
        PlayerCache.put(playerData);
    }

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

    public static void saveAll() {
        PlayerCache.values().forEach(playerData -> {
            SQL.update(playerData);
            playerData.setDirty(false);
        });
    }

    public static void saveDirty() {
        PlayerCache.values().stream()
                .filter(PlayerData::isDirty)
                .forEach(playerData -> {
                    SQL.update(playerData);
                    playerData.setDirty(false);
                });
    }

    public static void saveDirtyAndEvictExpired() {
        saveDirty();
        PlayerCache.evictExpired();
    }

    public static boolean isRegistered(UUID uuid) {
        return get(uuid).isPresent();
    }

    public static String getName(UUID uuid) {
        return get(uuid).map(PlayerData::getName).orElse(null);
    }

    public static UUID getUniqueId(String name) {
        return PlayerCache.getUniqueIdOrNull(name);
    }

    public static String getName(String name) {
        UUID uuid = getUniqueId(name);
        return uuid != null ? getName(uuid) : null;
    }
}