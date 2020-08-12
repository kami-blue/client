// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.tree;

import java.util.Map;
import org.spongepowered.asm.lib.Label;
import org.spongepowered.asm.lib.MethodVisitor;
import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class LookupSwitchInsnNode extends AbstractInsnNode
{
    public LabelNode dflt;
    public List<Integer> keys;
    public List<LabelNode> labels;
    
    public LookupSwitchInsnNode(final LabelNode dflt, final int[] keys, final LabelNode[] labels) {
        super(171);
        this.dflt = dflt;
        this.keys = new ArrayList<Integer>((keys == null) ? 0 : keys.length);
        this.labels = new ArrayList<LabelNode>((labels == null) ? 0 : labels.length);
        if (keys != null) {
            for (int i = 0; i < keys.length; ++i) {
                this.keys.add(keys[i]);
            }
        }
        if (labels != null) {
            this.labels.addAll(Arrays.asList(labels));
        }
    }
    
    @Override
    public int getType() {
        return 12;
    }
    
    @Override
    public void accept(final MethodVisitor mv) {
        final int[] keys = new int[this.keys.size()];
        for (int i = 0; i < keys.length; ++i) {
            keys[i] = this.keys.get(i);
        }
        final Label[] labels = new Label[this.labels.size()];
        for (int j = 0; j < labels.length; ++j) {
            labels[j] = this.labels.get(j).getLabel();
        }
        mv.visitLookupSwitchInsn(this.dflt.getLabel(), keys, labels);
        this.acceptAnnotations(mv);
    }
    
    @Override
    public AbstractInsnNode clone(final Map<LabelNode, LabelNode> labels) {
        final LookupSwitchInsnNode clone = new LookupSwitchInsnNode(AbstractInsnNode.clone(this.dflt, labels), null, AbstractInsnNode.clone(this.labels, labels));
        clone.keys.addAll(this.keys);
        return clone.cloneAnnotations(this);
    }
}
