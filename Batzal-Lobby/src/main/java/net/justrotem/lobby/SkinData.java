package net.justrotem.data.bukkit;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Random;

public class SkinData {

    public static SkinData create(String name, String value, String signature, boolean head) {
        return new SkinData(name, value, signature, head);
    }

    public static SkinData DEFAULT = new Random().nextBoolean() ? new SkinData(
            "Steve",
            "ewogICJ0aW1lc3RhbXAiIDogMTY2NjMwMjkxNzkzOCwKICAicHJvZmlsZUlkIiA6ICJjOWRlZTM4MDUzYjg0YzI5YjZlZjA5YjJlMDM5OTc0ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJTQVJfRGVjZW1iZXI1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzMxZjQ3N2ViMWE3YmVlZTYzMWMyY2E2NGQwNmY4ZjY4ZmE5M2EzMzg2ZDA0NDUyYWIyN2Y0M2FjZGYxYjYwY2IiCiAgICB9CiAgfQp9",
            "tTCtASRIyuzlNUgSoXgUr6arxABhCR4EQ9+eHaUoO8bADljmUFoQfb6oba8zqe2gIa2mnu5KQaOPQCxcTDjgNv9aIL2smINKxy/60VE4Mgnrh5ntH+mGuDi00V3Bk2CsObFZXz1vgk2UxdQUQ41eVQYm2xBrXFEbXMSoTafWGv0FMTPFpGxGRdduTe3QTEie3GcfAMHCn/9xMMmUxZZ6UVZ+mDe8ARt9/cmK+GmqT8m3kmrz/vq+i29KV4tWvJqsKIVAXm97jVPH9XxVR3tYlheimQSFNrCU8SzNPum/ZhxNAf5Uw90+/K0eaJE59y8tS7KDV5DHrRrHHXb/ywGGklSri1YjFm9AEBk6BeH8Y3Ot/e+zfQbF3rOny2DkBAm/v28FooYd25gXB4MjUFNPj3KdveQh7DpRAvnkmBZMqJCO+Z9fdY4Dw+jmqjII88r6mukWAODvXed/x8bvv55zzNOAxtqtwBTWHIdqWFr/7pMZF26RY1Tluw+pAWGWaKMHtqlGzyOLGMxMKwXqtLNEpIYw52ETwGKaWh8h34cOoI8dhpjfjym4UOihMmazK9LC0EUEHuBlgy5b/Ae71+6UsLNIX8bJwIvN16sP6wpSTNbV6htWoS7/ehvoxdKhI6XEUqWgEoAwmquClPfWiveCV057reoKeVHB9RdTl0sW+HM=",
            false
    ) : new SkinData(
            "Alex",
            "ewogICJ0aW1lc3RhbXAiIDogMTY2NjMwNzU4MDMyOSwKICAicHJvZmlsZUlkIiA6ICI0ZGEzNDFmNjgyOTg0YTgwYjE2MTIzMGUxNmJmZWRjNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJFZnJlZXRTUCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80NmFjZDA2ZTg0ODNiMTc2ZThlYTM5ZmMxMmZlMTA1ZWIzYTJhNDk3MGY1MTAwMDU3ZTlkODRkNGI2MGJkZmE3IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
            "xDoJ1XDQaiJtMPfagD/HodwiiuFbo60JHAA/ATSG6brzyHrk4N5k0U4eGFPOge/4XHLUCalQq49OPHKzVsYUvmkuoiTVDMSQnz5FUhjrHTVoL0hS1VJ5Zy8heF7bC74aTo4SCIcZzr2FiTBwkGEOoiXMWHFz8kJT5XcgNFgWsemaz5DqZuIklIlR5ZKh9FXoOLGsJaMR3Nn0c159Ud4y5cJMDqTR1N5LpSdApJWqkerlc15ioc78CFsGwAY5ObpTanIIKJvVPuWt7fdGez/ErG4BmaHaoXwkWy99+BVAroH/ajMICI32Bcrmy99x1oIjWx0M7y0YWKQGIpiO4UF8blImZlEvKXJvIr3nI5M3+22el0JTtei0Qv1JqxWfrc3oX10Io9pc5OWtsxQ51U4DXaqKB6wUpxGH3sAIeBxrVu0G3HoEP4J8Xzsqndo0+6hQkuzTcA2S55n3b6IRXU8MqBBXpw3ul8qjjNIahvfK1J6FGd1VcUny5jU7wUt5/16WY27otWEH37sd/F3p1wyoGXiuaRrc3xHum7LKU/lIiQPhUF9wlqziTchQ9qTFFEIhsg8MUPEIntshP3XrvpezIWe4qXazcJTuBS2xWU9G4/CP8yfkKU0iY68Xq0ryr/wk3yd4ZIy0jT1vRoz6ujsPSA/csWhoEYHZrN7uz1Waex0=",
            false
    );

    private boolean dirty;
    private final String name, value, signature;
    private final boolean head;

    protected SkinData(String name, String value, String signature, boolean head) {
        this.name = name;
        this.dirty = true;
        this.value = value;
        this.signature = signature;
        this.head = head;
    }

    public String getName() {
        return this.name;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public String getValue() {
        return this.value;
    }

    public String getSignature() {
        return this.signature;
    }

    public boolean isHead() {
        return this.head;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean equals(SkinData skinData) {
        return this.name.equals(skinData.name) && this.value.equals(skinData.value) && this.signature.equals(skinData.signature) && this.head == skinData.head;
    }

    public PlayerProfile getProfile() {
        return getProfile(null);
    }

    public PlayerProfile getProfile(Player player) {
        return update(player != null ? player.getPlayerProfile() : Bukkit.createProfile(this.name));
    }

    private PlayerProfile update(PlayerProfile profile) {
        profile.clearProperties();

        profile.setProperty(
                new ProfileProperty(
                    "textures",
                    this.value,
                    this.signature
                )
        );

        this.dirty = true;
        return profile;
    }
}
