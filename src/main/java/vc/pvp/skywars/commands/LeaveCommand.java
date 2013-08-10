package vc.pvp.skywars.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vc.pvp.skywars.controllers.PlayerController;
import vc.pvp.skywars.player.GamePlayer;

@CommandDescription("Leaves a SkyWars game")
public class LeaveCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GamePlayer gamePlayer = PlayerController.get().get((Player) sender);

        if (!gamePlayer.isPlaying()) {
            sender.sendMessage("\247cYou are not in a SkyWars game!");
        } else {
            gamePlayer.getGame().onPlayerLeave(gamePlayer);
        }

        return true;
    }
}
