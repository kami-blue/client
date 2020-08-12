// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.annotation;

import java.io.IOException;
import java.lang.reflect.Method;
import javassist.ClassPool;
import javassist.bytecode.ConstPool;

public class ByteMemberValue extends MemberValue
{
    int valueIndex;
    
    public ByteMemberValue(final int index, final ConstPool cp) {
        super('B', cp);
        this.valueIndex = index;
    }
    
    public ByteMemberValue(final byte b, final ConstPool cp) {
        super('B', cp);
        this.setValue(b);
    }
    
    public ByteMemberValue(final ConstPool cp) {
        super('B', cp);
        this.setValue((byte)0);
    }
    
    @Override
    Object getValue(final ClassLoader cl, final ClassPool cp, final Method m) {
        return new Byte(this.getValue());
    }
    
    @Override
    Class getType(final ClassLoader cl) {
        return Byte.TYPE;
    }
    
    public byte getValue() {
        return (byte)this.cp.getIntegerInfo(this.valueIndex);
    }
    
    public void setValue(final byte newValue) {
        this.valueIndex = this.cp.addIntegerInfo(newValue);
    }
    
    @Override
    public String toString() {
        return Byte.toString(this.getValue());
    }
    
    @Override
    public void write(final AnnotationsWriter writer) throws IOException {
        writer.constValueIndex(this.getValue());
    }
    
    @Override
    public void accept(final MemberValueVisitor visitor) {
        visitor.visitByteMemberValue(this);
    }
}
