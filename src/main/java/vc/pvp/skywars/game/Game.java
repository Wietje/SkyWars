package vc.pvp.skywars.game;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.CuboidClipboard;
import org.bukkit.*;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import vc.pvp.skywars.config.PluginConfig;
import vc.pvp.skywars.controllers.GameController;
import vc.pvp.skywars.controllers.KitController;
import vc.pvp.skywars.controllers.PlayerController;
import vc.pvp.skywars.controllers.WorldController;
import vc.pvp.skywars.player.GamePlayer;
import vc.pvp.skywars.utilities.PlayerUtil;
import vc.pvp.skywars.utilities.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Game {

    public static final String PREFIX = "\2477[\247cSkyWars\2477]: ";
    private GameState gameState;
    private Map<Integer, GamePlayer> idPlayerMap = Maps.newLinkedHashMap();
    private Map<GamePlayer, Integer> playerIdMap = Maps.newHashMap();
    private int playerCount = 0;
    private int slots;
    private Map<Integer, Location> spawnPlaces = Maps.newHashMap();
    private int timer;
    private Scoreboard scoreboard;
    private Objective objective;
    private boolean built;

    private World world;
    private int[] islandCoordinates;
    private List<Location> chestList = Lists.newArrayList();

    public Game(CuboidClipboard schematic) {
        world = WorldController.get().create(this, schematic);
        slots = spawnPlaces.size();
        gameState = GameState.WAITING;

        for (int iii = 0; iii < slots; iii++) {
            idPlayerMap.put(iii, null);
        }
    }

    public boolean isBuilt() {
        return built;
    }

    public void setBuilt(boolean built) {
        this.built = built;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public void addPopulatedChest(Location location) {
        chestList.add(location);
    }

    public boolean isChestPopulated(Location location) {
        return chestList.contains(location);
    }

    public boolean isReady() {
        return slots >= 2;
    }

    public World getWorld() {
        return world;
    }

    public void setIslandCoordinates(int[] coordinates) {
        islandCoordinates = coordinates;
    }

    public int[] getIslandCoordinates() {
        return islandCoordinates;
    }

    public void onPlayerJoin(GamePlayer gamePlayer) {
        Player player = gamePlayer.getBukkitPlayer();

        int id = getFistEmpty();
        playerCount++;
        idPlayerMap.put(getFistEmpty(), gamePlayer);
        playerIdMap.put(gamePlayer, id);

        sendMessage("&6%s &ehas joined the game (%d/%d)", player.getName(), getPlayerCount(), slots);
        if (slots - playerCount != 0) {
            sendMessage("&e%d &6more players are needed before the game starts", slots - playerCount);
        }

        PlayerUtil.refreshPlayer(player);

        if (player.getGameMode() != GameMode.SURVIVAL) {
            player.setGameMode(GameMode.SURVIVAL);
        }

        gamePlayer.setGame(this);
        gamePlayer.setChosenKit(false);
        gamePlayer.setSkipFallDamage(true);
        player.teleport(getSpawn(id).clone().add(0.5, 0.5, 0.5));

        List<String> availableKits = KitController.get().getAvailableKits(gamePlayer);

        player.sendMessage(PREFIX + "\247eAvailable kits: " + StringUtils.toString(availableKits, 'c', 'a'));
        player.sendMessage(PREFIX + "\247aUse /sw kit <name> to pick a kit.");

        if (!PluginConfig.buildSchematic()) {
            timer = 11;
        }
    }

    public void onPlayerLeave(GamePlayer gamePlayer) {
        onPlayerLeave(gamePlayer, true, true, true);
    }

    public void onPlayerLeave(GamePlayer gamePlayer, boolean displayText, boolean process, boolean left) {
        Player player = gamePlayer.getBukkitPlayer();

        if (displayText) {
            if (left && gameState == GameState.PLAYING) {
                int scorePerLeave = PluginConfig.getScorePerLeave(player);
                gamePlayer.addScore(scorePerLeave);

                sendMessage("&6%s &ehas left the game %s", player.getName(), StringUtils.formatScore(scorePerLeave, " score"));
            } else {
                sendMessage("&6%s &ehas left the game (%d/%d)", player.getName(), getPlayerCount() - 1, slots);
            }
        }

        if (scoreboard != null) {
            objective.getScore(player).setScore(-playerCount);
            try {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            } catch (IllegalStateException ignored) {

            }
        }

        playerCount--;
        idPlayerMap.put(playerIdMap.remove(gamePlayer), null);
        gamePlayer.setGame(null);
        gamePlayer.setChosenKit(false);
        PlayerUtil.refreshPlayer(player);
        player.teleport(PluginConfig.getLobbySpawn());

        if (process && gameState == GameState.PLAYING && playerCount == 1) {
            onGameEnd(getWinner());
        }
    }

    public void onPlayerDeath(final GamePlayer gamePlayer, PlayerDeathEvent event) {
        final Player player = gamePlayer.getBukkitPlayer();
        Player killer = player.getKiller();

        int scorePerDeath = PluginConfig.getScorePerDeath(player);
        gamePlayer.addScore(scorePerDeath);
        gamePlayer.setDeaths(gamePlayer.getDeaths() + 1);

        if (killer != null) {
            GamePlayer gameKiller = PlayerController.get().get(killer);

            int scorePerKill = PluginConfig.getScorePerKill(killer);
            gameKiller.addScore(scorePerKill);
            gameKiller.setKills(gameKiller.getKills() + 1);

            sendMessage(
                    "\2479%s %s\2476 has been killed by \2479%s %s\2476.",
                    player.getName(), StringUtils.formatScore(scorePerDeath, " score"),
                    killer.getName(), StringUtils.formatScore(scorePerKill, " score"));
        } else {
            sendMessage("\2479%s\2476 has been killed %s\2476.", player.getName(), StringUtils.formatScore(scorePerDeath, " score"));
        }
        sendMessage("\247b%d\2476 player(s) remain!", playerCount - 1);

        for (GamePlayer gp : getPlayers()) {
            if (gp.equals(gamePlayer)) {
                gp.getBukkitPlayer().sendMessage(String.format("%s\2475You have been eliminated. Better \247cluck\2475 next time!", PREFIX));
            } else {
                gp.getBukkitPlayer().sendMessage(String.format("%s\2476Player \2479%s\2476 has been eliminated!", PREFIX, player.getName()));
            }
        }

        if (event != null) {
            Location location = player.getLocation().clone();
            World world = location.getWorld();

            for (ItemStack itemStack : event.getDrops()) {
                world.dropItemNaturally(location, itemStack);
            }

            world.spawn(location, ExperienceOrb.class).setExperience(event.getDroppedExp());

            event.setDeathMessage(null);
            event.getDrops().clear();
            event.setDroppedExp(0);

            onPlayerLeave(gamePlayer, false, true, false);

        } else {
            onPlayerLeave(gamePlayer, false, true, false);
        }
    }

    public void onGameStart() {
        registerScoreboard();
        gameState = GameState.PLAYING;

        for (Map.Entry<Integer, GamePlayer> playerEntry : idPlayerMap.entrySet()) {
            GamePlayer gamePlayer = playerEntry.getValue();

            if (gamePlayer != null) {
                objective.getScore(gamePlayer.getBukkitPlayer()).setScore(0);
                getSpawn(playerEntry.getKey()).clone().add(0, -1D, 0).getBlock().setTypeId(0);
                gamePlayer.setGamesPlayed(gamePlayer.getGamesPlayed() + 1);
            }
        }

        for (GamePlayer gamePlayer : getPlayers()) {
            gamePlayer.getBukkitPlayer().setHealth(20D);
            gamePlayer.getBukkitPlayer().setFoodLevel(20);

            gamePlayer.getBukkitPlayer().setScoreboard(scoreboard);
            gamePlayer.getBukkitPlayer().sendMessage(String.format("%s\2475The battle has begun!", PREFIX));
        }
    }

    public void onGameEnd() {
        onGameEnd(null);
    }

    public void onGameEnd(GamePlayer gamePlayer) {
        if (gamePlayer != null) {
            Player player = gamePlayer.getBukkitPlayer();
            int score = PluginConfig.getScorePerWin(player);
            gamePlayer.addScore(score);
            gamePlayer.setGamesWon(gamePlayer.getGamesWon() + 1);
            Bukkit.broadcastMessage(String.format("%s\2476%s\247e has won SkyWars \247a(+%d score)\247e!", PREFIX, player.getName(), score));
        }

        for (GamePlayer player : getPlayers()) {
            onPlayerLeave(player, false, false, false);
        }

        gameState = GameState.ENDING;
        unregisterScoreboard();

        WorldController.get().unload(this);
        GameController.get().remove(this);
    }

    public void onTick() {
        if (timer <= 0 || (gameState == GameState.WAITING && !isFull())) {
            return;
        }

        timer--;

        switch (gameState) {
            case WAITING:
                if (timer == 0) {
                    onGameStart();
                } else if (timer % 10 == 0 || timer <= 5) {
                    sendMessage(true, "\247eGame starting in \247c%d\247e seconds!", timer);
                }
                break;

            case PLAYING:
                break;
        }
    }

    public GameState getState() {
        return gameState;
    }

    public boolean isFull() {
        return getPlayerCount() == slots;
    }

    public void sendMessage(String message, Object... args) {
        sendMessage(true, message, args);
    }

    public void sendMessage(boolean withPrefix, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }

        message = ChatColor.translateAlternateColorCodes('&', message);
        if (withPrefix) {
            message = PREFIX + message;
        }

        for (GamePlayer gamePlayer : getPlayers()) {
            gamePlayer.getBukkitPlayer().sendMessage(message);
        }
    }

    private GamePlayer getWinner() {
        for (GamePlayer gamePlayer : idPlayerMap.values()) {
            if (gamePlayer == null) {
                continue;
            }

            return gamePlayer;
        }

        return null;
    }

    private int getFistEmpty() {
        for (Map.Entry<Integer, GamePlayer> playerEntry : idPlayerMap.entrySet()) {
            if (playerEntry.getValue() == null) {
                return playerEntry.getKey();
            }
        }

        return -1;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public Collection<GamePlayer> getPlayers() {
        List<GamePlayer> playerList = Lists.newArrayList();

        for (GamePlayer gamePlayer : idPlayerMap.values()) {
            if (gamePlayer != null) {
                playerList.add(gamePlayer);
            }
        }

        return playerList;
    }

    private Location getSpawn(int id) {
        return spawnPlaces.get(id);
    }

    public void addSpawn(int id, Location location) {
        spawnPlaces.put(id, location);
    }

    private void registerScoreboard() {
        if (scoreboard != null) {
            unregisterScoreboard();
        }

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("info", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("\247c\247lLeaderBoard");
    }

    private void unregisterScoreboard() {
        if (objective != null) {
            objective.unregister();
        }

        if (scoreboard != null) {
            scoreboard = null;
        }
    }
}
