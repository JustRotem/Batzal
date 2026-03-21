package net.justrotem.lobby.utils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.listeners.LobbyHandler;
import net.justrotem.lobby.menu.MenuManager;
import net.justrotem.lobby.menu.menus.LobbySelector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyManager implements PluginMessageListener {

    private static final String CHANNEL = "batzal:servers"; // Velocity custom channel
    private static final Map<String, Lobby> lobbies = new ConcurrentHashMap<>();
    private static String backendName = null;

    private static JavaPlugin plugin;

    /** Initialize listeners and GUI updater */
    public static void initialize(JavaPlugin pl) {
        plugin = pl;

        // Register new Velocity channel
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, new LobbyManager());
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);

        // Register events
        plugin.getServer().getPluginManager().registerEvents(new LobbyHandler(), plugin);

        // Ask Velocity for server status every 5 seconds
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), LobbyManager::requestServers, 20L, 100L);
    }

    /** Ask Velocity for list of servers & player counts */
    public static void requestServers() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("RequestStatus");
        plugin.getServer().sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }

    /** Request Velocity to connect a player to another server */
    public static void connect(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }

    /** Handle messages from Velocity */
    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte @NotNull [] message) {
        if (!channel.equals(CHANNEL)) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String sub = in.readUTF();

        if (sub.equals("Status")) {
            String selfName = in.readUTF();
            if (!selfName.isEmpty()) {
                backendName = selfName;
                if (plugin.getConfig().getBoolean("debug", false)) {
                    plugin.getLogger().info("Detected backend name: " + backendName);
                }
            }

            lobbies.clear();

            int serverCount = in.readInt();
            for (int i = 0; i < serverCount; i++) {
                String serverName = in.readUTF();
                int onlinePlayers = in.readInt();
                boolean online = in.readBoolean();

                if (serverName.toLowerCase().contains("lobby") &&
                        (getServerType() == null || serverName.toLowerCase().contains(getServerType().toLowerCase()))) {
                    updateLobby(serverName, online, onlinePlayers);
                }
            }

            MenuManager.updateAllOpenGUIs(LobbySelector.class);
        }
    }


    /** Store/Update lobby info */
    private static void updateLobby(String serverName, boolean online, int onlinePlayers) {
        Lobby lobby = new Lobby(
                serverName,
                online,
                onlinePlayers,
                120
        );
        lobbies.put(serverName, lobby);
    }

    /** UI helper — return sorted lobby list */
    public static List<Lobby> getLobbies() {
        return lobbies.values().stream()
                .sorted(Comparator.comparing(Lobby::getServerName))
                .toList();
    }

    public static String getServerType() {
        return plugin.getConfig().getString("server-type");
    }

    /** Returns a random lobby. Never returns this backend server. */
    public static Lobby getRandomLobby() {
        List<Lobby> list = getLobbies(); // already sorted

        if (list.isEmpty()) return null;
        if (list.size() == 1) return list.getFirst();

        // Filter: exclude current backend
        String current = backendName; // detected earlier
        List<Lobby> filtered = list.stream()
                .filter(l -> !l.getServerName().equalsIgnoreCase(current))
                .toList();

        if (filtered.isEmpty()) return null;

        return filtered.get(new Random().nextInt(filtered.size()));
    }

    /** Returns a random lobby, INCLUDING the current one (if needed). */
    public static Lobby getRandomLobbyAllowCurrent() {
        List<Lobby> list = getLobbies();
        if (list.isEmpty()) return null;
        if (list.size() == 1) return list.getFirst();
        return list.get(new Random().nextInt(list.size()));
    }

    /** Returns a random lobby but avoids full ones */
    public static Lobby getRandomNonFullLobby() {
        List<Lobby> list = lobbies.values().stream()
                .filter(l -> !l.isFull())
                .toList();

        if (list.isEmpty()) return null;
        if (list.size() == 1) return list.getFirst();

        return list.get(new Random().nextInt(list.size()));
    }

    /** Lobby data holder */
    public static class Lobby {
        private final String serverName;
        private final boolean online;
        private final int onlinePlayers, maxPlayers;

        public Lobby(String serverName, boolean online, int onlinePlayers, int maxPlayers) {
            this.serverName = serverName;
            this.online = online;
            this.onlinePlayers = onlinePlayers;
            this.maxPlayers = maxPlayers;
        }

        public String getServerName() {
            return this.serverName;
        }

        public boolean isOnline() {
            return this.online;
        }

        public boolean isFull() {
            return this.maxPlayers == this.onlinePlayers;
        }

        public int getOnlinePlayers() {
            return this.onlinePlayers;
        }

        public int getMaxPlayers() {
            return this.maxPlayers;
        }

        public boolean isSameServer() {
            return backendName != null && backendName.equalsIgnoreCase(serverName);
        }
    }
}
