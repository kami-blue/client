// 
// Decompiled by Procyon v0.5.36
// 

package the_fireplace.ias.legacysupport;

import net.minecraft.client.resources.I18n;

public class OldJava implements ILegacyCompat
{
    @Override
    public int[] getDate() {
        final int[] ret = { 0, 0, 0 };
        return ret;
    }
    
    @Override
    public String getFormattedDate() {
        return I18n.func_135052_a("ias.updatejava", new Object[0]);
    }
}
