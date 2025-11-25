package net.justrotem.game.sql;

import net.justrotem.data.sql.AsyncMySQLManager;
import net.justrotem.game.nick.NickData;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AsyncNickDataManager {

    private final AsyncMySQLManager sql;
    private final String table = "batzal_nicked";

    public AsyncNickDataManager(AsyncMySQLManager sql) {
        this.sql = sql;

        sql.createTable(table, "uuid VARCHAR(36) PRIMARY KEY, " +
                "is_nicked BOOLEAN DEFAULT FALSE, " +
                "nickname VARCHAR(16), " +
                "skin VARCHAR(16), " +
                "rank VARCHAR(16)"
        );
    }

    public NickData registerPlayer(UUID uuid) {
        sql.executeAsync(() -> {
            try (PreparedStatement ps = sql.getConnection().prepareStatement(
                    "INSERT IGNORE INTO " + table + " (uuid, is_nicked, nickname, skin, rank) " +
                            "VALUES (?, FALSE, NULL, NULL, 'default')"
            )) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return NickData.create(uuid, false, null, null, null);
    }

    public void updatePlayer(UUID uuid, boolean nicked, String nickname, String skin, String rank) {
        sql.executeAsync(() -> {
            try (PreparedStatement ps = sql.getConnection().prepareStatement(
                    "REPLACE INTO " + table + " (uuid, is_nicked, nickname, skin, rank) " +
                            "VALUES (?, ?, ?, ?, ?)"
            )) {
                ps.setString(1, uuid.toString());
                ps.setBoolean(2, nicked);
                ps.setString(3, nickname);
                ps.setString(4, skin);
                ps.setString(5, rank);
                ps.executeUpdate();
            } catch (SQLException | IllegalStateException e) {
            }
        });
    }

    public void updatePlayer(Player player, boolean nicked, String nickname, String skin, String rank) {
        updatePlayer(player.getUniqueId(), nicked, nickname, skin, rank);
    }

    public void updatePlayer(NickData nickData) {
        updatePlayer(nickData.getUniqueId(), nickData.isNicked(), nickData.getNickname(), nickData.getSkin(), nickData.getRank());
    }

    public CompletableFuture<NickData> getNickData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = sql.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT * FROM " + table + " WHERE uuid = ?"
                 )) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    boolean nicked = rs.getBoolean("is_nicked");
                    String nickname = rs.getString("nickname");
                    String skin = rs.getString("skin");
                    String rank = rs.getString("rank");

                    return NickData.create(uuid, nicked, nickname, skin, rank);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
