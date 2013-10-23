package vc.pvp.skywars.player;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vc.pvp.skywars.SkyWars;
import vc.pvp.skywars.config.PluginConfig;
import vc.pvp.skywars.game.Game;
import vc.pvp.skywars.storage.DataStorage;

public class GamePlayer {

    private final Player bukkitPlayer;
    private final String playerName;
    private Game game;
    private boolean chosenKit;
    private int score;
    private int gamesPlayed;
    private int gamesWon;
    private int kills;
    private int deaths;
    private boolean skipFallDamage;
    private ItemStack[] savedInventoryContents = null;
    private ItemStack[] savedArmorContents = null;

    public GamePlayer(Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
        this.playerName = bukkitPlayer.getName();

        DataStorage.get().loadPlayer(this);
    }

    public void save() {
        DataStorage.get().savePlayer(this);
    }

    public Player getBukkitPlayer() {
        return bukkitPlayer;
    }

    public boolean isPlaying() {
        return game != null;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public boolean hasChosenKit() {
        return chosenKit;
    }

    public void setChosenKit(boolean yes) {
        chosenKit = yes;
    }

    public int getScore() {
        if (PluginConfig.useEconomy()) {
            return (int) SkyWars.getEconomy().getBalance(playerName);
        }

        return score;
    }

    public void setScore(int score) {
        if (PluginConfig.useEconomy()) {
            double balance = SkyWars.getEconomy().getBalance(playerName);
            if (balance < 0) {
                SkyWars.getEconomy().depositPlayer(playerName, -balance);
            } else {
                SkyWars.getEconomy().withdrawPlayer(playerName, balance);
            }
            addScore(score);

        } else {
            this.score = score;
        }
    }

    public void addScore(int score) {
        if (PluginConfig.useEconomy()) {
            if (score < 0) {
                SkyWars.getEconomy().withdrawPlayer(playerName, -score);
            } else {
                SkyWars.getEconomy().depositPlayer(playerName, score);
            }

        } else {
            this.score += score;
        }
    }

    @Override
    public String toString() {
        return playerName;
    }

    public String getName() {
        return playerName;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void setSkipFallDamage(boolean skipFallDamage) {
        this.skipFallDamage = skipFallDamage;
    }

    public boolean shouldSkipFallDamage() {
        return skipFallDamage;
    }

    public void saveCurrentState() {
        savedArmorContents = bukkitPlayer.getInventory().getArmorContents().clone();
        savedInventoryContents = bukkitPlayer.getInventory().getContents().clone();
    }

    @SuppressWarnings("deprecation")
    public void restoreState() {
        boolean shouldUpdateInventory = false;

        if (savedArmorContents != null) {
            bukkitPlayer.getInventory().setArmorContents(savedArmorContents);
            savedArmorContents = null;
            shouldUpdateInventory = true;
        }

        if (savedInventoryContents != null) {
            bukkitPlayer.getInventory().setContents(savedInventoryContents);
            savedInventoryContents = null;
            shouldUpdateInventory = true;
        }

        if (shouldUpdateInventory) {
            bukkitPlayer.updateInventory();
        }
    }
}
