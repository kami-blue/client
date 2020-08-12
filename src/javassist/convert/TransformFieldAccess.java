// 
// Decompiled by Procyon v0.5.36
// 

package javassist.convert;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.CodeAttribute;
import javassist.Modifier;
import javassist.CtField;
import javassist.bytecode.ConstPool;
import javassist.CtClass;

public final class TransformFieldAccess extends Transformer
{
    private String newClassname;
    private String newFieldname;
    private String fieldname;
    private CtClass fieldClass;
    private boolean isPrivate;
    private int newIndex;
    private ConstPool constPool;
    
    public TransformFieldAccess(final Transformer next, final CtField field, final String newClassname, final String newFieldname) {
        super(next);
        this.fieldClass = field.getDeclaringClass();
        this.fieldname = field.getName();
        this.isPrivate = Modifier.isPrivate(field.getModifiers());
        this.newClassname = newClassname;
        this.newFieldname = newFieldname;
        this.constPool = null;
    }
    
    @Override
    public void initialize(final ConstPool cp, final CodeAttribute attr) {
        if (this.constPool != cp) {
            this.newIndex = 0;
        }
    }
    
    @Override
    public int transform(final CtClass clazz, final int pos, final CodeIterator iterator, final ConstPool cp) {
        final int c = iterator.byteAt(pos);
        if (c == 180 || c == 178 || c == 181 || c == 179) {
            final int index = iterator.u16bitAt(pos + 1);
            final String typedesc = TransformReadField.isField(clazz.getClassPool(), cp, this.fieldClass, this.fieldname, this.isPrivate, index);
            if (typedesc != null) {
                if (this.newIndex == 0) {
                    final int nt = cp.addNameAndTypeInfo(this.newFieldname, typedesc);
                    this.newIndex = cp.addFieldrefInfo(cp.addClassInfo(this.newClassname), nt);
                    this.constPool = cp;
                }
                iterator.write16bit(this.newIndex, pos + 1);
            }
        }
        return pos;
    }
}
