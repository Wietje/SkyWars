package vc.pvp.skywars.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import vc.pvp.skywars.SkyWars;
import vc.pvp.skywars.config.PluginConfig;
import vc.pvp.skywars.controllers.GameController;
import vc.pvp.skywars.controllers.PlayerController;
import vc.pvp.skywars.controllers.SchematicController;
import vc.pvp.skywars.game.Game;
import vc.pvp.skywars.game.GameState;
import vc.pvp.skywars.player.GamePlayer;
import vc.pvp.skywars.utilities.StringUtils;

import java.util.Iterator;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerController.get().register(event.getPlayer());
        SkyWars.get().updateScoreboard();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = PlayerController.get().get(player);

        if (gamePlayer.isPlaying()) {
            gamePlayer.getGame().onPlayerLeave(gamePlayer);
        }

        gamePlayer.save();
        PlayerController.get().unregister(player);

        SkyWars.get().updateScoreboard();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = PlayerController.get().get(player);

        if (event.getAction() == Action.PHYSICAL && event.getClickedBlock().getTypeId() == Material.STONE_PLATE.getId()) {
            if (!gamePlayer.isPlaying() && player.getLocation().getWorld().equals(PluginConfig.getLobbySpawn().getWorld())) {
                if (SchematicController.get().size() == 0) {
                    player.sendMessage("\247cThere are no schematics available.");
                    return;
                }

                Game game = GameController.get().findEmpty();
                game.onPlayerJoin(gamePlayer);
            }

            return;
        }

        if (gamePlayer.isPlaying() && gamePlayer.getGame().getState() != GameState.PLAYING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = PlayerController.get().get(player);

        String score = StringUtils.formatScore(gamePlayer.getScore());

        if (!gamePlayer.isPlaying()) {
            for (Iterator<Player> iterator = event.getRecipients().iterator(); iterator.hasNext(); ) {
                Player recipient = iterator.next();

                if (recipient.isOnline() && PlayerController.get().get(recipient).isPlaying()) {
                    iterator.remove();
                }
            }

            event.setFormat(ChatColor.translateAlternateColorCodes('&', "&e[L] " + score + " &8%s &e&l> &r&7%s"));
            return;
        }

        gamePlayer.getGame().sendMessage(false, "&e[L] %s &8%s &e&l> &r&7%s", score, player.getDisplayName(), event.getMessage());
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = PlayerController.get().get(player);

        if (gamePlayer.isPlaying()) {
            String command = event.getMessage().split(" ")[0].toLowerCase();

            if (!command.equals("/sw") && !PluginConfig.isCommandWhitelisted(command)) {
                event.setCancelled(true);
                player.sendMessage(String.format("%s\247cThis command is disabled during the game!", Game.PREFIX));
            }
        }
    }
}
