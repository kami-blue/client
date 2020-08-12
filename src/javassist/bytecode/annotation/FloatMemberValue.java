// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.annotation;

import java.io.IOException;
import java.lang.reflect.Method;
import javassist.ClassPool;
import javassist.bytecode.ConstPool;

public class FloatMemberValue extends MemberValue
{
    int valueIndex;
    
    public FloatMemberValue(final int index, final ConstPool cp) {
        super('F', cp);
        this.valueIndex = index;
    }
    
    public FloatMemberValue(final float f, final ConstPool cp) {
        super('F', cp);
        this.setValue(f);
    }
    
    public FloatMemberValue(final ConstPool cp) {
        super('F', cp);
        this.setValue(0.0f);
    }
    
    @Override
    Object getValue(final ClassLoader cl, final ClassPool cp, final Method m) {
        return new Float(this.getValue());
    }
    
    @Override
    Class getType(final ClassLoader cl) {
        return Float.TYPE;
    }
    
    public float getValue() {
        return this.cp.getFloatInfo(this.valueIndex);
    }
    
    public void setValue(final float newValue) {
        this.valueIndex = this.cp.addFloatInfo(newValue);
    }
    
    @Override
    public String toString() {
        return Float.toString(this.getValue());
    }
    
    @Override
    public void write(final AnnotationsWriter writer) throws IOException {
        writer.constValueIndex(this.getValue());
    }
    
    @Override
    public void accept(final MemberValueVisitor visitor) {
        visitor.visitFloatMemberValue(this);
    }
}
