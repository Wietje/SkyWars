package vc.pvp.skywars.config;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import vc.pvp.skywars.SkyWars;
import vc.pvp.skywars.utilities.LocationUtil;

import java.util.List;
import java.util.Locale;

public class PluginConfig {

    private static FileConfiguration storage;
    private static Location lobbySpawn;
    private static List<String> whitelistedCommands = Lists.newArrayList();

    static {
        storage = SkyWars.get().getConfig();

        lobbySpawn = LocationUtil.getLocation(Bukkit.getWorld(storage.getString("lobby.world")), storage.getString("lobby.spawn"));
        if (storage.contains("whitelisted-commands")) {
            whitelistedCommands = storage.getStringList("whitelisted-commands");
        }
    }

    public static Location getLobbySpawn() {
        return lobbySpawn;
    }

    public static void setLobbySpawn(Location location) {
        lobbySpawn = location.clone();
        storage.set("lobby.world", lobbySpawn.getWorld().getName());
        storage.set("lobby.spawn", String.format(Locale.US, "%.2f %.2f %.2f %.2f %.2f", location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch()));
        saveConfig();
    }

    public static int getLobbyRadius() {
        return storage.getInt("lobby.radius", 0);
    }

    public static boolean isCommandWhitelisted(String command) {
        return whitelistedCommands.contains(command.replace("/", ""));
    }

    public static int getIslandsPerWorld() {
        return storage.getInt("islands-per-row", 100);
    }

    public static int getIslandBuffer() {
        return storage.getInt("island-buffer", 5);
    }

    public static int getScorePerKill(Player player) {
        if (SkyWars.getPermission().hasGroupSupport()) {
            String group = SkyWars.getPermission().getPrimaryGroup(player);
            if (storage.contains("score.groups." + group + ".per-kill")) {
                return storage.getInt("score.groups." + group + ".per-kill");
            }
        }
        return storage.getInt("score.per-kill", 3);
    }

    public static int getScorePerWin(Player player) {
        if (SkyWars.getPermission().hasGroupSupport()) {
            String group = SkyWars.getPermission().getPrimaryGroup(player);
            if (storage.contains("score.groups." + group + ".per-win")) {
                return storage.getInt("score.groups." + group + ".per-win");
            }
        }
        return storage.getInt("score.per-win", 10);
    }

    public static int getScorePerDeath(Player player) {
        if (SkyWars.getPermission().hasGroupSupport()) {
            String group = SkyWars.getPermission().getPrimaryGroup(player);
            if (storage.contains("score.groups." + group + ".per-death")) {
                return storage.getInt("score.groups." + group + ".per-death");
            }
        }
        return storage.getInt("score.per-death", -1);
    }

    public static int getScorePerLeave(Player player) {
        if (SkyWars.getPermission().hasGroupSupport()) {
            String group = SkyWars.getPermission().getPrimaryGroup(player);
            if (storage.contains("score.groups." + group + ".per-leave")) {
                return storage.getInt("score.groups." + group + ".per-leave");
            }
        }
        return storage.getInt("score.per-leave", -1);
    }

    public static long getStatisticsUpdateInterval() {
        return storage.getInt("statistics.update-interval", 600) * 20L;
    }

    public static int getStatisticsTop() {
        return storage.getInt("statistics.top", 30);
    }

    public static boolean buildSchematic() {
        return storage.getBoolean("island-building.enabled", false);
    }

    public static int blocksPerTick() {
        return storage.getInt("island-building.blocks-per-tick", 20);
    }

    public static long buildInterval() {
        return storage.getLong("island-building.interval", 1);
    }

    public static boolean buildCages() {
        return storage.getBoolean("build-cages", true);
    }

    public static boolean ignoreAir() {
        return storage.getBoolean("ignore-air", false);
    }

    public static boolean fillEmptyChests() {
        return storage.getBoolean("fill-empty-chests", true);
    }

    public static boolean fillPopulatedChests() {
        return storage.getBoolean("fill-populated-chests", true);
    }

    public static boolean useEconomy() {
        return storage.getBoolean("use-economy", false);
    }

    public static boolean disableKits() {
        return storage.getBoolean("disable-kits", false);
    }
    
    public static boolean enableSounds() {
        return storage.getBoolean("enable-soundeffects", true);
    }

    public static boolean chatHandledByOtherPlugin() {
        return storage.getBoolean("chat-handled-by-other-plugin", false);
    }

    public static boolean clearInventory() {
        return storage.getBoolean("clear-inventory-on-join", true);
    }

    public static boolean saveInventory() {
        return storage.getBoolean("save-inventory", false);
    }

    public static void setSchematicConfig(String schematicFile, int playerSize) {
        String schematicPath = "schematics." + schematicFile.replace(".schematic", "");
        if (!storage.isSet(schematicPath)) {
            storage.set(schematicPath + ".min-players", playerSize);
            storage.set(schematicPath + ".timer", 11);
            saveConfig();
        }
    }

    public static void migrateConfig() {
        if (storage.isSet("fill-chests")) {
            Boolean fill = storage.getBoolean("fill-chests");
            storage.set("fill-empty-chests", fill);
            storage.set("fill-populated-chests", fill);
            storage.set("fill-chests", null);
        }
        if (!storage.isSet("lobby.radius")) {
            storage.set("lobby.radius", 0);
        }
        if (storage.isSet("island-size")) {
            storage.set("island-size", null);
        }
        if (!storage.isSet("island-buffer")) {
            storage.set("island-buffer", 5);
        }
        if (!storage.isSet("disable-kits")) {
            storage.set("disable-kits", false);
        }
        if (!storage.isSet("enable-soundeffects")) {
            storage.set("enable-soundeffects", false);
        }
        saveConfig();
    }

    private static boolean saveConfig() {
        File file = new File("./plugins/SkyWars/config.yml");
        try {
            storage.save(file);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }
}
