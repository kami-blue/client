// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation.mirror;

import org.spongepowered.asm.obfuscation.mapping.IMapping;
import com.google.common.base.Strings;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;

public class FieldHandle extends MemberHandle<MappingField>
{
    private final VariableElement element;
    private final boolean rawType;
    
    public FieldHandle(final TypeElement owner, final VariableElement element) {
        this(TypeUtils.getInternalName(owner), element);
    }
    
    public FieldHandle(final String owner, final VariableElement element) {
        this(owner, element, false);
    }
    
    public FieldHandle(final TypeElement owner, final VariableElement element, final boolean rawType) {
        this(TypeUtils.getInternalName(owner), element, rawType);
    }
    
    public FieldHandle(final String owner, final VariableElement element, final boolean rawType) {
        this(owner, element, rawType, TypeUtils.getName(element), TypeUtils.getInternalName(element));
    }
    
    public FieldHandle(final String owner, final String name, final String desc) {
        this(owner, null, false, name, desc);
    }
    
    private FieldHandle(final String owner, final VariableElement element, final boolean rawType, final String name, final String desc) {
        super(owner, name, desc);
        this.element = element;
        this.rawType = rawType;
    }
    
    public boolean isImaginary() {
        return this.element == null;
    }
    
    public VariableElement getElement() {
        return this.element;
    }
    
    @Override
    public Visibility getVisibility() {
        return TypeUtils.getVisibility(this.element);
    }
    
    public boolean isRawType() {
        return this.rawType;
    }
    
    @Override
    public MappingField asMapping(final boolean includeOwner) {
        return new MappingField(includeOwner ? this.getOwner() : null, this.getName(), this.getDesc());
    }
    
    @Override
    public String toString() {
        final String owner = (this.getOwner() != null) ? ("L" + this.getOwner() + ";") : "";
        final String name = Strings.nullToEmpty(this.getName());
        final String desc = Strings.nullToEmpty(this.getDesc());
        return String.format("%s%s:%s", owner, name, desc);
    }
}
