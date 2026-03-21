package net.justrotem.proxy.sql;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.justrotem.data.service.DataServiceShutdownController;
import net.justrotem.data.storage.mysql.MySQLManager;
import net.justrotem.proxy.FriendData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FriendDataManager {

    private final Gson gson = new Gson();
    private final MySQLManager sql;
    private final String table = "batzal_friends";
    private final String statement;

    public FriendDataManager(MySQLManager sql) {
        this.sql = sql;

        List<String> statement = List.of("uuid VARCHAR(36) PRIMARY KEY",
                "notifications BOOLEAN",
                "friends JSON",
                "requests JSON"
        );
        sql.createTable(table, sql.getSQLStatement(statement, true));

        this.statement = sql.getSQLStatement(statement, false);
    }

    public void update(FriendData friendData) {
        sql.executeAsync(() -> {
            try (Connection conn = sql.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                    "REPLACE INTO " + table + " " + statement
            )) {
                ps.setString(1, friendData.getUniqueId().toString());
                ps.setBoolean(2, friendData.isEnabledNotifications());
                ps.setString(3, gson.toJson(friendData.getFriends()));
                ps.setString(4, gson.toJson(friendData.getRequests()));
                ps.executeUpdate();
            } catch (SQLException | IllegalStateException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<FriendData> getData(UUID uuid) {
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

    public CompletableFuture<List<FriendData>> getAll() {
        return DataServiceShutdownController.track(CompletableFuture.supplyAsync(() -> {
            try (Connection conn = sql.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + table);
                 ResultSet rs = ps.executeQuery()) {

                List<FriendData> players = new ArrayList<>();
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));

                    players.add(create(uuid, rs));
                }
                return players;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return java.util.Collections.emptyList();
        }));
    }

    private FriendData create(UUID uuid, ResultSet rs) {
        try {
            boolean notifications = rs.getBoolean("notifications");
            List<UUID> friends = gson.fromJson(rs.getString("friends"), new TypeToken<List<UUID>>(){}.getType());
            List<UUID> requests = gson.fromJson(rs.getString("requests"), new TypeToken<List<UUID>>(){}.getType());

            return FriendData.create(uuid, notifications, friends, requests);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
