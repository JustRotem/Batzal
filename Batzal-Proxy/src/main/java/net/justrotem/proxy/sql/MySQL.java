package net.justrotem.proxy.sql;

import com.velocitypowered.api.plugin.PluginContainer;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import net.justrotem.data.storage.mysql.MySQLManager;
import net.justrotem.proxy.Main;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class MySQL extends net.justrotem.data.storage.mysql.MySQL {

    private static FriendDataManager friendData;

    public static void connect(Main plugin) {
        MySQLManager mySQL = new MySQL().generateConfig(plugin.isDebug(), plugin.getLogger());
        net.justrotem.data.storage.mysql.MySQL.connect(mySQL);
        friendData = new FriendDataManager(mySQL);
    }

    public static FriendDataManager getFriendData() {
        return friendData;
    }

    @Override
    public MySQLManager generateConfig(boolean debug, Logger logger) {
        try {
            YamlDocument config = YamlDocument.create(new File(Main.getInstance().getDataDirectory().toFile(), "mysql.yml"),
                    getClass().getResourceAsStream("/mysql.yml"));
            config.save();

            return new MySQLManager(debug,
                    logger,
                    config.getString(Route.from("MySQL.address")),
                    3306,
                    config.getString(Route.from("MySQL.database")),
                    config.getString(Route.from("MySQL.username")),
                    config.getString(Route.from("MySQL.password"))
            );
        } catch (IOException e) {
            logger.error("Could not create/load MySQL config! This plugin will now shutdown");
            Optional<PluginContainer> container = Main.getInstance().getProxy().getPluginManager().getPlugin("batzal-proxy");
            container.ifPresent(pluginContainer -> pluginContainer.getExecutorService().shutdown());
        }
        return null;
    }
}
