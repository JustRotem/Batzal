package net.justrotem.lobby.nick.gui;

import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.nick.gui.pages.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class BookManager {

    private static final Map<String, BookGUI> GUIS = new HashMap<>();
    private static final Map<UUID, Map<String, Runnable>> ACTIONS = new HashMap<>();
    private static final AtomicInteger UNIQUE_ID = new AtomicInteger(0);

    // Stores extra data for each player's book (keyed by book id)
    private static final Map<UUID, Map<String, String>> BOOK_DATA = new HashMap<>();

    private BookManager() {} // prevent instantiation

    public static void registerBook(String id, BookGUI gui) {
        GUIS.put(id.toLowerCase(), gui);
    }

    public static void openBook(Player player, String id) {
        BookGUI gui = GUIS.get(id.toLowerCase());
        if (gui == null) {
            player.sendMessage(TextUtility.color("&cBook GUI not found: " + id));
            return;
        }

        gui.open(player);
    }

    /**
     * Store a string value for a player's book without opening it.
     *
     * @param player the player
     * @param id the book ID
     * @param data the string data to store
     */
    public static void setBookData(Player player, String id, String data) {
        BOOK_DATA.computeIfAbsent(player.getUniqueId(), uuid -> new HashMap<>())
                .put(id.toLowerCase(), data);
    }

    public static String getBookData(Player player, String id) {
        Map<String, String> playerData = BOOK_DATA.get(player.getUniqueId());
        if (playerData == null) return null;
        return playerData.get(id.toLowerCase());
    }

    public static void openBook(Player player, Component title, Component... pages) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.title(title);
        meta.author(TextUtility.color("NickName Main"));
        meta.addPages(pages);
        book.setItemMeta(meta);
        player.openBook(book);
    }

    public static void registerAction(Player player, String key, Runnable action) {
        ACTIONS.computeIfAbsent(player.getUniqueId(), id -> new HashMap<>())
                .put(key.toLowerCase(), action);
    }

    public static Component clickable(Component label, Player player, Component hoverText, Runnable action) {
        String actionKey = String.valueOf(UNIQUE_ID.getAndIncrement());
        registerAction(player, actionKey, action);

        return label.hoverEvent(hoverText)
                .clickEvent(ClickEvent.callback((audience) -> {
                    if (audience instanceof Player p && p.getUniqueId().equals(player.getUniqueId())) {
                        action.run();
                    }
                }));
    }

    public static Component clickable(String label, Player player, String hoverText, Runnable action) {
        return clickable(TextUtility.color(label), player, TextUtility.color(hoverText), action);
    }

    public static void init() {
        registerBook("nick", new FirstPage());
        registerBook("rank", new RankPage());
        registerBook("skin", new SkinPage());
        registerBook("name", new NamePage());
        registerBook("randomname", new RandomNamePage());
        registerBook("notallowed", new NotAllowedPage());
        registerBook("finished", new FinishedPage());
    }
}