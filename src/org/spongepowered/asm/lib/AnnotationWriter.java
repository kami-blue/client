// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib;

final class AnnotationWriter extends AnnotationVisitor
{
    private final ClassWriter cw;
    private int size;
    private final boolean named;
    private final ByteVector bv;
    private final ByteVector parent;
    private final int offset;
    AnnotationWriter next;
    AnnotationWriter prev;
    
    AnnotationWriter(final ClassWriter cw, final boolean named, final ByteVector bv, final ByteVector parent, final int offset) {
        super(327680);
        this.cw = cw;
        this.named = named;
        this.bv = bv;
        this.parent = parent;
        this.offset = offset;
    }
    
    @Override
    public void visit(final String name, final Object value) {
        ++this.size;
        if (this.named) {
            this.bv.putShort(this.cw.newUTF8(name));
        }
        if (value instanceof String) {
            this.bv.put12(115, this.cw.newUTF8((String)value));
        }
        else if (value instanceof Byte) {
            this.bv.put12(66, this.cw.newInteger((byte)value).index);
        }
        else if (value instanceof Boolean) {
            final int v = ((boolean)value) ? 1 : 0;
            this.bv.put12(90, this.cw.newInteger(v).index);
        }
        else if (value instanceof Character) {
            this.bv.put12(67, this.cw.newInteger((char)value).index);
        }
        else if (value instanceof Short) {
            this.bv.put12(83, this.cw.newInteger((short)value).index);
        }
        else if (value instanceof Type) {
            this.bv.put12(99, this.cw.newUTF8(((Type)value).getDescriptor()));
        }
        else if (value instanceof byte[]) {
            final byte[] v2 = (byte[])value;
            this.bv.put12(91, v2.length);
            for (int i = 0; i < v2.length; ++i) {
                this.bv.put12(66, this.cw.newInteger(v2[i]).index);
            }
        }
        else if (value instanceof boolean[]) {
            final boolean[] v3 = (boolean[])value;
            this.bv.put12(91, v3.length);
            for (int i = 0; i < v3.length; ++i) {
                this.bv.put12(90, this.cw.newInteger(v3[i] ? 1 : 0).index);
            }
        }
        else if (value instanceof short[]) {
            final short[] v4 = (short[])value;
            this.bv.put12(91, v4.length);
            for (int i = 0; i < v4.length; ++i) {
                this.bv.put12(83, this.cw.newInteger(v4[i]).index);
            }
        }
        else if (value instanceof char[]) {
            final char[] v5 = (char[])value;
            this.bv.put12(91, v5.length);
            for (int i = 0; i < v5.length; ++i) {
                this.bv.put12(67, this.cw.newInteger(v5[i]).index);
            }
        }
        else if (value instanceof int[]) {
            final int[] v6 = (int[])value;
            this.bv.put12(91, v6.length);
            for (int i = 0; i < v6.length; ++i) {
                this.bv.put12(73, this.cw.newInteger(v6[i]).index);
            }
        }
        else if (value instanceof long[]) {
            final long[] v7 = (long[])value;
            this.bv.put12(91, v7.length);
            for (int i = 0; i < v7.length; ++i) {
                this.bv.put12(74, this.cw.newLong(v7[i]).index);
            }
        }
        else if (value instanceof float[]) {
            final float[] v8 = (float[])value;
            this.bv.put12(91, v8.length);
            for (int i = 0; i < v8.length; ++i) {
                this.bv.put12(70, this.cw.newFloat(v8[i]).index);
            }
        }
        else if (value instanceof double[]) {
            final double[] v9 = (double[])value;
            this.bv.put12(91, v9.length);
            for (int i = 0; i < v9.length; ++i) {
                this.bv.put12(68, this.cw.newDouble(v9[i]).index);
            }
        }
        else {
            final Item j = this.cw.newConstItem(value);
            this.bv.put12(".s.IFJDCS".charAt(j.type), j.index);
        }
    }
    
    @Override
    public void visitEnum(final String name, final String desc, final String value) {
        ++this.size;
        if (this.named) {
            this.bv.putShort(this.cw.newUTF8(name));
        }
        this.bv.put12(101, this.cw.newUTF8(desc)).putShort(this.cw.newUTF8(value));
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(final String name, final String desc) {
        ++this.size;
        if (this.named) {
            this.bv.putShort(this.cw.newUTF8(name));
        }
        this.bv.put12(64, this.cw.newUTF8(desc)).putShort(0);
        return new AnnotationWriter(this.cw, true, this.bv, this.bv, this.bv.length - 2);
    }
    
    @Override
    public AnnotationVisitor visitArray(final String name) {
        ++this.size;
        if (this.named) {
            this.bv.putShort(this.cw.newUTF8(name));
        }
        this.bv.put12(91, 0);
        return new AnnotationWriter(this.cw, false, this.bv, this.bv, this.bv.length - 2);
    }
    
    @Override
    public void visitEnd() {
        if (this.parent != null) {
            final byte[] data = this.parent.data;
            data[this.offset] = (byte)(this.size >>> 8);
            data[this.offset + 1] = (byte)this.size;
        }
    }
    
    int getSize() {
        int size = 0;
        for (AnnotationWriter aw = this; aw != null; aw = aw.next) {
            size += aw.bv.length;
        }
        return size;
    }
    
    void put(final ByteVector out) {
        int n = 0;
        int size = 2;
        AnnotationWriter aw = this;
        AnnotationWriter last = null;
        while (aw != null) {
            ++n;
            size += aw.bv.length;
            aw.visitEnd();
            aw.prev = last;
            last = aw;
            aw = aw.next;
        }
        out.putInt(size);
        out.putShort(n);
        for (aw = last; aw != null; aw = aw.prev) {
            out.putByteArray(aw.bv.data, 0, aw.bv.length);
        }
    }
    
    static void put(final AnnotationWriter[] panns, final int off, final ByteVector out) {
        int size = 1 + 2 * (panns.length - off);
        for (int i = off; i < panns.length; ++i) {
            size += ((panns[i] == null) ? 0 : panns[i].getSize());
        }
        out.putInt(size).putByte(panns.length - off);
        for (int i = off; i < panns.length; ++i) {
            AnnotationWriter aw = panns[i];
            AnnotationWriter last = null;
            int n = 0;
            while (aw != null) {
                ++n;
                aw.visitEnd();
                aw.prev = last;
                last = aw;
                aw = aw.next;
            }
            out.putShort(n);
            for (aw = last; aw != null; aw = aw.prev) {
                out.putByteArray(aw.bv.data, 0, aw.bv.length);
            }
        }
    }
    
    static void putTarget(final int typeRef, final TypePath typePath, final ByteVector out) {
        switch (typeRef >>> 24) {
            case 0:
            case 1:
            case 22: {
                out.putShort(typeRef >>> 16);
                break;
            }
            case 19:
            case 20:
            case 21: {
                out.putByte(typeRef >>> 24);
                break;
            }
            case 71:
            case 72:
            case 73:
            case 74:
            case 75: {
                out.putInt(typeRef);
                break;
            }
            default: {
                out.put12(typeRef >>> 24, (typeRef & 0xFFFF00) >> 8);
                break;
            }
        }
        if (typePath == null) {
            out.putByte(0);
        }
        else {
            final int length = typePath.b[typePath.offset] * 2 + 1;
            out.putByteArray(typePath.b, typePath.offset, length);
        }
    }
}
