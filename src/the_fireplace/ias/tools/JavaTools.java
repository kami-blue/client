// 
// Decompiled by Procyon v0.5.36
// 

package the_fireplace.ias.tools;

import the_fireplace.ias.legacysupport.OldJava;
import the_fireplace.ias.legacysupport.NewJava;
import the_fireplace.ias.legacysupport.ILegacyCompat;

public class JavaTools
{
    private static double getJavaVersion() {
        final String version = System.getProperty("java.version");
        int pos = version.indexOf(46);
        pos = version.indexOf(46, pos + 1);
        return Double.parseDouble(version.substring(0, pos));
    }
    
    public static ILegacyCompat getJavaCompat() {
        if (getJavaVersion() >= 1.8) {
            return new NewJava();
        }
        return new OldJava();
    }
}
