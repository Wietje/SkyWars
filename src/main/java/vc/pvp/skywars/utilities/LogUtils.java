package vc.pvp.skywars.utilities;

import vc.pvp.skywars.SkyWars;

import java.util.logging.Level;

public class LogUtils {

    public static void log(Level level, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }

        SkyWars.get().getLogger().log(level, message);
    }

    public static void log(Level level, Class<?> clazz, String message, Object... args) {
        log(level, String.format("[%s] %s", clazz.getSimpleName(), message), args);
    }
}
