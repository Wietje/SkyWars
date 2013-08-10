package vc.pvp.skywars.utilities;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.MetaItemStack;
import org.bukkit.inventory.ItemStack;
import vc.pvp.skywars.SkyWars;

public class ItemUtils {

    public static ItemStack parseItem(String[] args) {
        if (args.length < 1) {
            return null;
        }

        IEssentials essentials = SkyWars.getEssentials();
        ItemStack itemStack = null;

        try {
            itemStack = essentials.getItemDb().get(args[0]);

            if (args.length > 1 && Integer.parseInt(args[1]) > 0) {
                itemStack.setAmount(Integer.parseInt(args[1]));
            }

            if (args.length > 2) {
                MetaItemStack metaItemStack = new MetaItemStack(itemStack);
                metaItemStack.parseStringMeta(null, true, args, 2, essentials);
                itemStack = metaItemStack.getItemStack();
            }

        } catch (Exception ignored) {

        }

        return itemStack;
    }
}
