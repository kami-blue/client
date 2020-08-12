// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class JavaVersion
{
    private static double current;
    
    private JavaVersion() {
    }
    
    public static double current() {
        if (JavaVersion.current == 0.0) {
            JavaVersion.current = resolveCurrentVersion();
        }
        return JavaVersion.current;
    }
    
    private static double resolveCurrentVersion() {
        final String version = System.getProperty("java.version");
        final Matcher matcher = Pattern.compile("[0-9]+\\.[0-9]+").matcher(version);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group());
        }
        return 1.6;
    }
    
    static {
        JavaVersion.current = 0.0;
    }
}
