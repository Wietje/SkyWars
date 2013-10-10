package vc.pvp.skywars.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vc.pvp.skywars.controllers.IconMenuController;
import vc.pvp.skywars.controllers.KitController;
import vc.pvp.skywars.controllers.PlayerController;
import vc.pvp.skywars.game.GameState;
import vc.pvp.skywars.player.GamePlayer;
import vc.pvp.skywars.utilities.Messaging;

@CommandDescription("Allows a player to pick kits")
public class KitCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        GamePlayer gamePlayer = PlayerController.get().get(player);

        if (!gamePlayer.isPlaying()) {
            sender.sendMessage(new Messaging.MessageFormatter().format("error.not-in-game"));
        } else if (gamePlayer.hasChosenKit()) {
            sender.sendMessage(new Messaging.MessageFormatter().format("error.already-has-kit"));
        } else if (gamePlayer.getGame().getState() != GameState.WAITING) {
            sender.sendMessage(new Messaging.MessageFormatter().format("error.can-not-pick-kit"));
        } else if (!IconMenuController.get().has(player)) {
            KitController.get().openKitMenu(gamePlayer);
        }

        return true;
    }
}