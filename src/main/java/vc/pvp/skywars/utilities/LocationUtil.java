package vc.pvp.skywars.utilities;

import org.bukkit.Location;
import org.bukkit.World;

public class LocationUtil {

    public static Location getLocation(World world, String coordinates) {
        String[] chunks = coordinates.split(" ");

        double posX = Double.parseDouble(chunks[0]);
        double posY = Double.parseDouble(chunks[1]);
        double posZ = Double.parseDouble(chunks[2]);

        float yaw = 0.0F;
        float pitch = 0.0F;

        if (chunks.length == 5) {
            yaw = (Float.parseFloat(chunks[3]) + 180.0F + 360.0F) % 360.0F;
            pitch = Float.parseFloat(chunks[4]);
        }

        return chunks.length == 5 ? new Location(world, posX, posY, posZ, yaw, pitch) : new Location(world, posX, posY, posZ);
    }
}