package net.justrotem.data.storage.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.justrotem.data.service.DataServiceShutdownController;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MySQLManager {

    private final boolean debug;
    private final Logger logger;
    private final String host, database, username, password;
    private final int port;
    private HikariDataSource dataSource;

    public MySQLManager(boolean debug, Logger logger, String host, int port, String database, String username, String password) {
        this.debug = debug;
        this.logger = logger;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    private String validateDatabaseName(String databaseName) {
        if (databaseName == null || databaseName.isBlank()) {
            throw new IllegalArgumentException("Database name cannot be null or blank.");
        }

        if (!databaseName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid database name: " + databaseName);
        }

        return databaseName;
    }

    public void connect() {
        String safeDatabase = validateDatabaseName(database);

        try {
            // Ensure database exists before Hikari connects to it
            try (Connection conn = java.sql.DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/?useSSL=false&autoReconnect=true", username, password);
                 PreparedStatement ps = conn.prepareStatement("CREATE DATABASE IF NOT EXISTS " + safeDatabase)) {
                ps.executeUpdate();
                if (debug) logger.info("Database '{}' ensured.", safeDatabase);
            }

            // Configure HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + safeDatabase + "?useSSL=false&autoReconnect=true");
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(300000); // 5 minutes
            config.setMaxLifetime(600000); // 10 minutes
            config.setConnectionTimeout(10000); // 10 seconds
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);

            if (debug) logger.info("Connected to MySQL.");
        } catch (Exception e) {
            logger.error("Failed to connect to MySQL.", e);
            throw new IllegalStateException("Failed to connect to MySQL.", e);
        }
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            if (debug) logger.info("MySQL pool closed.");
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new IllegalStateException("MySQL datasource is not initialized or already closed.");
        }

        return dataSource.getConnection();
    }

    public void executeAsync(Runnable task) {
        DataServiceShutdownController.executeAsync(task);
    }

    /**
     *
     * @param table The name of the table
     * @param sqlStatement The SQL command, for example: CREATE TABLE IF NOT EXISTS example_table (uuid VARCHAR(36) PRIMARY KEY, name VARCHAR(16), flying BOOLEAN DEFAULT FALSE);
     */
    public void createTable(String table, String sqlStatement) {
        executeAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS %table% (".replace("%table%", table) + sqlStatement + ");")) {
                ps.executeUpdate();
                 if (debug) logger.info("Table '%table%' ensured.".replace("%table%", table));
            } catch (SQLException e) {
                 if (debug) logger.info("Failed to create table '%table%'.".replace("%table%", table));
                e.printStackTrace();
            }
        });
    }

    public String getSQLStatement(List<String> keys, boolean create) {
        // CREATE TABLE mode → return full definitions as-is
        // Example: "name VARCHAR(32), value TEXT, signature TEXT, head BOOLEAN"
        if (create) {
            return String.join(", ", keys);
        }

        // INSERT/REPLACE mode → extract only the column names
        // keys: ["name VARCHAR(32)", "value TEXT", ...]
        // columnNames: ["name", "value", "signature", "head"]
        List<String> columnNames = keys.stream()
                .map(def -> def.split(" ", 2)[0])
                .toList();

        String columns = "(" + String.join(", ", columnNames) + ")";

        // VALUES (?, ?, ?, ...)
        String placeholders = "(" + String.join(", ",
                Collections.nCopies(columnNames.size(), "?")
        ) + ")";

        return columns + " VALUES " + placeholders;
    }
}
