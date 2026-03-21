package net.justrotem.lobby;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SkinData {

    public PlayerProfile getProfile() {
        return getProfile(null);
    }

    public PlayerProfile getProfile(Player player) {
        return update(player != null ? player.getPlayerProfile() : Bukkit.createProfile(this.name));
    }

    private PlayerProfile update(PlayerProfile profile) {
        profile.clearProperties();

        profile.setProperty(
                new ProfileProperty(
                    "textures",
                    this.value,
                    this.signature
                )
        );

        this.dirty = true;
        return profile;
    }
}
