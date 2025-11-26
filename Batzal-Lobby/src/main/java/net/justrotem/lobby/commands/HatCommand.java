package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.PlayerManager;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HatCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        try {
            ItemStack item;
            if (args.length == 1) {
                item = new ItemStack(Material.valueOf(args[0].toUpperCase()));
            } else item = player.getInventory().getItemInMainHand();

            player.getInventory().setHelmet(item);
            player.sendMessage(TextUtils.color("&aSet %item% as your hat!".replace("%item%", item.getType().name())));
        } catch (IllegalArgumentException e) {
            player.sendMessage(TextUtils.color("&cCan't find an item by the name %item%!".replace("%item%", args[0])));
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        Utility.addCompletion(args,  1, arguments, Material.values());

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.hat";
    }
}