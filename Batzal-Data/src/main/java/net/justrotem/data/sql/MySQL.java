package net.justrotem.data.sql;

import org.slf4j.Logger;

public abstract class MySQL {

    private static MySQLManager mySQL;
    private static PlayerDataManager userData;
    private static SkinDataManager skinData;

    public static void connect(MySQLManager mySQLManager) {
        mySQL = mySQLManager;

        mySQLManager.connect();

        userData = new PlayerDataManager(mySQLManager);
        skinData = new SkinDataManager(mySQLManager);
    }

    public static void disconnect() {
        mySQL.disconnect();
    }

    public static MySQLManager getMySQL() {
        return mySQL;
    }

    public static PlayerDataManager getPlayerData() {
        return userData;
    }

    public static SkinDataManager getSkinData() {
        return skinData;
    }

    public abstract MySQLManager generateConfig(boolean debug, Logger logger);
}
