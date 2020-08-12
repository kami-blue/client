// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer;

import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.lib.AnnotationVisitor;
import org.spongepowered.asm.lib.commons.ClassRemapper;
import java.io.IOException;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.lib.commons.Remapper;
import org.apache.logging.log4j.LogManager;
import java.util.UUID;
import org.spongepowered.asm.lib.ClassWriter;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;
import org.spongepowered.asm.lib.ClassVisitor;
import org.spongepowered.asm.transformers.MixinClassWriter;
import org.spongepowered.asm.lib.ClassReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.transformer.ext.IClassGenerator;

final class InnerClassGenerator implements IClassGenerator
{
    private static final Logger logger;
    private final Map<String, String> innerClassNames;
    private final Map<String, InnerClassInfo> innerClasses;
    
    InnerClassGenerator() {
        this.innerClassNames = new HashMap<String, String>();
        this.innerClasses = new HashMap<String, InnerClassInfo>();
    }
    
    public String registerInnerClass(final MixinInfo owner, final String originalName, final MixinTargetContext context) {
        final String id = String.format("%s%s", originalName, context);
        String ref = this.innerClassNames.get(id);
        if (ref == null) {
            ref = getUniqueReference(originalName, context);
            this.innerClassNames.put(id, ref);
            this.innerClasses.put(ref, new InnerClassInfo(ref, originalName, owner, context));
            InnerClassGenerator.logger.debug("Inner class {} in {} on {} gets unique name {}", new Object[] { originalName, owner.getClassRef(), context.getTargetClassRef(), ref });
        }
        return ref;
    }
    
    @Override
    public byte[] generate(final String name) {
        final String ref = name.replace('.', '/');
        final InnerClassInfo info = this.innerClasses.get(ref);
        if (info != null) {
            return this.generate(info);
        }
        return null;
    }
    
    private byte[] generate(final InnerClassInfo info) {
        try {
            InnerClassGenerator.logger.debug("Generating mapped inner class {} (originally {})", new Object[] { info.getName(), info.getOriginalName() });
            final ClassReader cr = new ClassReader(info.getClassBytes());
            final ClassWriter cw = new MixinClassWriter(cr, 0);
            cr.accept(new InnerClassAdapter(cw, info), 8);
            return cw.toByteArray();
        }
        catch (InvalidMixinException ex) {
            throw ex;
        }
        catch (Exception ex2) {
            InnerClassGenerator.logger.catching((Throwable)ex2);
            return null;
        }
    }
    
    private static String getUniqueReference(final String originalName, final MixinTargetContext context) {
        String name = originalName.substring(originalName.lastIndexOf(36) + 1);
        if (name.matches("^[0-9]+$")) {
            name = "Anonymous";
        }
        return String.format("%s$%s$%s", context.getTargetClassRef(), name, UUID.randomUUID().toString().replace("-", ""));
    }
    
    static {
        logger = LogManager.getLogger("mixin");
    }
    
    static class InnerClassInfo extends Remapper
    {
        private final String name;
        private final String originalName;
        private final MixinInfo owner;
        private final MixinTargetContext target;
        private final String ownerName;
        private final String targetName;
        
        InnerClassInfo(final String name, final String originalName, final MixinInfo owner, final MixinTargetContext target) {
            this.name = name;
            this.originalName = originalName;
            this.owner = owner;
            this.ownerName = owner.getClassRef();
            this.target = target;
            this.targetName = target.getTargetClassRef();
        }
        
        String getName() {
            return this.name;
        }
        
        String getOriginalName() {
            return this.originalName;
        }
        
        MixinInfo getOwner() {
            return this.owner;
        }
        
        MixinTargetContext getTarget() {
            return this.target;
        }
        
        String getOwnerName() {
            return this.ownerName;
        }
        
        String getTargetName() {
            return this.targetName;
        }
        
        byte[] getClassBytes() throws ClassNotFoundException, IOException {
            return MixinService.getService().getBytecodeProvider().getClassBytes(this.originalName, true);
        }
        
        public String mapMethodName(final String owner, final String name, final String desc) {
            if (this.ownerName.equalsIgnoreCase(owner)) {
                final ClassInfo.Method method = this.owner.getClassInfo().findMethod(name, desc, 10);
                if (method != null) {
                    return method.getName();
                }
            }
            return super.mapMethodName(owner, name, desc);
        }
        
        public String map(final String key) {
            if (this.originalName.equals(key)) {
                return this.name;
            }
            if (this.ownerName.equals(key)) {
                return this.targetName;
            }
            return key;
        }
        
        public String toString() {
            return this.name;
        }
    }
    
    static class InnerClassAdapter extends ClassRemapper
    {
        private final InnerClassInfo info;
        
        public InnerClassAdapter(final ClassVisitor cv, final InnerClassInfo info) {
            super(327680, cv, info);
            this.info = info;
        }
        
        @Override
        public void visitSource(final String source, final String debug) {
            super.visitSource(source, debug);
            final AnnotationVisitor av = this.cv.visitAnnotation("Lorg/spongepowered/asm/mixin/transformer/meta/MixinInner;", false);
            av.visit("mixin", this.info.getOwner().toString());
            av.visit("name", this.info.getOriginalName().substring(this.info.getOriginalName().lastIndexOf(47) + 1));
            av.visitEnd();
        }
        
        @Override
        public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
            if (name.startsWith(this.info.getOriginalName() + "$")) {
                throw new InvalidMixinException(this.info.getOwner(), "Found unsupported nested inner class " + name + " in " + this.info.getOriginalName());
            }
            super.visitInnerClass(name, outerName, innerName, access);
        }
    }
}
