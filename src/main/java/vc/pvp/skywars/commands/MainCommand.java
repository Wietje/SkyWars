package vc.pvp.skywars.commands;

import com.google.common.collect.Maps;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class MainCommand implements CommandExecutor {

    private Map<String, CommandExecutor> subCommandMap = Maps.newHashMap();

    public MainCommand() {
        subCommandMap.put("reload", new ReloadCommand());
        subCommandMap.put("kit", new KitCommand());
        subCommandMap.put("setlobby", new SetLobbyCommand());
        subCommandMap.put("start", new StartCommand());
        subCommandMap.put("leave", new LeaveCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\247cThis command may only be executed as a player");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            printHelp(player, label);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        if (!subCommandMap.containsKey(subCommandName)) {
            printHelp(player, label);
            return true;
        }

        CommandExecutor subCommand = subCommandMap.get(subCommandName);
        if (!hasPermission(player, subCommand)) {
            player.sendMessage("\247cInsufficient permissions!");
            return true;
        }

        return subCommand.onCommand(sender, command, label, args);
    }

    private boolean hasPermission(Player bukkitPlayer, CommandExecutor cmd) {
        CommandPermissions permissions = cmd.getClass().getAnnotation(CommandPermissions.class);
        if (permissions == null) {
            return true;
        }

        for (String permission : permissions.value()) {
            if (bukkitPlayer.hasPermission(permission)) {
                return true;
            }
        }

        return false;
    }

    private void printHelp(Player bukkitPlayer, String label) {
        bukkitPlayer.sendMessage("\2477[\247cSkyWars\2477 (build id: 5377)]: \247eAvailable commands:");

        for (Map.Entry<String, CommandExecutor> commandEntry : subCommandMap.entrySet())
            if (hasPermission(bukkitPlayer, commandEntry.getValue())) {
                String description = "No description available.";

                CommandDescription cmdDescription = commandEntry.getValue().getClass().getAnnotation(CommandDescription.class);
                if (cmdDescription != null) {
                    description = cmdDescription.value();
                }

                bukkitPlayer.sendMessage("\2477/" + label + " " + commandEntry.getKey() + " \247f-\247e " + description);
            }
    }
}