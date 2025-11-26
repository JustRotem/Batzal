package net.justrotem.data.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.justrotem.data.utils.Utility;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class MySQLManager {

    private final JavaPlugin plugin;
    private final String host, database, username, password;
    private final int port;
    private HikariDataSource dataSource;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public MySQLManager(JavaPlugin plugin, String host, int port, String database, String username, String password) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public void connect() {
        try {
            // 1️⃣ Load MySQL driver (optional, but safe)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2️⃣ Ensure database exists before Hikari connects to it
            try (Connection conn = java.sql.DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&autoReconnect=true",
                    username, password);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + database);
                if (Utility.isDebug(plugin)) plugin.getLogger().info("Database '" + database + "' ensured.");
            }

            // 3️⃣ Configure HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true");
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

            if (Utility.isDebug(plugin)) plugin.getLogger().info("Connected to MySQL.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to MySQL: ", e);
        }
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            executor.shutdown();
            if (Utility.isDebug(plugin)) plugin.getLogger().info("MySQL pool closed.");
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void executeAsync(Runnable task) {
        executor.execute(task);
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
                 if (Utility.isDebug(plugin)) plugin.getLogger().info("Table '%table%' ensured.".replace("%table%", table));
            } catch (SQLException e) {
                 if (Utility.isDebug(plugin)) plugin.getLogger().severe("Failed to create table '%table%'.".replace("%table%", table));
                e.printStackTrace();
            }
        });
    }
}
