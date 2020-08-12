// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.nodes;

public class AnchorNode extends Node
{
    private Node realNode;
    
    public AnchorNode(final Node realNode) {
        super(realNode.getTag(), realNode.getStartMark(), realNode.getEndMark());
        this.realNode = realNode;
    }
    
    @Override
    public NodeId getNodeId() {
        return NodeId.anchor;
    }
    
    public Node getRealNode() {
        return this.realNode;
    }
}
