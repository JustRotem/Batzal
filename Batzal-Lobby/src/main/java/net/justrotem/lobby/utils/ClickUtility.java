package net.justrotem.data.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TextUtility {

    private static final Map<UUID, Map<String, Runnable>> ACTIONS = new HashMap<>();
    private static final AtomicInteger UNIQUE_ID = new AtomicInteger(0);

    public static void registerAction(UUID uuid, String key, Runnable action) {
        ACTIONS.computeIfAbsent(uuid, id -> new HashMap<>())
                .put(key.toLowerCase(), action);
    }

    public static Component clickable(Component label, UUID uuid, Component hoverText, Runnable action) {
        String actionKey = String.valueOf(UNIQUE_ID.getAndIncrement());
        registerAction(uuid, actionKey, action);

        return label.hoverEvent(hoverText)
                .clickEvent(ClickEvent.callback((audience) -> {
                    if (audience instanceof Player p && p.getUniqueId().equals(uuid)) {
                        action.run();
                    }
                }));
    }

    public static Component clickable(String label, UUID uuid, String hoverText, Runnable action) {
        return clickable(TextUtility.color(label), uuid, TextUtility.color(hoverText), action);
    }
}
