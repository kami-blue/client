// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.struct;

import com.google.common.base.Strings;
import org.spongepowered.asm.mixin.refmap.IMixinContext;
import org.spongepowered.asm.mixin.refmap.IReferenceMapper;
import com.google.common.base.Objects;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.asm.util.SignaturePrinter;
import org.spongepowered.asm.mixin.throwables.MixinException;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.asm.obfuscation.mapping.IMapping;
import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;

public final class MemberInfo
{
    public final String owner;
    public final String name;
    public final String desc;
    public final boolean matchAll;
    private final boolean forceField;
    private final String unparsed;
    
    public MemberInfo(final String name, final boolean matchAll) {
        this(name, null, null, matchAll);
    }
    
    public MemberInfo(final String name, final String owner, final boolean matchAll) {
        this(name, owner, null, matchAll);
    }
    
    public MemberInfo(final String name, final String owner, final String desc) {
        this(name, owner, desc, false);
    }
    
    public MemberInfo(final String name, final String owner, final String desc, final boolean matchAll) {
        this(name, owner, desc, matchAll, null);
    }
    
    public MemberInfo(final String name, final String owner, final String desc, final boolean matchAll, final String unparsed) {
        if (owner != null && owner.contains(".")) {
            throw new IllegalArgumentException("Attempt to instance a MemberInfo with an invalid owner format");
        }
        this.owner = owner;
        this.name = name;
        this.desc = desc;
        this.matchAll = matchAll;
        this.forceField = false;
        this.unparsed = unparsed;
    }
    
    public MemberInfo(final AbstractInsnNode insn) {
        this.matchAll = false;
        this.forceField = false;
        this.unparsed = null;
        if (insn instanceof MethodInsnNode) {
            final MethodInsnNode methodNode = (MethodInsnNode)insn;
            this.owner = methodNode.owner;
            this.name = methodNode.name;
            this.desc = methodNode.desc;
        }
        else {
            if (!(insn instanceof FieldInsnNode)) {
                throw new IllegalArgumentException("insn must be an instance of MethodInsnNode or FieldInsnNode");
            }
            final FieldInsnNode fieldNode = (FieldInsnNode)insn;
            this.owner = fieldNode.owner;
            this.name = fieldNode.name;
            this.desc = fieldNode.desc;
        }
    }
    
    public MemberInfo(final IMapping<?> mapping) {
        this.owner = mapping.getOwner();
        this.name = mapping.getSimpleName();
        this.desc = mapping.getDesc();
        this.matchAll = false;
        this.forceField = (mapping.getType() == IMapping.Type.FIELD);
        this.unparsed = null;
    }
    
    private MemberInfo(final MemberInfo remapped, final MappingMethod method, final boolean setOwner) {
        this.owner = (setOwner ? method.getOwner() : remapped.owner);
        this.name = method.getSimpleName();
        this.desc = method.getDesc();
        this.matchAll = remapped.matchAll;
        this.forceField = false;
        this.unparsed = null;
    }
    
    private MemberInfo(final MemberInfo original, final String owner) {
        this.owner = owner;
        this.name = original.name;
        this.desc = original.desc;
        this.matchAll = original.matchAll;
        this.forceField = original.forceField;
        this.unparsed = null;
    }
    
    @Override
    public String toString() {
        final String owner = (this.owner != null) ? ("L" + this.owner + ";") : "";
        final String name = (this.name != null) ? this.name : "";
        final String qualifier = this.matchAll ? "*" : "";
        final String desc = (this.desc != null) ? this.desc : "";
        final String separator = desc.startsWith("(") ? "" : ((this.desc != null) ? ":" : "");
        return owner + name + qualifier + separator + desc;
    }
    
    @Deprecated
    public String toSrg() {
        if (!this.isFullyQualified()) {
            throw new MixinException("Cannot convert unqualified reference to SRG mapping");
        }
        if (this.desc.startsWith("(")) {
            return this.owner + "/" + this.name + " " + this.desc;
        }
        return this.owner + "/" + this.name;
    }
    
    public String toDescriptor() {
        if (this.desc == null) {
            return "";
        }
        return new SignaturePrinter(this).setFullyQualified(true).toDescriptor();
    }
    
