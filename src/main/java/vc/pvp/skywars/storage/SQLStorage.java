package vc.pvp.skywars.storage;

import org.bukkit.Bukkit;
import vc.pvp.skywars.SkyWars;
import vc.pvp.skywars.config.PluginConfig;
import vc.pvp.skywars.database.Database;
import vc.pvp.skywars.player.GamePlayer;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLStorage extends DataStorage {

    @Override
    public void loadPlayer(@Nonnull final GamePlayer gamePlayer) {
        Bukkit.getScheduler().runTaskAsynchronously(SkyWars.get(), new Runnable() {
            @Override
            public void run() {
                Database database = SkyWars.getDB();

                if (!database.checkConnection()) {
                    return;
                }

                if (!database.doesPlayerExist(gamePlayer.getName())) {
                    database.createNewPlayer(gamePlayer.getName());

                } else {
                    Connection connection = database.getConnection();
                    PreparedStatement preparedStatement = null;
                    ResultSet resultSet = null;

                    try {
                        StringBuilder queryBuilder = new StringBuilder();
                        queryBuilder.append("SELECT `score`, `games_played`, `games_won`, `kills`, `deaths` ");
                        queryBuilder.append("FROM `skywars_player` ");
                        queryBuilder.append("WHERE `player_name` = ? ");
                        queryBuilder.append("LIMIT 1;");

                        preparedStatement = connection.prepareStatement(queryBuilder.toString());
                        preparedStatement.setString(1, gamePlayer.getName());
                        resultSet = preparedStatement.executeQuery();

                        if (resultSet != null && resultSet.next()) {
                            if (!PluginConfig.useEconomy()) {
                                gamePlayer.setScore(resultSet.getInt("score"));
                            }
                            gamePlayer.setGamesPlayed(resultSet.getInt("games_played"));
                            gamePlayer.setGamesWon(resultSet.getInt("games_won"));
                            gamePlayer.setKills(resultSet.getInt("kills"));
                            gamePlayer.setDeaths(resultSet.getInt("deaths"));
                        }

                    } catch (final SQLException sqlException) {
                        sqlException.printStackTrace();

                    } finally {
                        if (resultSet != null) {
                            try {
                                resultSet.close();
                            } catch (final SQLException ignored) {
                            }
                        }

                        if (preparedStatement != null) {
                            try {
                                preparedStatement.close();
                            } catch (final SQLException ignored) {
                            }
                        }
                    }
                }
            }
        });
    }

    public final SaveProcessor saveProcessor = new SaveProcessor();

    public static class SaveProcessor implements Runnable {

        private final java.util.concurrent.LinkedBlockingQueue<Runnable> taskQueue = new java.util.concurrent.LinkedBlockingQueue<Runnable>();
        private final Thread thread = new Thread(this);
        private boolean running = true;

        public SaveProcessor() {
            thread.start();
        }

        public void submit(Runnable runnable) {
            try {
                taskQueue.put(runnable);
            } catch (InterruptedException ignored) {
            }
        }

        public boolean isEmpty() {
            return taskQueue.isEmpty();
        }

        public void stop() {
            running = false;
            thread.interrupt();
        }

        @Override
        public void run() {
            while (running) {
                try {
                    taskQueue.take().run();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    @Override
    public void savePlayer(@Nonnull final GamePlayer gamePlayer) {
        saveProcessor.submit(new Runnable() {
            @Override
            public void run() {
                Database database = SkyWars.getDB();

                if (!database.checkConnection()) {
                    return;
                }

                Connection connection = database.getConnection();
                PreparedStatement preparedStatement = null;

                try {
                    StringBuilder queryBuilder = new StringBuilder();
                    queryBuilder.append("UPDATE `skywars_player` SET ");
                    queryBuilder.append("`score` = ?, `games_played` = ?, ");
                    queryBuilder.append("`games_won` = ?, `kills` = ?, ");
                    queryBuilder.append("`deaths` = ?, `last_seen` = NOW() ");
                    queryBuilder.append("WHERE `player_name` = ?;");

                    preparedStatement = connection.prepareStatement(queryBuilder.toString());
                    preparedStatement.setInt(1, gamePlayer.getScore());
                    preparedStatement.setInt(2, gamePlayer.getGamesPlayed());
                    preparedStatement.setInt(3, gamePlayer.getGamesWon());
                    preparedStatement.setInt(4, gamePlayer.getKills());
                    preparedStatement.setInt(5, gamePlayer.getDeaths());
                    preparedStatement.setString(6, gamePlayer.getName());
                    preparedStatement.executeUpdate();

                } catch (final SQLException sqlException) {
                    sqlException.printStackTrace();

                } finally {
                    if (preparedStatement != null) {
                        try {
                            preparedStatement.close();
                        } catch (final SQLException ignored) {
                        }
                    }
                }
            }
        });
    }
}
