// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib;

public class Attribute
{
    public final String type;
    byte[] value;
    Attribute next;
    
    protected Attribute(final String type) {
        this.type = type;
    }
    
    public boolean isUnknown() {
        return true;
    }
    
    public boolean isCodeAttribute() {
        return false;
    }
    
    protected Label[] getLabels() {
        return null;
    }
    
    protected Attribute read(final ClassReader cr, final int off, final int len, final char[] buf, final int codeOff, final Label[] labels) {
        final Attribute attr = new Attribute(this.type);
        attr.value = new byte[len];
        System.arraycopy(cr.b, off, attr.value, 0, len);
        return attr;
    }
    
    protected ByteVector write(final ClassWriter cw, final byte[] code, final int len, final int maxStack, final int maxLocals) {
        final ByteVector v = new ByteVector();
        v.data = this.value;
        v.length = this.value.length;
        return v;
    }
    
    final int getCount() {
        int count = 0;
        for (Attribute attr = this; attr != null; attr = attr.next) {
            ++count;
        }
        return count;
    }
    
    final int getSize(final ClassWriter cw, final byte[] code, final int len, final int maxStack, final int maxLocals) {
        Attribute attr = this;
        int size = 0;
        while (attr != null) {
            cw.newUTF8(attr.type);
            size += attr.write(cw, code, len, maxStack, maxLocals).length + 6;
            attr = attr.next;
        }
        return size;
    }
    
    final void put(final ClassWriter cw, final byte[] code, final int len, final int maxStack, final int maxLocals, final ByteVector out) {
        for (Attribute attr = this; attr != null; attr = attr.next) {
            final ByteVector b = attr.write(cw, code, len, maxStack, maxLocals);
            out.putShort(cw.newUTF8(attr.type)).putInt(b.length);
            out.putByteArray(b.data, 0, b.length);
        }
    }
}
