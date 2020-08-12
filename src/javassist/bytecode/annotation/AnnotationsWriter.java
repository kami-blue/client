// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.annotation;

import javassist.bytecode.ByteArray;
import java.io.IOException;
import javassist.bytecode.ConstPool;
import java.io.OutputStream;

public class AnnotationsWriter
{
    protected OutputStream output;
    private ConstPool pool;
    
    public AnnotationsWriter(final OutputStream os, final ConstPool cp) {
        this.output = os;
        this.pool = cp;
    }
    
    public ConstPool getConstPool() {
        return this.pool;
    }
    
    public void close() throws IOException {
        this.output.close();
    }
    
    public void numParameters(final int num) throws IOException {
        this.output.write(num);
    }
    
    public void numAnnotations(final int num) throws IOException {
        this.write16bit(num);
    }
    
    public void annotation(final String type, final int numMemberValuePairs) throws IOException {
        this.annotation(this.pool.addUtf8Info(type), numMemberValuePairs);
    }
    
    public void annotation(final int typeIndex, final int numMemberValuePairs) throws IOException {
        this.write16bit(typeIndex);
        this.write16bit(numMemberValuePairs);
    }
    
    public void memberValuePair(final String memberName) throws IOException {
        this.memberValuePair(this.pool.addUtf8Info(memberName));
    }
    
    public void memberValuePair(final int memberNameIndex) throws IOException {
        this.write16bit(memberNameIndex);
    }
    
    public void constValueIndex(final boolean value) throws IOException {
        this.constValueIndex(90, this.pool.addIntegerInfo((int)(value ? 1 : 0)));
    }
    
    public void constValueIndex(final byte value) throws IOException {
        this.constValueIndex(66, this.pool.addIntegerInfo(value));
    }
    
    public void constValueIndex(final char value) throws IOException {
        this.constValueIndex(67, this.pool.addIntegerInfo(value));
    }
    
    public void constValueIndex(final short value) throws IOException {
        this.constValueIndex(83, this.pool.addIntegerInfo(value));
    }
    
    public void constValueIndex(final int value) throws IOException {
        this.constValueIndex(73, this.pool.addIntegerInfo(value));
    }
    
    public void constValueIndex(final long value) throws IOException {
        this.constValueIndex(74, this.pool.addLongInfo(value));
    }
    
    public void constValueIndex(final float value) throws IOException {
        this.constValueIndex(70, this.pool.addFloatInfo(value));
    }
    
    public void constValueIndex(final double value) throws IOException {
        this.constValueIndex(68, this.pool.addDoubleInfo(value));
    }
    
    public void constValueIndex(final String value) throws IOException {
        this.constValueIndex(115, this.pool.addUtf8Info(value));
    }
    
    public void constValueIndex(final int tag, final int index) throws IOException {
        this.output.write(tag);
        this.write16bit(index);
    }
    
    public void enumConstValue(final String typeName, final String constName) throws IOException {
        this.enumConstValue(this.pool.addUtf8Info(typeName), this.pool.addUtf8Info(constName));
    }
    
    public void enumConstValue(final int typeNameIndex, final int constNameIndex) throws IOException {
        this.output.write(101);
        this.write16bit(typeNameIndex);
        this.write16bit(constNameIndex);
    }
    
    public void classInfoIndex(final String name) throws IOException {
        this.classInfoIndex(this.pool.addUtf8Info(name));
    }
    
    public void classInfoIndex(final int index) throws IOException {
        this.output.write(99);
        this.write16bit(index);
    }
    
    public void annotationValue() throws IOException {
        this.output.write(64);
    }
    
    public void arrayValue(final int numValues) throws IOException {
        this.output.write(91);
        this.write16bit(numValues);
    }
    
    protected void write16bit(final int value) throws IOException {
        final byte[] buf = new byte[2];
        ByteArray.write16bit(value, buf, 0);
        this.output.write(buf);
    }
}
