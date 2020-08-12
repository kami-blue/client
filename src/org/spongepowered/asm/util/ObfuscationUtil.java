// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.util;

public abstract class ObfuscationUtil
{
    private ObfuscationUtil() {
    }
    
    public static String mapDescriptor(final String desc, final IClassRemapper remapper) {
        return remapDescriptor(desc, remapper, false);
    }
    
    public static String unmapDescriptor(final String desc, final IClassRemapper remapper) {
        return remapDescriptor(desc, remapper, true);
    }
    
    private static String remapDescriptor(final String desc, final IClassRemapper remapper, final boolean unmap) {
        final StringBuilder sb = new StringBuilder();
        StringBuilder token = null;
        for (int pos = 0; pos < desc.length(); ++pos) {
            final char c = desc.charAt(pos);
            if (token != null) {
                if (c == ';') {
                    sb.append('L').append(remap(token.toString(), remapper, unmap)).append(';');
                    token = null;
                }
                else {
                    token.append(c);
                }
            }
            else if (c == 'L') {
                token = new StringBuilder();
            }
            else {
                sb.append(c);
            }
        }
        if (token != null) {
            throw new IllegalArgumentException("Invalid descriptor '" + desc + "', missing ';'");
        }
        return sb.toString();
    }
    
    private static Object remap(final String typeName, final IClassRemapper remapper, final boolean unmap) {
        final String result = unmap ? remapper.unmap(typeName) : remapper.map(typeName);
        return (result != null) ? result : typeName;
    }
    
    public interface IClassRemapper
    {
        String map(final String p0);
        
        String unmap(final String p0);
    }
}
