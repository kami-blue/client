// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer;

import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.mixin.Unique;
import java.lang.annotation.Annotation;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.mixin.transformer.meta.MixinRenamed;
import org.spongepowered.asm.lib.tree.MethodNode;
import java.util.Iterator;
import java.util.HashSet;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;
import java.util.Set;
import org.spongepowered.asm.lib.Type;

public final class InterfaceInfo
{
    private final MixinInfo mixin;
    private final String prefix;
    private final Type iface;
    private final boolean unique;
    private Set<String> methods;
    
    private InterfaceInfo(final MixinInfo mixin, final String prefix, final Type iface, final boolean unique) {
        if (prefix == null || prefix.length() < 2 || !prefix.endsWith("$")) {
            throw new InvalidMixinException(mixin, String.format("Prefix %s for iface %s is not valid", prefix, iface.toString()));
        }
        this.mixin = mixin;
        this.prefix = prefix;
        this.iface = iface;
        this.unique = unique;
    }
    
    private void initMethods() {
        this.methods = new HashSet<String>();
        this.readInterface(this.iface.getInternalName());
    }
    
    private void readInterface(final String ifaceName) {
        final ClassInfo interfaceInfo = ClassInfo.forName(ifaceName);
        for (final ClassInfo.Method ifaceMethod : interfaceInfo.getMethods()) {
            this.methods.add(ifaceMethod.toString());
        }
        for (final String superIface : interfaceInfo.getInterfaces()) {
            this.readInterface(superIface);
        }
    }
    
    public String getPrefix() {
        return this.prefix;
    }
    
    public Type getIface() {
        return this.iface;
    }
    
    public String getName() {
        return this.iface.getClassName();
    }
    
    public String getInternalName() {
        return this.iface.getInternalName();
    }
    
    public boolean isUnique() {
        return this.unique;
    }
    
    public boolean renameMethod(final MethodNode method) {
        if (this.methods == null) {
            this.initMethods();
        }
        if (!method.name.startsWith(this.prefix)) {
            if (this.methods.contains(method.name + method.desc)) {
                this.decorateUniqueMethod(method);
            }
            return false;
        }
        final String realName = method.name.substring(this.prefix.length());
        final String signature = realName + method.desc;
        if (!this.methods.contains(signature)) {
            throw new InvalidMixinException(this.mixin, String.format("%s does not exist in target interface %s", realName, this.getName()));
        }
        if ((method.access & 0x1) == 0x0) {
            throw new InvalidMixinException(this.mixin, String.format("%s cannot implement %s because it is not visible", realName, this.getName()));
        }
        Annotations.setVisible(method, MixinRenamed.class, "originalName", method.name, "isInterfaceMember", true);
        this.decorateUniqueMethod(method);
        method.name = realName;
        return true;
    }
    
    private void decorateUniqueMethod(final MethodNode method) {
        if (!this.unique) {
            return;
        }
        if (Annotations.getVisible(method, Unique.class) == null) {
            Annotations.setVisible(method, Unique.class, new Object[0]);
            this.mixin.getClassInfo().findMethod(method).setUnique(true);
        }
    }
    
    static InterfaceInfo fromAnnotation(final MixinInfo mixin, final AnnotationNode node) {
        final String prefix = Annotations.getValue(node, "prefix");
        final Type iface = Annotations.getValue(node, "iface");
        final Boolean unique = Annotations.getValue(node, "unique");
        if (prefix == null || iface == null) {
            throw new InvalidMixinException(mixin, String.format("@Interface annotation on %s is missing a required parameter", mixin));
        }
        return new InterfaceInfo(mixin, prefix, iface, unique != null && unique);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final InterfaceInfo that = (InterfaceInfo)o;
        return this.mixin.equals(that.mixin) && this.prefix.equals(that.prefix) && this.iface.equals(that.iface);
    }
    
    @Override
    public int hashCode() {
        int result = this.mixin.hashCode();
        result = 31 * result + this.prefix.hashCode();
        result = 31 * result + this.iface.hashCode();
        return result;
    }
}
