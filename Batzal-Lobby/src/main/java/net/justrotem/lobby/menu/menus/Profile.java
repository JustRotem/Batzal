package net.justrotem.lobby.menu.menus;

import net.justrotem.data.player.PlayerData;
import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.hooks.LuckPermsManager;
import net.justrotem.lobby.hooks.PlayerManager;
import net.justrotem.lobby.menu.Menu;
import net.justrotem.lobby.menu.MenuManager;
import net.justrotem.lobby.menu.PlayerMenuUtility;
import net.justrotem.lobby.utils.ExperienceManager;
import net.justrotem.lobby.utils.ItemUtility;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Profile extends Menu {

    public Profile(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);

        PlayerData data = getTarget(player.getUniqueId());
        if (data != null && !data.getUniqueId().equals(player.getUniqueId())) this.playerData = data;
    }

    @Override
    public String getMenuName() {
        return isSelf() ? "My Profile" : playerData.getName() + "'s Profile";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public boolean cancelAllClicks() {
        return false;
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        if (event.getSlot() == 22) {
            close();
            player.sendMessage(TextUtility.color("&eRanks, SkyBlock Gems, Network Boosters, and more: ").append(TextUtility.color("&bhttps://rotem-laufer.com").hoverEvent(TextUtility.color("&eClick to open the store!")).clickEvent(ClickEvent.openUrl("https://rotem-laufer.com"))));
            return;
        }

        if (event.getSlot() == 29) {
            MenuManager.openMenu(CustomizeAppearances.class, player,this);
            return;
        }

        if (event.getSlot() == 31) {
            MenuManager.openMenu(Rewards.class, player,this);
        }
    }

    @Override
    public void setMenuItems() {
        if (isSelf()) {
            inventory.setItem(2, ItemUtility.createPlayerHead(player, PlayerManager.getLegacyRealDisplayName(playerData.getUniqueId()), List.of("&7Network Level: &6" + ExperienceManager.getLevel(player.getUniqueId()), "&7Achievement Points: &e0", "&7Guild: &bNone")));
            inventory.setItem(3, ItemUtility.createCustomHead("Friends", "&aFriends", List.of("&7View your Network friends' profiles,", "&7and interact with your online friends!")));
            inventory.setItem(4, ItemUtility.createCustomHead("Party", "&aParty", List.of("&7Create a party and join up with", "&7other players to play games", "&7together!")));
            inventory.setItem(5, ItemUtility.createCustomHead("Guild", "&aGuild", List.of("&7Form a guild with other the Network", "&7players to conquer game modes and", "&7work towards common the Network", "&7rewards.")));
            inventory.setItem(6, ItemUtility.createCustomHead("Recent-Players", "&aRecent Players", List.of("&7View players you have player recent", "&7games with.")));

            for (int i = 0; i < 9; i++) {
                inventory.setItem(i + 9, ItemUtility.createItem(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE, 1), "&7", null, false));
            }

            inventory.setItem(20, ItemUtility.createItem(Material.DARK_OAK_DOOR, "&aGo to Housing", null, false));
            inventory.setItem(21, ItemUtility.createCustomHead("Social-Media", "&aSocial Media", List.of("&7Click to edit your Social Media links.")));
            inventory.setItem(22, ItemUtility.createPlayerHead(player, "&aCharacter Information", List.of("&7Rank: " + LuckPermsManager.getLegacyGroupDisplayName(playerData.getUniqueId()), "&7Network Level: &6" + ExperienceManager.getLevel(player.getUniqueId()), "&7Experience until next Level: &60", "&7Achievement Points: &e0", "&7Quests Completed: &60", "&7Karma: &d0", "&7Network Gold: &60", "", "&eClick to see the Network Store link.")));
            inventory.setItem(23, ItemUtility.createItem(Material.PAPER, "&aStats Viewer", List.of("&7Showcases your stats for each", "&7game and an overview of all.", "", "&7Players ranked &bMVP &7or higher", "&7can use &f/stats (username) &7to view", "&7other players' stats."), false));
            inventory.setItem(24, ItemUtility.createItem(Material.POTION, "&aCoin Boosters", List.of("&7Activate your personal and", "&7network boosters for extra", "&7coins.", "", "&eClick to activate boosters!"), true));

            inventory.setItem(29, ItemUtility.createItem(ItemUtility.createLeatherArmor(Material.LEATHER_CHESTPLATE, Color.fromRGB(38, 38, 166)), "&aCustomize Appearances", List.of("", "&7Customize the following visual options", "&7for your player!", "&f ・ MVP+ Rank Color", "&f ・ Punch Messages", "&f ・ Glow", "&f ・ Status", "", "&eClick to view!"), false));

            inventory.setItem(30, ItemUtility.createItem(Material.DIAMOND, "&aAchievements", List.of("&7Track your progress as you unlock", "&7Achievements and rack up points.", "", "&eClick to view your achievements!"), false));

            inventory.setItem(31, getLevelingItem(player));

            inventory.setItem(32, ItemUtility.createItem(Material.ENCHANTED_BOOK, "&aQuests & Challenges", List.of("&7Competing quests and challenges", "&7will reward you with &6Coins77, &3Network", "&3Experdience &7and more!", "", "&7You can complete a maximum of &a10", "&7challenges every day.", "", "&7Challenges completed today: &a0", "", "&eClick to view Quests & Challenges"), false));
            inventory.setItem(33, ItemUtility.createItem(Material.COMPARATOR, "&aSettings & Visibility", List.of("&7Allows you to edit and control", "&7various personal settings.", "", "&eClick to edit your settings!"), false));
            inventory.setItem(39, ItemUtility.createItem(Material.BOOK, "&aRecent Games", List.of("&7View your recently played games.", "", "&eClick to view!"), false));
            inventory.setItem(40, ItemUtility.createItem(Material.ANVIL, "&aAccount Status", List.of("&7Check your punishment history and", "&7see where you stand.", "", "&eClick to view!"), false));
            inventory.setItem(41, ItemUtility.createCustomHead("Language", "&aSelect Language", List.of("&7Change your language.", "", "&7Currently available:", "  &7&l・ &fEnglish", "", "&7More languages coming soon!", "", "&eClick to change your language!")));
            inventory.setItem(49, ItemUtility.createItem(Material.GOLD_INGOT, "&aNetwork Store", List.of("&7View the Network Store from right", "&7here in-game!", "", "&7Your Network Gold: &60", "", "&eClick to view!"), true));
        } else {
            inventory.setItem(0, ItemUtility.createPlayerHead(player, PlayerManager.getLegacyRealDisplayName(playerData.getUniqueId()), List.of("&7Network Level: &6" + ExperienceManager.getLevel(playerData.getUniqueId()), "&7Achievement Points: &e0", "&7Guild: &bNone", "", "&7Online Status: &b" + playerData.getStatus().name())));

            for (int i = 0; i < 9; i++) {
                inventory.setItem(i + 9, ItemUtility.createItem(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE, 1), "&7", null, false));
            }
        }
    }

    private boolean isSelf() {
        return playerData.getUniqueId().equals(player.getUniqueId());
    }

    public static ItemStack getLevelingItem(Player player) {
        return ItemUtility.createItem(Material.BREWING_STAND, "&aNetwork Leveling", List.of("&7Playing games and completing quests", "&7will reward you with &3Network Experience&7,", "&7which is required  to level up and", "&7acquire new perks and rewards!", "", "&3Network Level &a" + player.getLevel() + " " + "&3╏".repeat(ExperienceManager.getCurrentLevelPercentage(player, 40)) + "&7╏".repeat(ExperienceManager.getCurrentLevelUpPercentage(player, 40)) + "&3 " + ExperienceManager.getCurrentLevelPercentageAsText(player) + "%", "", "&7Experience until next level: &3" + String.format("%,d", ExperienceManager.getExperienceToLevelUp(player)), "", "&eClick to see your rewards!"), false);
    }

    private static final HashMap<UUID, PlayerData> dataMap = new HashMap<>();

    public static void setTarget(UUID uuid, PlayerData playerData) {
        dataMap.put(uuid, playerData);
    }

    public static void setTarget(UUID uuid, UUID targetUUID) {
        setTarget(uuid, PlayerManager.get(targetUUID));
    }

    public static void setTarget(UUID uuid) {
        dataMap.put(uuid, PlayerManager.get(uuid));
    }

    public static void removeTarget(UUID uuid) {
        dataMap.remove(uuid);
    }

    public static PlayerData getTarget(UUID uuid) {
        if (!dataMap.containsKey(uuid)) return null;

        return dataMap.get(uuid);
    }
}
