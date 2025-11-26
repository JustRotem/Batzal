package net.justrotem.data.sql;

import net.justrotem.data.PlayerData;
import net.justrotem.data.RankColor;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AsyncUserDataManager {

    private final MySQLManager sql;
    private final String table = "batzal_players";

    public AsyncUserDataManager(MySQLManager sql) {
        this.sql = sql;

        sql.createTable(table, "uuid VARCHAR(36) PRIMARY KEY, " +
                "username VARCHAR(16), " +
                "is_vanished BOOLEAN DEFAULT FALSE, " +
                "toggle_chat BOOLEAN DEFAULT FALSE, " +
                "toggle_punch BOOLEAN DEFAULT FALSE, " +
                "total_experience INT, " +
                "rank_color VARCHAR(16), " +
                "prefix_color VARCHAR(16), " +
                "message_mode VARCHAR(16), " +
                "status VARCHAR(16)"
        );
    }

    public PlayerData registerPlayer(Player player) {
        sql.executeAsync(() -> {
            try (PreparedStatement ps = sql.getConnection().prepareStatement(
                    "INSERT IGNORE INTO " + table + " (uuid, username, is_vanished, toggle_chat, toggle_punch, total_experience, rank_color, prefix_color, message_mode, status) " +
                            "VALUES (?, ?, FALSE, FALSE, FALSE, 0, 'Red', 'Gold', NULL, NULL)"
            )) {
                ps.setString(1, player.getUniqueId().toString());
                ps.setString(2, player.getName());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return PlayerData.create(player.getUniqueId(), player.getName());
    }

    public void updatePlayer(UUID uuid, String username, boolean vanished, boolean toggleChat, boolean togglePunch, int totalExperience, RankColor.Color rankColor, RankColor.PrefixColor prefixColor) {
        sql.executeAsync(() -> {
            try (PreparedStatement ps = sql.getConnection().prepareStatement(
                    "REPLACE INTO " + table + " (uuid, username, is_vanished, toggle_chat, toggle_punch, total_experience, rank_color, prefix_color, message_mode, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            )) {
                ps.setString(1, uuid.toString());
                ps.setString(2, username);
                ps.setBoolean(3, vanished);
                ps.setBoolean(4, toggleChat);
                ps.setBoolean(5, togglePunch);
                ps.setInt(6, totalExperience);
                ps.setString(7, rankColor.name());
                ps.setString(8, prefixColor.name());
                ps.setString(9, null);
                ps.setString(10, null);
                ps.executeUpdate();
            } catch (SQLException | IllegalStateException e) {
            }
        });
    }

    public void updatePlayer(Player player, boolean vanished, boolean toggleChat, boolean togglePunch, int totalExperience, RankColor.Color rankColor, RankColor.PrefixColor prefixColor) {
        updatePlayer(player.getUniqueId(), player.getName(), vanished, toggleChat, togglePunch, totalExperience, rankColor, prefixColor);
    }

    public void updatePlayer(PlayerData playerData) {
        updatePlayer(playerData.getUniqueId(), playerData.getUsername(), playerData.isVanished(), playerData.isToggleChat(), playerData.isTogglePunch(), playerData.getTotalExperience(), playerData.getRankColor(), playerData.getPrefixColor());
    }

    public CompletableFuture<PlayerData> getPlayerData(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = sql.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT * FROM " + table + " WHERE uuid = ?"
                 )) {
                ps.setString(1, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String username = rs.getString("username");
                    boolean vanished = rs.getBoolean("is_vanished");
                    boolean toggleChat = rs.getBoolean("toggle_chat");
                    boolean togglePunch = rs.getBoolean("toggle_punch");
                    int totalExperience = rs.getInt("total_experience");
                    RankColor.Color rankColor = RankColor.Color.valueOf(rs.getString("rank_color"));
                    RankColor.PrefixColor prefixColor = RankColor.PrefixColor.valueOf(rs.getString("prefix_color"));

                    return PlayerData.create(player.getUniqueId(), username, vanished, toggleChat, togglePunch, totalExperience, rankColor, prefixColor);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
