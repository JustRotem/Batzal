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

/**
 * Utility class for validating and filtering player chat messages.
 *
 * <p>This class provides:
 * <ul>
 *     <li>Chat toggle checks</li>
 *     <li>Duplicate message detection</li>
 *     <li>Advertisement detection</li>
 * </ul>
 * </p>
 *
 * <p>Includes both sync (cached-only) and async (accurate) variants
 * depending on whether LuckPerms data is guaranteed to be loaded.</p>
 */
public final class ChatMessageGuard {

    private ChatMessageGuard() {
    }

    /**
     * Checks whether a player has chat enabled.
     *
     * @param uuid player UUID
     * @return true if chat is enabled
     */
    public static boolean isChatToggled(@NotNull UUID uuid) {
        return PlayerLookupService.get(uuid)
                .map(PlayerData::isToggleChat)
                .orElse(false);
    }

    /**
     * Checks if the current message is identical to the last sent message.
     *
     * <p>Sync, cache-only version. Assumes LuckPerms data is already loaded.</p>
     *
     * @param uuid player UUID
     * @param message current message
     * @return true if message is identical to previous one
     */
    public static boolean isSameMessage(@NotNull UUID uuid, Component message) {
        if (LuckPermsService.hasPermission(uuid, "batzal.chat.samemessage")) {
            return false;
        }

        return isSameMessageInternal(uuid, message);
    }

    /**
     * Checks if the current message is identical to the last sent message (async).
     *
     * <p>This version ensures permission accuracy by loading LuckPerms data if needed.</p>
     *
     * @param uuid player UUID
     * @param message current message
     * @return future containing result
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

    /**
     * Internal duplicate-message check without permission handling.
     *
     * <p>If no previous message exists for the player, the current message is stored
     * and this method returns {@code false}.</p>
     *
     * @param uuid player UUID
     * @param message current message
     * @return true if the current message matches the previously stored message
     */
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

    /**
     * Stores the last message sent by a player.
     *
     * @param uuid player UUID
     * @param message message component
     */
    public static void setLastMessage(UUID uuid, Component message) {
        PlayerCache.setLastMessage(uuid, message);
    }

    /**
     * Checks whether a message contains advertising content.
     *
     * <p>Sync, cache-only version.</p>
     *
     * @param uuid player UUID
     * @param message message
     * @return true if advertising detected
     */
    public static boolean isAdvertising(@NotNull UUID uuid, Component message) {
        if (LuckPermsService.hasPermission(uuid, "batzal.chat.advertisement")) {
            return false;
        }

        return isAdvertisingInternal(message);
    }

    /**
     * Checks whether a message contains advertising content (async).
     *
     * @param uuid player UUID
     * @param message message
     * @return future containing result
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

    /**
     * Internal advertisement detection without permission handling.
     *
     * <p>This performs a simple plain-text check for common link patterns such as
     * {@code http} and {@code www.}.</p>
     *
     * @param message message component
     * @return true if advertising-like content is detected
     */
    private static boolean isAdvertisingInternal(Component message) {
        String text = TextFormatter.getText(message).toLowerCase(Locale.ROOT);
        return text.contains("http") || text.contains("www.");
    }
}