package vc.pvp.skywars.controllers;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.CuboidClipboard;
import vc.pvp.skywars.SkyWars;
import vc.pvp.skywars.game.Game;
import vc.pvp.skywars.game.GameState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public class GameController {

    private static GameController instance;
    private List<Game> gameList = Lists.newArrayList();

    public Game findEmpty() {
        for (Game game : gameList) {
            if (game.getState() != GameState.PLAYING && !game.isFull()) {
                return game;
            }
        }

        return create();
    }

    public Game create() {
        CuboidClipboard schematic = SchematicController.get().getRandom();
        Game game = new Game(schematic);

        while (!game.isReady()) {
            String schematicName = SchematicController.get().getName(schematic);
            SkyWars.get().getLogger().log(Level.SEVERE, String.format("Schematic '%s' does not have any spawns set!", schematicName));
            SchematicController.get().remove(schematicName);

            schematic = SchematicController.get().getRandom();
            game = new Game(schematic);
        }

        gameList.add(game);
        return game;
    }

    public void remove(@Nonnull Game game) {
        gameList.remove(game);
    }

    public void shutdown() {
        for (Game game : new ArrayList<Game>(gameList)) {
            game.onGameEnd();
        }
    }

    public Collection<Game> getAll() {
        return gameList;
    }

    public static GameController get() {
        if (instance == null) {
            return instance = new GameController();
        }

        return instance;
    }
}
