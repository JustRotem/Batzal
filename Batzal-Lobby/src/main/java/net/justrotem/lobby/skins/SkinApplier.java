package net.justrotem.lobby.skins;

import net.justrotem.data.bukkit.SkinData;
import net.justrotem.lobby.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SkinApplier {

    public static void applySkin(Player player, SkinData skinData) {
        player.setPlayerProfile(skinData.getProfile(player));
        player.updateCommands();
    }

    public static void refreshPlayer(Player player) {
        player.hidePlayer(Main.getInstance(), player);
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> player.showPlayer(Main.getInstance(), player), 10L);
    }

}
