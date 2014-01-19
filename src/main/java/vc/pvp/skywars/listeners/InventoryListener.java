package vc.pvp.skywars.listeners;

import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vc.pvp.skywars.config.PluginConfig;
import vc.pvp.skywars.controllers.ChestController;
import vc.pvp.skywars.controllers.PlayerController;
import vc.pvp.skywars.player.GamePlayer;

public class InventoryListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {

        if (!(event.getInventory().getHolder() instanceof Chest)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        GamePlayer gamePlayer = PlayerController.get().get(player);

        if (!gamePlayer.isPlaying()) {
            return;
        }

        Chest chest = (Chest) event.getInventory().getHolder();
        Location location = chest.getLocation();

        if (!gamePlayer.getGame().isChest(location)) {
            return;
        }

        Inventory inv = chest.getInventory();
        boolean empty = true;
        for (ItemStack itemStack : inv.getContents()) {
            if (itemStack != null) {
                empty = false;
                break;
            }
        }
        
        if (!PluginConfig.fillEmptyChests() && empty) {
            return;
        }
        
        if (!PluginConfig.fillPopulatedChests() && !empty) {
            return;
        } else {
            inv.clear();
        }

        gamePlayer.getGame().removeChest(location);
        ChestController.get().populateChest(chest);
    }
}
