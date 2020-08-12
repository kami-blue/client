// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.tree;

import org.spongepowered.asm.lib.MethodVisitor;
import java.util.Iterator;
import java.util.List;

public class TryCatchBlockNode
{
    public LabelNode start;
    public LabelNode end;
    public LabelNode handler;
    public String type;
    public List<TypeAnnotationNode> visibleTypeAnnotations;
    public List<TypeAnnotationNode> invisibleTypeAnnotations;
    
    public TryCatchBlockNode(final LabelNode start, final LabelNode end, final LabelNode handler, final String type) {
        this.start = start;
        this.end = end;
        this.handler = handler;
        this.type = type;
    }
    
    public void updateIndex(final int index) {
        final int newTypeRef = 0x42000000 | index << 8;
        if (this.visibleTypeAnnotations != null) {
            for (final TypeAnnotationNode tan : this.visibleTypeAnnotations) {
                tan.typeRef = newTypeRef;
            }
        }
        if (this.invisibleTypeAnnotations != null) {
            for (final TypeAnnotationNode tan : this.invisibleTypeAnnotations) {
                tan.typeRef = newTypeRef;
            }
        }
    }
    
    public void accept(final MethodVisitor mv) {
        mv.visitTryCatchBlock(this.start.getLabel(), this.end.getLabel(), (this.handler == null) ? null : this.handler.getLabel(), this.type);
        for (int n = (this.visibleTypeAnnotations == null) ? 0 : this.visibleTypeAnnotations.size(), i = 0; i < n; ++i) {
            final TypeAnnotationNode an = this.visibleTypeAnnotations.get(i);
            an.accept(mv.visitTryCatchAnnotation(an.typeRef, an.typePath, an.desc, true));
        }
        for (int n = (this.invisibleTypeAnnotations == null) ? 0 : this.invisibleTypeAnnotations.size(), i = 0; i < n; ++i) {
            final TypeAnnotationNode an = this.invisibleTypeAnnotations.get(i);
            an.accept(mv.visitTryCatchAnnotation(an.typeRef, an.typePath, an.desc, false));
        }
    }
}
