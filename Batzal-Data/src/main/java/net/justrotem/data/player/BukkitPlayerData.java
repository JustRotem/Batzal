package net.justrotem.data.player;

import com.destroystokyo.paper.profile.ProfileProperty;
import net.justrotem.data.bukkit.RankColor;
import net.justrotem.data.data.MessageMode;
import net.justrotem.data.data.PunchMessage;
import net.justrotem.data.data.Status;
import net.justrotem.data.data.Visibility;
import org.bukkit.entity.Player;

import java.util.NoSuchElementException;

public class BukkitPlayerData {

    public static PlayerData create(Player player) {
        String value;
        String signature;
        try {
            ProfileProperty property = player.getPlayerProfile().getProperties().iterator().next();
            value = property.getValue();
            signature = property.getSignature();
        } catch (NoSuchElementException e) {
            value = "";
            signature = "";
        }

        return PlayerData.create(player.getUniqueId(), player.getName(), value, signature, false, false, false, 7, RankColor.Color.Red, RankColor.PrefixColor.Gold, PunchMessage.NONE, MessageMode.ANYONE, Status.Online, Visibility.State.VISIBLE);
    }

    public static PlayerData checkForUpdates(Player player, PlayerData playerData) {
        ProfileProperty property = player.getPlayerProfile().getProperties().iterator().next();
        playerData.updatePlayer(player.getName(), property.getValue(), property.getSignature());
        return playerData;
    }

}
