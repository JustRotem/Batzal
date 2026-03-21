package net.justrotem.lobby.skins;

import net.justrotem.data.bukkit.SkinData;
import net.justrotem.data.sql.SkinDataManager;
import net.justrotem.data.utils.TextUtility;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.sql.MySQL;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.function.Function;

public class SkinManager {

    //<editor-fold desc="Data methods">

    private static final HashMap<String, SkinData> CACHE = new HashMap<>();
    private static final SkinDataManager sql = MySQL.getSkinData();

    public static void startAutoSave(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (String name : CACHE.keySet()) {
                SkinData skinData = CACHE.get(name);
                if (!skinData.isDirty()) continue;

                sql.update(skinData);
                skinData.setDirty(false);
            }
        }, 20 * 30, 20 * 30); // every 30 seconds
    }

    public static void load(SkinData skinData) {
        if (skinData == null) return;

        if (!CACHE.containsKey(skinData.getName())) CACHE.put(skinData.getName(), skinData);
    }

    public static SkinData get(String name) {
        if (name == null || name.isEmpty()) return null;

        if (CACHE.containsKey(name)) return CACHE.get(name);

        return null;
    }

    public static List<SkinData> getAll() {
        return CACHE.values().stream().toList();
    }

    public static void save(SkinData skinData) {
        sql.update(skinData);
    }

    public static void saveAll() {
        CACHE.values().forEach(sql::update);
    }

    public static boolean isRegistered(String name) {
        if (name == null) return false;

        if (CACHE.containsKey(name)) return true;

        try {
            return sql.getData(name).join() != null;
        } catch (CancellationException e) {
            return false;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Config">
    public static void loadSkinsFromYaml(JavaPlugin plugin) {
        sql.getAll().thenAccept(skins -> skins.forEach(SkinManager::load));

        if (plugin.getConfig().getBoolean("skins-file.generate")) {
            // Save default skins.yml if it doesn’t exist
            PlayerUtility.saveResource(plugin,"skins.yml", false); // false = don’t overwrite existing file
        }

        if (plugin.getConfig().getBoolean("skins-file.load")) {
            File file = new File(plugin.getDataFolder(), "skins.yml");
            if (!file.exists()) return;

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            ConfigurationSection section = config.getConfigurationSection("skins");
            if (section == null) {
                plugin.getLogger().warning("No 'Skins:' section found in skins.yml");
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                for (String name : section.getKeys(false)) {
                    String value = section.getString(name + ".value");
                    String signature = section.getString(name + ".signature");
                    boolean head = section.getBoolean(name + ".head");

                    if (value == null || signature == null) {
                        plugin.getLogger().warning("Invalid skin entry for '" + name + "' — skipping.");
                        continue;
                    }

                    load(SkinData.create(name, value, signature, head));
                }
            });
        }
    }
    //</editor-fold>

    public static SkinData getHead(String name) {
        return get(name);
    }

    public static SkinData getSkin(String name) {
        // Load skin async (Cache → MySQL → Mojang)
        SkinData skinData = get(name);
        if (skinData == null) {
            try {
                skinData = SkinFetcher.fetchSkin(name);
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }

            // Didn't find...
        }

        return skinData;
    }

    public static void setSkin(Player player, SkinData skinData, Function<SkinData, String> message) {
        applySkin(player, SkinData.DEFAULT);

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            try {
                if (skinData != null) {
                    applySkin(player, skinData);
//                    if (save) NickManager.saveSkin(player, skinData.getName());
                }

                if (message != null) player.sendMessage(TextUtility.color(message.apply(skinData)));
            } catch (Exception ignored) {}
        });
    }

    public static void setSkin(Player player, String skin, Function<SkinData, String> message) {
        SkinData skinData = getSkin(skin);
        if (skinData == null) return;

        setSkin(player, skinData, message);
    }

    public static void setSkin(Player player, String skin) {
        setSkin(player, skin, null);
    }

    public static void setRandomSkin(Player player, Function<SkinData, String> message) {
        setSkin(player, getRandomSkin(player), message);
    }

    public static SkinData getRandomSkin(Player player) {
        List<SkinData> skins = getAll().stream().filter(skinData -> !skinData.isHead()).toList();

        if (skins.isEmpty()) return null;
        SkinData skin = skins.get(new Random().nextInt(skins.size()) - 1);

        if (skin == null) return null;

        if (NickManager.isNicked(player)) {
            String usedSkin = NickManager.getSkin(player);
            if (usedSkin != null && !usedSkin.isEmpty() && usedSkin.equalsIgnoreCase(skin.getName())) return getRandomSkin(player);
        }

        return skin;
    }

    public static void resetSkin(Player player, Function<SkinData, String> message) {
        setSkin(player, player.getName(), message);
    }

    public static void resetSkin(Player player) {
        setSkin(player, player.getName(), null);
    }

    private static void applySkin(Player player, SkinData skin) {
        if (!NickManager.canChange(player)) return;

        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            SkinApplier.applySkin(player, skin);
            SkinApplier.refreshPlayer(player);
        });
    }

    public static boolean isSkinAllowed(Player player, String name) {
        String message = "";
        if (name.length() < 3) message = "Your nickname cannot be less than 3 letters";
        else if (name.length() > 16) message = "Your nickname cannot be more than 16 letters";
        else if (TextUtility.containsSpecialChars(name)) message = "Your nickname can contain only 0-9, a-z and A-Z";

        if (!message.isEmpty()) {
            player.sendMessage(TextUtility.color("&c" + message + "&c!"));
            return false;
        }

        return true;
    }
}
