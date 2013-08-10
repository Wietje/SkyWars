package vc.pvp.skywars.controllers;

import com.google.common.collect.Lists;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vc.pvp.skywars.SkyWars;
import vc.pvp.skywars.utilities.ItemUtils;
import vc.pvp.skywars.utilities.LogUtils;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class ChestController {

    private static ChestController chestController;
    private final List<ChestItem> chestItemList = Lists.newArrayList();
    private final Random random = new Random();

    public ChestController() {
        load();
    }

    public void load() {
        chestItemList.clear();
        File chestFile = new File(SkyWars.get().getDataFolder(), "chest.yml");

        if (!chestFile.exists()) {
            SkyWars.get().saveResource("chest.yml", false);
        }

        if (chestFile.exists()) {
            FileConfiguration storage = YamlConfiguration.loadConfiguration(chestFile);

            if (storage.contains("items")) {
                for (String item : storage.getStringList("items")) {
                    String[] itemData = item.split(" ", 2);

                    int chance = Integer.parseInt(itemData[0]);
                    ItemStack itemStack = ItemUtils.parseItem(itemData[1].split(" "));

                    if (itemStack != null) {
                        chestItemList.add(new ChestItem(itemStack, chance));
                    }
                }
            }
        }

        LogUtils.log(Level.INFO, getClass(), "Registered %d chest items ...", chestItemList.size());
    }

    public void populateChest(Chest chest) {
        Inventory inventory = chest.getBlockInventory();
        int added = 0;

        for (ChestItem chestItem : chestItemList) {
            if (random.nextInt(100) + 1 <= chestItem.getChance()) {
                inventory.addItem(chestItem.getItem());

                if (added++ > inventory.getSize()) {
                    break;
                }
            }
        }
    }

    public class ChestItem {

        private ItemStack item;
        private int chance;

        public ChestItem(ItemStack item, int chance) {
            this.item = item;
            this.chance = chance;
        }

        public ItemStack getItem() {
            return item;
        }

        public int getChance() {
            return chance;
        }
    }

    public static ChestController get() {
        if (chestController == null) {
            chestController = new ChestController();
        }

        return chestController;
    }
}
