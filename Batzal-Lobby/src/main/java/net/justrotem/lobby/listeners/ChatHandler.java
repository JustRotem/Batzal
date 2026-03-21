package net.justrotem.lobby.listeners;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.justrotem.data.utils.TextUtility;
import net.justrotem.lobby.Main;
import net.justrotem.lobby.hooks.LuckPermsManager;
import net.justrotem.lobby.hooks.PlayerManager;
import net.justrotem.lobby.menu.MenuManager;
import net.justrotem.lobby.menu.menus.Profile;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.utils.PlayerUtility;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ChatHandler implements Listener {

    private static FileConfiguration config;
    private static final List<Emoji> emojis = new ArrayList<>();
    private static final List<String> bannedWords = new ArrayList<>();

    public ChatHandler(JavaPlugin plugin) {
        // Save default replacements.yml if it doesn’t exist
        PlayerUtility.saveResource(plugin,"replacements.yml", false);

        // Load replacements.yml
        File file = new File(plugin.getDataFolder(), "replacements.yml");
        config = YamlConfiguration.loadConfiguration(file);

        loadEmojis();
        loadBannedWords();
    }

    public static void loadEmojis() {
        ConfigurationSection section = config.getConfigurationSection("emojis");
        if (section == null) return;

        for (String type : section.getKeys(false)) {
            ConfigurationSection typeSection = section.getConfigurationSection(type);
            if (typeSection == null) continue;

            for (String key : typeSection.getKeys(false)) {
                String text = typeSection.getString(key + ".text");
                String replacement = typeSection.getString(key + ".emoji");
                String permission = "batzal.emojis." + (Emoji.Type.valueOf(type) == Emoji.Type.Gifting ? key : "mvp++");

                try {
                    emojis.add(new Emoji(Emoji.Type.valueOf(type), text, replacement, permission));
                } catch (IllegalArgumentException e) {
                    Main.getInstance().getLogger().warning("Couldn't save Emoji: " + key + " type " + type);
                }
            }
        }
    }

    public static void loadBannedWords() {
        bannedWords.addAll(config.getStringList("banned-words"));
    }

    public static String checkEmojis(Player player, String message) {
        if (message == null || player == null) return "";

        for (Emoji emoji : emojis) {
            if (LuckPermsManager.hasPermission(player, emoji.permission()) && message.toLowerCase().contains(emoji.text().toLowerCase())) {
                message = message.replaceAll("(?i)" + emoji.text(), TextUtility.legacyToMiniMessage(emoji.replacement()));
            }
        }

        return message;
    }

    public static String checkBannedWords(Player player, String message) {
        if (message == null || player == null) return "";

        for (String word : bannedWords) {
            if (!LuckPermsManager.hasPermission(player, "batzal.chat.swear") && message.toLowerCase().contains(word.toLowerCase())) {
                message = message.replaceAll("(?i)" + word.toLowerCase(), "*".repeat(word.length()));
            }
        }

        return message;
    }

    public record Emoji(Type type, String text, String replacement, String permission) {
        enum Type {
            Default,
            Gifting
        }
    }

    public static class EmojiCommand implements BasicCommand {

        @Override
        public void execute(CommandSourceStack source, String[] args) {
            if (PlayerUtility.isConsole(source)) return;
            Player player = (Player) source.getSender();

            player.sendMessage(TextUtility.color("&aAvailable to &6MVP&c++&a:"));
            emojis.stream().filter(emoji -> emoji.type == Emoji.Type.Default).forEach(emoji -> player.sendMessage(TextUtility.color("&6" + emoji.text + "  &f-  " + emoji.replacement)));

            player.sendMessage(TextUtility.color("&aAvailable through Rank Gifting:"));
            emojis.stream().filter(emoji -> emoji.type == Emoji.Type.Gifting).forEach(emoji -> player.sendMessage(TextUtility.color("&6" + emoji.text + "  &f-  " + emoji.replacement)));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void chat(AsyncChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        event.setCancelled(true);

        // afk update

        // Toggle chat
        if (PlayerManager.isChatToggled(player.getUniqueId())) {
            player.sendMessage(TextUtility.color("&cYou have chat disabled! Enable it again with /togglechat"));
            return;
        }

        // Same message
        if (PlayerManager.isSameMessage(player.getUniqueId(), event.message())) {
            player.sendMessage(TextUtility.color("&6&m---------------------------------------------\n&r&cYou cannot say the same message twice!\n&6&m---------------------------------------------"));
            return;
        }

        // Advertising - should add a check for how many times and maybe adding mute/ban feature.
        if (PlayerManager.isAdvertising(player.getUniqueId(), event.message())) {
            player.sendMessage(TextUtility.color("&6&m---------------------------------------------\n&r&cAdvertising is against the rules. You will be permanently\n&c banned from the server if you attempt to advertise.\n&6&m---------------------------------------------"));
            return;
        }

        // Save the message as the @player last message
        PlayerManager.setLastMessage(player.getUniqueId(), event.originalMessage());

        // Prefix
        Component message;
        if (LuckPermsManager.isHooked()) {
            String rank;
            if (NickManager.isLobbyNicked(player)) rank = NickManager.getRank(player);
            else rank = LuckPermsManager.getPrimaryGroup(player.getUniqueId());

            String name = PlayerManager.getLegacyDisplayName(player);
            message = TextUtility.color("&" + (rank.equalsIgnoreCase("default") ? '7' : 'f'))
                    .append(MenuManager.clickable(name, player.getUniqueId(), "&eClick to see " + name + "&e's profile!", p -> {
                        if (p != player) Profile.setTarget(p.getUniqueId(), player.getUniqueId());
                        MenuManager.openMenu(Profile.class, p, null);
                    })).append(TextUtility.color(": "));
        } else message = Component.text("<", NamedTextColor.WHITE).append(player.displayName(), Component.text("> "));

        String text = TextUtility.escapeTags(TextUtility.getText(event.message()));
        text = checkBannedWords(player, text);
        text = checkEmojis(player, text); // has legacy color codes

        // Checks if mentioning a Player
        String mention = text;
        List<Player> mentionedPlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(player)) continue;

            String name = NickManager.isLobbyNicked(p) ? NickManager.getNickName(p) : p.getName();

            if (mention.toLowerCase().contains(name.toLowerCase())) {
                mention = mention.replaceAll("(?i)" + Pattern.quote(name), "<yellow>" + name + "</yellow>");
                mentionedPlayers.add(p);
            }
        }

        // For mentioned players only
        Component mentionMessage = player.hasPermission("batzal.chat.colors")
                ? TextUtility.color(mention)
                : MiniMessage.miniMessage().deserialize(mention);

        // For non-mentioned players
        Component plainMessage = player.hasPermission("batzal.chat.colors")
                ? TextUtility.color(text)
                : MiniMessage.miniMessage().deserialize(text);

        for (Audience viewer : event.viewers()) {
            if (viewer instanceof Player p && mentionedPlayers.contains(p)) {
                event.message(mentionMessage);
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            }
            else event.message(plainMessage);

            viewer.sendMessage(message.append(event.message()));
        }
    }
}
