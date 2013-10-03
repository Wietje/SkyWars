package vc.pvp.skywars.listeners;

import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import vc.pvp.skywars.config.PluginConfig;
import vc.pvp.skywars.controllers.ChestController;
import vc.pvp.skywars.controllers.PlayerController;
import vc.pvp.skywars.player.GamePlayer;

public class InventoryListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!PluginConfig.fillChests()) {
            return;
        }

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

        gamePlayer.getGame().removeChest(location);
        ChestController.get().populateChest(chest);
    }
}
