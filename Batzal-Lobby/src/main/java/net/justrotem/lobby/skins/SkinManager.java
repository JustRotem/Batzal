package net.justrotem.lobby.skins;

import net.justrotem.lobby.Main;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.utils.TextUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SkinManager {

    private static List<SkinData> skins;

    public static void initialize(List<SkinData> recordedSkins) {
        skins = recordedSkins;
    }

    public static void setSkin(Player player, String name, boolean save, Component errorMessage, Component successMessage) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            try {
                // Searching for a skin in skins.yml
                SkinData skin = searchLocalSkin(name);

                // If there isn't a skin in skins.yml, then fetch to https://api.mojang.com/users/profiles/minecraft/
                if (skin == null) {
                    skin = SkinFetcher.fetchSkin(name);

                    if (skin == null) {
                        if (errorMessage != null) player.sendMessage(errorMessage);
                        return;
                    }
                }

                ApplySkin(player, skin);
                if (save) NickManager.saveSkin(player, name);
                if (successMessage != null) player.sendMessage(successMessage);
            } catch (Exception e) {
                if (errorMessage != null) player.sendMessage(errorMessage);
            }
        });
    }

    public static void setSkin(Player player, String name) {
        setSkin(player, name, false, null, null);
    }

    public static void setRandomSkin(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            try {
                SkinData skin = getRandomSkin(player);

                ApplySkin(player, skin);
                NickManager.saveSkin(player, skin.name());
            } catch (Exception e) {
            }
        });
    }

    public static void resetSkin(Player player, boolean savePrevious) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            try {
                SkinData skin = SkinFetcher.fetchSkin(player.getName());

                if (skin == null) return;

                ApplySkin(player, skin);
                if (!savePrevious) NickManager.saveSkin(player, null);
            } catch (Exception e) {
            }
        });
    }

    /**
     * Returns current skin, or null if none.
     */
    public static String getSkin(Player player) {
        return NickManager.getSkin(player);
    }

    public static SkinData getRandomSkin(Player player) {
        SkinData skin = new ArrayList<>(skins).get(new Random().nextInt(skins.size()));

        if (skin == null) return null;

        if (NickManager.isNicked(player)) {
            String usedSkin = getSkin(player);
            if (usedSkin != null && !usedSkin.isEmpty() && usedSkin.equalsIgnoreCase(skin.name())) return getRandomSkin(player);
        }

        return skin;
    }

    public static boolean isSkinAllowed(Player player, String name) {
        String message = "";
        if (name.length() < 3) message = "Your nickname cannot be less than 3 letters";
        else if (name.length() > 16) message = "Your nickname cannot be more than 16 letters";
        else if (TextUtils.containsSpecialChars(name)) message = "Your nickname can contain only 0-9, a-z and A-Z";

        if (!message.isEmpty()) {
            player.sendMessage(TextUtils.color("&c" + message + "&c!"));
            return false;
        }

        return true;
    }

    public static SkinData searchLocalSkin(String name) {
        return skins.stream()
                .filter(skinData -> skinData.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private static void ApplySkin(Player player, SkinData skin) {
        if (!NickManager.canChange(player)) return;

        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            SkinApplier.applySkin(player, skin);
            SkinApplier.refreshPlayer(player);
        });
    }
}
