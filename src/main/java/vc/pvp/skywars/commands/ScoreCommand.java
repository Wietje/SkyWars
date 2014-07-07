/*
 * Copyright (C) 2014 andypandy89 <andypandy89@ultimateminecraft.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package vc.pvp.skywars.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vc.pvp.skywars.controllers.PlayerController;
import vc.pvp.skywars.player.GamePlayer;
import vc.pvp.skywars.utilities.Messaging;

@CommandDescription("Displays the score of the given user")
@CommandPermissions("skywars.command.score")
public class ScoreCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                GamePlayer gamePlayer = PlayerController.get().get(player);
                sender.sendMessage(new Messaging.MessageFormatter()
                        .setVariable("player", gamePlayer.getName())
                        .setVariable("value", String.valueOf(gamePlayer.getScore()))
                        .format("cmd.score"));
            }
        } else if (args.length == 2) {
            if (!sender.hasPermission("skywars.command.score.others")) {
                sender.sendMessage(new Messaging.MessageFormatter().format("error.insufficient-permissions"));
                return true;
            }
            OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(args[1]);
            if (!player.isOnline() && !player.hasPlayedBefore()) {
                sender.sendMessage(new Messaging.MessageFormatter()
                        .format("error.no-valid-player"));
                return true;
            }
            GamePlayer gamePlayer = new GamePlayer(args[1]);
            sender.sendMessage(new Messaging.MessageFormatter()
                    .setVariable("value", String.valueOf(gamePlayer.getScore()))
                    .setVariable("player", gamePlayer.getName())
                    .format("cmd.score"));
        } else {
            if ("set".equals(args[1])) {
                if (!sender.hasPermission("skywars.command.score.set")) {
                    sender.sendMessage(new Messaging.MessageFormatter()
                            .format("error.insufficient-permissions"));
                    return true;
                }
                if (args.length < 4) {
                    sender.sendMessage(new Messaging.MessageFormatter()
                            .setVariable("example", "/sw set player score")
                            .format("error.not-enough-arguments"));
                    return true;
                }
                OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(args[2]);
                if (!player.isOnline() && !player.hasPlayedBefore()) {
                    sender.sendMessage(new Messaging.MessageFormatter()
                            .format("error.no-valid-player"));
                    return true;
                }
                GamePlayer gamePlayer = new GamePlayer(player.getName());
                try {
                    int amount = Integer.parseInt(args[3]);
                    gamePlayer.setScore(amount);
                    sender.sendMessage(new Messaging.MessageFormatter()
                            .setVariable("value", String.valueOf(amount))
                            .setVariable("player", gamePlayer.getName())
                            .format("success.score-set"));
                    gamePlayer.save();
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(new Messaging.MessageFormatter()
                            .format("error.no-valid-score"));
                    return true;
                }
            } else if ("give".equals(args[1])) {
                if (!sender.hasPermission("skywars.command.score.give")) {
                    sender.sendMessage(new Messaging.MessageFormatter()
                            .format("error.insufficient-permissions"));
                    return true;
                }
                if (args.length < 4) {
                    sender.sendMessage(new Messaging.MessageFormatter()
                            .setVariable("example", "/sw give player score")
                            .format("error.not-enough-arguments"));
                    return true;
                }
                OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(args[2]);
                if (!player.isOnline() && !player.hasPlayedBefore()) {
                    sender.sendMessage(new Messaging.MessageFormatter()
                            .format("error.no-valid-player"));
                    return true;
                }
                GamePlayer gamePlayer = new GamePlayer(player.getName());
                try {
                    int amount = Integer.parseInt(args[3]);
                    gamePlayer.setScore(gamePlayer.getScore() + amount);
                    sender.sendMessage(new Messaging.MessageFormatter()
                            .setVariable("value", String.valueOf(amount))
                            .setVariable("player", gamePlayer.getName())
                            .format("success.score-give"));
                    gamePlayer.save();
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(new Messaging.MessageFormatter()
                            .format("error.no-valid-score"));
                    return true;
                }
            } else if ("take".equals(args[1])) {
                if (!sender.hasPermission("skywars.command.score.take")) {
                    sender.sendMessage(new Messaging.MessageFormatter()
                            .format("error.insufficient-permissions"));
                    return true;
                }
                if (args.length < 4) {
                    sender.sendMessage(new Messaging.MessageFormatter()
                            .setVariable("example", "/sw take player score")
                            .format("error.not-enough-arguments"));
                    return true;
                }
                OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(args[2]);
                if (!player.isOnline() && !player.hasPlayedBefore()) {
                    sender.sendMessage(new Messaging.MessageFormatter()
                            .format("error.no-valid-player"));
                    return true;
                }
                GamePlayer gamePlayer = new GamePlayer(player.getName());
                try {
                    int amount = Integer.parseInt(args[3]);
                    gamePlayer.setScore(gamePlayer.getScore() - amount);
                    sender.sendMessage(new Messaging.MessageFormatter()
                            .setVariable("value", String.valueOf(amount))
                            .setVariable("player", gamePlayer.getName())
                            .format("success.score-take"));
                    gamePlayer.save();
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(new Messaging.MessageFormatter()
                            .format("error.no-valid-score"));
                    return true;
                }
            } else {
                sender.sendMessage(new Messaging.MessageFormatter()
                        .format("error.invalid-cmd"));
            }
        }
        return false;
    }

}
