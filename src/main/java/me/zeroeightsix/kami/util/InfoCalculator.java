package me.zeroeightsix.kami.util;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.text.DecimalFormat;

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 18/01/20
 * Updated by dominikaaaa on 06/02/20
 * <p>
 * Speed:
 * @author dominikaaaa
 * Created by dominikaaaa on 18/01/20
 * Credit to Seppuku for the following calculation I made more efficient and got inspiration from
 * final String bps = "BPS: " + df.format((MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ) / tickRate));
 * <p>
 * Durability:
 * @author TBM
 * Created by TBM on 8/12/19
 * <p>
 * TPS:
 * @author 086
 */
public class InfoCalculator {

    public static String getServerType(Minecraft mc) {
        if (mc.getCurrentServerData() != null) {
            return mc.getCurrentServerData().serverIP;
        } else if (mc.isIntegratedServerRunning()) {
            return "Singleplayer";
        }
        return "Main Menu";
    }

    // Ping {
    public static int ping(Minecraft mc) {
        if (mc.getConnection() == null) { // tested, this is not null in mp
            return 1;
        } else if (mc.player == null) { // this actually takes about 30 seconds to load in Minecraft
            return -1;
        } else {
            try {
                return mc.getConnection().getPlayerInfo(mc.player.getUniqueID()).getResponseTime();
            } catch (NullPointerException ignored) {
            }
            return -1;
        }
    }
    // }

    // Speed {
    private static DecimalFormat formatter = new DecimalFormat("#.#");

    public static String speed(boolean useUnitKmH, Minecraft mc, int places) {
        float currentTps = mc.timer.tickLength / 1000.0f;
        double multiply = 1.0;
        if (useUnitKmH) multiply = 3.6; // convert mps to kmh
        return "" + MathsUtils.round(((MathHelper.sqrt(Math.pow(coordsDiff('x', mc), 2) + Math.pow(coordsDiff('z', mc), 2)) / currentTps)) * multiply, places);
    }

    private static double coordsDiff(char s, Minecraft mc) {
        switch (s) {
            case 'x':
                return mc.player.posX - mc.player.prevPosX;
            case 'z':
                return mc.player.posZ - mc.player.prevPosZ;
            default:
                return 0.0;
        }
    }
    // }

    // Durability {
    public static int dura(Minecraft mc) {
        ItemStack itemStack = mc.player.getHeldItemMainhand();
        return itemStack.getMaxDamage() - itemStack.getItemDamage();
    }
    // }

    // Memory {
    public static String memory() {
        return "" + (Runtime.getRuntime().freeMemory() / 1000000);
    }
    // }

    // Ticks Per Second {
    public static double tps(int places) {
        return MathsUtils.round(LagCompensator.INSTANCE.getTickRate(), places);
    }
    // }

    // Dimension {
    public static String playerDimension(Minecraft mc) {
        if (mc.player == null) return "No Dimension";
        switch (mc.player.dimension) {
            case -1:
                return "Nether";
            case 0:
                return "Overworld";
            case 1:
                return "End";
            default:
                return "No Dimension";
        }
    }
}
