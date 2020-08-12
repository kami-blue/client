// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.analysis;

import java.util.ArrayList;
import javassist.bytecode.stackmap.BasicBlock;
import javassist.bytecode.BadBytecode;
import javassist.CtMethod;
import javassist.bytecode.MethodInfo;
import javassist.CtClass;

public class ControlFlow
{
    private CtClass clazz;
    private MethodInfo methodInfo;
    private Block[] basicBlocks;
    private Frame[] frames;
    
    public ControlFlow(final CtMethod method) throws BadBytecode {
        this(method.getDeclaringClass(), method.getMethodInfo2());
    }
    
    public ControlFlow(final CtClass ctclazz, final MethodInfo minfo) throws BadBytecode {
        this.clazz = ctclazz;
        this.methodInfo = minfo;
        this.frames = null;
        this.basicBlocks = (Block[])new BasicBlock.Maker() {
            @Override
            protected BasicBlock makeBlock(final int pos) {
                return new Block(pos, ControlFlow.this.methodInfo);
            }
            
            @Override
            protected BasicBlock[] makeArray(final int size) {
                return new Block[size];
            }
        }.make(minfo);
        if (this.basicBlocks == null) {
            this.basicBlocks = new Block[0];
        }
        final int size = this.basicBlocks.length;
        final int[] counters = new int[size];
        for (int i = 0; i < size; ++i) {
            final Block b = this.basicBlocks[i];
            b.index = i;
            b.entrances = new Block[b.incomings()];
            counters[i] = 0;
        }
        for (int i = 0; i < size; ++i) {
            final Block b = this.basicBlocks[i];
            for (int k = 0; k < b.exits(); ++k) {
                final Block e = b.exit(k);
                e.entrances[counters[e.index]++] = b;
            }
            final Catcher[] catchers = b.catchers();
            for (int j = 0; j < catchers.length; ++j) {
                final Block catchBlock = catchers[j].node;
                catchBlock.entrances[counters[catchBlock.index]++] = b;
            }
        }
    }
    
    public Block[] basicBlocks() {
        return this.basicBlocks;
    }
    
    public Frame frameAt(final int pos) throws BadBytecode {
        if (this.frames == null) {
            this.frames = new Analyzer().analyze(this.clazz, this.methodInfo);
        }
        return this.frames[pos];
    }
    
    public Node[] dominatorTree() {
        final int size = this.basicBlocks.length;
        if (size == 0) {
            return null;
        }
        final Node[] nodes = new Node[size];
        final boolean[] visited = new boolean[size];
        final int[] distance = new int[size];
        for (int i = 0; i < size; ++i) {
            nodes[i] = new Node(this.basicBlocks[i]);
            visited[i] = false;
        }
        final Access access = new Access(nodes) {
            @Override
            BasicBlock[] exits(final Node n) {
                return n.block.getExit();
            }
            
            @Override
            BasicBlock[] entrances(final Node n) {
                return n.block.entrances;
            }
        };
        nodes[0].makeDepth1stTree(null, visited, 0, distance, access);
        do {
            for (int j = 0; j < size; ++j) {
                visited[j] = false;
            }
        } while (nodes[0].makeDominatorTree(visited, distance, access));
        setChildren(nodes);
        return nodes;
    }
    
    public Node[] postDominatorTree() {
        final int size = this.basicBlocks.length;
        if (size == 0) {
            return null;
        }
        final Node[] nodes = new Node[size];
        final boolean[] visited = new boolean[size];
        final int[] distance = new int[size];
        for (int i = 0; i < size; ++i) {
            nodes[i] = new Node(this.basicBlocks[i]);
            visited[i] = false;
        }
        final Access access = new Access(nodes) {
            @Override
            BasicBlock[] exits(final Node n) {
                return n.block.entrances;
            }
            
            @Override
            BasicBlock[] entrances(final Node n) {
                return n.block.getExit();
            }
        };
        int counter = 0;
        for (int j = 0; j < size; ++j) {
            if (nodes[j].block.exits() == 0) {
                counter = nodes[j].makeDepth1stTree(null, visited, counter, distance, access);
            }
        }
        boolean changed;
        do {
            for (int k = 0; k < size; ++k) {
                visited[k] = false;
            }
            changed = false;
            for (int k = 0; k < size; ++k) {
                if (nodes[k].block.exits() == 0 && nodes[k].makeDominatorTree(visited, distance, access)) {
                    changed = true;
                }
            }
        } while (changed);
        setChildren(nodes);
        return nodes;
    }
    
    public static class Block extends BasicBlock
    {
        public Object clientData;
        int index;
        MethodInfo method;
        Block[] entrances;
        
        Block(final int pos, final MethodInfo minfo) {
            super(pos);
            this.clientData = null;
            this.method = minfo;
        }
        
        @Override
        protected void toString2(final StringBuffer sbuf) {
            super.toString2(sbuf);
            sbuf.append(", incoming{");
            for (int i = 0; i < this.entrances.length; ++i) {
                sbuf.append(this.entrances[i].position).append(", ");
            }
            sbuf.append("}");
        }
        
        BasicBlock[] getExit() {
            return this.exit;
        }
        