    public String toCtorType() {
        if (this.unparsed == null) {
            return null;
        }
        final String returnType = this.getReturnType();
        if (returnType != null) {
            return returnType;
        }
        if (this.owner != null) {
            return this.owner;
        }
        if (this.name != null && this.desc == null) {
            return this.name;
        }
        return (this.desc != null) ? this.desc : this.unparsed;
    }
    
    public String toCtorDesc() {
        if (this.desc != null && this.desc.startsWith("(") && this.desc.indexOf(41) > -1) {
            return this.desc.substring(0, this.desc.indexOf(41) + 1) + "V";
        }
        return null;
    }
    
    public String getReturnType() {
        if (this.desc == null || this.desc.indexOf(41) == -1 || this.desc.indexOf(40) != 0) {
            return null;
        }
        final String returnType = this.desc.substring(this.desc.indexOf(41) + 1);
        if (returnType.startsWith("L") && returnType.endsWith(";")) {
            return returnType.substring(1, returnType.length() - 1);
        }
        return returnType;
    }
    
    public IMapping<?> asMapping() {
        return (IMapping<?>)(this.isField() ? this.asFieldMapping() : this.asMethodMapping());
    }
    
    public MappingMethod asMethodMapping() {
        if (!this.isFullyQualified()) {
            throw new MixinException("Cannot convert unqualified reference " + this + " to MethodMapping");
        }
        if (this.isField()) {
            throw new MixinException("Cannot convert a non-method reference " + this + " to MethodMapping");
        }
        return new MappingMethod(this.owner, this.name, this.desc);
    }
    
    public MappingField asFieldMapping() {
        if (!this.isField()) {
            throw new MixinException("Cannot convert non-field reference " + this + " to FieldMapping");
        }
        return new MappingField(this.owner, this.name, this.desc);
    }
    
    public boolean isFullyQualified() {
        return this.owner != null && this.name != null && this.desc != null;
    }
    
    public boolean isField() {
        return this.forceField || (this.desc != null && !this.desc.startsWith("("));
    }
    
    public boolean isConstructor() {
        return "<init>".equals(this.name);
    }
    
    public boolean isClassInitialiser() {
        return "<clinit>".equals(this.name);
    }
    
    public boolean isInitialiser() {
        return this.isConstructor() || this.isClassInitialiser();
    }
    
    public MemberInfo validate() throws InvalidMemberDescriptorException {
        if (this.owner != null) {
            if (!this.owner.matches("(?i)^[\\w\\p{Sc}/]+$")) {
                throw new InvalidMemberDescriptorException("Invalid owner: " + this.owner);
            }
            try {
                if (!this.owner.equals(Type.getType(this.owner).getDescriptor())) {
                    throw new InvalidMemberDescriptorException("Invalid owner type specified: " + this.owner);
                }
            }
            catch (Exception ex) {
                throw new InvalidMemberDescriptorException("Invalid owner type specified: " + this.owner);
            }
        }
        if (this.name != null && !this.name.matches("(?i)^<?[\\w\\p{Sc}]+>?$")) {
            throw new InvalidMemberDescriptorException("Invalid name: " + this.name);
        }
        if (this.desc != null) {
            if (!this.desc.matches("^(\\([\\w\\p{Sc}\\[/;]*\\))?\\[*[\\w\\p{Sc}/;]+$")) {
                throw new InvalidMemberDescriptorException("Invalid descriptor: " + this.desc);
            }
            if (this.isField()) {
                if (!this.desc.equals(Type.getType(this.desc).getDescriptor())) {
                    throw new InvalidMemberDescriptorException("Invalid field type in descriptor: " + this.desc);
                }
            }
            else {
                try {
                    Type.getArgumentTypes(this.desc);
                }
                catch (Exception ex) {
                    throw new InvalidMemberDescriptorException("Invalid descriptor: " + this.desc);
                }
                final String retString = this.desc.substring(this.desc.indexOf(41) + 1);
                try {
                    final Type retType = Type.getType(retString);
                    if (!retString.equals(retType.getDescriptor())) {
                        throw new InvalidMemberDescriptorException("Invalid return type \"" + retString + "\" in descriptor: " + this.desc);
                    }
                }
                catch (Exception ex2) {
                    throw new InvalidMemberDescriptorException("Invalid return type \"" + retString + "\" in descriptor: " + this.desc);
                }
            }
        }
        return this;
    }
    
