package net.justrotem.data.sql;

import net.justrotem.data.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MySQLConfig implements MySQL {

    private final AsyncMySQLManager sql;
    private final AsyncUserDataManager userData;

    public MySQLConfig(final Main plugin) {
        // Save default mysql.yml if it doesn’t exist
        plugin.saveResource("mysql.yml", false); // false = don’t overwrite existing file

        // Load mysql.yml
        File sqlFile = new File(plugin.getDataFolder(), "mysql.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(sqlFile);

        sql = new AsyncMySQLManager(
                config.getString("mysql.host"),
                config.getInt("mysql.port"),
                config.getString("mysql.database"),
                config.getString("mysql.username"),
                config.getString("mysql.password")
        );

        sql.connect();

        userData = new AsyncUserDataManager(sql);
    }

    @Override
    public void disconnect() {
        sql.disconnect();
    }

    @Override
    public AsyncMySQLManager getSQL() {
        return this.sql;
    }

    @Override
    public AsyncUserDataManager getUserData() {
        return this.userData;
    }
}
