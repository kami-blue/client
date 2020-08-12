// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

class ByteVector implements Cloneable
{
    private byte[] buffer;
    private int size;
    
    public ByteVector() {
        this.buffer = new byte[64];
        this.size = 0;
    }
    
    public Object clone() throws CloneNotSupportedException {
        final ByteVector bv = (ByteVector)super.clone();
        bv.buffer = this.buffer.clone();
        return bv;
    }
    
    public final int getSize() {
        return this.size;
    }
    
    public final byte[] copy() {
        final byte[] b = new byte[this.size];
        System.arraycopy(this.buffer, 0, b, 0, this.size);
        return b;
    }
    
    public int read(final int offset) {
        if (offset < 0 || this.size <= offset) {
            throw new ArrayIndexOutOfBoundsException(offset);
        }
        return this.buffer[offset];
    }
    
    public void write(final int offset, final int value) {
        if (offset < 0 || this.size <= offset) {
            throw new ArrayIndexOutOfBoundsException(offset);
        }
        this.buffer[offset] = (byte)value;
    }
    
    public void add(final int code) {
        this.addGap(1);
        this.buffer[this.size - 1] = (byte)code;
    }
    
    public void add(final int b1, final int b2) {
        this.addGap(2);
        this.buffer[this.size - 2] = (byte)b1;
        this.buffer[this.size - 1] = (byte)b2;
    }
    
    public void add(final int b1, final int b2, final int b3, final int b4) {
        this.addGap(4);
        this.buffer[this.size - 4] = (byte)b1;
        this.buffer[this.size - 3] = (byte)b2;
        this.buffer[this.size - 2] = (byte)b3;
        this.buffer[this.size - 1] = (byte)b4;
    }
    
    public void addGap(final int length) {
        if (this.size + length > this.buffer.length) {
            int newSize = this.size << 1;
            if (newSize < this.size + length) {
                newSize = this.size + length;
            }
            final byte[] newBuf = new byte[newSize];
            System.arraycopy(this.buffer, 0, newBuf, 0, this.size);
            this.buffer = newBuf;
        }
        this.size += length;
    }
}
