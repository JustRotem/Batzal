package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public class FlyCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        boolean flying = player.getAllowFlight();
        player.setAllowFlight(!flying);
        player.setFlying(!flying);

        player.sendMessage(TextUtils.color("&aTurn %mode% flight!".replace("%mode%", !flying ? "on" : "off")));
    }

    @Override
    public @Nullable String permission() {
        return "batzal.fly";
    }
}