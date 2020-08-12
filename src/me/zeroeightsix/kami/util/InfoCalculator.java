// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util;

import me.zeroeightsix.kami.module.ModuleManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import me.zeroeightsix.kami.module.modules.gui.InfoOverlay;
import java.text.DecimalFormat;
import me.zeroeightsix.kami.module.Module;

public class InfoCalculator extends Module
{
    private static DecimalFormat formatter;
    private static InfoOverlay info;
    
    public static int ping() {
        if (InfoCalculator.mc.func_147114_u() == null) {
            return 1;
        }
        if (InfoCalculator.mc.field_71439_g == null) {
            return -1;
        }
        try {
            return InfoCalculator.mc.func_147114_u().func_175102_a(InfoCalculator.mc.field_71439_g.func_110124_au()).func_178853_c();
        }
        catch (NullPointerException ex) {
            return -1;
        }
    }
    
    public static String speed() {
        final float currentTps = InfoCalculator.mc.field_71428_T.field_194147_b / 1000.0f;
        if (InfoCalculator.info.useUnitKmH()) {
            return InfoCalculator.formatter.format(MathHelper.func_76133_a(Math.pow(coordsDiff("x"), 2.0) + Math.pow(coordsDiff("y"), 2.0)) / currentTps * 3.6);
        }
        return InfoCalculator.formatter.format(MathHelper.func_76133_a(Math.pow(coordsDiff("x"), 2.0) + Math.pow(coordsDiff("y"), 2.0)) / currentTps);
    }
    
    private static double coordsDiff(final String s) {
        switch (s) {
            case "x": {
                return InfoCalculator.mc.field_71439_g.field_70165_t - InfoCalculator.mc.field_71439_g.field_70169_q;
            }
            case "z": {
                return InfoCalculator.mc.field_71439_g.field_70161_v - InfoCalculator.mc.field_71439_g.field_70166_s;
            }
            default: {
                return 0.0;
            }
        }
    }
    
    public static int dura() {
        final ItemStack itemStack = Wrapper.getMinecraft().field_71439_g.func_184614_ca();
        return itemStack.func_77958_k() - itemStack.func_77952_i();
    }
    
    public static String memory() {
        return "" + Runtime.getRuntime().freeMemory() / 1000000L;
    }
    
    public static String tps() {
        return "" + Math.round(LagCompensator.INSTANCE.getTickRate());
    }
    
    public static double round(final double value, final int places) {
        final double scale = Math.pow(10.0, places);
        return Math.round(value * scale) / scale;
    }
    
    static {
        InfoCalculator.formatter = new DecimalFormat("#.#");
        InfoCalculator.info = (InfoOverlay)ModuleManager.getModuleByName("InfoOverlay");
    }
}
