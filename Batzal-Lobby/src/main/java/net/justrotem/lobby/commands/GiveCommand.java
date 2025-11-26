package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.PlayerManager;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
        if (Utility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        if (args.length >= 1) {
            Collection<Player> list = new ArrayList<>();
            if (args[0].equalsIgnoreCase("*") || args[0].equalsIgnoreCase("all")) list.addAll(Bukkit.getOnlinePlayers());
            else {
                Player target = Utility.getTargetNonNull(player, args[0]);
                if (target == null) return;

                list.add(target);
            }

            ItemStack item = null;
            if (args.length >= 2) {
                int amount;
                try {
                    amount = args.length == 2 ? 1 : Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(TextUtils.color("&cThis is not a valid number!"));
                    return;
                }

                try {
                    item = new ItemStack(Material.valueOf(args[1].toUpperCase()), amount);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(TextUtils.color("&cCan't find an item by the name %item%!".replace("%item%", args[1])));
                    return;
                }
            }

            if (item != null) {
                for (Player target : list) {
                    target.give(item);

                    if (target != player) player.sendMessage(TextUtils.color("%target% &ahas been given &e%amount%x%item%&a!"
                            .replace("%target%", PlayerManager.getLegacyDisplayName(target))
                            .replace("%amount%", String.valueOf(item.getAmount()))
                            .replace("%item%", item.getType().name())
                    ));

                    target.sendMessage(TextUtils.color("&aYou been given &e%amount%x%item%&a!"
                            .replace("%amount%", String.valueOf(item.getAmount()))
                            .replace("%item%", item.getType().name())
                    ));
                }

                return;
            }
        }

        player.sendMessage(TextUtils.color("&cUsage: /give <player> <item> <amount>"));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        Utility.addPlayerCompletion(args, 1, arguments, source, "batzal.give", true);
        Utility.addCompletion(args, 2, arguments, Material.values());

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.give";
    }
}