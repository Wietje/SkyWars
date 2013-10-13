package vc.pvp.skywars.controllers;

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import vc.pvp.skywars.player.GamePlayer;
import vc.pvp.skywars.storage.DataStorage;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

public class PlayerController {

    private final Map<Player, GamePlayer> playerRegistry = Maps.newHashMap();

    private PlayerController() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            register(player);
        }
    }

    public GamePlayer register(@Nonnull Player bukkitPlayer) {
        GamePlayer gamePlayer = null;

        if (!this.playerRegistry.containsKey(bukkitPlayer)) {
            gamePlayer = new GamePlayer(bukkitPlayer);
            this.playerRegistry.put(bukkitPlayer, gamePlayer);
        }

        return gamePlayer;
    }

    public GamePlayer unregister(@Nonnull Player bukkitPlayer) {
        return this.playerRegistry.remove(bukkitPlayer);
    }

    public GamePlayer get(@Nonnull Player bukkitPlayer) {
        return this.playerRegistry.get(bukkitPlayer);
    }

    public Collection<GamePlayer> getAll() {
        return playerRegistry.values();
    }

    public void shutdown() {
        for (GamePlayer gamePlayer : playerRegistry.values()) {
            DataStorage.get().savePlayer(gamePlayer);
        }

        playerRegistry.clear();
    }

    private static PlayerController instance;

    public static PlayerController get() {
        if (instance == null) {
            instance = new PlayerController();
        }

        return instance;
    }
}
