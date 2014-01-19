package vc.pvp.skywars.utilities;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.World;

public class WorldGenerator extends ChunkGenerator {

    public byte[] generate(World world, Random random, int cx, int cz) {
        return new byte[65536];
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        if (!world.isChunkLoaded(0, 0)) {
            world.loadChunk(0, 0);
        }
        return new Location(world, 0, 64, 0);
    }

}
