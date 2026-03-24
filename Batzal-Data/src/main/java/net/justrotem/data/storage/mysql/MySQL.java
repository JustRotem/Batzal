package net.justrotem.data.storage.mysql;

import org.slf4j.Logger;

/**
 * Central access point for MySQL-related services.
 *
 * <p>This class acts as a static holder for:
 * <ul>
 *     <li>{@link MySQLManager} - connection pool and database access</li>
 *     <li>{@link PlayerDataManager} - player data persistence</li>
 *     <li>{@link SkinDataManager} - skin data persistence</li>
 * </ul>
 * </p>
 *
 * <p>Typical lifecycle:
 * <ol>
 *     <li>Call {@link #connect(MySQLManager)} during plugin startup</li>
 *     <li>Use getter methods to access managers</li>
 *     <li>Call {@link #disconnect()} during shutdown</li>
 * </ol>
 * </p>
 *
 * <p>This class uses a singleton-like pattern via static fields.</p>
 */
public abstract class MySQL {

    private static MySQLManager mySQL;
    private static PlayerDataManager userData;
    private static SkinDataManager skinData;

    /**
     * Initializes the MySQL system.
     *
     * <p>This method:
     * <ul>
     *     <li>Initializes the connection pool</li>
     *     <li>Creates required managers</li>
     * </ul>
     * </p>
     *
     * <p>This should be called once during application startup.</p>
     *
     * @param mySQLManager configured MySQL manager instance
     */
    public static void connect(MySQLManager mySQLManager) {
        mySQL = mySQLManager;

        mySQLManager.connect();

        userData = new PlayerDataManager(mySQLManager);
        skinData = new SkinDataManager(mySQLManager);
    }

    /**
     * Shuts down the MySQL system.
     *
     * <p>This closes the connection pool if it was initialized.</p>
     */
    public static void disconnect() {
        if (mySQL != null) {
            mySQL.disconnect();
        }
    }

    /**
     * Returns the underlying MySQL manager.
     *
     * @return MySQLManager instance
     */
    public static MySQLManager getMySQL() {
        return mySQL;
    }

    /**
     * Returns the player data manager.
     *
     * @return PlayerDataManager instance
     */
    public static PlayerDataManager getPlayerData() {
        return userData;
    }

    /**
     * Returns the skin data manager.
     *
     * @return SkinDataManager instance
     */
    public static SkinDataManager getSkinData() {
        return skinData;
    }

    /**
     * Factory method for building a {@link MySQLManager}.
     *
     * <p>This allows implementations to define how the manager is created
     * (e.g. reading config, injecting logger, etc.).</p>
     *
     * @param debug  whether debug logging is enabled
     * @param logger logger instance
     * @return configured MySQLManager
     */
    public abstract MySQLManager buildManager(boolean debug, Logger logger);
}