// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.util;

import org.spongepowered.asm.lib.Label;
import java.util.Map;

public interface Textifiable
{
    void textify(final StringBuffer p0, final Map<Label, String> p1);
}
