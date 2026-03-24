package net.justrotem.data.model;

/**
 * Represents a cooldown category that may define a bypass permission.
 *
 * <p>Typically implemented by enums used with the cooldown system.</p>
 */
public interface CooldownType {

    /**
     * Returns the permission required to bypass this cooldown.
     *
     * <p>If null or empty, no bypass is applied.</p>
     *
     * @return permission node or null
     */
    String getPermission();
}