// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.nodes;

import java.util.Iterator;
import org.yaml.snakeyaml.error.Mark;
import java.util.List;

public class MappingNode extends CollectionNode<NodeTuple>
{
    private List<NodeTuple> value;
    private boolean merged;
    
    public MappingNode(final Tag tag, final boolean resolved, final List<NodeTuple> value, final Mark startMark, final Mark endMark, final Boolean flowStyle) {
        super(tag, startMark, endMark, flowStyle);
        this.merged = false;
        if (value == null) {
            throw new NullPointerException("value in a Node is required.");
        }
        this.value = value;
        this.resolved = resolved;
    }
    
    public MappingNode(final Tag tag, final List<NodeTuple> value, final Boolean flowStyle) {
        this(tag, true, value, null, null, flowStyle);
    }
    
    @Override
    public NodeId getNodeId() {
        return NodeId.mapping;
    }
    
    @Override
    public List<NodeTuple> getValue() {
        return this.value;
    }
    
    public void setValue(final List<NodeTuple> merge) {
        this.value = merge;
    }
    
    public void setOnlyKeyType(final Class<?> keyType) {
        for (final NodeTuple nodes : this.value) {
            nodes.getKeyNode().setType(keyType);
        }
    }
    
    public void setTypes(final Class<?> keyType, final Class<?> valueType) {
        for (final NodeTuple nodes : this.value) {
            nodes.getValueNode().setType(valueType);
            nodes.getKeyNode().setType(keyType);
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        for (final NodeTuple node : this.getValue()) {
            buf.append("{ key=");
            buf.append(node.getKeyNode());
            buf.append("; value=");
            if (node.getValueNode() instanceof CollectionNode) {
                buf.append(System.identityHashCode(node.getValueNode()));
            }
            else {
                buf.append(node.toString());
            }
            buf.append(" }");
        }
        final String values = buf.toString();
        return "<" + this.getClass().getName() + " (tag=" + this.getTag() + ", values=" + values + ")>";
    }
    
    public void setMerged(final boolean merged) {
        this.merged = merged;
    }
    
    public boolean isMerged() {
        return this.merged;
    }
}
