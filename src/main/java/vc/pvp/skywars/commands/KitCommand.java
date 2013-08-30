package vc.pvp.skywars.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vc.pvp.skywars.controllers.KitController;
import vc.pvp.skywars.controllers.PlayerController;
import vc.pvp.skywars.game.GameState;
import vc.pvp.skywars.player.GamePlayer;
import vc.pvp.skywars.utilities.Messaging;
import vc.pvp.skywars.utilities.StringUtils;

import java.util.List;

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
        } else if (args.length > 1) {
            KitController.Kit kit = KitController.get().getByName(args[1]);

            if (kit == null) {
                sender.sendMessage(new Messaging.MessageFormatter().format("error.no-such-kit"));
            } else if (!KitController.get().hasPermission(player, kit)) {
                if (KitController.get().isPurchaseAble(kit)) {
                    if (KitController.get().canPurchase(gamePlayer, kit)) {
                        gamePlayer.setScore(gamePlayer.getScore() - kit.getPoints());
                        giveKit(gamePlayer, kit);

                    } else {
                        sender.sendMessage(new Messaging.MessageFormatter().format("error.not-enough-score"));
                    }

                } else {
                    sender.sendMessage(new Messaging.MessageFormatter().format("error.no-permission-kit"));
                }
            } else {
                giveKit(gamePlayer, kit);
            }

        } else {
            List<String> availableKits = KitController.get().getAvailableKits(gamePlayer);
            char color1 = Messaging.getInstance().getMessage("kit.color.kit").charAt(0);
            char color2 = Messaging.getInstance().getMessage("kit.color.separator").charAt(0);

            player.sendMessage(new Messaging.MessageFormatter()
                    .withPrefix()
                    .setVariable( "kits", StringUtils.toString(availableKits, color1, color2))
                    .format("kit.available"));
            player.sendMessage(new Messaging.MessageFormatter().withPrefix().format("kit.usage"));
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    private void giveKit(GamePlayer gamePlayer, KitController.Kit kit) {
        KitController.get().populateInventory(gamePlayer.getBukkitPlayer().getInventory(), kit);
        gamePlayer.getBukkitPlayer().updateInventory();
        gamePlayer.setChosenKit(true);
        gamePlayer.getBukkitPlayer().sendMessage(new Messaging.MessageFormatter().format("success.enjoy-kit"));
    }
}