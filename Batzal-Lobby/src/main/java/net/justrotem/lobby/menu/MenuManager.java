package net.justrotem.lobby.menu;

import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class MenuManager {
    private static final HashMap<UUID, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();
    private static final Map<UUID, Menu> openMenu = new HashMap<>();
    private static boolean isSetup = false;

    public static void setup(Server server, JavaPlugin plugin) {
        server.getPluginManager().registerEvents(new MenuListener(), plugin);
        isSetup = true;
    }

    public static void openMenu(Class<? extends Menu> menuClass, Player player, Menu lastMenu) {
        try {
            Menu menu = menuClass.getConstructor(PlayerMenuUtility.class).newInstance(getPlayerMenuUtility(player.getUniqueId()));
            menu.open(lastMenu);

            openMenu.put(player.getUniqueId(), menu);
        } catch (Exception e) {
            Main.getInstance().getLogger().warning("An error has occurred while opening menu");
            e.printStackTrace();
        }
    }

    public static void updateAllOpenGUIs(Class<? extends Menu> type) {
        for (Map.Entry<UUID, Menu> entry : openMenu.entrySet()) {
            Menu menu = entry.getValue();

            if (type != menu.getClass()) continue;

            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) return;

            InventoryView view = player.getOpenInventory();
            Inventory top = view.getTopInventory();

            if (top.equals(menu.getInventory())) menu.reloadItems();
        }
    }

    public static PlayerMenuUtility getPlayerMenuUtility(UUID uuid) throws Exception {
        if (!isSetup) throw new Exception("Menus aren't setup in the server!");
        else if (!playerMenuUtilityMap.containsKey(uuid)) {
            PlayerMenuUtility playerMenuUtility = new PlayerMenuUtility(uuid);
            playerMenuUtilityMap.put(uuid, playerMenuUtility);
            return playerMenuUtility;
        }
        return playerMenuUtilityMap.get(uuid);
    }

    private static final Map<UUID, Map<String, Consumer<Player>>> ACTIONS = new HashMap<>();
    private static final AtomicInteger UNIQUE_ID = new AtomicInteger(0);

    public static void registerAction(UUID uuid, String key, Consumer<Player> action) {
        ACTIONS.computeIfAbsent(uuid, id -> new HashMap<>())
                .put(key.toLowerCase(), action);
    }

    public static Component clickable(Component label, UUID uuid, Component hoverText, Consumer<Player> action) {
        String actionKey = String.valueOf(UNIQUE_ID.getAndIncrement());
        registerAction(uuid, actionKey, action);

        return label.hoverEvent(hoverText)
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player p) action.accept(p);
                }, ClickCallback.Options.builder().uses(Integer.MAX_VALUE).build()));
    }

    public static Component clickable(String label, UUID uuid, String hoverText, Consumer<Player> action) {
        return clickable(TextUtility.color(label), uuid, TextUtility.color(hoverText), action);
    }
}