    public boolean matches(final String owner, final String name, final String desc) {
        return this.matches(owner, name, desc, 0);
    }
    
    public boolean matches(final String owner, final String name, final String desc, final int ordinal) {
        return (this.desc == null || desc == null || this.desc.equals(desc)) && (this.name == null || name == null || this.name.equals(name)) && (this.owner == null || owner == null || this.owner.equals(owner)) && (ordinal == 0 || this.matchAll);
    }
    
    public boolean matches(final String name, final String desc) {
        return this.matches(name, desc, 0);
    }
    
    public boolean matches(final String name, final String desc, final int ordinal) {
        return (this.name == null || this.name.equals(name)) && (this.desc == null || (desc != null && desc.equals(this.desc))) && (ordinal == 0 || this.matchAll);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == null || obj.getClass() != MemberInfo.class) {
            return false;
        }
        final MemberInfo other = (MemberInfo)obj;
        return this.matchAll == other.matchAll && this.forceField == other.forceField && Objects.equal((Object)this.owner, (Object)other.owner) && Objects.equal((Object)this.name, (Object)other.name) && Objects.equal((Object)this.desc, (Object)other.desc);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(new Object[] { this.matchAll, this.owner, this.name, this.desc });
    }
    
    public MemberInfo move(final String newOwner) {
        if ((newOwner == null && this.owner == null) || (newOwner != null && newOwner.equals(this.owner))) {
            return this;
        }
        return new MemberInfo(this, newOwner);
    }
    
    public MemberInfo transform(final String newDesc) {
        if ((newDesc == null && this.desc == null) || (newDesc != null && newDesc.equals(this.desc))) {
            return this;
        }
        return new MemberInfo(this.name, this.owner, newDesc, this.matchAll);
    }
    
    public MemberInfo remapUsing(final MappingMethod srgMethod, final boolean setOwner) {
        return new MemberInfo(this, srgMethod, setOwner);
    }
    
    public static MemberInfo parseAndValidate(final String string) throws InvalidMemberDescriptorException {
        return parse(string, null, null).validate();
    }
    
    public static MemberInfo parseAndValidate(final String string, final IMixinContext context) throws InvalidMemberDescriptorException {
        return parse(string, context.getReferenceMapper(), context.getClassRef()).validate();
    }
    
    public static MemberInfo parse(final String string) {
        return parse(string, null, null);
    }
    
    public static MemberInfo parse(final String string, final IMixinContext context) {
        return parse(string, context.getReferenceMapper(), context.getClassRef());
    }
    
    private static MemberInfo parse(final String input, final IReferenceMapper refMapper, final String mixinClass) {
        String desc = null;
        String owner = null;
        String name = Strings.nullToEmpty(input).replaceAll("\\s", "");
        if (refMapper != null) {
            name = refMapper.remap(mixinClass, name);
        }
        final int lastDotPos = name.lastIndexOf(46);
        final int semiColonPos = name.indexOf(59);
        if (lastDotPos > -1) {
            owner = name.substring(0, lastDotPos).replace('.', '/');
            name = name.substring(lastDotPos + 1);
        }
        else if (semiColonPos > -1 && name.startsWith("L")) {
            owner = name.substring(1, semiColonPos).replace('.', '/');
            name = name.substring(semiColonPos + 1);
        }
        final int parenPos = name.indexOf(40);
        final int colonPos = name.indexOf(58);
        if (parenPos > -1) {
            desc = name.substring(parenPos);
            name = name.substring(0, parenPos);
        }
        else if (colonPos > -1) {
            desc = name.substring(colonPos + 1);
            name = name.substring(0, colonPos);
        }
        if ((name.indexOf(47) > -1 || name.indexOf(46) > -1) && owner == null) {
            owner = name;
            name = "";
        }
        final boolean matchAll = name.endsWith("*");
        if (matchAll) {
            name = name.substring(0, name.length() - 1);
        }
        if (name.isEmpty()) {
            name = null;
        }
        return new MemberInfo(name, owner, desc, matchAll, input);
    }
    
    public static MemberInfo fromMapping(final IMapping<?> mapping) {
        return new MemberInfo(mapping);
    }
}
