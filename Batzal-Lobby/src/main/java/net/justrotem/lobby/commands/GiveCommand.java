package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.utils.TextUtility;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GiveCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        ItemStack item = null;
        if (args.length >= 2) {
            int amount;
            try {
                amount = args.length == 2 ? 1 : Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(TextUtility.color("&cThis is an invalid number!"));
                return;
            }

            try {
                item = new ItemStack(Material.valueOf(args[1].toUpperCase()), amount);
            } catch (IllegalArgumentException e) {
                player.sendMessage(TextUtility.color("&cCan't find an item by the name %item%!".replace("%item%", args[1])));
                return;
            }
        }

        if (item != null) {
            ItemStack finalItem = item;
            PlayerUtility.runTarget(player, args, 1, permission(), target -> {
                target.give(finalItem);

                return List.of(String.valueOf(finalItem.getAmount()), finalItem.getType().name());
            }, "&aYou've been given &e%value-1%x%value-2%&a!", "%target% &ahas been given &e%value-1%x%value-2%&a!");

            return;
        }

        player.sendMessage(TextUtility.color("&cUsage: /give <player> <item> <amount>"));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        PlayerUtility.addPlayerCompletion(args, 1, arguments, source, "batzal.give", true);
        PlayerUtility.addCompletion(args, 2, arguments, Material.values());

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.give";
    }
}