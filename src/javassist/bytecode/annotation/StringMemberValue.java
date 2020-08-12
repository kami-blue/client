// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.annotation;

import java.io.IOException;
import java.lang.reflect.Method;
import javassist.ClassPool;
import javassist.bytecode.ConstPool;

public class StringMemberValue extends MemberValue
{
    int valueIndex;
    
    public StringMemberValue(final int index, final ConstPool cp) {
        super('s', cp);
        this.valueIndex = index;
    }
    
    public StringMemberValue(final String str, final ConstPool cp) {
        super('s', cp);
        this.setValue(str);
    }
    
    public StringMemberValue(final ConstPool cp) {
        super('s', cp);
        this.setValue("");
    }
    
    @Override
    Object getValue(final ClassLoader cl, final ClassPool cp, final Method m) {
        return this.getValue();
    }
    
    @Override
    Class getType(final ClassLoader cl) {
        return String.class;
    }
    
    public String getValue() {
        return this.cp.getUtf8Info(this.valueIndex);
    }
    
    public void setValue(final String newValue) {
        this.valueIndex = this.cp.addUtf8Info(newValue);
    }
    
    @Override
    public String toString() {
        return "\"" + this.getValue() + "\"";
    }
    
    @Override
    public void write(final AnnotationsWriter writer) throws IOException {
        writer.constValueIndex(this.getValue());
    }
    
    @Override
    public void accept(final MemberValueVisitor visitor) {
        visitor.visitStringMemberValue(this);
    }
}
