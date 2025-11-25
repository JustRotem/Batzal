package net.justrotem.game.skins;

import net.justrotem.game.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SkinConfig {

    public static void initialize(Main plugin) {
        // Save default skins.yml if it doesn’t exist
        plugin.saveResource("skins.yml", false); // false = don’t overwrite existing file

        // Load skins.yml
        File skinsFile = new File(plugin.getDataFolder(), "skins.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(skinsFile);

        List<SkinData> recordedSkins = new ArrayList<>();

        ConfigurationSection section = config.getConfigurationSection("Skins");
        for (String key : section.getKeys(false)) {
            String value = section.getString(key + ".value");
            String signature = section.getString(key + ".signature");

            SkinData skin = new SkinData(key, value, signature);
            recordedSkins.add(skin);
        }

        // Initialize skins from skin.yml
        SkinManager.initialize(recordedSkins);
    }
}
