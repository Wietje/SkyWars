package vc.pvp.skywars.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vc.pvp.skywars.controllers.KitController;
import vc.pvp.skywars.controllers.PlayerController;
import vc.pvp.skywars.game.GameState;
import vc.pvp.skywars.player.GamePlayer;
import vc.pvp.skywars.utilities.StringUtils;

import java.util.List;

@CommandDescription("Allows a player to pick kits")
public class KitCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        GamePlayer gamePlayer = PlayerController.get().get(player);

        if (!gamePlayer.isPlaying()) {
            sender.sendMessage("\247cYou need to be in a game in order to pick a kit!");
        } else if (gamePlayer.hasChosenKit()) {
            sender.sendMessage("\247cYou have already chosen a kit!");
        } else if (gamePlayer.getGame().getState() != GameState.WAITING) {
            sender.sendMessage("\247cYou can't pick a kit at this time!");
        } else if (args.length > 1) {
            KitController.Kit kit = KitController.get().getByName(args[1]);

            if (kit == null) {
                sender.sendMessage("\247cNo such kit!");
            } else if (!KitController.get().hasPermission(player, kit)) {
                if (KitController.get().isPurchaseAble(kit)) {
                    if (KitController.get().canPurchase(gamePlayer, kit)) {
                        gamePlayer.setScore(gamePlayer.getScore() - kit.getPoints());
                        giveKit(gamePlayer, kit);

                    } else {
                        sender.sendMessage("\2474Error:\247c: No enough score to purchase this kit!");
                    }

                } else {
                    sender.sendMessage("\2474Error:\247c No permission to use this kit!");
                }
            } else {
                giveKit(gamePlayer, kit);
            }

        } else {
            List<String> availableKits = KitController.get().getAvailableKits(gamePlayer);

            player.sendMessage("\2477[\247cSkyWars\2477]: \247eAvailable kits: " + StringUtils.toString(availableKits, 'c', 'a'));
            player.sendMessage("\2477[\247cSkyWars\2477]: \247aUse /sw kit <name> to pick a kit.");
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    private void giveKit(GamePlayer gamePlayer, KitController.Kit kit) {
        KitController.get().populateInventory(gamePlayer.getBukkitPlayer().getInventory(), kit);
        gamePlayer.getBukkitPlayer().updateInventory();
        gamePlayer.setChosenKit(true);
        gamePlayer.getBukkitPlayer().sendMessage("\247aEnjoy your kit!");
    }
}