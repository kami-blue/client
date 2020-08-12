// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.util;

import com.google.common.base.Strings;
import org.spongepowered.asm.lib.tree.LocalVariableNode;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.Type;

public class SignaturePrinter
{
    private final String name;
    private final Type returnType;
    private final Type[] argTypes;
    private final String[] argNames;
    private String modifiers;
    private boolean fullyQualified;
    
    public SignaturePrinter(final MethodNode method) {
        this(method.name, Type.VOID_TYPE, Type.getArgumentTypes(method.desc));
        this.setModifiers(method);
    }
    
    public SignaturePrinter(final MethodNode method, final String[] argNames) {
        this(method.name, Type.VOID_TYPE, Type.getArgumentTypes(method.desc), argNames);
        this.setModifiers(method);
    }
    
    public SignaturePrinter(final MemberInfo member) {
        this(member.name, member.desc);
    }
    
    public SignaturePrinter(final String name, final String desc) {
        this(name, Type.getReturnType(desc), Type.getArgumentTypes(desc));
    }
    
    public SignaturePrinter(final String name, final Type returnType, final Type[] args) {
        this.modifiers = "private void";
        this.name = name;
        this.returnType = returnType;
        this.argTypes = new Type[args.length];
        this.argNames = new String[args.length];
        int l = 0;
        int v = 0;
        while (l < args.length) {
            if (args[l] != null) {
                this.argTypes[l] = args[l];
                this.argNames[l] = "var" + v++;
            }
            ++l;
        }
    }
    
    public SignaturePrinter(final String name, final Type returnType, final LocalVariableNode[] args) {
        this.modifiers = "private void";
        this.name = name;
        this.returnType = returnType;
        this.argTypes = new Type[args.length];
        this.argNames = new String[args.length];
        for (int l = 0; l < args.length; ++l) {
            if (args[l] != null) {
                this.argTypes[l] = Type.getType(args[l].desc);
                this.argNames[l] = args[l].name;
            }
        }
    }
    
    public SignaturePrinter(final String name, final Type returnType, final Type[] argTypes, final String[] argNames) {
        this.modifiers = "private void";
        this.name = name;
        this.returnType = returnType;
        this.argTypes = argTypes;
        this.argNames = argNames;
        if (this.argTypes.length > this.argNames.length) {
            throw new IllegalArgumentException(String.format("Types array length must not exceed names array length! (names=%d, types=%d)", this.argNames.length, this.argTypes.length));
        }
    }
    
    public String getFormattedArgs() {
        return this.appendArgs(new StringBuilder(), true, true).toString();
    }
    
    public String getReturnType() {
        return getTypeName(this.returnType, false, this.fullyQualified);
    }
    
    public void setModifiers(final MethodNode method) {
        final String returnType = getTypeName(Type.getReturnType(method.desc), false, this.fullyQualified);
        if ((method.access & 0x1) != 0x0) {
            this.setModifiers("public " + returnType);
        }
        else if ((method.access & 0x4) != 0x0) {
            this.setModifiers("protected " + returnType);
        }
        else if ((method.access & 0x2) != 0x0) {
            this.setModifiers("private " + returnType);
        }
        else {
            this.setModifiers(returnType);
        }
    }
    
    public SignaturePrinter setModifiers(final String modifiers) {
        this.modifiers = modifiers.replace("${returnType}", this.getReturnType());
        return this;
    }
    
    public SignaturePrinter setFullyQualified(final boolean fullyQualified) {
        this.fullyQualified = fullyQualified;
        return this;
    }
    
    public boolean isFullyQualified() {
        return this.fullyQualified;
    }
    
    @Override
    public String toString() {
        return this.appendArgs(new StringBuilder().append(this.modifiers).append(" ").append(this.name), false, true).toString();
    }
    
    public String toDescriptor() {
        final StringBuilder args = this.appendArgs(new StringBuilder(), true, false);
        return args.append(getTypeName(this.returnType, false, this.fullyQualified)).toString();
    }
    
    private StringBuilder appendArgs(final StringBuilder sb, final boolean typesOnly, final boolean pretty) {
        sb.append('(');
        for (int var = 0; var < this.argTypes.length; ++var) {
            if (this.argTypes[var] != null) {
                if (var > 0) {
                    sb.append(',');
                    if (pretty) {
                        sb.append(' ');
                    }
                }
                try {
                    final String name = typesOnly ? null : (Strings.isNullOrEmpty(this.argNames[var]) ? ("unnamed" + var) : this.argNames[var]);
                    this.appendType(sb, this.argTypes[var], name);
                }
                catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return sb.append(")");
    }
    
    private StringBuilder appendType(final StringBuilder sb, final Type type, final String name) {
        switch (type.getSort()) {
            case 9: {
                return appendArraySuffix(this.appendType(sb, type.getElementType(), name), type);
            }
            case 10: {
                return this.appendType(sb, type.getClassName(), name);
            }
            default: {
                sb.append(getTypeName(type, false, this.fullyQualified));
                if (name != null) {
                    sb.append(' ').append(name);
                }
                return sb;
            }
        }
    }
    
    private StringBuilder appendType(final StringBuilder sb, String typeName, final String name) {
        if (!this.fullyQualified) {
            typeName = typeName.substring(typeName.lastIndexOf(46) + 1);
        }
        sb.append(typeName);
        if (typeName.endsWith("CallbackInfoReturnable")) {
            sb.append('<').append(getTypeName(this.returnType, true, this.fullyQualified)).append('>');
        }
        if (name != null) {
            sb.append(' ').append(name);
        }
        return sb;
    }
    
    public static String getTypeName(final Type type, final boolean box) {
        return getTypeName(type, box, false);
    }
    
    public static String getTypeName(final Type type, final boolean box, final boolean fullyQualified) {
        switch (type.getSort()) {
            case 0: {
                return box ? "Void" : "void";
            }
            case 1: {
                return box ? "Boolean" : "boolean";
            }
            case 2: {
                return box ? "Character" : "char";
            }
            case 3: {
                return box ? "Byte" : "byte";
            }
            case 4: {
                return box ? "Short" : "short";
            }
            case 5: {
                return box ? "Integer" : "int";
            }
            case 6: {
                return box ? "Float" : "float";
            }
            case 7: {
                return box ? "Long" : "long";
            }
            case 8: {
                return box ? "Double" : "double";
            }
            case 9: {
                return getTypeName(type.getElementType(), box, fullyQualified) + arraySuffix(type);
            }
            case 10: {
                String typeName = type.getClassName();
                if (!fullyQualified) {
                    typeName = typeName.substring(typeName.lastIndexOf(46) + 1);
                }
                return typeName;
            }
            default: {
                return "Object";
            }
        }
    }
    
    private static String arraySuffix(final Type type) {
        return Strings.repeat("[]", type.getDimensions());
    }
    
    private static StringBuilder appendArraySuffix(final StringBuilder sb, final Type type) {
        for (int i = 0; i < type.getDimensions(); ++i) {
            sb.append("[]");
        }
        return sb;
    }
}
