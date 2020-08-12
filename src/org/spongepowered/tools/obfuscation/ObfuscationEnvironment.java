// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation;

import org.spongepowered.tools.obfuscation.mapping.IMappingConsumer;
import java.util.Collection;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.asm.util.ObfuscationUtil;
import javax.lang.model.type.TypeMirror;
import org.spongepowered.tools.obfuscation.mirror.TypeHandle;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import java.util.Iterator;
import java.io.File;
import javax.tools.Diagnostic;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import java.util.List;
import org.spongepowered.tools.obfuscation.interfaces.IMixinAnnotationProcessor;
import org.spongepowered.tools.obfuscation.mapping.IMappingWriter;
import org.spongepowered.tools.obfuscation.mapping.IMappingProvider;
import org.spongepowered.tools.obfuscation.interfaces.IObfuscationEnvironment;

public abstract class ObfuscationEnvironment implements IObfuscationEnvironment
{
    protected final ObfuscationType type;
    protected final IMappingProvider mappingProvider;
    protected final IMappingWriter mappingWriter;
    protected final RemapperProxy remapper;
    protected final IMixinAnnotationProcessor ap;
    protected final String outFileName;
    protected final List<String> inFileNames;
    private boolean initDone;
    
    protected ObfuscationEnvironment(final ObfuscationType type) {
        this.remapper = new RemapperProxy();
        this.type = type;
        this.ap = type.getAnnotationProcessor();
        this.inFileNames = type.getInputFileNames();
        this.outFileName = type.getOutputFileName();
        this.mappingProvider = this.getMappingProvider(this.ap, this.ap.getProcessingEnvironment().getFiler());
        this.mappingWriter = this.getMappingWriter(this.ap, this.ap.getProcessingEnvironment().getFiler());
    }
    
    @Override
    public String toString() {
        return this.type.toString();
    }
    
    protected abstract IMappingProvider getMappingProvider(final Messager p0, final Filer p1);
    
    protected abstract IMappingWriter getMappingWriter(final Messager p0, final Filer p1);
    
