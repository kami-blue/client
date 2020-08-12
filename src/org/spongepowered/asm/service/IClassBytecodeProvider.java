// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.service;

import org.spongepowered.asm.lib.tree.ClassNode;
import java.io.IOException;

public interface IClassBytecodeProvider
{
    byte[] getClassBytes(final String p0, final String p1) throws IOException;
    
    byte[] getClassBytes(final String p0, final boolean p1) throws ClassNotFoundException, IOException;
    
    ClassNode getClassNode(final String p0) throws ClassNotFoundException, IOException;
}
