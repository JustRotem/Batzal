package net.justrotem.lobby.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.justrotem.data.PlayerManager;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.nick.NickConfig;
import net.justrotem.lobby.nick.NickData;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.skins.SkinConfig;
import net.justrotem.lobby.utils.TextUtils;
import net.justrotem.lobby.utils.Utility;
import net.justrotem.lobby.vanish.VanishManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class BatzalCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();

        if (args.length == 0) {
            sender.sendMessage(TextUtils.color("&cUsage: /batzal <reload/nicked-players/vanished-players>"));
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            Main plugin = Main.getInstance();

            // Saves config.yml
            plugin.reloadConfig();

            // Initialize NameConfig
            NickConfig.initialize(plugin);

            // Initialize SkinConfig
            SkinConfig.initialize(plugin);

            sender.sendMessage(TextUtils.color("&aBatzal has been reloaded!"));
            return;
        }

        if (args[0].equalsIgnoreCase("nickedplayers")) {
            Component message = Component.text().build();
            List<NickData> list = NickManager.getNickedPlayers();

            List<NickData> online = list.stream().filter(nickData -> Bukkit.getPlayer(nickData.getUniqueId()) != null).toList();
            if (!online.isEmpty()) {
                message = TextUtils.color("&eList of all &aOnline &eNicked Players (&b%size%&e): ".replace("%size%", String.valueOf(online.size())));

                for (int i = 0; i < online.size(); i++) {
                    NickData nickData = online.get(i);
                    UUID uuid = nickData.getUniqueId();

                    message = message.append(PlayerManager.getRealDisplayName(uuid))
                            .append(TextUtils.color(" &8- ")
                                    .append(NickManager.getDisplayName(nickData.getNickname(), nickData.getRank())));

                    if (i < online.size() - 1) message = message.append(TextUtils.color("&8, "));
                }
            } else {
                List<NickData> offline = list.stream().filter(nickData -> Bukkit.getPlayer(nickData.getUniqueId()) == null).toList();
                if (!offline.isEmpty()) {
                    message = TextUtils.color("&eList of all &cOffline &eNicked Players (&b%size%&e): ".replace("%size%", String.valueOf(offline.size())));

                    for (int i = 0; i < offline.size(); i++) {
                        NickData nickData = offline.get(i);
                        UUID uuid = nickData.getUniqueId();

                        message = message.append(PlayerManager.getRealDisplayName(uuid))
                                .append(TextUtils.color(" &8- ")
                                        .append(NickManager.getDisplayName(nickData.getNickname(), nickData.getRank())));

                        if (i < offline.size() - 1) message = message.append(TextUtils.color("&8, "));
                    }
                }
            }

            if (TextUtils.serialize(message, TextUtils.Format.PLAIN).isEmpty()) message = TextUtils.color("&eThere are currently &b0 &eNicked Players.");

            sender.sendMessage(message);
            return;
        }

        if (args[0].equalsIgnoreCase("vanishedplayers")) {
            Component message = Component.text().build();
            List<Player> online = VanishManager.getOnlineVanishedPlayers();
            if (!online.isEmpty()) {
                message = TextUtils.color("&eList of all &aOnline &eVanished Players (&b%size%&e): ".replace("%size%", String.valueOf(online.size())));

                for (int i = 0; i < online.size(); i++) {
                    Player p = online.get(i);

                    message = message.append(PlayerManager.getRealDisplayName(p));

                    if (i < online.size() - 1) message = message.append(TextUtils.color("&8, "));
                }
            }

            List<UUID> offline = VanishManager.getOfflineVanishedPlayers();
            if (!offline.isEmpty()) {
                message = TextUtils.color("&eList of all &cOffline &eVanished Players (&b%size%&e): ".replace("%size%", String.valueOf(offline.size())));

                for (int i = 0; i < offline.size(); i++) {
                    UUID uuid = offline.get(i);

                    message = message.append(PlayerManager.getRealDisplayName(uuid));

                    if (i < offline.size() - 1) message = message.append(TextUtils.color("&8, "));
                }
            }

            if (TextUtils.serialize(message, TextUtils.Format.PLAIN).isEmpty()) message = TextUtils.color("&eThere are currently &b0 &eVanished Players.");

            sender.sendMessage(message);
            return;
        }

        if (args[0].equalsIgnoreCase("spawn")) {
            if (Utility.isConsole(source)) return;
            Player player = (Player) source.getSender();

            Configuration config = Main.getInstance().getConfig();
            World world = player.getWorld();
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            float yaw = player.getYaw();
            float pitch = player.getPitch();

            config.set("Spawn.world", world.getName());
            config.set("Spawn.x", x);
            config.set("Spawn.y", y);
            config.set("Spawn.z", z);
            config.set("Spawn.yaw", yaw);
            config.set("Spawn.pitch", pitch);

            Main.getInstance().saveConfig();

            player.sendMessage(TextUtils.color("&aSet spawn in " + player.getLocation().toString().replace("Location{", "").replace("}", "")));
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        List<String> arguments = new ArrayList<>();

        Utility.addCompletion(args, 1, arguments, "reload", "nickedplayers", "vanishedplayers", "spawn");

        return arguments;
    }

    @Override
    public @Nullable String permission() {
        return "batzal.use";
    }
}
