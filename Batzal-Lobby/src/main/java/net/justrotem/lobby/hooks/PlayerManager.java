package net.justrotem.lobby.hooks;

import net.justrotem.data.player.BukkitPlayerManager;
import net.justrotem.lobby.commands.FlyCommand;
import net.justrotem.lobby.commands.StuckCommand;
import net.justrotem.lobby.commands.WarCommand;
import net.justrotem.lobby.listeners.LobbyHandler;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.utils.ExperienceManager;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerManager extends BukkitPlayerManager {

    public static void registerPlayer(Player player) {
        register(player);
        NickManager.register(player.getUniqueId());

        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue());
        player.setFoodLevel(20);
        respawn(player);
        StuckCommand.teleport(player);
    }

    public static void respawn(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, -1, 0, false, false));
        ExperienceManager.setCurrentTotalExperience(player);

        FlyCommand.flyByPermission(player);

        if (WarCommand.isWarMode()) WarCommand.giveWarModeItems(player);
         else giveLobbyItems(player);
    }

    private static void giveLobbyItems(Player player) {
        player.getInventory().setItem(0, LobbyHandler.GAME_MENU);
        player.getInventory().setItem(1, LobbyHandler.createProfile(player));
        player.getInventory().setItem(8, LobbyHandler.LOBBY_SELECTOR);
    }
}
