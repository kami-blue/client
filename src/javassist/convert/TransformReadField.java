// 
// Decompiled by Procyon v0.5.36
// 

package javassist.convert;

import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.NotFoundException;
import javassist.bytecode.ConstPool;
import javassist.ClassPool;
import javassist.Modifier;
import javassist.CtField;
import javassist.CtClass;

public class TransformReadField extends Transformer
{
    protected String fieldname;
    protected CtClass fieldClass;
    protected boolean isPrivate;
    protected String methodClassname;
    protected String methodName;
    
    public TransformReadField(final Transformer next, final CtField field, final String methodClassname, final String methodName) {
        super(next);
        this.fieldClass = field.getDeclaringClass();
        this.fieldname = field.getName();
        this.methodClassname = methodClassname;
        this.methodName = methodName;
        this.isPrivate = Modifier.isPrivate(field.getModifiers());
    }
    
    static String isField(final ClassPool pool, final ConstPool cp, final CtClass fclass, final String fname, final boolean is_private, final int index) {
        if (!cp.getFieldrefName(index).equals(fname)) {
            return null;
        }
        try {
            final CtClass c = pool.get(cp.getFieldrefClassName(index));
            if (c == fclass || (!is_private && isFieldInSuper(c, fclass, fname))) {
                return cp.getFieldrefType(index);
            }
        }
        catch (NotFoundException ex) {}
        return null;
    }
    
    static boolean isFieldInSuper(final CtClass clazz, final CtClass fclass, final String fname) {
        if (!clazz.subclassOf(fclass)) {
            return false;
        }
        try {
            final CtField f = clazz.getField(fname);
            return f.getDeclaringClass() == fclass;
        }
        catch (NotFoundException ex) {
            return false;
        }
    }
    
    @Override
    public int transform(final CtClass tclazz, int pos, final CodeIterator iterator, final ConstPool cp) throws BadBytecode {
        final int c = iterator.byteAt(pos);
        if (c == 180 || c == 178) {
            final int index = iterator.u16bitAt(pos + 1);
            final String typedesc = isField(tclazz.getClassPool(), cp, this.fieldClass, this.fieldname, this.isPrivate, index);
            if (typedesc != null) {
                if (c == 178) {
                    iterator.move(pos);
                    pos = iterator.insertGap(1);
                    iterator.writeByte(1, pos);
                    pos = iterator.next();
                }
                final String type = "(Ljava/lang/Object;)" + typedesc;
                final int mi = cp.addClassInfo(this.methodClassname);
                final int methodref = cp.addMethodrefInfo(mi, this.methodName, type);
                iterator.writeByte(184, pos);
                iterator.write16bit(methodref, pos + 1);
                return pos;
            }
        }
        return pos;
    }
}
