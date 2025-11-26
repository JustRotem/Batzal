package net.justrotem.lobby.sql;

import org.bukkit.plugin.java.JavaPlugin;

public class MySQL extends net.justrotem.data.sql.MySQL {

    private static AsyncNickDataManager nickData;

    public static void connect(JavaPlugin plugin) {
        net.justrotem.data.sql.MySQL.connect(plugin);
        nickData = new AsyncNickDataManager(getMySQL());
    }

    public static AsyncNickDataManager getNickData() {
        return nickData;
    }
}
