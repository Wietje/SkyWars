package vc.pvp.skywars;

import com.earth2me.essentials.IEssentials;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import vc.pvp.skywars.commands.MainCommand;
import vc.pvp.skywars.controllers.*;
import vc.pvp.skywars.database.Database;
import vc.pvp.skywars.listeners.BlockListener;
import vc.pvp.skywars.listeners.EntityListener;
import vc.pvp.skywars.listeners.InventoryListener;
import vc.pvp.skywars.listeners.PlayerListener;
import vc.pvp.skywars.metrics.MetricsLite;
import vc.pvp.skywars.player.GamePlayer;
import vc.pvp.skywars.storage.DataStorage;
import vc.pvp.skywars.tasks.SyncTask;
import vc.pvp.skywars.utilities.FileUtils;
import vc.pvp.skywars.utilities.StringUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

public class SkyWars extends JavaPlugin {

    private static SkyWars instance;
    private static Permission permission;
    private Database database;

    @Override
    public void onEnable() {
        instance = this;

        deleteIslandWorlds();

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        reloadConfig();

        getCommand("skywars").setExecutor(new MainCommand());
        getCommand("global").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (!(sender instanceof Player)) {
                    return false;
                }

                if (args.length == 0) {
                    sender.sendMessage("\247cUsage: /" + label + " <message>");
                    return true;
                }

                StringBuilder messageBuilder = new StringBuilder();
                for (String arg : args) {
                    messageBuilder.append(arg);
                    messageBuilder.append(" ");
                }

                GamePlayer gamePlayer = PlayerController.get().get((Player) sender);
                String score = StringUtils.formatScore(gamePlayer.getScore());

                Bukkit.broadcastMessage(String.format("\247c[G] %s \2478%s \247c\247l> \247r\2477%s", score, ((Player) sender).getDisplayName(), messageBuilder.toString()));
                return true;
            }
        });

        try {
            DataStorage.DataStorageType dataStorageType = DataStorage.DataStorageType.valueOf(getConfig().getString("data-storage", "FILE"));
            if (dataStorageType == DataStorage.DataStorageType.SQL && !setupDatabase()) {
                getLogger().log(Level.INFO, "Couldn't setup database, now using file storage.");
                DataStorage.setInstance(DataStorage.DataStorageType.FILE);

            } else {
                DataStorage.setInstance(dataStorageType);
            }

        } catch (NullPointerException npe) {
            DataStorage.setInstance(DataStorage.DataStorageType.FILE);
        }

        setupChat();

        SchematicController.get();
        WorldController.get();
        GameController.get();
        PlayerController.get();
        ChestController.get();
        KitController.get();

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }

        /*if (getDB() != null) {
            StatisticsController.get();
            new StatisticsUpdater();
        }*/

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new EntityListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new SyncTask(), 20L, 20L);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);

        GameController.get().shutdown();
        PlayerController.get().shutdown();

        if (database != null) {
            database.close();
        }

        deleteIslandWorlds();
    }

    private void deleteIslandWorlds() {
        // Worlds
        File workingDirectory = new File(".");
        File[] contents = workingDirectory.listFiles();

        if (contents != null) {
            for (File file : contents) {
                if (!file.isDirectory() || !file.getName().matches("island-\\d+")) {
                    continue;
                }

                FileUtils.deleteFolder(file);
            }
        }

        // WorldGuard
        workingDirectory = new File("./plugins/WorldGuard/worlds/");
        contents = workingDirectory.listFiles();

        if (contents != null) {
            for (File file : contents) {
                if (!file.isDirectory() || !file.getName().matches("island-\\d+")) {
                    continue;
                }

                FileUtils.deleteFolder(file);
            }
        }
    }

    private boolean setupDatabase() {
        try {
            database = new Database(getConfig().getConfigurationSection("database"));

        } catch (ClassNotFoundException exception) {
            getLogger().log(Level.SEVERE, String.format("Unable to register JDCB driver: %s", exception.getMessage()));
            return false;

        } catch (SQLException exception) {
            getLogger().log(Level.SEVERE, String.format("Unable to connect to SQL server: %s", exception.getMessage()));
            return false;
        }

        try {
            database.createTables();
        } catch (Exception exception) {
            getLogger().log(Level.SEVERE, String.format("An exception was thrown while attempting to create tables: %s", exception.getMessage()));
            return false;
        }

        return true;
    }

    private void setupChat() {
        RegisteredServiceProvider<Permission> chatProvider = getServer().getServicesManager().getRegistration(Permission.class);
        if (chatProvider != null) {
            permission = chatProvider.getProvider();
        }
    }

    public static SkyWars get() {
        return instance;
    }

    public static IEssentials getEssentials() {
        return (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
    }

    public static Permission getPermission() {
        return permission;
    }

    public static Database getDB() {
        return instance.database;
    }
}
