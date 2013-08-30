package vc.pvp.skywars.build;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import org.bukkit.scheduler.BukkitRunnable;
import vc.pvp.skywars.SkyWars;

import java.util.List;

public class BlockBuilder extends BukkitRunnable {

    private EditSession editSession;
    private List<BlockBuilderEntry> vectorList;
    private List<BlockBuilderEntry> delayedList;
    private int blocksPerTick;
    private BuildFinishedHandler buildFinishedHandler;

    public BlockBuilder(EditSession editSession, List<BlockBuilderEntry> vectorList, List<BlockBuilderEntry> delayedList,
                        int blocksPerTick, BuildFinishedHandler buildFinishedHandler) {
        this.editSession = editSession;
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
        try {
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
        } catch (MaxChangedBlocksException ex) {
            cancel();
            buildFinishedHandler.onBuildFinish();
        }
    }

    private void place(BlockBuilderEntry entry) throws MaxChangedBlocksException {
        editSession.setBlock(entry.getLocation(), entry.getBlock());
    }

    public interface BuildFinishedHandler {

        void onBuildFinish();
    }
}
