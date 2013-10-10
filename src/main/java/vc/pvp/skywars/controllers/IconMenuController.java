package vc.pvp.skywars.controllers;

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import vc.pvp.skywars.SkyWars;
import vc.pvp.skywars.utilities.IconMenu;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Map;

public class IconMenuController implements Listener {

    private static IconMenuController instance;
    private final Map<Player, IconMenu> menuMap = Maps.newHashMap();

    public IconMenuController() {
        Bukkit.getPluginManager().registerEvents(this, SkyWars.get());
    }

    public void create(Player player, String name, int size, IconMenu.OptionClickEventHandler handler) {
        destroy(player);
        menuMap.put(player, new IconMenu(name, size, handler, SkyWars.get()));
    }

    public void show(@Nonnull Player player) {
        if (menuMap.containsKey(player)) {
            menuMap.get(player).open(player);
        }
    }

    public void setOption(Player player, int position, ItemStack icon, String name, String... info) {
        if (menuMap.containsKey(player)) {
            menuMap.get(player).setOption(position, icon, name, info);
        }
    }

    public void destroy(Player player) {
        if (menuMap.containsKey(player)) {
            menuMap.remove(player).destroy();
            player.getOpenInventory().close();
        }
    }

    public void destroyAll() {
        for (Player player : new HashSet<Player>(menuMap.keySet())) {
            destroy(player);
        }
    }

    public boolean has(Player player) {
        return menuMap.containsKey(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player && menuMap.containsKey(event.getWhoClicked())) {
            menuMap.get(event.getWhoClicked()).onInventoryClick(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player && menuMap.containsKey(event.getPlayer())) {
            destroy((Player) event.getPlayer());
        }
    }

    public static IconMenuController get() {
        if (instance == null) {
            instance = new IconMenuController();
        }

        return instance;
    }
}