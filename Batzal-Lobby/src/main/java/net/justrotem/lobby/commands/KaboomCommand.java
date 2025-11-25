package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.PlayerManager;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public class KaboomCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.isFlying()) target.setFlying(false);

            Utility.strikeLightningWithoutFire(target.getLocation());
            target.setVelocity(new Vector(0.0D, 5.0, 0.0D));
            target.setFallDistance(0);
            player.sendMessage(TextUtils.color("&aLaunched %target%!".replace("%target%", PlayerManager.getLegacyDisplayName(target))));
        }
    }

    @Override
    public @Nullable String permission() {
        return "batzal.kaboom";
    }
}
