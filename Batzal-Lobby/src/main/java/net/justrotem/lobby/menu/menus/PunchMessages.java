package net.justrotem.lobby.menu.menus;

import net.justrotem.data.data.PunchMessage;
import net.justrotem.data.util.TextUtility;
import net.justrotem.lobby.hooks.PlayerManager;
import net.justrotem.lobby.menu.Menu;
import net.justrotem.lobby.menu.MenuManager;
import net.justrotem.lobby.menu.PlayerMenuUtility;
import net.justrotem.lobby.utils.ItemUtility;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class PunchMessages extends Menu {

    public PunchMessages(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return "Punch Messages";
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
        if (event.getCurrentItem().equals(getBackItem())) {
            if (playerMenuUtility.peekMenu() != null) this.back();
            else MenuManager.openMenu(CustomizeAppearances.class, player, this);
            return;
        }

        if (event.getCurrentItem().equals(getResetItem())) {
            select(PunchMessage.NONE);
            close();
            return;
        }

        for (PunchMessage punchMessage : PunchMessage.values()) {
            if (event.getCurrentItem().equals(getPunchItem(punchMessage))) {
                if (event.isRightClick()) {
                    player.sendMessage(TextUtility.color("&7Preview: " + getPunchMessages(punchMessage).replace("%player%", PlayerManager.getLegacyDisplayName(player)).replace("%target%", PlayerManager.getLegacyDisplayName(player))));
                    player.sendMessage(
                            MenuManager.clickable(
                                    "&eClick here to go back to the Punch Messages menu!",
                                    player.getUniqueId(),
                                    "&aReturn to the Punch Messages menu",
                                    p -> MenuManager.openMenu(PunchMessages.class, player, this)
                            )
                    );
                } else select(punchMessage);

                close();
                break;
            }
        }
    }

    @Override
    public void setMenuItems() {
        inventory.setItem(10, getPunchItem(PunchMessage.Loving));
        inventory.setItem(11, getPunchItem(PunchMessage.Boop));
        inventory.setItem(12, getPunchItem(PunchMessage.Snowball));
        inventory.setItem(13, getPunchItem(PunchMessage.Glorious));
        inventory.setItem(14, getPunchItem(PunchMessage.Spooky));
        inventory.setItem(15, getPunchItem(PunchMessage.Fished));
        inventory.setItem(16, getPunchItem(PunchMessage.Code_Breaker));
        inventory.setItem(19, getPunchItem(PunchMessage.Solar));
        inventory.setItem(20, getPunchItem(PunchMessage.Celebratory));
        inventory.setItem(21, getPunchItem(PunchMessage.Rocket));

        inventory.setItem(48, getResetItem());
        inventory.setItem(49, getBackItem());
        inventory.setItem(50, ItemUtility.createItem(Material.WRITTEN_BOOK, "&aPunching Players", List.of("&7Punching staff members (&2[GM] &7and", "&c[ADMIN]&7) is a feature available to all", "&bMVP" + playerData.getRankColor().getColorCode() + "+ &7players.", "", "&7Players may also unlock the ability to", "&7punch their friends in lobbies as a", "&7reward in the Tournament Hall during", "&7active tournaments."), true));
    }

    private ItemStack getBackItem() {
        return ItemUtility.createItem(Material.ARROW, "&aGo Back", "&7To Customize Appearances");
    }

    private ItemStack getResetItem() {
        String selected = playerData.getPunchMessage().name();
        return ItemUtility.createItem(Material.BARRIER, "&cReset Punch Message", "&eCurrently Selected: " + (selected.equals("NONE") ? "&c" : "&a") + selected);
    }

    private void select(PunchMessage punchMessage) {
        if (punchMessage.equals(PunchMessage.NONE)) {
            playerData.setPunchMessage(PunchMessage.NONE);
            player.sendMessage(TextUtility.color("&cYour Punch Message has been reset."));
            return;
        }

        if (isSelected(punchMessage)) return;

        if (!hasPunchMessage(punchMessage)) {
            String notUnlocked = "";
            switch (punchMessage) {
                case Loving, Boop -> notUnlocked = "&cThis can be unlocked through &eRank Gifting Rewards&c!";
                case Snowball -> notUnlocked = "&cUnlocked in the 2020 Advent Calendar.";
                case Glorious -> notUnlocked = "&cUnlocked in the Tournament Hall!";
                case Spooky -> notUnlocked = "&cUnlocked in the Main Lobby Witch's Cauldron!";
                case Fished -> notUnlocked = "&cUnlocked through Seasonal Fishing Rewards!";
                case Code_Breaker -> notUnlocked = "&cUnknown!";
                case Solar -> notUnlocked = "&cUnlocked in the Event Shop!";
                case Celebratory -> notUnlocked = "&cUnlocked as an Anniversary Bingo reward!";
                case Rocket -> notUnlocked = "&cUnlocked in the Summer Bingo Cards!";
            }

            player.sendMessage(TextUtility.color(notUnlocked));
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 5, 0);
            return;
        }

        playerData.setPunchMessage(punchMessage);
        player.sendMessage(TextUtility.color("&aSet your Punch Message to &6" + getName(punchMessage) + " Punch Message"));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 5, -5);
    }

    private ItemStack getPunchItem(PunchMessage punchMessage) {
        ItemStack item = new ItemStack(Material.BARRIER);
        switch (punchMessage) {
            case Loving -> item = new ItemStack(Material.RED_DYE);
            case Boop -> item = new ItemStack(Material.PINK_DYE);
            case Snowball -> item = new ItemStack(Material.SNOWBALL);
            case Glorious -> item = new ItemStack(Material.GOLD_INGOT);
            case Spooky -> item = new ItemStack(Material.PUMPKIN_PIE);
            case Fished -> item = new ItemStack(Material.FISHING_ROD);
            case Code_Breaker -> item = new ItemStack(Material.NAME_TAG);
            case Solar -> item = new ItemStack(Material.SUNFLOWER);
            case Celebratory -> item = new ItemStack(Material.GOLD_BLOCK);
            case Rocket -> item = new ItemStack(Material.FIREWORK_ROCKET);
        }

        return ItemUtility.createItem(item, getItemName(punchMessage), getLore(punchMessage), isSelected(punchMessage));
    }

    private String getItemName(PunchMessage punchMessage) {
        return (hasPunchMessage(punchMessage) ? "&a" : "&c") + getName(punchMessage) + " Punch Message";
    }

    private String getName(PunchMessage punchMessage) {
        return punchMessage.name().replace("_", " ");
    }

    private List<String> getLore(PunchMessage punchMessage) {
        List<String> notUnlocked;
        if (isSelected(punchMessage)) notUnlocked = List.of("&aSelected!");
        else if (hasPunchMessage(punchMessage)) notUnlocked = List.of("&eClick to select!");
        else {
            switch (punchMessage) {
                case Loving, Boop ->
                        notUnlocked = List.of("&cThis can be unlocked through &eRank", "&eGifting Rewards&c!");
                case Snowball -> notUnlocked = List.of("&cUnlocked in the 2020 Advent", "&cCalendar.");
                case Glorious -> notUnlocked = List.of("&cUnlocked in the Tournament Hall!");
                case Spooky -> notUnlocked = List.of("&cUnlocked in the Main Lobby Witch's", "&cCauldron!");
                case Fished -> notUnlocked = List.of("&cUnlocked through Seasonal Fishing", "&cRewards!");
                case Code_Breaker -> notUnlocked = List.of("&cUnknown!");
                case Solar -> notUnlocked = List.of("&cUnlocked in the Event Shop!");
                case Celebratory -> notUnlocked = List.of("&cUnlocked as an Anniversary Bingo", "&creward!");
                case Rocket -> notUnlocked = List.of("&cUnlocked in the Summer Bingo Cards!", "&creward!");

                default -> notUnlocked = List.of("&eClick to select!");
            }
        }

        return notUnlocked.size() == 2 ? List.of("&8Punch Message", "", "&7Select the " + getName(punchMessage) + " Punch Message", "&7message for when you punch a staff", "&7or friend.", "", "&aRight-click to preview!", notUnlocked.get(0), notUnlocked.get(1)) : List.of("&8Punch Message", "", "&7Select the " + getName(punchMessage) + " Punch Message", "&7message for when you punch a staff", "&7or friend.", "", "&aRight-click to preview!", notUnlocked.get(0));
    }

    private boolean isSelected(PunchMessage punchMessage) {
        return playerData.getPunchMessage().equals(punchMessage);
    }

    private boolean hasPunchMessage(PunchMessage punchMessage) {
        return player.hasPermission("batzal.punch.messages." + punchMessage.name().toLowerCase());
    }

    public static String getPunchMessages(UUID uuid) {
        return getPunchMessages(PlayerManager.get(uuid).getPunchMessage());
    }

    private static String getPunchMessages(PunchMessage punchMessage) {
        String message;
        switch (punchMessage) {
            case Loving -> message = "%player% &7lovingly punched %target% &7into the sky!";
            case Boop -> message = "%player% &d&lbooped %target% &7into the sky!";
            case Snowball -> message = "%player% &f&lsnowballed %target% &7into the sky!";
            case Glorious -> message = "%player% &6gloriously &7punched %target% &7into the sky!";
            case Spooky -> message = "%player% &6&lspooked %target% &7into the sky!";
            case Fished -> message = "%player% &a&lfished %target% &7into the sky!";
            case Code_Breaker ->
                    message = "%player% &e&ka &c&lcode broke &e&ka %target% &7into the sky!";
            case Solar -> message = "%player% &e&llaunched %target% &7into the &e&lsun&7!";
            case Celebratory ->
                    message = "%player% &6&llaunched %target% &7into the sky in &6&la moment of celebration&7!";
            case Rocket -> message = "%player% &c&lrocketed %target% &7into the sky!";

            default -> message = "%player%&e has punched %target%&e into the sky!";
        }

        return message;
    }
}