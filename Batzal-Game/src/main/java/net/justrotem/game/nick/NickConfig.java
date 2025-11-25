package net.justrotem.game.nick;

import net.justrotem.game.Main;
import net.justrotem.game.skins.SkinConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class NickConfig {

    private static FileConfiguration config;

    public static void initialize(Main plugin) {
        // Save default names.yml if it doesn’t exist
        plugin.saveResource("names.yml", false); // false = don’t overwrite existing file

        // Load names.yml
        File namesFile = new File(plugin.getDataFolder(), "names.yml");
        config = YamlConfiguration.loadConfiguration(namesFile);

        // Initialize NickManager
        NickManager.initialize(getNames());

        // Initialize SkinConfig
        SkinConfig.initialize(plugin);
    }

    public static List<String> getNames() {
        return config.getStringList("NickNames");
    }
}
