package net.justrotem.data.storage.mysql;

import net.justrotem.data.model.SkinData;
import net.justrotem.data.service.DataServiceShutdownController;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    public void update(SkinData skinData) {
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
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<SkinData> getData(String name) {
        return DataServiceShutdownController.track(CompletableFuture.supplyAsync(() -> {
            try (Connection conn = sql.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE name = ?")) {
                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return create(name, rs);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }));
    }

    public CompletableFuture<List<SkinData>> getAll() {
        return DataServiceShutdownController.track(CompletableFuture.supplyAsync(() -> {
            try (Connection conn = sql.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + table);
                 ResultSet rs = ps.executeQuery()) {

                List<SkinData> skins = new ArrayList<>();
                while (rs.next()) {
                    skins.add(create(rs.getString("name"), rs));
                }
                return skins;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return java.util.Collections.emptyList();
        }));
    }

    private SkinData create(String name, ResultSet rs) {
        try {
            String value = rs.getString("value");
            String signature = rs.getString("signature");
            boolean head = rs.getBoolean("head");
            return SkinData.create(name, value, signature, head);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
