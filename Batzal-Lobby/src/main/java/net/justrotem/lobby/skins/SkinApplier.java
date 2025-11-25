package net.justrotem.lobby.skins;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.justrotem.lobby.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SkinApplier {

    public static void applySkin(Player player, SkinData data) {
        PlayerProfile profile = player.getPlayerProfile();

        profile.clearProperties();
        profile.setProperty(new ProfileProperty("textures", data.value(), data.signature()));

        player.setPlayerProfile(profile);
        player.updateCommands(); // Optional: refreshes data
    }

    public static void refreshPlayer(Player player) {
        player.hidePlayer(Main.getInstance(), player);
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> player.showPlayer(Main.getInstance(), player), 10L);
    }

}
