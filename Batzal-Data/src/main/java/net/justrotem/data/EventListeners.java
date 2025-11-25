package net.justrotem.data;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListeners implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerManager.registerPlayer(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerManager.savePlayer(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // afk update

        // Toggle chat
        if (PlayerManager.isChatToggled(player)) {
            event.setCancelled(true);
            player.sendMessage(color("&cYou have chat disabled! Enable it again with /togglechat"));
            return;
        }

        // Same message
        if (PlayerManager.isSameMessage(player, event.message())) {
            event.setCancelled(true);
            player.sendMessage(color("&6&m---------------------------------------------\n&r&cYou cannot say the same message twice!\n&6&m---------------------------------------------"));
            return;
        }

        // Advertising - should add a check for how many times and maybe adding mute/ban feature.
        if (PlayerManager.isAdvertising(player, event.message())) {
            event.setCancelled(true);
            player.sendMessage(color("&6&m---------------------------------------------\n&r&cAdvertising is against the rules. You will be permanently\n&c banned from the server if you attempt to advertise.\n&6&m---------------------------------------------"));
            return;
        }

        // Save the message as the @player last message
        PlayerManager.setLastMessage(player, event.originalMessage());
    }

    private static Component color(String text) {
        final MiniMessage mm = MiniMessage.miniMessage();

        // Convert & codes to MiniMessage-compatible tags
        String replaced = text
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&l", "<bold>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&m", "<strikethrough>")
                .replace("&r", "<reset>");
        return mm.deserialize(replaced);
    }
}
