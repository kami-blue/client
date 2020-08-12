// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.annotation;

import java.io.IOException;
import java.lang.reflect.Method;
import javassist.ClassPool;
import javassist.bytecode.ConstPool;

public class ShortMemberValue extends MemberValue
{
    int valueIndex;
    
    public ShortMemberValue(final int index, final ConstPool cp) {
        super('S', cp);
        this.valueIndex = index;
    }
    
    public ShortMemberValue(final short s, final ConstPool cp) {
        super('S', cp);
        this.setValue(s);
    }
    
    public ShortMemberValue(final ConstPool cp) {
        super('S', cp);
        this.setValue((short)0);
    }
    
    @Override
    Object getValue(final ClassLoader cl, final ClassPool cp, final Method m) {
        return new Short(this.getValue());
    }
    
    @Override
    Class getType(final ClassLoader cl) {
        return Short.TYPE;
    }
    
    public short getValue() {
        return (short)this.cp.getIntegerInfo(this.valueIndex);
    }
    
    public void setValue(final short newValue) {
        this.valueIndex = this.cp.addIntegerInfo(newValue);
    }
    
    @Override
    public String toString() {
        return Short.toString(this.getValue());
    }
    
    @Override
    public void write(final AnnotationsWriter writer) throws IOException {
        writer.constValueIndex(this.getValue());
    }
    
    @Override
    public void accept(final MemberValueVisitor visitor) {
        visitor.visitShortMemberValue(this);
    }
}
