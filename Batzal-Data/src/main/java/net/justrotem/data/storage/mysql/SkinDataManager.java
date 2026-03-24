package net.justrotem.data.storage.mysql;

import net.justrotem.data.model.SkinData;
import net.justrotem.data.service.DataServiceShutdownController;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Handles persistence and retrieval of {@link SkinData} using MySQL.
 *
 * <p>All operations are executed asynchronously.</p>
 */
public class SkinDataManager {

    private final MySQLManager sql;
    private final String table = "batzal_skins";
    private final String statement;

    public SkinDataManager(MySQLManager sql) {
        this.sql = sql;

        List<String> statement = List.of("name VARCHAR(32) PRIMARY KEY", "value TEXT", "signature TEXT", "head BOOLEAN");

        sql.createTable(table, sql.getSQLStatement(statement, true));
        this.statement = sql.getSQLStatement(statement, false);
    }

    /**
     * Saves or updates a skin asynchronously.
     *
     * @param skinData skin data to persist
     */
    public void update(SkinData skinData) {
        if (skinData == null) return;

        sql.executeAsync(() -> {
            try (Connection conn = sql.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "REPLACE INTO " + table + " " + statement
                 )) {
                ps.setString(1, skinData.getName());
                ps.setString(2, skinData.getValue());
                ps.setString(3, skinData.getSignature());
                ps.setBoolean(4, skinData.isHead());
                ps.executeUpdate();
            } catch (SQLException | IllegalStateException e) {
                // TODO: Replace with proper logger
                e.printStackTrace();
            }
        });
    }

    /**
     * Retrieves a skin by name asynchronously.
     *
     * @param name skin name
     * @return future containing skin if found
     */
    public CompletableFuture<Optional<SkinData>> getData(String name) {
        return DataServiceShutdownController.supplyAsync(() -> {
            try (Connection conn = sql.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT * FROM " + table + " WHERE name = ?"
                 )) {

                ps.setString(1, name);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.ofNullable(create(name, rs));
                }
            } catch (SQLException e) {
                // TODO: Replace with proper logger
                e.printStackTrace();
            }

            return Optional.empty();
        });
    }

    /**
     * Retrieves all stored skins asynchronously.
     *
     * @return future containing list of skins
     */
    public CompletableFuture<List<SkinData>> getAll() {
        return DataServiceShutdownController.supplyAsync(() -> {
            try (Connection conn = sql.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + table);
                 ResultSet rs = ps.executeQuery()) {

                List<SkinData> skins = new ArrayList<>();

                while (rs.next()) {
                    SkinData skinData = create(rs.getString("name"), rs);

                    if (skinData != null) skins.add(skinData);
                }

                return skins;

            } catch (SQLException e) {
                // TODO: Replace with proper logger
                e.printStackTrace();
            }

            return java.util.Collections.emptyList();
        });
    }

    /**
     * Creates a SkinData instance from a database row.
     *
     * @return SkinData or null if parsing fails
     */
    private SkinData create(String name, ResultSet rs) {
        try {
            String value = rs.getString("value");
            String signature = rs.getString("signature");
            boolean head = rs.getBoolean("head");
            return SkinData.create(name, value, signature, head);
        } catch (SQLException e) {
            // TODO: Replace with proper logger
            e.printStackTrace();
        }
        return null;
    }
}
