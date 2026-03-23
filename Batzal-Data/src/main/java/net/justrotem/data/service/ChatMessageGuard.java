package net.justrotem.data.service;

import net.justrotem.data.cache.PlayerCache;
import net.justrotem.data.integration.luckperms.LuckPermsService;
import net.justrotem.data.model.PlayerData;
import net.justrotem.data.util.TextFormatter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ChatMessageGuard {

    private ChatMessageGuard() {
    }

    public static boolean isChatToggled(@NotNull UUID uuid) {
        return PlayerLookupService.get(uuid)
                .map(PlayerData::isToggleChat)
                .orElse(false);
    }

    /**
     * Sync cached-only.
     * טוב כשאתה יודע שהשחקן כבר טעון ב-LuckPerms.
     */
    public static boolean isSameMessage(@NotNull UUID uuid, Component message) {
        if (LuckPermsService.hasPermission(uuid, "batzal.chat.samemessage")) {
            return false;
        }

        return isSameMessageInternal(uuid, message);
    }

    /**
     * Async מדויק.
     * טוב אם אתה לא בטוח שה-user כבר טעון ב-LuckPerms.
     */
    public static CompletableFuture<Boolean> isSameMessageAsync(@NotNull UUID uuid, Component message) {
        return LuckPermsService.hasPermissionAsync(uuid, "batzal.chat.samemessage")
                .thenApply(hasBypass -> {
                    if (hasBypass) {
                        return false;
                    }

                    return isSameMessageInternal(uuid, message);
                });
    }

    private static boolean isSameMessageInternal(@NotNull UUID uuid, Component message) {
        Component lastMessage = PlayerCache.getLastMessage(uuid);
        if (lastMessage == null) {
            setLastMessage(uuid, message);
            return false;
        }

        String current = TextFormatter.getText(message);
        String last = TextFormatter.getText(lastMessage);
        return current.equals(last);
    }

    public static void setLastMessage(UUID uuid, Component message) {
        PlayerCache.setLastMessage(uuid, message);
    }

    /**
     * Sync cached-only.
     */
    public static boolean isAdvertising(@NotNull UUID uuid, Component message) {
        if (LuckPermsService.hasPermission(uuid, "batzal.chat.advertisement")) {
            return false;
        }

        return isAdvertisingInternal(message);
    }

    /**
     * Async מדויק.
     */
    public static CompletableFuture<Boolean> isAdvertisingAsync(@NotNull UUID uuid, Component message) {
        return LuckPermsService.hasPermissionAsync(uuid, "batzal.chat.advertisement")
                .thenApply(hasBypass -> {
                    if (hasBypass) {
                        return false;
                    }

                    return isAdvertisingInternal(message);
                });
    }

    private static boolean isAdvertisingInternal(Component message) {
        String text = TextFormatter.getText(message).toLowerCase(Locale.ROOT);
        return text.contains("http") || text.contains("www.");
    }
}