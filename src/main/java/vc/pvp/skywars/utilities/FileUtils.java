package vc.pvp.skywars.utilities;

import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.logging.Level;

public class FileUtils {

    public static boolean deleteFolder(@Nonnull File file) {
        if (file.exists()) {
            boolean result = true;

            if (file.isDirectory()) {
                File[] contents = file.listFiles();

                if (contents != null) {
                    for (File f : contents) {
                        result = result && deleteFolder(f);
                    }
                }
            }

            return result && file.delete();
        }

        return false;
    }

    public static void saveResource(Plugin plugin, String resourcePath, File outFile, boolean replace) {
        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = plugin.getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found.");
        }

        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(plugin.getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));

        if (!outDir.exists() && !outDir.mkdirs()) {
            return;
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else {
                plugin.getLogger().log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
            }

        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }
}
