package net.justrotem.data.storage.mysql;

import net.justrotem.data.enums.*;
import net.justrotem.data.model.PlayerData;
import net.justrotem.data.service.DataServiceShutdownController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Handles persistence and retrieval of {@link PlayerData} using MySQL.
 *
 * <p>This class is responsible for:
 * <ul>
 *     <li>Creating the player table if it does not exist</li>
 *     <li>Saving player data asynchronously</li>
 *     <li>Loading player data asynchronously</li>
 * </ul>
 *
 * <p>All database operations are executed asynchronously to avoid blocking the main thread.</p>
 */
public class PlayerDataManager {

    private final MySQLManager sql;
    private final String table = "batzal_players";

    /**
     * SQL insert/update statement (generated dynamically).
     */
    private final String statement;

    public PlayerDataManager(MySQLManager sql) {
        this.sql = sql;

        List<String> columns = List.of(
                "uuid VARCHAR(36) PRIMARY KEY",
                "name VARCHAR(16)",
                "skin_value TEXT",
                "skin_signature TEXT",
                "is_vanished BOOLEAN",
                "toggle_chat BOOLEAN",
                "toggle_punch BOOLEAN",
                "total_experience INT",
                "rank_color VARCHAR(16)",
                "prefix_color VARCHAR(16)",
                "punch_message VARCHAR(16)",
                "message_mode VARCHAR(16)",
                "status VARCHAR(16)",
                "visibility_state VARCHAR(16)"
        );

        sql.createTable(table, sql.getSQLStatement(columns, true));
        this.statement = sql.getSQLStatement(columns, false);
    }

    /**
     * Saves or updates player data asynchronously.
     *
     * <p>This uses {@code REPLACE INTO}, meaning:
     * <ul>
     *     <li>If the row exists → it will be replaced</li>
     *     <li>If it does not exist → it will be inserted</li>
     * </ul>
     * </p>
     *
     * <p>This operation is fire-and-forget (no result returned).</p>
     *
     * @param playerData the player data to persist
     */
    public void update(PlayerData playerData) {
        if (playerData == null) return;

        sql.executeAsync(() -> {
            try (Connection conn = sql.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "REPLACE INTO " + table + " " + statement
                 )) {

                ps.setString(1, playerData.getUniqueId().toString());
                ps.setString(2, playerData.getName());
                ps.setString(3, playerData.getValue());
                ps.setString(4, playerData.getSignature());
                ps.setBoolean(5, playerData.isVanished());
                ps.setBoolean(6, playerData.isToggleChat());
                ps.setBoolean(7, playerData.isTogglePunch());
                ps.setInt(8, playerData.getTotalExperience());
                ps.setString(9, playerData.getRankColor().name());
                ps.setString(10, playerData.getPrefixColor().name());
                ps.setString(11, playerData.getPunchMessage().name());
                ps.setString(12, playerData.getMessageMode().name());
                ps.setString(13, playerData.getStatus().name());
                ps.setString(14, playerData.getVisibilityState().name());

                ps.executeUpdate();

            } catch (SQLException | IllegalStateException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Retrieves player data asynchronously by UUID.
     *
     * <p>If no data is found, an empty {@link Optional} is returned.</p>
     *
     * @param uuid the player UUID
     * @return future containing player data if found
     */
    public CompletableFuture<Optional<PlayerData>> getData(UUID uuid) {
        if (uuid == null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        return DataServiceShutdownController.supplyAsync(() -> {
            try (Connection conn = sql.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT * FROM " + table + " WHERE uuid = ?"
                 )) {

                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return Optional.ofNullable(create(uuid, rs));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return Optional.empty();
        });
    }

    /**
     * Retrieves all player data asynchronously.
     *
     * @return future containing a list of all players
     */
    public CompletableFuture<List<PlayerData>> getAll() {
        return DataServiceShutdownController.supplyAsync(() -> {
            try (Connection conn = sql.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + table);
                 ResultSet rs = ps.executeQuery()) {

                List<PlayerData> players = new ArrayList<>();

                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    PlayerData playerData = create(uuid, rs);

                    if (playerData != null) {
                        players.add(playerData);
                    }
                }

                return players;

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return Collections.emptyList();
        });
    }

    /**
     * Creates a {@link PlayerData} instance from a database row.
     *
     * <p>If parsing fails (e.g. invalid enum value), this method returns {@code null}.</p>
     *
     * @param uuid the player UUID
     * @param rs   the result set positioned at a valid row
     * @return constructed PlayerData or null if failed
     */
    private PlayerData create(UUID uuid, ResultSet rs) {
        try {
            String name = rs.getString("name");
            String value = rs.getString("skin_value");
            String signature = rs.getString("skin_signature");

            boolean vanished = rs.getBoolean("is_vanished");
            boolean toggleChat = rs.getBoolean("toggle_chat");
            boolean togglePunch = rs.getBoolean("toggle_punch");

            int totalExperience = rs.getInt("total_experience");

            RankColor.Color rankColor = RankColor.Color.valueOf(rs.getString("rank_color"));
            RankColor.PrefixColor prefixColor = RankColor.PrefixColor.valueOf(rs.getString("prefix_color"));

            PunchMessage punchMessage = PunchMessage.valueOf(rs.getString("punch_message"));
            MessageMode messageMode = MessageMode.valueOf(rs.getString("message_mode"));
            Status status = Status.valueOf(rs.getString("status"));
            Visibility.State visibilityState = Visibility.State.valueOf(rs.getString("visibility_state"));

            return PlayerData.create(
                    uuid, name, value, signature,
                    vanished, toggleChat, togglePunch,
                    totalExperience,
                    rankColor, prefixColor,
                    punchMessage, messageMode,
                    status, visibilityState
            );

        } catch (SQLException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        return null;
    }
}