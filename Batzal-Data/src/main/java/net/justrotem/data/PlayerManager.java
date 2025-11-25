package net.justrotem.data;

import net.justrotem.data.hooks.LuckPermsManager;
import net.justrotem.data.sql.AsyncUserDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerManager {

    //<editor-fold desc="Data methods">
    private static final AsyncUserDataManager userData = Main.getInstance().getMySQLConfig().getUserData();
    private static final HashMap<UUID, PlayerData> recordedPlayers = new HashMap<>();
    private static final HashMap<Player, Component> lastMessages = new HashMap<>();

    public static List<PlayerData> getAllPlayers() {
        return recordedPlayers.values().stream().toList();
    }

    public static PlayerData getData(Player player) {
        if (recordedPlayers.containsKey(player.getUniqueId())) return recordedPlayers.get(player.getUniqueId());

        registerPlayer(player);
        return recordedPlayers.get(player.getUniqueId());
    }

    public static PlayerData getData(String name) {
        return getAllPlayers().stream().filter(playerData -> playerData.getUsername().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static PlayerData getData(UUID uuid) {
        return getAllPlayers().stream().filter(playerData -> playerData.getUniqueId().equals(uuid)).findFirst().orElse(null);
    }

    public static void registerPlayer(Player player) {
        if (recordedPlayers.containsKey(player.getUniqueId())) return;

        recordedPlayers.put(player.getUniqueId(), userData.getPlayerData(player).thenApply(playerData -> {
            if (playerData == null) return userData.registerPlayer(player);
            return playerData;
        }).join());
    }

    public static void registerAllPlayers() {
        Bukkit.getOnlinePlayers().forEach(PlayerManager::registerPlayer);
    }

    public static void updatePlayer(Player player, PlayerData playerData) {
        recordedPlayers.put(player.getUniqueId(), playerData);
    }

    public static void savePlayer(Player player) {
        if (recordedPlayers.containsKey(player.getUniqueId())) userData.updatePlayer(recordedPlayers.get(player.getUniqueId()));
    }

    public static void saveAllPlayers() {
        recordedPlayers.values().forEach(userData::updatePlayer);
    }
    //</editor-fold>

    //<editor-fold desc="Bukkit methods">
    public static boolean isNameRegistered(String name) {
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .anyMatch(p -> p.getName() != null && p.getName().equalsIgnoreCase(name));
    }

    public static UUID getUniqueId(String name) {
        try {
            return Arrays.stream(Bukkit.getOfflinePlayers()).filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(name)).findFirst().orElse(null).getUniqueId();
        } catch (NoSuchElementException e) {
            return getAllPlayers().stream().map(PlayerData::getUniqueId).filter(uuid -> getName(uuid).equalsIgnoreCase(name)).findFirst().orElse(null);
        }
    }

    public static String getName(UUID uuid) {
        return getData(uuid).getUsername();
    }

    public static String getName(String name) {
        return getData(name).getUsername();
    }

    public static Component getRealDisplayName(UUID uuid) {
        return LuckPermsManager.getGroupPrefix(LuckPermsManager.getPrimaryGroup(uuid)).append(Component.text(getName(uuid)));
    }

    public static Component getRealDisplayName(Player player) {
        return LuckPermsManager.getGroupPrefix(LuckPermsManager.getPrimaryGroup(player.getUniqueId())).append(player.name());
    }

    public static Component getDisplayName(Player player) {
        return LuckPermsManager.getPrefix(player.getUniqueId()).append(player.displayName());
    }

    public static String getLegacyRealDisplayName(UUID uuid) {
        return LuckPermsManager.getLegacyGroupPrefix(LuckPermsManager.getPrimaryGroup(uuid)) + getName(uuid);
    }

    public static String getLegacyRealDisplayName(Player player) {
        return LuckPermsManager.getLegacyGroupPrefix(LuckPermsManager.getPrimaryGroup(player.getUniqueId())) + player.getName();
    }

    public static String getLegacyDisplayName(Player player) {
        return LuckPermsManager.getLegacyPrefix(player.getUniqueId()) + PlainTextComponentSerializer.plainText().serialize(player.displayName());
    }

    public static boolean isChatToggled(@NotNull Player player) {
        return getData(player.getUniqueId()).isToggleChat();
    }

    public static boolean isSameMessage(@NotNull Player player, Component message) {
        if (player.hasPermission("batzal.chat.samemessage")) return false;

        if (!lastMessages.containsKey(player)) {
            setLastMessage(player, message);
            return false;
        }

        return getText(message).equals(getText(lastMessages.get(player)));
    }

    public static void setLastMessage(Player player, Component message) {
        lastMessages.put(player, message);
    }

    public static boolean isAdvertising(@NotNull Player player, Component message) {
        if (player.hasPermission("batzal.chat.advertisement")) return false;

        String text = getText(message).toLowerCase();
        return text.contains("http") || text.contains("www.");
    }
    //</editor-fold>

    private static String getText(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
