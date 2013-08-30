package vc.pvp.skywars.build;

import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import vc.pvp.skywars.SkyWars;

import java.util.List;

public class BlockBuilder extends BukkitRunnable {

    private World world;
    private List<BlockBuilderEntry> vectorList;
    private List<BlockBuilderEntry> delayedList;
    private int blocksPerTick;
    private BuildFinishedHandler buildFinishedHandler;

    public BlockBuilder(World world, List<BlockBuilderEntry> vectorList, List<BlockBuilderEntry> delayedList,
                        int blocksPerTick, BuildFinishedHandler buildFinishedHandler) {
        this.world = world;
        this.vectorList = vectorList;
        this.delayedList = delayedList;
        this.blocksPerTick = blocksPerTick;
        this.buildFinishedHandler = buildFinishedHandler;
    }

    public void start(long delay, long period) {
        runTaskTimer(SkyWars.get(), delay, period);
    }

    @Override
    public void run() {
        for (int iii = 0; iii < blocksPerTick; iii++) {
            if (!vectorList.isEmpty()) {
                place(vectorList.remove(0));

            } else if (!delayedList.isEmpty()) {
                place(delayedList.remove(0));

            } else {
                cancel();
                buildFinishedHandler.onBuildFinish();

                break;
            }
        }
    }

    private void place(BlockBuilderEntry entry) {
        world.getBlockAt(
                entry.getLocation().getBlockX(),
                entry.getLocation().getBlockY(),
                entry.getLocation().getBlockZ())
        .setTypeIdAndData(
            entry.getBlock().getType(),
            (byte) entry.getBlock().getData(),
            false
        );
    }

    public interface BuildFinishedHandler {

        void onBuildFinish();
    }
}
