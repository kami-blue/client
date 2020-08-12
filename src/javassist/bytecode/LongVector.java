// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

final class LongVector
{
    static final int ASIZE = 128;
    static final int ABITS = 7;
    static final int VSIZE = 8;
    private ConstInfo[][] objects;
    private int elements;
    
    public LongVector() {
        this.objects = new ConstInfo[8][];
        this.elements = 0;
    }
    
    public LongVector(final int initialSize) {
        final int vsize = (initialSize >> 7 & 0xFFFFFFF8) + 8;
        this.objects = new ConstInfo[vsize][];
        this.elements = 0;
    }
    
    public int size() {
        return this.elements;
    }
    
    public int capacity() {
        return this.objects.length * 128;
    }
    
    public ConstInfo elementAt(final int i) {
        if (i < 0 || this.elements <= i) {
            return null;
        }
        return this.objects[i >> 7][i & 0x7F];
    }
    
    public void addElement(final ConstInfo value) {
        final int nth = this.elements >> 7;
        final int offset = this.elements & 0x7F;
        final int len = this.objects.length;
        if (nth >= len) {
            final ConstInfo[][] newObj = new ConstInfo[len + 8][];
            System.arraycopy(this.objects, 0, newObj, 0, len);
            this.objects = newObj;
        }
        if (this.objects[nth] == null) {
            this.objects[nth] = new ConstInfo[128];
        }
        this.objects[nth][offset] = value;
        ++this.elements;
    }
}
