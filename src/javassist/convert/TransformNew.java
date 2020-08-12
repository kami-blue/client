// 
// Decompiled by Procyon v0.5.36
// 

package javassist.convert;

import javassist.bytecode.Descriptor;
import javassist.bytecode.StackMap;
import javassist.bytecode.StackMapTable;
import javassist.CannotCompileException;
import javassist.bytecode.CodeIterator;
import javassist.CtClass;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;

public final class TransformNew extends Transformer
{
    private int nested;
    private String classname;
    private String trapClass;
    private String trapMethod;
    
    public TransformNew(final Transformer next, final String classname, final String trapClass, final String trapMethod) {
        super(next);
        this.classname = classname;
        this.trapClass = trapClass;
        this.trapMethod = trapMethod;
    }
    
    @Override
    public void initialize(final ConstPool cp, final CodeAttribute attr) {
        this.nested = 0;
    }
    
    @Override
    public int transform(final CtClass clazz, final int pos, final CodeIterator iterator, final ConstPool cp) throws CannotCompileException {
        final int c = iterator.byteAt(pos);
        if (c == 187) {
            final int index = iterator.u16bitAt(pos + 1);
            if (cp.getClassInfo(index).equals(this.classname)) {
                if (iterator.byteAt(pos + 3) != 89) {
                    throw new CannotCompileException("NEW followed by no DUP was found");
                }
                iterator.writeByte(0, pos);
                iterator.writeByte(0, pos + 1);
                iterator.writeByte(0, pos + 2);
                iterator.writeByte(0, pos + 3);
                ++this.nested;
                final StackMapTable smt = (StackMapTable)iterator.get().getAttribute("StackMapTable");
                if (smt != null) {
                    smt.removeNew(pos);
                }
                final StackMap sm = (StackMap)iterator.get().getAttribute("StackMap");
                if (sm != null) {
                    sm.removeNew(pos);
                }
            }
        }
        else if (c == 183) {
            final int index = iterator.u16bitAt(pos + 1);
            final int typedesc = cp.isConstructor(this.classname, index);
            if (typedesc != 0 && this.nested > 0) {
                final int methodref = this.computeMethodref(typedesc, cp);
                iterator.writeByte(184, pos);
                iterator.write16bit(methodref, pos + 1);
                --this.nested;
            }
        }
        return pos;
    }
    
    private int computeMethodref(int typedesc, final ConstPool cp) {
        final int classIndex = cp.addClassInfo(this.trapClass);
        final int mnameIndex = cp.addUtf8Info(this.trapMethod);
        typedesc = cp.addUtf8Info(Descriptor.changeReturnType(this.classname, cp.getUtf8Info(typedesc)));
        return cp.addMethodrefInfo(classIndex, cp.addNameAndTypeInfo(mnameIndex, typedesc));
    }
}
