package net.justrotem.lobby;

import net.justrotem.data.utils.CooldownType;
import net.justrotem.lobby.commands.*;
import net.justrotem.lobby.hooks.NickExpansion;
import net.justrotem.lobby.listeners.ActionBarManager;
import net.justrotem.lobby.listeners.ChatHandler;
import net.justrotem.lobby.listeners.EventListeners;
import net.justrotem.lobby.menu.MenuCommands;
import net.justrotem.lobby.menu.MenuManager;
import net.justrotem.lobby.nick.NamesConfig;
import net.justrotem.lobby.nick.NickCommand;
import net.justrotem.lobby.nick.gui.BookManager;
import net.justrotem.lobby.ride.RideCommand;
import net.justrotem.lobby.ride.listener.EntitiesLoadListener;
import net.justrotem.lobby.utils.ExperienceManager;
import net.justrotem.lobby.utils.LobbyManager;
import net.justrotem.lobby.utils.PlayerUtility;
import net.justrotem.lobby.utils.WorldResetService;
import net.justrotem.lobby.vanish.VanishCommand;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

    @Override
    public void onLoad() {
        instance = this;

        // Saves config.yml if it doesn't exist
        getLogger().info("Loading configuration!");
        saveDefaultConfig();

        WorldResetService.resetWorldOnLoad(this);
    }

    @Override
    public void onEnable() {
        Logger logger = Logger.getLogger("");
        logger.info("".repeat(5) + " __       ___ ___            ");
        logger.info("".repeat(5) + "|  )  /\\   |   /   /\\  |   ");
        logger.info("".repeat(5) + "|__) /--\\  |  /__ /--\\ |___");
        logger.info("".repeat(5));

        PlayerUtility.initialize(this);

        // Initialize NameConfig
        NamesConfig.initialize(this);

        // Starting the Nick & Vanish actionbar
        ActionBarManager.startActionBarTask();

        // Initialize PlaceholderAPI safely
        NickExpansion.init(this);

        // Initialize Nick book gui
        BookManager.init();

        MenuManager.setup(getServer(), this);

        LobbyManager.initialize(this);

        // Registering Commands
        registerCommand("batzal", new BatzalCommand());
        registerCommand("nick", new NickCommand());
        registerCommand("unnick", new NickCommand.UnNickCommand());
        registerCommand("vanish", List.of("v"), new VanishCommand());
        registerCommand("togglechat", new ToggleChatCommand());
        registerCommand("togglepunch", new TogglePunchCommand());
        registerCommand("zoo", new ZooCommand());
        registerCommand("whatdoyoudo", new WhatDoYouDoCommand());
        registerCommand("ping", new PingCommand());
        registerCommand("build", List.of("b"), new BuildCommand());
        registerCommand("fly", new FlyCommand());
        registerCommand("fw", new FireworkCommand());
        registerCommand("leavemealone", new LeaveMeAloneCommand());
        registerCommand("ride", new RideCommand());
        registerCommand("warmode", new WarCommand());
        registerCommand("lightningstick", new LightningStickCommand());
        registerCommand("kaboom", new KaboomCommand());
        registerCommand("top", new TopCommand());
        registerCommand("fireball", new FireballCommand());
        registerCommand("god", new GodCommand());
        registerCommand("sudo", new SudoCommand());
        registerCommand("smite", new SmiteCommand());
        registerCommand("list", List.of("online"), new ListCommand());
        registerCommand("heal", new HealCommand());
        registerCommand("hat", new HatCommand());
        registerCommand("stuck", new StuckCommand());
        registerCommand("speed", new SpeedCommand());
        registerCommand("give", new GiveCommand());
        registerCommand("level", new ExperienceManager.LevelCommand("level"));
        registerCommand("xp", new ExperienceManager.LevelCommand("xp"));
        registerCommand("gamemode", List.of("gm"), new GamemodeCommand(null));
        registerCommand("gmc", new GamemodeCommand(GameMode.CREATIVE));
        registerCommand("gms", new GamemodeCommand(GameMode.SURVIVAL));
        registerCommand("gmsp", new GamemodeCommand(GameMode.SPECTATOR));
        registerCommand("gma", new GamemodeCommand(GameMode.ADVENTURE));
        registerCommand("emoji", List.of("emojihelp", "emojis", "emotes"), new ChatHandler.EmojiCommand());
        registerCommand("teleport", List.of("tp"), new TeleportCommand());
        registerCommand("profile", new MenuCommands("profile"));
        registerCommand("openpunchmessagemenu", new MenuCommands("openpunchmessagemenu"));
        registerCommand("rewards", new MenuCommands("rewards"));
        registerCommand("rankcolor", List.of("rankcolour"), new MenuCommands("rankcolor"));

        getServer().getPluginManager().registerEvents(new EventListeners(), this);
        getServer().getPluginManager().registerEvents(new ChatHandler(this), this);
        EntitiesLoadListener.initialize(this);

        Bukkit.getWorlds().forEach(world -> world.setTime(1000));

        getLogger().info("Enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Starting shutdown process..");

        PlayerUtility.shutdown(this);

        getLogger().info("Disabled successfully :)");
    }

    private static Main instance;

    public static JavaPlugin getInstance() {
        return instance;
    }

    public enum ToggleCategory {
        LeaveMeAlone,
        God
    }

    public enum CooldownCategory implements CooldownType {
        FireWork("batzal.firework.bypass"),
        WarMode(""),
        Punch("batzal.punch.bypass"),
        Visibility("batzal.visibility.bypass");

        private final String permission;

        CooldownCategory(String permission) {
            this.permission = permission;
        }

        public String getPermission() {
            return permission;
        }
    }
}
