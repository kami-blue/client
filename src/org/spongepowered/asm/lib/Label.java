// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib;

public class Label
{
    static final int DEBUG = 1;
    static final int RESOLVED = 2;
    static final int RESIZED = 4;
    static final int PUSHED = 8;
    static final int TARGET = 16;
    static final int STORE = 32;
    static final int REACHABLE = 64;
    static final int JSR = 128;
    static final int RET = 256;
    static final int SUBROUTINE = 512;
    static final int VISITED = 1024;
    static final int VISITED2 = 2048;
    public Object info;
    int status;
    int line;
    int position;
    private int referenceCount;
    private int[] srcAndRefPositions;
    int inputStackTop;
    int outputStackMax;
    Frame frame;
    Label successor;
    Edge successors;
    Label next;
    
    public int getOffset() {
        if ((this.status & 0x2) == 0x0) {
            throw new IllegalStateException("Label offset position has not been resolved yet");
        }
        return this.position;
    }
    
    void put(final MethodWriter owner, final ByteVector out, final int source, final boolean wideOffset) {
        if ((this.status & 0x2) == 0x0) {
            if (wideOffset) {
                this.addReference(-1 - source, out.length);
                out.putInt(-1);
            }
            else {
                this.addReference(source, out.length);
                out.putShort(-1);
            }
        }
        else if (wideOffset) {
            out.putInt(this.position - source);
        }
        else {
            out.putShort(this.position - source);
        }
    }
    
    private void addReference(final int sourcePosition, final int referencePosition) {
        if (this.srcAndRefPositions == null) {
            this.srcAndRefPositions = new int[6];
        }
        if (this.referenceCount >= this.srcAndRefPositions.length) {
            final int[] a = new int[this.srcAndRefPositions.length + 6];
            System.arraycopy(this.srcAndRefPositions, 0, a, 0, this.srcAndRefPositions.length);
            this.srcAndRefPositions = a;
        }
        this.srcAndRefPositions[this.referenceCount++] = sourcePosition;
        this.srcAndRefPositions[this.referenceCount++] = referencePosition;
    }
    
    boolean resolve(final MethodWriter owner, final int position, final byte[] data) {
        boolean needUpdate = false;
        this.status |= 0x2;
        this.position = position;
        int i = 0;
        while (i < this.referenceCount) {
            final int source = this.srcAndRefPositions[i++];
            int reference = this.srcAndRefPositions[i++];
            if (source >= 0) {
                final int offset = position - source;
                if (offset < -32768 || offset > 32767) {
                    final int opcode = data[reference - 1] & 0xFF;
                    if (opcode <= 168) {
                        data[reference - 1] = (byte)(opcode + 49);
                    }
                    else {
                        data[reference - 1] = (byte)(opcode + 20);
                    }
                    needUpdate = true;
                }
                data[reference++] = (byte)(offset >>> 8);
                data[reference] = (byte)offset;
            }
            else {
                final int offset = position + source + 1;
                data[reference++] = (byte)(offset >>> 24);
                data[reference++] = (byte)(offset >>> 16);
                data[reference++] = (byte)(offset >>> 8);
                data[reference] = (byte)offset;
            }
        }
        return needUpdate;
    }
    
    Label getFirst() {
        return (this.frame == null) ? this : this.frame.owner;
    }
    
    boolean inSubroutine(final long id) {
        return (this.status & 0x400) != 0x0 && (this.srcAndRefPositions[(int)(id >>> 32)] & (int)id) != 0x0;
    }
    
    boolean inSameSubroutine(final Label block) {
        if ((this.status & 0x400) == 0x0 || (block.status & 0x400) == 0x0) {
            return false;
        }
        for (int i = 0; i < this.srcAndRefPositions.length; ++i) {
            if ((this.srcAndRefPositions[i] & block.srcAndRefPositions[i]) != 0x0) {
                return true;
            }
        }
        return false;
    }
    
    void addToSubroutine(final long id, final int nbSubroutines) {
        if ((this.status & 0x400) == 0x0) {
            this.status |= 0x400;
            this.srcAndRefPositions = new int[nbSubroutines / 32 + 1];
        }
        final int[] srcAndRefPositions = this.srcAndRefPositions;
        final int n = (int)(id >>> 32);
        srcAndRefPositions[n] |= (int)id;
    }
    
    void visitSubroutine(final Label JSR, final long id, final int nbSubroutines) {
        Label stack = this;
        while (stack != null) {
            final Label l = stack;
            stack = l.next;
            l.next = null;
            if (JSR != null) {
                if ((l.status & 0x800) != 0x0) {
                    continue;
                }
                final Label label = l;
                label.status |= 0x800;
                if ((l.status & 0x100) != 0x0 && !l.inSameSubroutine(JSR)) {
                    final Edge e = new Edge();
                    e.info = l.inputStackTop;
                    e.successor = JSR.successors.successor;
                    e.next = l.successors;
                    l.successors = e;
                }
            }
            else {
                if (l.inSubroutine(id)) {
                    continue;
                }
                l.addToSubroutine(id, nbSubroutines);
            }
            for (Edge e = l.successors; e != null; e = e.next) {
                if (((l.status & 0x80) == 0x0 || e != l.successors.next) && e.successor.next == null) {
                    e.successor.next = stack;
                    stack = e.successor;
                }
            }
        }
    }
    
    @Override
    public String toString() {
        return "L" + System.identityHashCode(this);
    }
}
