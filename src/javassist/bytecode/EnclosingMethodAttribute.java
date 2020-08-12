// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

public class EnclosingMethodAttribute extends AttributeInfo
{
    public static final String tag = "EnclosingMethod";
    
    EnclosingMethodAttribute(final ConstPool cp, final int n, final DataInputStream in) throws IOException {
        super(cp, n, in);
    }
    
    public EnclosingMethodAttribute(final ConstPool cp, final String className, final String methodName, final String methodDesc) {
        super(cp, "EnclosingMethod");
        final int ci = cp.addClassInfo(className);
        final int ni = cp.addNameAndTypeInfo(methodName, methodDesc);
        final byte[] bvalue = { (byte)(ci >>> 8), (byte)ci, (byte)(ni >>> 8), (byte)ni };
        this.set(bvalue);
    }
    
    public EnclosingMethodAttribute(final ConstPool cp, final String className) {
        super(cp, "EnclosingMethod");
        final int ci = cp.addClassInfo(className);
        final int ni = 0;
        final byte[] bvalue = { (byte)(ci >>> 8), (byte)ci, (byte)(ni >>> 8), (byte)ni };
        this.set(bvalue);
    }
    
    public int classIndex() {
        return ByteArray.readU16bit(this.get(), 0);
    }
    
    public int methodIndex() {
        return ByteArray.readU16bit(this.get(), 2);
    }
    
    public String className() {
        return this.getConstPool().getClassInfo(this.classIndex());
    }
    
    public String methodName() {
        final ConstPool cp = this.getConstPool();
        final int mi = this.methodIndex();
        if (mi == 0) {
            return "<clinit>";
        }
        final int ni = cp.getNameAndTypeName(mi);
        return cp.getUtf8Info(ni);
    }
    
    public String methodDescriptor() {
        final ConstPool cp = this.getConstPool();
        final int mi = this.methodIndex();
        final int ti = cp.getNameAndTypeDescriptor(mi);
        return cp.getUtf8Info(ti);
    }
    
    @Override
    public AttributeInfo copy(final ConstPool newCp, final Map classnames) {
        if (this.methodIndex() == 0) {
            return new EnclosingMethodAttribute(newCp, this.className());
        }
        return new EnclosingMethodAttribute(newCp, this.className(), this.methodName(), this.methodDescriptor());
    }
}
