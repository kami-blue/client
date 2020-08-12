// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.IOException;
import java.io.OutputStream;

final class ByteStream extends OutputStream
{
    private byte[] buf;
    private int count;
    
    public ByteStream() {
        this(32);
    }
    
    public ByteStream(final int size) {
        this.buf = new byte[size];
        this.count = 0;
    }
    
    public int getPos() {
        return this.count;
    }
    
    public int size() {
        return this.count;
    }
    
    public void writeBlank(final int len) {
        this.enlarge(len);
        this.count += len;
    }
    
    @Override
    public void write(final byte[] data) {
        this.write(data, 0, data.length);
    }
    
    @Override
    public void write(final byte[] data, final int off, final int len) {
        this.enlarge(len);
        System.arraycopy(data, off, this.buf, this.count, len);
        this.count += len;
    }
    
    @Override
    public void write(final int b) {
        this.enlarge(1);
        final int oldCount = this.count;
        this.buf[oldCount] = (byte)b;
        this.count = oldCount + 1;
    }
    
    public void writeShort(final int s) {
        this.enlarge(2);
        final int oldCount = this.count;
        this.buf[oldCount] = (byte)(s >>> 8);
        this.buf[oldCount + 1] = (byte)s;
        this.count = oldCount + 2;
    }
    
    public void writeInt(final int i) {
        this.enlarge(4);
        final int oldCount = this.count;
        this.buf[oldCount] = (byte)(i >>> 24);
        this.buf[oldCount + 1] = (byte)(i >>> 16);
        this.buf[oldCount + 2] = (byte)(i >>> 8);
        this.buf[oldCount + 3] = (byte)i;
        this.count = oldCount + 4;
    }
    
    public void writeLong(final long i) {
        this.enlarge(8);
        final int oldCount = this.count;
        this.buf[oldCount] = (byte)(i >>> 56);
        this.buf[oldCount + 1] = (byte)(i >>> 48);
        this.buf[oldCount + 2] = (byte)(i >>> 40);
        this.buf[oldCount + 3] = (byte)(i >>> 32);
        this.buf[oldCount + 4] = (byte)(i >>> 24);
        this.buf[oldCount + 5] = (byte)(i >>> 16);
        this.buf[oldCount + 6] = (byte)(i >>> 8);
        this.buf[oldCount + 7] = (byte)i;
        this.count = oldCount + 8;
    }
    
    public void writeFloat(final float v) {
        this.writeInt(Float.floatToIntBits(v));
    }
    
    public void writeDouble(final double v) {
        this.writeLong(Double.doubleToLongBits(v));
    }
    
    public void writeUTF(final String s) {
        final int sLen = s.length();
        int pos = this.count;
        this.enlarge(sLen + 2);
        final byte[] buffer = this.buf;
        buffer[pos++] = (byte)(sLen >>> 8);
        buffer[pos++] = (byte)sLen;
        for (int i = 0; i < sLen; ++i) {
            final char c = s.charAt(i);
            if ('\u0001' > c || c > '\u007f') {
                this.writeUTF2(s, sLen, i);
                return;
            }
            buffer[pos++] = (byte)c;
        }
        this.count = pos;
    }
    
    private void writeUTF2(final String s, final int sLen, final int offset) {
        int size = sLen;
        for (int i = offset; i < sLen; ++i) {
            final int c = s.charAt(i);
            if (c > 2047) {
                size += 2;
            }
            else if (c == 0 || c > 127) {
                ++size;
            }
        }
        if (size > 65535) {
            throw new RuntimeException("encoded string too long: " + sLen + size + " bytes");
        }
        this.enlarge(size + 2);
        int pos = this.count;
        final byte[] buffer = this.buf;
        buffer[pos] = (byte)(size >>> 8);
        buffer[pos + 1] = (byte)size;
        pos += 2 + offset;
        for (int j = offset; j < sLen; ++j) {
            final int c2 = s.charAt(j);
            if (1 <= c2 && c2 <= 127) {
                buffer[pos++] = (byte)c2;
            }
            else if (c2 > 2047) {
                buffer[pos] = (byte)(0xE0 | (c2 >> 12 & 0xF));
                buffer[pos + 1] = (byte)(0x80 | (c2 >> 6 & 0x3F));
                buffer[pos + 2] = (byte)(0x80 | (c2 & 0x3F));
                pos += 3;
            }
            else {
                buffer[pos] = (byte)(0xC0 | (c2 >> 6 & 0x1F));
                buffer[pos + 1] = (byte)(0x80 | (c2 & 0x3F));
                pos += 2;
            }
        }
        this.count = pos;
    }
    
    public void write(final int pos, final int value) {
        this.buf[pos] = (byte)value;
    }
    
    public void writeShort(final int pos, final int value) {
        this.buf[pos] = (byte)(value >>> 8);
        this.buf[pos + 1] = (byte)value;
    }
    
    public void writeInt(final int pos, final int value) {
        this.buf[pos] = (byte)(value >>> 24);
        this.buf[pos + 1] = (byte)(value >>> 16);
        this.buf[pos + 2] = (byte)(value >>> 8);
        this.buf[pos + 3] = (byte)value;
    }
    
    public byte[] toByteArray() {
        final byte[] buf2 = new byte[this.count];
        System.arraycopy(this.buf, 0, buf2, 0, this.count);
        return buf2;
    }
    
    public void writeTo(final OutputStream out) throws IOException {
        out.write(this.buf, 0, this.count);
    }
    
    public void enlarge(final int delta) {
        final int newCount = this.count + delta;
        if (newCount > this.buf.length) {
            final int newLen = this.buf.length << 1;
            final byte[] newBuf = new byte[(newLen > newCount) ? newLen : newCount];
            System.arraycopy(this.buf, 0, newBuf, 0, this.count);
            this.buf = newBuf;
        }
    }
}
