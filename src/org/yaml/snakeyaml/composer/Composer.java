// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.composer;

import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.events.MappingStartEvent;
import java.util.List;
import org.yaml.snakeyaml.nodes.SequenceNode;
import java.util.ArrayList;
import org.yaml.snakeyaml.events.SequenceStartEvent;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.events.ScalarEvent;
import org.yaml.snakeyaml.events.NodeEvent;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.events.AliasEvent;
import org.yaml.snakeyaml.events.Event;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import org.yaml.snakeyaml.nodes.Node;
import java.util.Map;
import org.yaml.snakeyaml.resolver.Resolver;
import org.yaml.snakeyaml.parser.Parser;

public class Composer
{
    protected final Parser parser;
    private final Resolver resolver;
    private final Map<String, Node> anchors;
    private final Set<Node> recursiveNodes;
    
    public Composer(final Parser parser, final Resolver resolver) {
        this.parser = parser;
        this.resolver = resolver;
        this.anchors = new HashMap<String, Node>();
        this.recursiveNodes = new HashSet<Node>();
    }
    
    public boolean checkNode() {
        if (this.parser.checkEvent(Event.ID.StreamStart)) {
            this.parser.getEvent();
        }
        return !this.parser.checkEvent(Event.ID.StreamEnd);
    }
    
    public Node getNode() {
        if (!this.parser.checkEvent(Event.ID.StreamEnd)) {
            return this.composeDocument();
        }
        return null;
    }
    
    public Node getSingleNode() {
        this.parser.getEvent();
        Node document = null;
        if (!this.parser.checkEvent(Event.ID.StreamEnd)) {
            document = this.composeDocument();
        }
        if (!this.parser.checkEvent(Event.ID.StreamEnd)) {
            final Event event = this.parser.getEvent();
            throw new ComposerException("expected a single document in the stream", document.getStartMark(), "but found another document", event.getStartMark());
        }
        this.parser.getEvent();
        return document;
    }
    
    private Node composeDocument() {
        this.parser.getEvent();
        final Node node = this.composeNode(null);
        this.parser.getEvent();
        this.anchors.clear();
        this.recursiveNodes.clear();
        return node;
    }
    
    private Node composeNode(final Node parent) {
        this.recursiveNodes.add(parent);
        Node node = null;
        if (this.parser.checkEvent(Event.ID.Alias)) {
            final AliasEvent event = (AliasEvent)this.parser.getEvent();
            final String anchor = event.getAnchor();
            if (!this.anchors.containsKey(anchor)) {
                throw new ComposerException(null, null, "found undefined alias " + anchor, event.getStartMark());
            }
            node = this.anchors.get(anchor);
            if (this.recursiveNodes.remove(node)) {
                node.setTwoStepsConstruction(true);
            }
        }
        else {
            final NodeEvent event2 = (NodeEvent)this.parser.peekEvent();
            String anchor = null;
            anchor = event2.getAnchor();
            if (this.parser.checkEvent(Event.ID.Scalar)) {
                node = this.composeScalarNode(anchor);
            }
            else if (this.parser.checkEvent(Event.ID.SequenceStart)) {
                node = this.composeSequenceNode(anchor);
            }
            else {
                node = this.composeMappingNode(anchor);
            }
        }
        this.recursiveNodes.remove(parent);
        return node;
    }
    
    protected Node composeScalarNode(final String anchor) {
        final ScalarEvent ev = (ScalarEvent)this.parser.getEvent();
        final String tag = ev.getTag();
        boolean resolved = false;
        Tag nodeTag;
        if (tag == null || tag.equals("!")) {
            nodeTag = this.resolver.resolve(NodeId.scalar, ev.getValue(), ev.getImplicit().canOmitTagInPlainScalar());
            resolved = true;
        }
        else {
            nodeTag = new Tag(tag);
        }
        final Node node = new ScalarNode(nodeTag, resolved, ev.getValue(), ev.getStartMark(), ev.getEndMark(), ev.getStyle());
        if (anchor != null) {
            node.setAnchor(anchor);
            this.anchors.put(anchor, node);
        }
        return node;
    }
    
    protected Node composeSequenceNode(final String anchor) {
        final SequenceStartEvent startEvent = (SequenceStartEvent)this.parser.getEvent();
        final String tag = startEvent.getTag();
        boolean resolved = false;
        Tag nodeTag;
        if (tag == null || tag.equals("!")) {
            nodeTag = this.resolver.resolve(NodeId.sequence, null, startEvent.getImplicit());
            resolved = true;
        }
        else {
            nodeTag = new Tag(tag);
        }
        final ArrayList<Node> children = new ArrayList<Node>();
        final SequenceNode node = new SequenceNode(nodeTag, resolved, children, startEvent.getStartMark(), null, startEvent.getFlowStyle());
        if (anchor != null) {
            node.setAnchor(anchor);
            this.anchors.put(anchor, node);
        }
        while (!this.parser.checkEvent(Event.ID.SequenceEnd)) {
            children.add(this.composeNode(node));
        }
        final Event endEvent = this.parser.getEvent();
        node.setEndMark(endEvent.getEndMark());
        return node;
    }
    
    protected Node composeMappingNode(final String anchor) {
        final MappingStartEvent startEvent = (MappingStartEvent)this.parser.getEvent();
        final String tag = startEvent.getTag();
        boolean resolved = false;
        Tag nodeTag;
        if (tag == null || tag.equals("!")) {
            nodeTag = this.resolver.resolve(NodeId.mapping, null, startEvent.getImplicit());
            resolved = true;
        }
        else {
            nodeTag = new Tag(tag);
        }
        final List<NodeTuple> children = new ArrayList<NodeTuple>();
        final MappingNode node = new MappingNode(nodeTag, resolved, children, startEvent.getStartMark(), null, startEvent.getFlowStyle());
        if (anchor != null) {
            node.setAnchor(anchor);
            this.anchors.put(anchor, node);
        }
        while (!this.parser.checkEvent(Event.ID.MappingEnd)) {
            this.composeMappingChildren(children, node);
        }
        final Event endEvent = this.parser.getEvent();
        node.setEndMark(endEvent.getEndMark());
        return node;
    }
    
    protected void composeMappingChildren(final List<NodeTuple> children, final MappingNode node) {
        final Node itemKey = this.composeKeyNode(node);
        if (itemKey.getTag().equals(Tag.MERGE)) {
            node.setMerged(true);
        }
        final Node itemValue = this.composeValueNode(node);
        children.add(new NodeTuple(itemKey, itemValue));
    }
    
    protected Node composeKeyNode(final MappingNode node) {
        return this.composeNode(node);
    }
    
    protected Node composeValueNode(final MappingNode node) {
        return this.composeNode(node);
    }
}
