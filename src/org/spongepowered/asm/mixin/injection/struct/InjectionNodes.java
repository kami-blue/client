// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.struct;

import org.spongepowered.asm.util.Bytecode;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import java.util.ArrayList;

public class InjectionNodes extends ArrayList<InjectionNode>
{
    private static final long serialVersionUID = 1L;
    
    public InjectionNode add(final AbstractInsnNode node) {
        InjectionNode injectionNode = this.get(node);
        if (injectionNode == null) {
            injectionNode = new InjectionNode(node);
            this.add(injectionNode);
        }
        return injectionNode;
    }
    
    public InjectionNode get(final AbstractInsnNode node) {
        for (final InjectionNode injectionNode : this) {
            if (injectionNode.matches(node)) {
                return injectionNode;
            }
        }
        return null;
    }
    
    public boolean contains(final AbstractInsnNode node) {
        return this.get(node) != null;
    }
    
    public void replace(final AbstractInsnNode oldNode, final AbstractInsnNode newNode) {
        final InjectionNode injectionNode = this.get(oldNode);
        if (injectionNode != null) {
            injectionNode.replace(newNode);
        }
    }
    
    public void remove(final AbstractInsnNode node) {
        final InjectionNode injectionNode = this.get(node);
        if (injectionNode != null) {
            injectionNode.remove();
        }
    }
    
    public static class InjectionNode implements Comparable<InjectionNode>
    {
        private static int nextId;
        private final int id;
        private final AbstractInsnNode originalTarget;
        private AbstractInsnNode currentTarget;
        private Map<String, Object> decorations;
        
        public InjectionNode(final AbstractInsnNode node) {
            this.originalTarget = node;
            this.currentTarget = node;
            this.id = InjectionNode.nextId++;
        }
        
        public int getId() {
            return this.id;
        }
        
        public AbstractInsnNode getOriginalTarget() {
            return this.originalTarget;
        }
        
        public AbstractInsnNode getCurrentTarget() {
            return this.currentTarget;
        }
        
        public InjectionNode replace(final AbstractInsnNode target) {
            this.currentTarget = target;
            return this;
        }
        
        public InjectionNode remove() {
            this.currentTarget = null;
            return this;
        }
        
        public boolean matches(final AbstractInsnNode node) {
            return this.originalTarget == node || this.currentTarget == node;
        }
        
        public boolean isReplaced() {
            return this.originalTarget != this.currentTarget;
        }
        
        public boolean isRemoved() {
            return this.currentTarget == null;
        }
        
        public <V> InjectionNode decorate(final String key, final V value) {
            if (this.decorations == null) {
                this.decorations = new HashMap<String, Object>();
            }
            this.decorations.put(key, value);
            return this;
        }
        
        public boolean hasDecoration(final String key) {
            return this.decorations != null && this.decorations.get(key) != null;
        }
        
        public <V> V getDecoration(final String key) {
            return (V)((this.decorations == null) ? null : this.decorations.get(key));
        }
        
        @Override
        public int compareTo(final InjectionNode other) {
            return (other == null) ? Integer.MAX_VALUE : (this.hashCode() - other.hashCode());
        }
        
        @Override
        public String toString() {
            return String.format("InjectionNode[%s]", Bytecode.describeNode(this.currentTarget).replaceAll("\\s+", " "));
        }
        
        static {
            InjectionNode.nextId = 0;
        }
    }
}
