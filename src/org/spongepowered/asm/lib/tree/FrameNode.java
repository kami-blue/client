// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.tree;

import java.util.ArrayList;
import java.util.Map;
import org.spongepowered.asm.lib.MethodVisitor;
import java.util.Arrays;
import java.util.List;

public class FrameNode extends AbstractInsnNode
{
    public int type;
    public List<Object> local;
    public List<Object> stack;
    
    private FrameNode() {
        super(-1);
    }
    
    public FrameNode(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
        super(-1);
        switch (this.type = type) {
            case -1:
            case 0: {
                this.local = asList(nLocal, local);
                this.stack = asList(nStack, stack);
                break;
            }
            case 1: {
                this.local = asList(nLocal, local);
                break;
            }
            case 2: {
                this.local = Arrays.asList(new Object[nLocal]);
            }
            case 4: {
                this.stack = asList(1, stack);
                break;
            }
        }
    }
    
    @Override
    public int getType() {
        return 14;
    }
    
    @Override
    public void accept(final MethodVisitor mv) {
        switch (this.type) {
            case -1:
            case 0: {
                mv.visitFrame(this.type, this.local.size(), asArray(this.local), this.stack.size(), asArray(this.stack));
                break;
            }
            case 1: {
                mv.visitFrame(this.type, this.local.size(), asArray(this.local), 0, null);
                break;
            }
            case 2: {
                mv.visitFrame(this.type, this.local.size(), null, 0, null);
                break;
            }
            case 3: {
                mv.visitFrame(this.type, 0, null, 0, null);
                break;
            }
            case 4: {
                mv.visitFrame(this.type, 0, null, 1, asArray(this.stack));
                break;
            }
        }
    }
    
    @Override
    public AbstractInsnNode clone(final Map<LabelNode, LabelNode> labels) {
        final FrameNode clone = new FrameNode();
        clone.type = this.type;
        if (this.local != null) {
            clone.local = new ArrayList<Object>();
            for (int i = 0; i < this.local.size(); ++i) {
                Object l = this.local.get(i);
                if (l instanceof LabelNode) {
                    l = labels.get(l);
                }
                clone.local.add(l);
            }
        }
        if (this.stack != null) {
            clone.stack = new ArrayList<Object>();
            for (int i = 0; i < this.stack.size(); ++i) {
                Object s = this.stack.get(i);
                if (s instanceof LabelNode) {
                    s = labels.get(s);
                }
                clone.stack.add(s);
            }
        }
        return clone;
    }
    
    private static List<Object> asList(final int n, final Object[] o) {
        return Arrays.asList(o).subList(0, n);
    }
    
    private static Object[] asArray(final List<Object> l) {
        final Object[] objs = new Object[l.size()];
        for (int i = 0; i < objs.length; ++i) {
            Object o = l.get(i);
            if (o instanceof LabelNode) {
                o = ((LabelNode)o).getLabel();
            }
            objs[i] = o;
        }
        return objs;
    }
}
