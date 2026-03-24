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

/**
 * Manages MySQL connection pool and basic database operations.
 *
 * <p>This class is responsible for:
 * <ul>
 *     <li>Initializing and managing a HikariCP connection pool</li>
 *     <li>Ensuring database and tables exist</li>
 *     <li>Providing connections for queries</li>
 *     <li>Executing asynchronous database tasks</li>
 * </ul>
 *
 * <p>All operations that interact with the database should be executed asynchronously
 * to avoid blocking the main thread.</p>
 */
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

    /**
     * Validates database name to prevent SQL injection or invalid names.
     *
     * @param databaseName the database name
     * @return validated database name
     */
    private String validateDatabaseName(String databaseName) {
        if (databaseName == null || databaseName.isBlank()) {
            throw new IllegalArgumentException("Database name cannot be null or blank.");
        }

        if (!databaseName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid database name: " + databaseName);
        }

        return databaseName;
    }

    /**
     * Initializes the MySQL connection pool.
     *
     * <p>This method:
     * <ul>
     *     <li>Ensures the database exists</li>
     *     <li>Initializes HikariCP</li>
     * </ul>
     * </p>
     *
     * @throws IllegalStateException if connection fails
     */
    public void connect() {
        String safeDatabase = validateDatabaseName(database);

        try {
            // Ensure database exists
            try (Connection conn = java.sql.DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&autoReconnect=true",
                    username,
                    password
            );
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
            config.setIdleTimeout(300000);
            config.setMaxLifetime(600000);
            config.setConnectionTimeout(10000);

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

    /**
     * Closes the connection pool.
     */
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            if (debug) logger.info("MySQL pool closed.");
        }
    }

    /**
     * Retrieves a connection from the pool.
     *
     * @return SQL connection
     * @throws SQLException if connection retrieval fails
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new IllegalStateException("MySQL datasource is not initialized or already closed.");
        }

        return dataSource.getConnection();
    }

    /**
     * Executes a task asynchronously using the data service executor.
     *
     * @param task the task to execute
     */
    public void executeAsync(Runnable task) {
        DataServiceShutdownController.executeAsync(task);
    }

    /**
     * Creates a table if it does not already exist.
     *
     * @param table        the table name
     * @param sqlStatement SQL column definitions
     */
    public void createTable(String table, String sqlStatement) {
        executeAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement ps = connection.prepareStatement(
                         "CREATE TABLE IF NOT EXISTS " + table + " (" + sqlStatement + ");"
                 )) {

                ps.executeUpdate();
                if (debug) logger.info("Table '{}' ensured.", table);

            } catch (SQLException e) {
                logger.error("Failed to create table '{}'.", table, e);
            }
        });
    }

    /**
     * Generates SQL statement for CREATE or INSERT/REPLACE.
     *
     * @param keys   column definitions
     * @param create true for CREATE TABLE, false for INSERT/REPLACE
     * @return SQL statement
     */
    public String getSQLStatement(List<String> keys, boolean create) {
        if (create) {
            return String.join(", ", keys);
        }

        List<String> columnNames = keys.stream()
                .map(def -> def.split(" ", 2)[0])
                .toList();

        String columns = "(" + String.join(", ", columnNames) + ")";

        String placeholders = "(" + String.join(", ",
                Collections.nCopies(columnNames.size(), "?")
        ) + ")";

        return columns + " VALUES " + placeholders;
    }
}