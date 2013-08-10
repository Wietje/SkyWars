package vc.pvp.skywars.controllers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vc.pvp.skywars.SkyWars;
import vc.pvp.skywars.player.GamePlayer;
import vc.pvp.skywars.utilities.FileUtils;
import vc.pvp.skywars.utilities.ItemUtils;
import vc.pvp.skywars.utilities.LogUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class KitController {

    private static final String PERMISSION_NODE = "skywars.kit.";
    private static KitController instance;
    private final Map<String, Kit> kitMap = Maps.newHashMap();

    public KitController() {
        load();
    }

    public void load() {
        kitMap.clear();
        File dataDirectory = SkyWars.get().getDataFolder();
        File kitsDirectory = new File(dataDirectory, "kits");

        if (!kitsDirectory.exists()) {
            if (!kitsDirectory.mkdirs())  {
                return;
            }


            FileUtils.saveResource(SkyWars.get(), "example.yml", new File(kitsDirectory, "Example.yml"), false);
        }

        File[] kits = kitsDirectory.listFiles();
        if (kits == null) {
            return;
        }

        for (File kit : kits) {
            if (!kit.getName().endsWith(".yml")) {
                continue;
            }

            String name = kit.getName().replace(".yml", "");

            if (!kitMap.containsKey(name)) {
                kitMap.put(name, new Kit(name, YamlConfiguration.loadConfiguration(kit)));
            }
        }

        LogUtils.log(Level.INFO, getClass(), "Registered %d kits ...", kitMap.size());
    }

    public boolean hasPermission(Player player, Kit kit) {
        return player.isOp() || player.hasPermission(PERMISSION_NODE + kit.getName().toLowerCase());
    }

    public boolean isPurchaseAble(Kit kit) {
        return kit.getPoints() > 0;
    }

    public boolean canPurchase(GamePlayer gamePlayer, Kit kit) {
        return kit.getPoints() > 0 && (gamePlayer.getScore() >= kit.getPoints());
    }

    public void populateInventory(Inventory inventory, Kit kit) {
        for (ItemStack itemStack : kit.getItems()) {
            inventory.addItem(itemStack);
        }
    }

    public Kit getByName(String name) {
        return kitMap.get(name);
    }

    public List<String> getAvailableKits(GamePlayer gamePlayer) {
        Player player = gamePlayer.getBukkitPlayer();
        List<String> availableKits = Lists.newArrayList();

        for (Kit kit : kitMap.values()) {
            if (hasPermission(player, kit)) {
                availableKits.add(kit.getName());

            } else if (canPurchase(gamePlayer, kit)) {
                availableKits.add(kit.getName() + " \247a(costs " + kit.getPoints() + " score)");
            }
        }

        if (availableKits.size() == 0) {
            availableKits.add("No kits available");
        }

        return availableKits;
    }

    public class Kit {

        private String name;
        private int points;
        private List<ItemStack> items = Lists.newArrayList();

        public Kit(String name, FileConfiguration storage) {
            this.name = name;

            for (String item : storage.getStringList("items")) {
                ItemStack itemStack = ItemUtils.parseItem(item.split(" "));

                if (itemStack != null) {
                    items.add(itemStack);
                }
            }

            points = storage.getInt("points", 0);
        }

        public Collection<ItemStack> getItems() {
            return items;
        }

        public String getName() {
            return name;
        }

        public int getPoints() {
            return points;
        }
    }

    public static KitController get() {
        if (instance == null) {
            instance = new KitController();
        }

        return instance;
    }
}
