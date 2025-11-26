package net.justrotem.lobby;

import com.mojang.serialization.Lifecycle;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.justrotem.data.utils.CooldownType;
import net.justrotem.lobby.commands.*;
import net.justrotem.lobby.hooks.NickExpansion;
import net.justrotem.lobby.nick.NickCommand;
import net.justrotem.lobby.nick.NickConfig;
import net.justrotem.lobby.nick.NickManager;
import net.justrotem.lobby.nick.gui.BookManager;
import net.justrotem.lobby.ride.RideCommand;
import net.justrotem.lobby.ride.listener.EntitiesLoadListener;
import net.justrotem.lobby.utils.Utility;
import net.justrotem.lobby.vanish.VanishCommand;
import net.minecraft.commands.Commands;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    @Override
    public void onLoad() {
        instance = this;

        // Saves config.yml if it doesn't exists
        getLogger().info("Loading configuration!");
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        Logger logger = Logger.getLogger("");
        logger.info("".repeat(5) + " __       ___ ___            ");
        logger.info("".repeat(5) + "|  )  /\\   |   /   /\\  |   ");
        logger.info("".repeat(5) + "|__) /--\\  |  /__ /--\\ |___");
        logger.info("".repeat(5));

        Utility.initialize(this);

        // Initialize NameConfig
        NickConfig.initialize(this);

        // Starting the Nick & Vanish actionbar
        ActionBarManager.startActionBarTask();

        // Initialize PlaceholderAPI safely
        NickExpansion.init(this);

        // Initialize Nick book gui
        BookManager.init();

        // Registering Commands

        registerCommand("batzal", new BatzalCommand());
        registerCommand("nick", new NickCommand());
        registerCommand("unnick", new NickCommand.UnNickCommand());
        registerCommand("vanish", Collections.singleton("v"), new VanishCommand());
        registerCommand("togglechat", new ToggleChatCommand());
        registerCommand("togglepunch", new TogglePunchCommand());
        registerCommand("zoo", new ZooCommand());
        registerCommand("whatdoyoudo", new WhatDoYouDoCommand());
        registerCommand("ping", new PingCommand());
        registerCommand("build", Collections.singleton("b"), new BuildCommand());
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
        registerCommand("heal", new HealCommand());
        registerCommand("hat", new HatCommand());
        registerCommand("stuck", new StuckCommand());
        registerCommand("speed", new SpeedCommand());
        registerCommand("give", new GiveCommand());
        registerCommand("list", Collections.singleton("online"), new ListCommand());

        getServer().getPluginManager().registerEvents(new EventListeners(), this);
        EntitiesLoadListener.initialize(this);

        Bukkit.getWorlds().forEach(world -> world.setTime(1000));

        getLogger().info("Enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Starting shutdown process..");

        Utility.shutdown(this);

        getLogger().info("Disabled successfully :)");
    }

    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    public enum ToggleCategory {
        LeaveMeAlone,
        God
    }

    public enum CooldownCategory implements CooldownType {
        FireWork("batzal.firework.bypass"),
        WarMode(""),
        Punch("batzal.punch.bypass");

        private final String permission;

        CooldownCategory(String permission) {
            this.permission = permission;
        }

        public String getPermission() {
            return permission;
        }
    }

    @Override
    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found");
        }

        File outFile = new File(getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }
}
