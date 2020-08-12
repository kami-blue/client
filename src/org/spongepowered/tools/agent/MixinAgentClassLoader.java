// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.agent;

import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.lib.MethodVisitor;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.lib.ClassWriter;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.Logger;

class MixinAgentClassLoader extends ClassLoader
{
    private static final Logger logger;
    private Map<Class<?>, byte[]> mixins;
    private Map<String, byte[]> targets;
    
    MixinAgentClassLoader() {
        this.mixins = new HashMap<Class<?>, byte[]>();
        this.targets = new HashMap<String, byte[]>();
    }
    
    void addMixinClass(final String name) {
        MixinAgentClassLoader.logger.debug("Mixin class {} added to class loader", new Object[] { name });
        try {
            final byte[] bytes = this.materialise(name);
            final Class<?> clazz = this.defineClass(name, bytes, 0, bytes.length);
            clazz.newInstance();
            this.mixins.put(clazz, bytes);
        }
        catch (Throwable e) {
            MixinAgentClassLoader.logger.catching(e);
        }
    }
    
    void addTargetClass(final String name, final byte[] bytecode) {
        this.targets.put(name, bytecode);
    }
    
    byte[] getFakeMixinBytecode(final Class<?> clazz) {
        return this.mixins.get(clazz);
    }
    
    byte[] getOriginalTargetBytecode(final String name) {
        return this.targets.get(name);
    }
    
    private byte[] materialise(final String name) {
        final ClassWriter cw = new ClassWriter(3);
        cw.visit(MixinEnvironment.getCompatibilityLevel().classVersion(), 1, name.replace('.', '/'), null, Type.getInternalName(Object.class), null);
        final MethodVisitor mv = cw.visitMethod(1, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(25, 0);
        mv.visitMethodInsn(183, Type.getInternalName(Object.class), "<init>", "()V", false);
        mv.visitInsn(177);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }
    
    static {
        logger = LogManager.getLogger("mixin.agent");
    }
}
