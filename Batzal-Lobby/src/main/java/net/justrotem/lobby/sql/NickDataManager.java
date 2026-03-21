package net.justrotem.lobby.sql;

import net.justrotem.data.sql.DataServiceShutdownController;
import net.justrotem.data.sql.MySQLManager;
import net.justrotem.lobby.nick.NickData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NickDataManager {

    private final MySQLManager sql;
    private final String table = "batzal_nicked";
    private final String statement;

    public NickDataManager(MySQLManager sql) {
        this.sql = sql;

        List<String> statement = List.of("uuid VARCHAR(36) PRIMARY KEY",
                "is_nicked BOOLEAN DEFAULT FALSE",
                "nickname VARCHAR(16)",
                "skin VARCHAR(16)",
                "rank VARCHAR(16)");

        sql.createTable(table, sql.getSQLStatement(statement, true));

        this.statement = sql.getSQLStatement(statement, false);
    }

    public void update(NickData nickData) {
        sql.executeAsync(() -> {
            try (Connection conn = sql.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                    "REPLACE INTO " + table + " " + statement
            )) {
                ps.setString(1, nickData.getUniqueId().toString());
                ps.setBoolean(2, nickData.isNicked());
                ps.setString(3, nickData.getNickname());
                ps.setString(4, nickData.getSkin());
                ps.setString(5, nickData.getRank());
                ps.executeUpdate();
            } catch (SQLException | IllegalStateException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<NickData> getData(UUID uuid) {
        return DataServiceShutdownController.track(CompletableFuture.supplyAsync(() -> {
            try (Connection conn = sql.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT * FROM " + table + " WHERE uuid = ?"
                 )) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return create(uuid, rs);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }));
    }

    public CompletableFuture<List<NickData>> getAll() {
        return DataServiceShutdownController.track(CompletableFuture.supplyAsync(() -> {
            try (Connection conn = sql.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + table);
                 ResultSet rs = ps.executeQuery()) {

                List<NickData> players = new ArrayList<>();
                while (rs.next()) {
                    players.add(create(UUID.fromString(rs.getString("uuid")), rs));
                }
                return players;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return java.util.Collections.emptyList();
        }));
    }

    private NickData create(UUID uuid, ResultSet rs) {
        try {
            boolean nicked = rs.getBoolean("is_nicked");
            String nickname = rs.getString("nickname");
            String skin = rs.getString("skin");
            String rank = rs.getString("rank");

            return NickData.create(uuid, nicked, nickname, skin, rank);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
