package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public class TopCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        Location location = player.getLocation();
        location = player.getWorld().getHighestBlockAt(location).getLocation();

        location.setX(location.getX() + 0.5);
        location.setZ(location.getZ() + 0.5);

        player.teleport(location);
    }

    @Override
    public @Nullable String permission() {
        return "batzal.top";
    }
}
