package net.justrotem.proxy.utils;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.justrotem.proxy.Main;
import org.slf4j.Logger;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class LobbyManager {

    public static void init(Main plugin, ProxyServer proxy, Logger logger) {
        new LobbyManager(plugin, proxy, logger);
    }

    private final ProxyServer proxy;
    private final Logger logger;

    private final ChannelIdentifier CHANNEL =
            MinecraftChannelIdentifier.from("batzal:servers");

    private LobbyManager(Main plugin, ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;

        proxy.getChannelRegistrar().register(CHANNEL);
        proxy.getEventManager().register(plugin, this);
        logger.info("Registered plugin messaging channel {}", CHANNEL.getId());
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(CHANNEL)) return;

        byte[] data = event.getData();

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            String sub = in.readUTF();

            switch (sub) {
                case "RequestStatus" -> handleRequestStatus(event);
                case "Connect" -> handleConnect(event, in);
                default -> logger.warn("Unknown subchannel: {}", sub);
            }

        } catch (IOException e) {
            logger.error("Failed to read plugin message", e);
        }

        // Stop the message from traveling further
        event.setResult(PluginMessageEvent.ForwardResult.handled());
    }

    /** Handles RequestStatus from Paper backend */
    private void handleRequestStatus(PluginMessageEvent event) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        out.writeUTF("Status");

        // Detect which backend sent the message
        String selfName = "";
        if (event.getSource() instanceof ServerConnection conn) {
            selfName = conn.getServerInfo().getName(); // e.g. "build-lobby-1"
        } else if (event.getSource() instanceof Player player) {
            selfName = player.getCurrentServer()
                    .map(sc -> sc.getServerInfo().getName())
                    .orElse("");
        }

        // FIRST send the name of THIS backend server
        out.writeUTF(selfName);

        var servers = proxy.getAllServers();
        out.writeInt(servers.size());

        for (RegisteredServer rs : servers) {
            boolean online;
            try {
                rs.ping().get(200, TimeUnit.MILLISECONDS); // wait up to 200ms
                online = true;
            } catch (Exception ex) {
                online = false; // timeout or connection failed
            }

            String name = rs.getServerInfo().getName();
            int players = rs.getPlayersConnected().size();

            try {
                out.writeUTF(name);
                out.writeInt(players);
                out.writeBoolean(online);
            } catch (IOException ignored) {
            }
        }

        byte[] response = baos.toByteArray();

        if (event.getSource() instanceof Player player) {
            player.getCurrentServer().ifPresent(conn ->
                    conn.sendPluginMessage(CHANNEL, response));
        } else if (event.getSource() instanceof ServerConnection conn) {
            conn.sendPluginMessage(CHANNEL, response);
        }
    }


    /** Handles Connect requests from Paper backend */
    private void handleConnect(PluginMessageEvent event, DataInputStream in) throws IOException {
        String targetServer = in.readUTF();

        Player player;

        if (event.getSource() instanceof Player p) {
            player = p;
        } else if (event.getSource() instanceof ServerConnection conn) {
            player = conn.getPlayer();
        } else {
            player = null;
        }

        if (player == null) {
            logger.warn("Connect request but no player?");
            return;
        }

        var opt = proxy.getServer(targetServer);
        if (opt.isEmpty()) {
            player.sendMessage(
                    net.kyori.adventure.text.Component.text("Server not found: " + targetServer)
            );
            return;
        }

        RegisteredServer server = opt.get();

        player.createConnectionRequest(server).connect().thenAccept(result -> {
            switch (result.getStatus()) {
                case SUCCESS ->
                        logger.info("Connected {} → {}", player.getUsername(), targetServer);

                case ALREADY_CONNECTED ->
                        logger.info("{} already on {}", player.getUsername(), targetServer);

                default ->
                        logger.warn("Failed to connect {} → {}: {}", player.getUsername(), targetServer, result.getStatus());
            }
        });
    }
}
