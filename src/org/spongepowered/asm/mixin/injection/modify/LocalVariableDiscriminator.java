// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.modify;

import org.spongepowered.asm.util.SignaturePrinter;
import java.util.Map;
import java.util.HashMap;
import org.spongepowered.asm.lib.tree.LocalVariableNode;
import org.spongepowered.asm.util.Locals;
import org.spongepowered.asm.util.Bytecode;
import org.spongepowered.asm.util.PrettyPrinter;
import java.util.Collection;
import java.util.List;
import java.util.HashSet;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.lib.Type;
import java.util.Collections;
import java.util.Set;

public class LocalVariableDiscriminator
{
    private final boolean argsOnly;
    private final int ordinal;
    private final int index;
    private final Set<String> names;
    private final boolean print;
    
    public LocalVariableDiscriminator(final boolean argsOnly, final int ordinal, final int index, final Set<String> names, final boolean print) {
        this.argsOnly = argsOnly;
        this.ordinal = ordinal;
        this.index = index;
        this.names = Collections.unmodifiableSet((Set<? extends String>)names);
        this.print = print;
    }
    
    public boolean isArgsOnly() {
        return this.argsOnly;
    }
    
    public int getOrdinal() {
        return this.ordinal;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    public Set<String> getNames() {
        return this.names;
    }
    
    public boolean hasNames() {
        return !this.names.isEmpty();
    }
    
    public boolean printLVT() {
        return this.print;
    }
    
    protected boolean isImplicit(final Context context) {
        return this.ordinal < 0 && this.index < context.baseArgIndex && this.names.isEmpty();
    }
    
    public int findLocal(final Type returnType, final boolean argsOnly, final Target target, final AbstractInsnNode node) {
        try {
            return this.findLocal(new Context(returnType, argsOnly, target, node));
        }
        catch (InvalidImplicitDiscriminatorException ex) {
            return -2;
        }
    }
    
    public int findLocal(final Context context) {
        if (this.isImplicit(context)) {
            return this.findImplicitLocal(context);
        }
        return this.findExplicitLocal(context);
    }
    
    private int findImplicitLocal(final Context context) {
        int found = 0;
        int count = 0;
        for (int index = context.baseArgIndex; index < context.locals.length; ++index) {
            final Context.Local local = context.locals[index];
            if (local != null) {
                if (local.type.equals(context.returnType)) {
                    ++count;
                    found = index;
                }
            }
        }
        if (count == 1) {
            return found;
        }
        throw new InvalidImplicitDiscriminatorException("Found " + count + " candidate variables but exactly 1 is required.");
    }
    
    private int findExplicitLocal(final Context context) {
        for (int index = context.baseArgIndex; index < context.locals.length; ++index) {
            final Context.Local local = context.locals[index];
            if (local != null) {
                if (local.type.equals(context.returnType)) {
                    if (this.ordinal > -1) {
                        if (this.ordinal == local.ord) {
                            return index;
                        }
                    }
                    else if (this.index >= context.baseArgIndex) {
                        if (this.index == index) {
                            return index;
                        }
                    }
                    else if (this.names.contains(local.name)) {
                        return index;
                    }
                }
            }
        }
        return -1;
    }
    
    public static LocalVariableDiscriminator parse(final AnnotationNode annotation) {
        final boolean argsOnly = Annotations.getValue(annotation, "argsOnly", Boolean.FALSE);
        final int ordinal = Annotations.getValue(annotation, "ordinal", -1);
        final int index = Annotations.getValue(annotation, "index", -1);
        final boolean print = Annotations.getValue(annotation, "print", Boolean.FALSE);
        final Set<String> names = new HashSet<String>();
        final List<String> namesList = Annotations.getValue(annotation, "name", (List<String>)null);
        if (namesList != null) {
            names.addAll(namesList);
        }
        return new LocalVariableDiscriminator(argsOnly, ordinal, index, names, print);
    }
    
    public static class Context implements PrettyPrinter.IPrettyPrintable
    {
        final Target target;
        final Type returnType;
        final AbstractInsnNode node;
        final int baseArgIndex;
        final Local[] locals;
        private final boolean isStatic;
        
        public Context(final Type returnType, final boolean argsOnly, final Target target, final AbstractInsnNode node) {
            this.isStatic = Bytecode.methodIsStatic(target.method);
            this.returnType = returnType;
            this.target = target;
            this.node = node;
            this.baseArgIndex = (this.isStatic ? 0 : 1);
            this.locals = this.initLocals(target, argsOnly, node);
            this.initOrdinals();
        }
        
        private Local[] initLocals(final Target target, final boolean argsOnly, final AbstractInsnNode node) {
            if (!argsOnly) {
                final LocalVariableNode[] locals = Locals.getLocalsAt(target.classNode, target.method, node);
                if (locals != null) {
                    final Local[] lvt = new Local[locals.length];
                    for (int l = 0; l < locals.length; ++l) {
                        if (locals[l] != null) {
                            lvt[l] = new Local(locals[l].name, Type.getType(locals[l].desc));
                        }
                    }
                    return lvt;
                }
            }
            final Local[] lvt2 = new Local[this.baseArgIndex + target.arguments.length];
            if (!this.isStatic) {
                lvt2[0] = new Local("this", Type.getType(target.classNode.name));
            }
            for (int local = this.baseArgIndex; local < lvt2.length; ++local) {
                final Type arg = target.arguments[local - this.baseArgIndex];
                lvt2[local] = new Local("arg" + local, arg);
            }
            return lvt2;
        }
        
        private void initOrdinals() {
            final Map<Type, Integer> ordinalMap = new HashMap<Type, Integer>();
            for (int l = 0; l < this.locals.length; ++l) {
                Integer ordinal = 0;
                if (this.locals[l] != null) {
                    ordinal = ordinalMap.get(this.locals[l].type);
                    ordinalMap.put(this.locals[l].type, ordinal = ((ordinal == null) ? 0 : (ordinal + 1)));
                    this.locals[l].ord = ordinal;
                }
            }
        }
        
        @Override
        public void print(final PrettyPrinter printer) {
            printer.add("%5s  %7s  %30s  %-50s  %s", "INDEX", "ORDINAL", "TYPE", "NAME", "CANDIDATE");
            for (int l = this.baseArgIndex; l < this.locals.length; ++l) {
                final Local local = this.locals[l];
                if (local != null) {
                    final Type localType = local.type;
                    final String localName = local.name;
                    final int ordinal = local.ord;
                    final String candidate = this.returnType.equals(localType) ? "YES" : "-";
                    printer.add("[%3d]    [%3d]  %30s  %-50s  %s", l, ordinal, SignaturePrinter.getTypeName(localType, false), localName, candidate);
                }
                else if (l > 0) {
                    final Local prevLocal = this.locals[l - 1];
                    final boolean isTop = prevLocal != null && prevLocal.type != null && prevLocal.type.getSize() > 1;
                    printer.add("[%3d]           %30s", l, isTop ? "<top>" : "-");
                }
            }
        }
        
        public class Local
        {
            int ord;
            String name;
            Type type;
            
            public Local(final String name, final Type type) {
                this.ord = 0;
                this.name = name;
                this.type = type;
            }
            
            @Override
            public String toString() {
                return String.format("Local[ordinal=%d, name=%s, type=%s]", this.ord, this.name, this.type);
            }
        }
    }
}
