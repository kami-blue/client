// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.annotation;

import java.io.IOException;
import java.lang.reflect.Method;
import javassist.ClassPool;
import javassist.bytecode.ConstPool;

public class IntegerMemberValue extends MemberValue
{
    int valueIndex;
    
    public IntegerMemberValue(final int index, final ConstPool cp) {
        super('I', cp);
        this.valueIndex = index;
    }
    
    public IntegerMemberValue(final ConstPool cp, final int value) {
        super('I', cp);
        this.setValue(value);
    }
    
    public IntegerMemberValue(final ConstPool cp) {
        super('I', cp);
        this.setValue(0);
    }
    
    @Override
    Object getValue(final ClassLoader cl, final ClassPool cp, final Method m) {
        return new Integer(this.getValue());
    }
    
    @Override
    Class getType(final ClassLoader cl) {
        return Integer.TYPE;
    }
    
    public int getValue() {
        return this.cp.getIntegerInfo(this.valueIndex);
    }
    
    public void setValue(final int newValue) {
        this.valueIndex = this.cp.addIntegerInfo(newValue);
    }
    
    @Override
    public String toString() {
        return Integer.toString(this.getValue());
    }
    
    @Override
    public void write(final AnnotationsWriter writer) throws IOException {
        writer.constValueIndex(this.getValue());
    }
    
    @Override
    public void accept(final MemberValueVisitor visitor) {
        visitor.visitIntegerMemberValue(this);
    }
}
