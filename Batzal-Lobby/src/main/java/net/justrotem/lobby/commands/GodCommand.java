package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.utils.ToggleManager;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public class GodCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        ToggleManager.toggle(Main.ToggleCategory.God, player);

        player.sendMessage(TextUtils.color("&aTurned God mode " + (isOn(player) ? "on" : "off") + "!"));
    }

    @Override
    public @Nullable String permission() {
        return "batzal.god";
    }

    public static boolean isOn(Player player) {
        return ToggleManager.isOn(Main.ToggleCategory.God, player);
    }
}