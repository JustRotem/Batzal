package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.utils.PlayerUtility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HatCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (PlayerUtility.isConsole(source)) return;
        Player player = (Player) source.getSender();

        final ItemStack[] item = {null};
        if (args.length == 1) {
            try {
                item[0] = new ItemStack(Material.valueOf(args[0].toUpperCase()));
            } catch (IllegalArgumentException e) {
                player.sendMessage(TextUtility.color("&cCan't find an item by the name %item%!".replace("%item%", args[0])));
                return;
            }
        }

        PlayerUtility.runTarget(player, args, item[0] == null ? 1 : 2, permission() + ".others", target -> {
            if (item[0] == null) item[0] = target.getInventory().getItemInMainHand();
            target.getInventory().setHelmet(item[0]);
            return item[0].getType().name();
        }, "&aSet %value% as your hat!%staff%", "&aSet %value% as %target%&a's hat!");
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        PlayerUtility.addCompletion(args, 1, arguments, Material.values());

        try {
            new ItemStack(Material.valueOf(args[0].toUpperCase()));
            PlayerUtility.addPlayerCompletion(args, 2, arguments, source, permission() + ".others");
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            PlayerUtility.addPlayerCompletion(args, 1, arguments, source, permission() + ".others");
        }

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.hat";
    }
}