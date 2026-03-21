package net.justrotem.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.justrotem.data.util.ClickUtility;
import net.justrotem.proxy.sql.MySQL;
import net.justrotem.proxy.utils.LobbyManager;
import net.luckperms.api.LuckPerms;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Plugin(
    id = "batzal-proxy",
    name = "Batzal-Proxy",
    version = BuildConstants.VERSION
)
public class Main {

    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;
    private final YamlDocument config;
    private final LuckPerms luckPerms;

    public YamlDocument getConfig() {
        return config;
    }

    public ProxyServer getProxy() {
        return this.proxy;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Path getDataDirectory() {
        return this.dataDirectory;
    }

    public boolean isDebug() {
        return config.getBoolean(Route.from("debug"));
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Loading MySQL!");
        MySQL.connect(this);

        VelocityLuckPermsManager.init(logger, luckPerms, isDebug());
        LobbyManager.init(this, proxy, logger);
        VelocityPlayerManager.startAutoSave(proxy, this);

        logger.info("Enabled successfully!");
    }

    @Inject
    public Main(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory, LuckPerms luckPerms) {
        YamlDocument config;
        instance = this;
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.luckPerms = luckPerms;

        try {
            logger.info("Loading configuration!");
            config = YamlDocument.create(new File(dataDirectory.toFile(), "config.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/config.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build());
            config.update();
            config.save();
        } catch (IOException | NullPointerException e) {
            logger.error("Could not create/load plugin config! This plugin will now shutdown");
            config = null;
            Optional<PluginContainer> container = proxy.getPluginManager().getPlugin("batzal-proxy");
            container.ifPresent(pluginContainer -> pluginContainer.getExecutorService().shutdown());
        }

        this.config = config;
    }

    @Subscribe
    public void onServerPreConnectEvent(ServerPreConnectEvent event) {
        Player player = event.getPlayer();

        // Joined the proxy
        if (event.getPreviousServer() == null) {
            VelocityPlayerManager.register(player); // Get PlayerData
            FriendManager.register(player.getUniqueId());

            FriendData friendData = FriendManager.get(player.getUniqueId());
            List<UUID> requests = friendData.getRequests();

            if (!requests.isEmpty()) {
                player.sendMessage(FriendManager.UP_HYPHEN
                        .append(ClickUtility.clickable("&aYou have %requests% pending friend requests.\n&eUse &b/f requests &eto see them!"
                                .replace("%requests%", String.valueOf(requests.size())), player.getUniqueId(), "&eClick to see &brequests&e!", () -> {
                    // Open friend requests..
                })).append(FriendManager.DOWN_HYPHEN));
            }

            for (Player target : friendData.getOnlineFriends()) {
                if (VanishManager.canSee(target, player)) {
                    if (FriendManager.get(target.getUniqueId()).isEnabledNotifications())
                        target.sendMessage(ClickUtility.color("&aFriend > " + VelocityPlayerManager.getColoredName(player.getUniqueId()) + " &ejoined."));
                }
            }
        }
    }

    @Subscribe
    public void onKickedFromServerEvent(KickedFromServerEvent event) {
        Player player = event.getPlayer();
        FriendData friendData = FriendManager.get(player.getUniqueId());

        if (event.kickedDuringServerConnect()) return;

        for (Player target : friendData.getOnlineFriends()) {
            if (VanishManager.canSee(target, player)) {
                if (FriendManager.get(target.getUniqueId()).isEnabledNotifications())
                    target.sendMessage(ClickUtility.color("&aFriend > " + VelocityPlayerManager.getColoredName(player.getUniqueId()) + " &eleft."));
            }
        }
    }
}
