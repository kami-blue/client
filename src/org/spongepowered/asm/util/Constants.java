// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.util;

import org.spongepowered.asm.mixin.Mixin;
import java.io.File;

public abstract class Constants
{
    public static final String CTOR = "<init>";
    public static final String CLINIT = "<clinit>";
    public static final String IMAGINARY_SUPER = "super$";
    public static final String DEBUG_OUTPUT_PATH = ".mixin.out";
    public static final String MIXIN_PACKAGE;
    public static final String MIXIN_PACKAGE_REF;
    public static final String STRING = "Ljava/lang/String;";
    public static final String OBJECT = "Ljava/lang/Object;";
    public static final String CLASS = "Ljava/lang/Class;";
    public static final String SYNTHETIC_PACKAGE = "org.spongepowered.asm.synthetic";
    public static final char UNICODE_SNOWMAN = '\u2603';
    public static final File DEBUG_OUTPUT_DIR;
    
    private Constants() {
    }
    
    static {
        MIXIN_PACKAGE = Mixin.class.getPackage().getName();
        MIXIN_PACKAGE_REF = Constants.MIXIN_PACKAGE.replace('.', '/');
        DEBUG_OUTPUT_DIR = new File(".mixin.out");
    }
    
    public abstract static class ManifestAttributes
    {
        public static final String TWEAKER = "TweakClass";
        public static final String MAINCLASS = "Main-Class";
        public static final String MIXINCONFIGS = "MixinConfigs";
        public static final String TOKENPROVIDERS = "MixinTokenProviders";
        @Deprecated
        public static final String COMPATIBILITY = "MixinCompatibilityLevel";
        
        private ManifestAttributes() {
        }
    }
}
