// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.annotation;

import java.io.IOException;
import java.lang.reflect.Method;
import javassist.ClassPool;
import javassist.bytecode.ConstPool;

public class DoubleMemberValue extends MemberValue
{
    int valueIndex;
    
    public DoubleMemberValue(final int index, final ConstPool cp) {
        super('D', cp);
        this.valueIndex = index;
    }
    
    public DoubleMemberValue(final double d, final ConstPool cp) {
        super('D', cp);
        this.setValue(d);
    }
    
    public DoubleMemberValue(final ConstPool cp) {
        super('D', cp);
        this.setValue(0.0);
    }
    
    @Override
    Object getValue(final ClassLoader cl, final ClassPool cp, final Method m) {
        return new Double(this.getValue());
    }
    
    @Override
    Class getType(final ClassLoader cl) {
        return Double.TYPE;
    }
    
    public double getValue() {
        return this.cp.getDoubleInfo(this.valueIndex);
    }
    
    public void setValue(final double newValue) {
        this.valueIndex = this.cp.addDoubleInfo(newValue);
    }
    
    @Override
    public String toString() {
        return Double.toString(this.getValue());
    }
    
    @Override
    public void write(final AnnotationsWriter writer) throws IOException {
        writer.constValueIndex(this.getValue());
    }
    
    @Override
    public void accept(final MemberValueVisitor visitor) {
        visitor.visitDoubleMemberValue(this);
    }
}
