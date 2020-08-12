// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.tree;

import org.spongepowered.asm.lib.Label;
import org.spongepowered.asm.lib.MethodVisitor;
import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import org.spongepowered.asm.lib.TypePath;
import java.util.List;

public class LocalVariableAnnotationNode extends TypeAnnotationNode
{
    public List<LabelNode> start;
    public List<LabelNode> end;
    public List<Integer> index;
    
    public LocalVariableAnnotationNode(final int typeRef, final TypePath typePath, final LabelNode[] start, final LabelNode[] end, final int[] index, final String desc) {
        this(327680, typeRef, typePath, start, end, index, desc);
    }
    
    public LocalVariableAnnotationNode(final int api, final int typeRef, final TypePath typePath, final LabelNode[] start, final LabelNode[] end, final int[] index, final String desc) {
        super(api, typeRef, typePath, desc);
        (this.start = new ArrayList<LabelNode>(start.length)).addAll(Arrays.asList(start));
        (this.end = new ArrayList<LabelNode>(end.length)).addAll(Arrays.asList(end));
        this.index = new ArrayList<Integer>(index.length);
        for (final int i : index) {
            this.index.add(i);
        }
    }
    
    public void accept(final MethodVisitor mv, final boolean visible) {
        final Label[] start = new Label[this.start.size()];
        final Label[] end = new Label[this.end.size()];
        final int[] index = new int[this.index.size()];
        for (int i = 0; i < start.length; ++i) {
            start[i] = this.start.get(i).getLabel();
            end[i] = this.end.get(i).getLabel();
            index[i] = this.index.get(i);
        }
        this.accept(mv.visitLocalVariableAnnotation(this.typeRef, this.typePath, start, end, index, this.desc, true));
    }
}
