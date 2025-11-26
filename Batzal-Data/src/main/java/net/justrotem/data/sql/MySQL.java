package net.justrotem.data.sql;

import net.justrotem.data.utils.Utility;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public abstract class MySQL {

    private static MySQLManager mySQL;
    private static  AsyncUserDataManager userData;

    public static void connect(JavaPlugin plugin) {
        // Save default mysql.yml if it doesn’t exist
        Utility.saveResource(plugin, "mysql.yml", false); // false = don’t overwrite existing file

        // Load mysql.yml
        File sqlFile = new File(plugin.getDataFolder(), "mysql.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(sqlFile);

        mySQL = new MySQLManager(plugin,
                config.getString("mysql.host"),
                config.getInt("mysql.port"),
                config.getString("mysql.database"),
                config.getString("mysql.username"),
                config.getString("mysql.password")
        );

        mySQL.connect();

        userData = new AsyncUserDataManager(mySQL);
    }

    public static void disconnect() {
        mySQL.disconnect();
    }

    public static MySQLManager getMySQL() {
        return mySQL;
    }

    public static AsyncUserDataManager getUserData() {
        return userData;
    }
}
