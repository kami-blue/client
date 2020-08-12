// 
// Decompiled by Procyon v0.5.36
// 

package javassist.convert;

import javassist.NotFoundException;
import javassist.ClassPool;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.CtClass;
import javassist.bytecode.CodeAttribute;
import javassist.Modifier;
import javassist.CtMethod;
import javassist.bytecode.ConstPool;

public class TransformCall extends Transformer
{
    protected String classname;
    protected String methodname;
    protected String methodDescriptor;
    protected String newClassname;
    protected String newMethodname;
    protected boolean newMethodIsPrivate;
    protected int newIndex;
    protected ConstPool constPool;
    
    public TransformCall(final Transformer next, final CtMethod origMethod, final CtMethod substMethod) {
        this(next, origMethod.getName(), substMethod);
        this.classname = origMethod.getDeclaringClass().getName();
    }
    
    public TransformCall(final Transformer next, final String oldMethodName, final CtMethod substMethod) {
        super(next);
        this.methodname = oldMethodName;
        this.methodDescriptor = substMethod.getMethodInfo2().getDescriptor();
        final String name = substMethod.getDeclaringClass().getName();
        this.newClassname = name;
        this.classname = name;
        this.newMethodname = substMethod.getName();
        this.constPool = null;
        this.newMethodIsPrivate = Modifier.isPrivate(substMethod.getModifiers());
    }
    
    @Override
    public void initialize(final ConstPool cp, final CodeAttribute attr) {
        if (this.constPool != cp) {
            this.newIndex = 0;
        }
    }
    
    @Override
    public int transform(final CtClass clazz, int pos, final CodeIterator iterator, final ConstPool cp) throws BadBytecode {
        final int c = iterator.byteAt(pos);
        if (c == 185 || c == 183 || c == 184 || c == 182) {
            final int index = iterator.u16bitAt(pos + 1);
            final String cname = cp.eqMember(this.methodname, this.methodDescriptor, index);
            if (cname != null && this.matchClass(cname, clazz.getClassPool())) {
                final int ntinfo = cp.getMemberNameAndType(index);
                pos = this.match(c, pos, iterator, cp.getNameAndTypeDescriptor(ntinfo), cp);
            }
        }
        return pos;
    }
    
    private boolean matchClass(final String name, final ClassPool pool) {
        if (this.classname.equals(name)) {
            return true;
        }
        try {
            final CtClass clazz = pool.get(name);
            final CtClass declClazz = pool.get(this.classname);
            if (clazz.subtypeOf(declClazz)) {
                try {
                    final CtMethod m = clazz.getMethod(this.methodname, this.methodDescriptor);
                    return m.getDeclaringClass().getName().equals(this.classname);
                }
                catch (NotFoundException e) {
                    return true;
                }
            }
        }
        catch (NotFoundException e2) {
            return false;
        }
        return false;
    }
    
    protected int match(final int c, final int pos, final CodeIterator iterator, final int typedesc, final ConstPool cp) throws BadBytecode {
        if (this.newIndex == 0) {
            final int nt = cp.addNameAndTypeInfo(cp.addUtf8Info(this.newMethodname), typedesc);
            final int ci = cp.addClassInfo(this.newClassname);
            if (c == 185) {
                this.newIndex = cp.addInterfaceMethodrefInfo(ci, nt);
            }
            else {
                if (this.newMethodIsPrivate && c == 182) {
                    iterator.writeByte(183, pos);
                }
                this.newIndex = cp.addMethodrefInfo(ci, nt);
            }
            this.constPool = cp;
        }
        iterator.write16bit(this.newIndex, pos + 1);
        return pos;
    }
}
