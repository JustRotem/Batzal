package net.justrotem.data.cache;

import net.justrotem.data.integration.luckperms.LuckPermsService;
import net.justrotem.data.model.PlayerData;
import net.justrotem.data.storage.mysql.MySQL;
import net.justrotem.data.storage.mysql.PlayerDataManager;
import net.justrotem.data.util.TextFormatter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CancellationException;

public class PlayerManager {

    //<editor-fold desc="Data methods">
    static final HashMap<UUID, PlayerData> CACHE = new HashMap<>();
    static final PlayerDataManager sql = MySQL.getPlayerData();

    static void update(PlayerData playerData) {
        if (CACHE.containsKey(playerData.getUniqueId())) CACHE.get(playerData.getUniqueId()).clone(playerData);

        CACHE.put(playerData.getUniqueId(), playerData);
    }

    public static PlayerData get(UUID uuid) {
        if (uuid == null) return null;

        if (CACHE.containsKey(uuid)) return CACHE.get(uuid);

        try {
            return sql.getData(uuid).join();
        } catch (CancellationException e) {
            return null;
        }
    }

    public static List<PlayerData> getAll() {
        return sql.getAll().join();
    }

    public static void saveAndRemove(UUID uuid) {
        PlayerData playerData = get(uuid);
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

    public static String getName(UUID uuid) {
        try {
            return Objects.requireNonNull(get(uuid)).getName();
        } catch (NullPointerException e) {
            return null;
        }
    }
    //</editor-fold>

    public static UUID getUniqueId(String name) {
        try {
            return Objects.requireNonNull(CACHE.values().stream().filter(playerData -> playerData.getName().equalsIgnoreCase(name)).findFirst().orElse(null)).getUniqueId();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static String getName(String name) {
        try {
            return Objects.requireNonNull(getName(getUniqueId(name)));
        } catch (NullPointerException e) {
            return null;
        }
    }

    private static final HashMap<UUID, Component> LAST_MESSAGES = new HashMap<>();

    public static boolean isChatToggled(@NotNull UUID uuid) {
        try {
            return Objects.requireNonNull(get(uuid)).isToggleChat();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isSameMessage(@NotNull UUID uuid, Component message) {
        if (LuckPermsService.hasPermission(uuid, "batzal.chat.samemessage")) return false;

        if (!LAST_MESSAGES.containsKey(uuid)) {
            setLastMessage(uuid, message);
            return false;
        }

        return TextFormatter.getText(message).equals(TextFormatter.getText(LAST_MESSAGES.get(uuid)));
    }

    public static void setLastMessage(UUID uuid, Component message) {
        LAST_MESSAGES.put(uuid, message);
    }

    public static boolean isAdvertising(@NotNull UUID uuid, Component message) {
        if (LuckPermsService.hasPermission(uuid, "batzal.chat.advertisement")) return false;

        String text = TextFormatter.getText(message).toLowerCase();
        return text.contains("http") || text.contains("www.");
    }

}
