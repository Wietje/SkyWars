package vc.pvp.skywars.listeners;

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
import vc.pvp.skywars.utilities.Messaging;
import vc.pvp.skywars.utilities.StringUtils;

import java.util.Iterator;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerController.get().register(event.getPlayer());
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
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = PlayerController.get().get(player);

        if (event.getAction() == Action.PHYSICAL && event.getClickedBlock().getTypeId() == Material.STONE_PLATE.getId()) {
            if (!gamePlayer.isPlaying() && player.getLocation().getWorld().equals(PluginConfig.getLobbySpawn().getWorld())) {
                if (SchematicController.get().size() == 0) {
                    player.sendMessage(new Messaging.MessageFormatter().format("error.no-schematics"));
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

        if (PluginConfig.chatHandledByOtherPlugin()) {
            event.setFormat(event.getFormat().replace("{score}", String.valueOf(gamePlayer.getScore())));

            if (gamePlayer.isPlaying()) {
                for (Iterator<Player> iterator = event.getRecipients().iterator(); iterator.hasNext();) {
                    GamePlayer gp = PlayerController.get().get(iterator.next());

                    if (!gp.isPlaying() || !gp.getGame().equals(gamePlayer.getGame())) {
                        iterator.remove();
                    }
                }

            } else {
                for (Iterator<Player> iterator = event.getRecipients().iterator(); iterator.hasNext();) {
                    GamePlayer gp = PlayerController.get().get(iterator.next());

                    if (gp.isPlaying()) {
                        iterator.remove();
                    }
                }
            }

            return;
        }

        String message = new Messaging.MessageFormatter()
                .setVariable("score", StringUtils.formatScore(gamePlayer.getScore()))
                .setVariable("player", player.getDisplayName())
                .setVariable("message", Messaging.stripColor(event.getMessage()))
                .setVariable("prefix", SkyWars.getChat().getPlayerPrefix(player))
                .format("chat.local");

        event.setCancelled(true);

        if (gamePlayer.isPlaying()) {
            gamePlayer.getGame().sendMessage(message);

        } else {
            for (GamePlayer gp : PlayerController.get().getAll()) {
                if (!gp.isPlaying()) {
                    gp.getBukkitPlayer().sendMessage(message);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = PlayerController.get().get(player);

        if (gamePlayer.isPlaying()) {
            String command = event.getMessage().split(" ")[0].toLowerCase();

            if (!command.equals("/sw") && !PluginConfig.isCommandWhitelisted(command)) {
                event.setCancelled(true);
                player.sendMessage( new Messaging.MessageFormatter().withPrefix().format("error.cmd-disabled"));
            }
        }
    }
}
