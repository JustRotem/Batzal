package net.justrotem.proxy;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.GameProfile;
import net.justrotem.data.bukkit.RankColor;
import net.justrotem.data.enums.MessageMode;
import net.justrotem.data.enums.PunchMessage;
import net.justrotem.data.enums.Status;
import net.justrotem.data.enums.Visibility;
import net.justrotem.data.model.PlayerData;

import java.util.NoSuchElementException;

public class VelocityPlayerData {

    public static PlayerData create(Player player) {
        String value;
        String signature;
        try {
            GameProfile.Property property = player.getGameProfileProperties().getFirst();
            value = property.getValue();
            signature = property.getSignature();
        } catch (NoSuchElementException e) {
            value = "";
            signature = "";
        }

        return PlayerData.create(player.getUniqueId(), player.getUsername(), value, signature, false, false, false, 7, RankColor.Color.Red, RankColor.PrefixColor.Gold, PunchMessage.NONE, MessageMode.ANYONE, Status.Online, Visibility.State.VISIBLE);
    }

    public static PlayerData checkForUpdates(Player player, PlayerData playerData) {
        GameProfile.Property property = player.getGameProfileProperties().getFirst();
        if (property != null) playerData.updatePlayer(player.getUsername(), property.getValue(), property.getSignature());
        return playerData;
    }

}
