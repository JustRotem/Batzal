package net.justrotem.lobby.sql;

import net.justrotem.data.sql.MySQLManager;
import net.justrotem.data.bukkit.BukkitUtility;
import net.justrotem.lobby.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.io.File;

public class MySQL extends net.justrotem.data.sql.MySQL {

    private static NickDataManager nickData;

    public static void connect(JavaPlugin plugin) {
        MySQLManager mySQL = new MySQL().generateConfig(BukkitUtility.isDebug(plugin), plugin.getSLF4JLogger());
        net.justrotem.data.sql.MySQL.connect(mySQL);
        nickData = new NickDataManager(mySQL);
    }

    public static NickDataManager getNickData() {
        return nickData;
    }

    @Override
    public MySQLManager generateConfig(boolean debug, Logger logger) {
        // Save default mysql.yml if it doesn’t exist
        BukkitUtility.saveResource(Main.getInstance(), "mysql.yml", false); // false = don’t overwrite existing file

        // Load mysql.yml
        File file = new File(Main.getInstance().getDataFolder(), "mysql.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        return new MySQLManager(debug,
                logger,
                config.getString("MySQL.address"),
                3306,
                config.getString("MySQL.database"),
                config.getString("MySQL.username"),
                config.getString("MySQL.password")
        );
    }
}
