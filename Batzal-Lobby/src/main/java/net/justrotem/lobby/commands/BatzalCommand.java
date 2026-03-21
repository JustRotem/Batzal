package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.hooks.PlayerManager;
import net.justrotem.lobby.listeners.ChatHandler;
import net.justrotem.lobby.nick.NamesConfig;
import net.justrotem.lobby.nick.NickData;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.utils.PlayerUtility;
import net.justrotem.lobby.vanish.VanishManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class BatzalCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();


        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                JavaPlugin plugin = Main.getInstance();

                // Saves config.yml
                plugin.reloadConfig();

                // Initialize NameConfig
                NamesConfig.initialize(plugin);

                ChatHandler.loadEmojis();
                ChatHandler.loadBannedWords();

                sender.sendMessage(TextUtility.color("&aBatzal has been reloaded!"));
                return;
            }

            if (args[0].equalsIgnoreCase("nickedplayers") && NickManager.canSee(sender)) {
                String message;
                List<NickData> list = NickManager.getNickedPlayers();

                List<NickData> online = list.stream().filter(nickData -> Bukkit.getPlayer(nickData.getUniqueId()) != null).toList();
                if (!online.isEmpty()) {
                    message = "&eList of all &aOnline &eNicked Players (&b%size%&e): %players%".replace("%size%", String.valueOf(online.size())).replace("%players%", String.join(" &8,- ", online.stream().map(nickData -> PlayerManager.getLegacyRealDisplayName(nickData.getUniqueId()) + "&8, " + NickManager.getLegacyDisplayName(nickData.getNickname(), nickData.getRank())).toList()));
                } else {
                    List<NickData> offline = list.stream().filter(nickData -> Bukkit.getPlayer(nickData.getUniqueId()) == null).toList();
                    if (!offline.isEmpty()) {
                        message = "&eList of all &cOffline &eNicked Players (&b%size%&e): %players%".replace("%size%", String.valueOf(offline.size())).replace("%players%", String.join(" &8- ", offline.stream().map(nickData -> PlayerManager.getLegacyRealDisplayName(nickData.getUniqueId()) + "&8, " + NickManager.getLegacyDisplayName(nickData.getNickname(), nickData.getRank())).toList()));
                    } else message = "&eThere are currently &b0 &eNicked Players.";
                }

                sender.sendMessage(TextUtility.color(message));
                return;
            }

            if (args[0].equalsIgnoreCase("vanishedplayers") && VanishManager.canSee(sender)) {
                String message;

                List<Player> online = VanishManager.getOnlineVanishedPlayers();
                if (!online.isEmpty()) {
                    message = "&eList of all &aOnline &eVanished Players (&b%size%&e): %players%".replace("%size%", String.valueOf(online.size())).replace("%players%", String.join("&8,", online.stream().map(PlayerManager::getLegacyRealDisplayName).toList()));
                } else {
                    List<UUID> offline = VanishManager.getOfflineVanishedPlayers();
                    if (!offline.isEmpty()) {
                        message = "&eList of all &cOffline &eVanished Players (&b%size%&e): %players%".replace("%size%", String.valueOf(offline.size())).replace("%players%", String.join("&8, ", offline.stream().map(PlayerManager::getLegacyRealDisplayName).toList()));
                    } else message = "&eThere are currently &b0 &eVanished Players.";
                }

                sender.sendMessage(TextUtility.color(message));
                return;
            }

            if (args[0].equalsIgnoreCase("spawn") && !PlayerUtility.isConsole(sender)) {
                Player player = (Player) sender;

                Configuration config = Main.getInstance().getConfig();
                World world = player.getWorld();

                double x = player.getX();
                double z = player.getZ();
                if (args.length > 1 && args[1].equalsIgnoreCase("center")) {
                    x = ((int)x) + 0.5;
                    z = ((int)z) + 0.5;
                }

                double y = player.getY();
                float yaw = player.getYaw();
                float pitch = player.getPitch();

                config.set("Spawn.world", world.getName());
                config.set("Spawn.x", x);
                config.set("Spawn.y", y);
                config.set("Spawn.z", z);
                config.set("Spawn.yaw", yaw);
                config.set("Spawn.pitch", pitch);

                Main.getInstance().saveConfig();

                player.sendMessage(TextUtility.color("&a" + TeleportCommand.getLocation(player.getLocation(), true)));
                return;
            }

            if (args[0].equalsIgnoreCase("world") && !PlayerUtility.isConsole(sender)) {
                Player player = (Player) sender;

                try {
                    World world = Bukkit.getWorld(args[1]);
                    player.teleport(Objects.requireNonNull(world).getSpawnLocation());
                } catch (IllegalArgumentException e) {
                    player.sendMessage(TextUtility.color("&cUsage: /world <world>"));
                } catch (NullPointerException e) {
                    player.sendMessage(TextUtility.color("&cThis is an invalid world!"));
                }

                return;
            }
        }

        sender.sendMessage(TextUtility.color("&cUsage: /batzal <reload/nicked-players/vanished-players" + (!PlayerUtility.isConsole(source) ? "/spawn/world" : "") + ">"));

    }

    @Override
    public @NotNull Collection<String> suggest(CommandSourceStack source, String @NotNull [] args) {
        List<String> arguments = new ArrayList<>();

        CommandSender sender = source.getSender();

        PlayerUtility.addCompletion(args, 1, arguments, "reload", NickManager.canSee(sender) ? "nickedplayers" : "", VanishManager.canSee(sender) ? "vanishedplayers" : "", "spawn");
        if (args.length > 1 && args[0].equalsIgnoreCase("spawn")) PlayerUtility.addCompletion(args, 2, arguments, "center");

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.use";
    }
}
