package net.justrotem.lobby.nick;

import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class NamesConfig {

    private static FileConfiguration config;

    public static void initialize(JavaPlugin plugin) {
        // Save default names.yml if it doesn’t exist
        PlayerUtility.saveResource(plugin,"names.yml", false); // false = don’t overwrite existing file

        // Load names.yml
        File file = new File(plugin.getDataFolder(), "names.yml");
        config = YamlConfiguration.loadConfiguration(file);

        // Initialize NickManager
        NickManager.initialize(getNames());
    }

    public static List<String> getNames() {
        return config.getStringList("names");
    }
}
