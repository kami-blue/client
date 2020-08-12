// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.annotation;

import java.io.IOException;
import javassist.bytecode.Descriptor;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.SignatureAttribute;
import java.lang.reflect.Method;
import javassist.ClassPool;
import javassist.bytecode.ConstPool;

public class ClassMemberValue extends MemberValue
{
    int valueIndex;
    
    public ClassMemberValue(final int index, final ConstPool cp) {
        super('c', cp);
        this.valueIndex = index;
    }
    
    public ClassMemberValue(final String className, final ConstPool cp) {
        super('c', cp);
        this.setValue(className);
    }
    
    public ClassMemberValue(final ConstPool cp) {
        super('c', cp);
        this.setValue("java.lang.Class");
    }
    
    @Override
    Object getValue(final ClassLoader cl, final ClassPool cp, final Method m) throws ClassNotFoundException {
        final String classname = this.getValue();
        if (classname.equals("void")) {
            return Void.TYPE;
        }
        if (classname.equals("int")) {
            return Integer.TYPE;
        }
        if (classname.equals("byte")) {
            return Byte.TYPE;
        }
        if (classname.equals("long")) {
            return Long.TYPE;
        }
        if (classname.equals("double")) {
            return Double.TYPE;
        }
        if (classname.equals("float")) {
            return Float.TYPE;
        }
        if (classname.equals("char")) {
            return Character.TYPE;
        }
        if (classname.equals("short")) {
            return Short.TYPE;
        }
        if (classname.equals("boolean")) {
            return Boolean.TYPE;
        }
        return MemberValue.loadClass(cl, classname);
    }
    
    @Override
    Class getType(final ClassLoader cl) throws ClassNotFoundException {
        return MemberValue.loadClass(cl, "java.lang.Class");
    }
    
    public String getValue() {
        final String v = this.cp.getUtf8Info(this.valueIndex);
        try {
            return SignatureAttribute.toTypeSignature(v).jvmTypeName();
        }
        catch (BadBytecode e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setValue(final String newClassName) {
        final String setTo = Descriptor.of(newClassName);
        this.valueIndex = this.cp.addUtf8Info(setTo);
    }
    
    @Override
    public String toString() {
        return this.getValue().replace('$', '.') + ".class";
    }
    
    @Override
    public void write(final AnnotationsWriter writer) throws IOException {
        writer.classInfoIndex(this.cp.getUtf8Info(this.valueIndex));
    }
    
    @Override
    public void accept(final MemberValueVisitor visitor) {
        visitor.visitClassMemberValue(this);
    }
}
