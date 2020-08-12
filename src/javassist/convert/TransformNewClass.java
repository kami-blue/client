// 
// Decompiled by Procyon v0.5.36
// 

package javassist.convert;

import javassist.CannotCompileException;
import javassist.bytecode.CodeIterator;
import javassist.CtClass;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;

public final class TransformNewClass extends Transformer
{
    private int nested;
    private String classname;
    private String newClassName;
    private int newClassIndex;
    private int newMethodNTIndex;
    private int newMethodIndex;
    
    public TransformNewClass(final Transformer next, final String classname, final String newClassName) {
        super(next);
        this.classname = classname;
        this.newClassName = newClassName;
    }
    
    @Override
    public void initialize(final ConstPool cp, final CodeAttribute attr) {
        this.nested = 0;
        final int newClassIndex = 0;
        this.newMethodIndex = newClassIndex;
        this.newMethodNTIndex = newClassIndex;
        this.newClassIndex = newClassIndex;
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
                if (this.newClassIndex == 0) {
                    this.newClassIndex = cp.addClassInfo(this.newClassName);
                }
                iterator.write16bit(this.newClassIndex, pos + 1);
                ++this.nested;
            }
        }
        else if (c == 183) {
            final int index = iterator.u16bitAt(pos + 1);
            final int typedesc = cp.isConstructor(this.classname, index);
            if (typedesc != 0 && this.nested > 0) {
                final int nt = cp.getMethodrefNameAndType(index);
                if (this.newMethodNTIndex != nt) {
                    this.newMethodNTIndex = nt;
                    this.newMethodIndex = cp.addMethodrefInfo(this.newClassIndex, nt);
                }
                iterator.write16bit(this.newMethodIndex, pos + 1);
                --this.nested;
            }
        }
        return pos;
    }
}
