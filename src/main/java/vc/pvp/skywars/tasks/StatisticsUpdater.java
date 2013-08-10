package vc.pvp.skywars.tasks;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import vc.pvp.skywars.SkyWars;
import vc.pvp.skywars.config.PluginConfig;
import vc.pvp.skywars.controllers.StatisticsController;
import vc.pvp.skywars.database.Database;

public class StatisticsUpdater extends BukkitRunnable {

    public StatisticsUpdater() {
        runTaskTimerAsynchronously(SkyWars.get(), 20L, PluginConfig.getStatisticsUpdateInterval());
    }

    @Override
    public void run() {
        final StatisticsController statisticsController = StatisticsController.get();

        try {
            Database database = new Database(SkyWars.get().getConfig().getConfigurationSection("database"));
            statisticsController.setTopList(database.getTopScore(PluginConfig.getStatisticsTop()));
            database.close();

        } catch (Exception ignored) {
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(SkyWars.get(), new Runnable() {
            @Override
            public void run() {
                statisticsController.update();
            }
        });
    }
}
