package net.justrotem.proxy;

import net.justrotem.data.util.ClickUtility;
import net.justrotem.proxy.sql.FriendDataManager;
import net.justrotem.proxy.sql.MySQL;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;

public class FriendManager {

    public static final Component UP_HYPHEN = ClickUtility.color("&9&m---------------------------------------------\n");
    public static final Component DOWN_HYPHEN = ClickUtility.color("\n&9&m---------------------------------------------");

    static final HashMap<UUID, FriendData> CACHE = new HashMap<>();
    static final FriendDataManager sql = MySQL.getFriendData();

    static void update(FriendData playerData) {
        if (CACHE.containsKey(playerData.getUniqueId())) CACHE.get(playerData.getUniqueId()).clone(playerData);

        CACHE.put(playerData.getUniqueId(), playerData);
    }

    public static void register(UUID uuid) {
        FriendData friendData = get(uuid);
        if (friendData == null) friendData = FriendData.create(uuid);

        update(friendData);
    }

    public static FriendData get(UUID uuid) {
        if (uuid == null) return null;

        if (CACHE.containsKey(uuid)) return CACHE.get(uuid);

        try {
            return sql.getData(uuid).join();
        } catch (CancellationException e) {
            return null;
        }
    }

    public static List<FriendData> getAll() {
        return sql.getAll().join();
    }

    public static void saveAndRemove(UUID uuid) {
        FriendData playerData = get(uuid);
        if (playerData == null) return;

        sql.update(playerData);
        CACHE.remove(playerData.getUniqueId());
    }

    public static void saveAll() {
        CACHE.values().forEach(sql::update);
    }

    public static boolean isRegistered(UUID uuid) {
        if (uuid == null) return false;

        if (CACHE.containsKey(uuid)) return true;

        try {
            return sql.getData(uuid).join() != null;
        } catch (CancellationException e) {
            return false;
        }
    }
}
