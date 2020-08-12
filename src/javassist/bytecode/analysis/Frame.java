// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.analysis;

public class Frame
{
    private Type[] locals;
    private Type[] stack;
    private int top;
    private boolean jsrMerged;
    private boolean retMerged;
    
    public Frame(final int locals, final int stack) {
        this.locals = new Type[locals];
        this.stack = new Type[stack];
    }
    
    public Type getLocal(final int index) {
        return this.locals[index];
    }
    
    public void setLocal(final int index, final Type type) {
        this.locals[index] = type;
    }
    
    public Type getStack(final int index) {
        return this.stack[index];
    }
    
    public void setStack(final int index, final Type type) {
        this.stack[index] = type;
    }
    
    public void clearStack() {
        this.top = 0;
    }
    
    public int getTopIndex() {
        return this.top - 1;
    }
    
    public int localsLength() {
        return this.locals.length;
    }
    
    public Type peek() {
        if (this.top < 1) {
            throw new IndexOutOfBoundsException("Stack is empty");
        }
        return this.stack[this.top - 1];
    }
    
    public Type pop() {
        if (this.top < 1) {
            throw new IndexOutOfBoundsException("Stack is empty");
        }
        final Type[] stack = this.stack;
        final int top = this.top - 1;
        this.top = top;
        return stack[top];
    }
    
    public void push(final Type type) {
        this.stack[this.top++] = type;
    }
    
    public Frame copy() {
        final Frame frame = new Frame(this.locals.length, this.stack.length);
        System.arraycopy(this.locals, 0, frame.locals, 0, this.locals.length);
        System.arraycopy(this.stack, 0, frame.stack, 0, this.stack.length);
        frame.top = this.top;
        return frame;
    }
    
    public Frame copyStack() {
        final Frame frame = new Frame(this.locals.length, this.stack.length);
        System.arraycopy(this.stack, 0, frame.stack, 0, this.stack.length);
        frame.top = this.top;
        return frame;
    }
    
    public boolean mergeStack(final Frame frame) {
        boolean changed = false;
        if (this.top != frame.top) {
            throw new RuntimeException("Operand stacks could not be merged, they are different sizes!");
        }
        for (int i = 0; i < this.top; ++i) {
            if (this.stack[i] != null) {
                final Type prev = this.stack[i];
                final Type merged = prev.merge(frame.stack[i]);
                if (merged == Type.BOGUS) {
                    throw new RuntimeException("Operand stacks could not be merged due to differing primitive types: pos = " + i);
                }
                this.stack[i] = merged;
                if (!merged.equals(prev) || merged.popChanged()) {
                    changed = true;
                }
            }
        }
        return changed;
    }
    
    public boolean merge(final Frame frame) {
        boolean changed = false;
        for (int i = 0; i < this.locals.length; ++i) {
            if (this.locals[i] != null) {
                final Type prev = this.locals[i];
                final Type merged = prev.merge(frame.locals[i]);
                this.locals[i] = merged;
                if (!merged.equals(prev) || merged.popChanged()) {
                    changed = true;
                }
            }
            else if (frame.locals[i] != null) {
                this.locals[i] = frame.locals[i];
                changed = true;
            }
        }
        changed |= this.mergeStack(frame);
        return changed;
    }
    
    @Override
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("locals = [");
        for (int i = 0; i < this.locals.length; ++i) {
            buffer.append((this.locals[i] == null) ? "empty" : this.locals[i].toString());
            if (i < this.locals.length - 1) {
                buffer.append(", ");
            }
        }
        buffer.append("] stack = [");
        for (int i = 0; i < this.top; ++i) {
            buffer.append(this.stack[i]);
            if (i < this.top - 1) {
                buffer.append(", ");
            }
        }
        buffer.append("]");
        return buffer.toString();
    }
    
    boolean isJsrMerged() {
        return this.jsrMerged;
    }
    
    void setJsrMerged(final boolean jsrMerged) {
        this.jsrMerged = jsrMerged;
    }
    
    boolean isRetMerged() {
        return this.retMerged;
    }
    
    void setRetMerged(final boolean retMerged) {
        this.retMerged = retMerged;
    }
}
