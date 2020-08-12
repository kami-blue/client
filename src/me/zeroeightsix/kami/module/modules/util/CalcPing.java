// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.util;

import net.minecraft.launchwrapper.LogWrapper;
import net.minecraft.client.Minecraft;

public class CalcPing
{
    public static int globalInfoPingValue() {
        final Minecraft mc = Minecraft.func_71410_x();
        if (mc.func_147114_u() == null) {
            return 1;
        }
        if (mc.field_71439_g == null) {
            return -1;
        }
        try {
            return mc.func_147114_u().func_175102_a(mc.field_71439_g.func_110124_au()).func_178853_c();
        }
        catch (NullPointerException npe) {
            LogWrapper.info("Caught NPE l25 CalcPing.java", new Object[0]);
            return -1;
        }
    }
}