    private boolean initMappings() {
        if (!this.initDone) {
            this.initDone = true;
            if (this.inFileNames == null) {
                this.ap.printMessage(Diagnostic.Kind.ERROR, "The " + this.type.getConfig().getInputFileOption() + " argument was not supplied, obfuscation processing will not occur");
                return false;
            }
            int successCount = 0;
            for (final String inputFileName : this.inFileNames) {
                final File inputFile = new File(inputFileName);
                try {
                    if (!inputFile.isFile()) {
                        continue;
                    }
                    this.ap.printMessage(Diagnostic.Kind.NOTE, "Loading " + this.type + " mappings from " + inputFile.getAbsolutePath());
                    this.mappingProvider.read(inputFile);
                    ++successCount;
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (successCount < 1) {
                this.ap.printMessage(Diagnostic.Kind.ERROR, "No valid input files for " + this.type + " could be read, processing may not be sucessful.");
                this.mappingProvider.clear();
            }
        }
        return !this.mappingProvider.isEmpty();
    }
    
    public ObfuscationType getType() {
        return this.type;
    }
    
    @Override
    public MappingMethod getObfMethod(final MemberInfo method) {
        final MappingMethod obfd = this.getObfMethod(method.asMethodMapping());
        if (obfd != null || !method.isFullyQualified()) {
            return obfd;
        }
        final TypeHandle type = this.ap.getTypeProvider().getTypeHandle(method.owner);
        if (type == null || type.isImaginary()) {
            return null;
        }
        final TypeMirror superClass = type.getElement().getSuperclass();
        if (superClass.getKind() != TypeKind.DECLARED) {
            return null;
        }
        final String superClassName = ((TypeElement)((DeclaredType)superClass).asElement()).getQualifiedName().toString();
        return this.getObfMethod(new MemberInfo(method.name, superClassName.replace('.', '/'), method.desc, method.matchAll));
    }
    
    @Override
    public MappingMethod getObfMethod(final MappingMethod method) {
        return this.getObfMethod(method, true);
    }
    
    @Override
    public MappingMethod getObfMethod(final MappingMethod method, final boolean lazyRemap) {
        if (!this.initMappings()) {
            return null;
        }
        boolean remapped = true;
        MappingMethod mapping = null;
        for (MappingMethod md = method; md != null && mapping == null; mapping = this.mappingProvider.getMethodMapping(md), md = md.getSuper()) {}
        if (mapping == null) {
            if (lazyRemap) {
                return null;
            }
            mapping = method.copy();
            remapped = false;
        }
        final String remappedOwner = this.getObfClass(mapping.getOwner());
        if (remappedOwner == null || remappedOwner.equals(method.getOwner()) || remappedOwner.equals(mapping.getOwner())) {
            return remapped ? mapping : null;
        }
        if (remapped) {
            return mapping.move(remappedOwner);
        }
        final String desc = ObfuscationUtil.mapDescriptor(mapping.getDesc(), this.remapper);
        return new MappingMethod(remappedOwner, mapping.getSimpleName(), desc);
    }
    
    @Override
    public MemberInfo remapDescriptor(final MemberInfo method) {
        boolean transformed = false;
        String owner = method.owner;
        if (owner != null) {
            final String newOwner = this.remapper.map(owner);
            if (newOwner != null) {
                owner = newOwner;
                transformed = true;
            }
        }
        String desc = method.desc;
        if (desc != null) {
            final String newDesc = ObfuscationUtil.mapDescriptor(method.desc, this.remapper);
            if (!newDesc.equals(method.desc)) {
                desc = newDesc;
                transformed = true;
            }
        }
        return transformed ? new MemberInfo(method.name, owner, desc, method.matchAll) : null;
    }
    
    @Override
    public String remapDescriptor(final String desc) {
        return ObfuscationUtil.mapDescriptor(desc, this.remapper);
    }
    
    @Override
    public MappingField getObfField(final MemberInfo field) {
        return this.getObfField(field.asFieldMapping(), true);
    }
    
    @Override
    public MappingField getObfField(final MappingField field) {
        return this.getObfField(field, true);
    }
    
    @Override
    public MappingField getObfField(final MappingField field, final boolean lazyRemap) {
        if (!this.initMappings()) {
            return null;
        }
        MappingField mapping = this.mappingProvider.getFieldMapping(field);
        if (mapping == null) {
            if (lazyRemap) {
                return null;
            }
            mapping = field;
        }
        final String remappedOwner = this.getObfClass(mapping.getOwner());
        if (remappedOwner == null || remappedOwner.equals(field.getOwner()) || remappedOwner.equals(mapping.getOwner())) {
            return (mapping != field) ? mapping : null;
        }
        return mapping.move(remappedOwner);
    }
    
    @Override
    public String getObfClass(final String className) {
        if (!this.initMappings()) {
            return null;
        }
        return this.mappingProvider.getClassMapping(className);
    }
    
    @Override
    public void writeMappings(final Collection<IMappingConsumer> consumers) {
        final IMappingConsumer.MappingSet<MappingField> fields = new IMappingConsumer.MappingSet<MappingField>();
        final IMappingConsumer.MappingSet<MappingMethod> methods = new IMappingConsumer.MappingSet<MappingMethod>();
        for (final IMappingConsumer mappings : consumers) {
            fields.addAll((Collection<?>)mappings.getFieldMappings(this.type));
            methods.addAll((Collection<?>)mappings.getMethodMappings(this.type));
        }
        this.mappingWriter.write(this.outFileName, this.type, fields, methods);
    }
    
    final class RemapperProxy implements ObfuscationUtil.IClassRemapper
    {
        @Override
        public String map(final String typeName) {
            if (ObfuscationEnvironment.this.mappingProvider == null) {
                return null;
            }
            return ObfuscationEnvironment.this.mappingProvider.getClassMapping(typeName);
        }
        
        @Override
        public String unmap(final String typeName) {
            if (ObfuscationEnvironment.this.mappingProvider == null) {
                return null;
            }
            return ObfuscationEnvironment.this.mappingProvider.getClassMapping(typeName);
        }
    }
}
