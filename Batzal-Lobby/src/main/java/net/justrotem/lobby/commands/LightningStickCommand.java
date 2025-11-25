package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

public class LightningStickCommand implements BasicCommand {

    public static final ItemStack LIGHTNING_STICK = Utility.createItem(Material.STICK, "&eLightning Stick", null, true);

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        player.give(LIGHTNING_STICK);
    }

    @Override
    public @Nullable String permission() {
        return "batzal.smite";
    }
}