        public int index() {
            return this.index;
        }
        
        public int position() {
            return this.position;
        }
        
        public int length() {
            return this.length;
        }
        
        public int incomings() {
            return this.incoming;
        }
        
        public Block incoming(final int n) {
            return this.entrances[n];
        }
        
        public int exits() {
            return (this.exit == null) ? 0 : this.exit.length;
        }
        
        public Block exit(final int n) {
            return (Block)this.exit[n];
        }
        
        public Catcher[] catchers() {
            final ArrayList catchers = new ArrayList();
            for (Catch c = this.toCatch; c != null; c = c.next) {
                catchers.add(new Catcher(c));
            }
            return catchers.toArray(new Catcher[catchers.size()]);
        }
    }
    
    abstract static class Access
    {
        Node[] all;
        
        Access(final Node[] nodes) {
            this.all = nodes;
        }
        
        Node node(final BasicBlock b) {
            return this.all[((Block)b).index];
        }
        
        abstract BasicBlock[] exits(final Node p0);
        
        abstract BasicBlock[] entrances(final Node p0);
    }
    
    public static class Node
    {
        private Block block;
        private Node parent;
        private Node[] children;
        
        Node(final Block b) {
            this.block = b;
            this.parent = null;
        }
        
        @Override
        public String toString() {
            final StringBuffer sbuf = new StringBuffer();
            sbuf.append("Node[pos=").append(this.block().position());
            sbuf.append(", parent=");
            sbuf.append((this.parent == null) ? "*" : Integer.toString(this.parent.block().position()));
            sbuf.append(", children{");
            for (int i = 0; i < this.children.length; ++i) {
                sbuf.append(this.children[i].block().position()).append(", ");
            }
            sbuf.append("}]");
            return sbuf.toString();
        }
        
        public Block block() {
            return this.block;
        }
        
        public Node parent() {
            return this.parent;
        }
        
        public int children() {
            return this.children.length;
        }
        
        public Node child(final int n) {
            return this.children[n];
        }
        
        int makeDepth1stTree(final Node caller, final boolean[] visited, int counter, final int[] distance, final Access access) {
            final int index = this.block.index;
            if (visited[index]) {
                return counter;
            }
            visited[index] = true;
            this.parent = caller;
            final BasicBlock[] exits = access.exits(this);
            if (exits != null) {
                for (int i = 0; i < exits.length; ++i) {
                    final Node n = access.node(exits[i]);
                    counter = n.makeDepth1stTree(this, visited, counter, distance, access);
                }
            }
            distance[index] = counter++;
            return counter;
        }
        
        boolean makeDominatorTree(final boolean[] visited, final int[] distance, final Access access) {
            final int index = this.block.index;
            if (visited[index]) {
                return false;
            }
            visited[index] = true;
            boolean changed = false;
            final BasicBlock[] exits = access.exits(this);
            if (exits != null) {
                for (int i = 0; i < exits.length; ++i) {
                    final Node n = access.node(exits[i]);
                    if (n.makeDominatorTree(visited, distance, access)) {
                        changed = true;
                    }
                }
            }
            final BasicBlock[] entrances = access.entrances(this);
            if (entrances != null) {
                for (int j = 0; j < entrances.length; ++j) {
                    if (this.parent != null) {
                        final Node n2 = getAncestor(this.parent, access.node(entrances[j]), distance);
                        if (n2 != this.parent) {
                            this.parent = n2;
                            changed = true;
                        }
                    }
                }
            }
            return changed;
        }
        
        private static Node getAncestor(Node n1, Node n2, final int[] distance) {
            while (n1 != n2) {
                if (distance[n1.block.index] < distance[n2.block.index]) {
                    n1 = n1.parent;
                }
                else {
                    n2 = n2.parent;
                }
                if (n1 == null || n2 == null) {
                    return null;
                }
            }
            return n1;
        }
        
        private static void setChildren(final Node[] all) {
            final int size = all.length;
            final int[] nchildren = new int[size];
            for (int i = 0; i < size; ++i) {
                nchildren[i] = 0;
            }
            for (int i = 0; i < size; ++i) {
                final Node p = all[i].parent;
                if (p != null) {
                    final int[] array = nchildren;
                    final int index = p.block.index;
                    ++array[index];
                }
            }
            for (int i = 0; i < size; ++i) {
                all[i].children = new Node[nchildren[i]];
            }
            for (int i = 0; i < size; ++i) {
                nchildren[i] = 0;
            }
            for (final Node n : all) {
                final Node p2 = n.parent;
                if (p2 != null) {
                    p2.children[nchildren[p2.block.index]++] = n;
                }
            }
        }
    }
    
    public static class Catcher
    {
        private Block node;
        private int typeIndex;
        
        Catcher(final BasicBlock.Catch c) {
            this.node = (Block)c.body;
            this.typeIndex = c.typeIndex;
        }
        
        public Block block() {
            return this.node;
        }
        
        public String type() {
            if (this.typeIndex == 0) {
                return "java.lang.Throwable";
            }
            return this.node.method.getConstPool().getClassInfo(this.typeIndex);
        }
    }
}
