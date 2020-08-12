// 
// Decompiled by Procyon v0.5.36
// 

package com.dazo66.shulkerboxshower.asm;

import java.util.List;
import net.minecraft.launchwrapper.IClassTransformer;

public interface IRegisterTransformer extends IClassTransformer
{
    List<String> getMcVersion();
    
    List<String> getClassName();
}
