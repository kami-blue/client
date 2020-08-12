// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.tree;

import org.spongepowered.asm.lib.AnnotationVisitor;
import java.util.ArrayList;
import java.util.Map;
import org.spongepowered.asm.lib.MethodVisitor;
import java.util.List;

public abstract class AbstractInsnNode
{
    public static final int INSN = 0;
    public static final int INT_INSN = 1;
    public static final int VAR_INSN = 2;
    public static final int TYPE_INSN = 3;
    public static final int FIELD_INSN = 4;
    public static final int METHOD_INSN = 5;
    public static final int INVOKE_DYNAMIC_INSN = 6;
    public static final int JUMP_INSN = 7;
    public static final int LABEL = 8;
    public static final int LDC_INSN = 9;
    public static final int IINC_INSN = 10;
    public static final int TABLESWITCH_INSN = 11;
    public static final int LOOKUPSWITCH_INSN = 12;
    public static final int MULTIANEWARRAY_INSN = 13;
    public static final int FRAME = 14;
    public static final int LINE = 15;
    protected int opcode;
    public List<TypeAnnotationNode> visibleTypeAnnotations;
    public List<TypeAnnotationNode> invisibleTypeAnnotations;
    AbstractInsnNode prev;
    AbstractInsnNode next;
    int index;
    
    protected AbstractInsnNode(final int opcode) {
        this.opcode = opcode;
        this.index = -1;
    }
    
    public int getOpcode() {
        return this.opcode;
    }
    
    public abstract int getType();
    
    public AbstractInsnNode getPrevious() {
        return this.prev;
    }
    
    public AbstractInsnNode getNext() {
        return this.next;
    }
    
    public abstract void accept(final MethodVisitor p0);
    
    protected final void acceptAnnotations(final MethodVisitor mv) {
        for (int n = (this.visibleTypeAnnotations == null) ? 0 : this.visibleTypeAnnotations.size(), i = 0; i < n; ++i) {
            final TypeAnnotationNode an = this.visibleTypeAnnotations.get(i);
            an.accept(mv.visitInsnAnnotation(an.typeRef, an.typePath, an.desc, true));
        }
        for (int n = (this.invisibleTypeAnnotations == null) ? 0 : this.invisibleTypeAnnotations.size(), i = 0; i < n; ++i) {
            final TypeAnnotationNode an = this.invisibleTypeAnnotations.get(i);
            an.accept(mv.visitInsnAnnotation(an.typeRef, an.typePath, an.desc, false));
        }
    }
    
    public abstract AbstractInsnNode clone(final Map<LabelNode, LabelNode> p0);
    
    static LabelNode clone(final LabelNode label, final Map<LabelNode, LabelNode> map) {
        return map.get(label);
    }
    
    static LabelNode[] clone(final List<LabelNode> labels, final Map<LabelNode, LabelNode> map) {
        final LabelNode[] clones = new LabelNode[labels.size()];
        for (int i = 0; i < clones.length; ++i) {
            clones[i] = map.get(labels.get(i));
        }
        return clones;
    }
    
    protected final AbstractInsnNode cloneAnnotations(final AbstractInsnNode insn) {
        if (insn.visibleTypeAnnotations != null) {
            this.visibleTypeAnnotations = new ArrayList<TypeAnnotationNode>();
            for (int i = 0; i < insn.visibleTypeAnnotations.size(); ++i) {
                final TypeAnnotationNode src = insn.visibleTypeAnnotations.get(i);
                final TypeAnnotationNode ann = new TypeAnnotationNode(src.typeRef, src.typePath, src.desc);
                src.accept(ann);
                this.visibleTypeAnnotations.add(ann);
            }
        }
        if (insn.invisibleTypeAnnotations != null) {
            this.invisibleTypeAnnotations = new ArrayList<TypeAnnotationNode>();
            for (int i = 0; i < insn.invisibleTypeAnnotations.size(); ++i) {
                final TypeAnnotationNode src = insn.invisibleTypeAnnotations.get(i);
                final TypeAnnotationNode ann = new TypeAnnotationNode(src.typeRef, src.typePath, src.desc);
                src.accept(ann);
                this.invisibleTypeAnnotations.add(ann);
            }
        }
        return this;
    }
}
