// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.event;

import me.zeroeightsix.kami.util.Wrapper;
import me.zero.alpine.type.Cancellable;

public class KamiEvent extends Cancellable
{
    private Era era;
    private final float partialTicks;
    
    public KamiEvent() {
        this.era = Era.PRE;
        this.partialTicks = Wrapper.getMinecraft().func_184121_ak();
    }
    
    public Era getEra() {
        return this.era;
    }
    
    public float getPartialTicks() {
        return this.partialTicks;
    }
    
    public enum Era
    {
        PRE, 
        PERI, 
        POST;
    }
}
