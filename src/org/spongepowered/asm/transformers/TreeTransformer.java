// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.transformers;

import org.spongepowered.asm.lib.ClassWriter;
import org.spongepowered.asm.lib.ClassVisitor;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.service.ILegacyClassTransformer;

public abstract class TreeTransformer implements ILegacyClassTransformer
{
    private ClassReader classReader;
    private ClassNode classNode;
    
    protected final ClassNode readClass(final byte[] basicClass) {
        return this.readClass(basicClass, true);
    }
    
    protected final ClassNode readClass(final byte[] basicClass, final boolean cacheReader) {
        final ClassReader classReader = new ClassReader(basicClass);
        if (cacheReader) {
            this.classReader = classReader;
        }
        final ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 8);
        return classNode;
    }
    
    protected final byte[] writeClass(final ClassNode classNode) {
        if (this.classReader != null && this.classNode == classNode) {
            this.classNode = null;
            final ClassWriter writer = new MixinClassWriter(this.classReader, 3);
            this.classReader = null;
            classNode.accept(writer);
            return writer.toByteArray();
        }
        this.classNode = null;
        final ClassWriter writer = new MixinClassWriter(3);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}
