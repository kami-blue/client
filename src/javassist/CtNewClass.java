// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import java.io.IOException;
import java.io.DataOutputStream;
import javassist.bytecode.ClassFile;

class CtNewClass extends CtClassType
{
    protected boolean hasConstructor;
    
    CtNewClass(final String name, final ClassPool cp, final boolean isInterface, final CtClass superclass) {
        super(name, cp);
        this.wasChanged = true;
        String superName;
        if (isInterface || superclass == null) {
            superName = null;
        }
        else {
            superName = superclass.getName();
        }
        this.classfile = new ClassFile(isInterface, name, superName);
        if (isInterface && superclass != null) {
            this.classfile.setInterfaces(new String[] { superclass.getName() });
        }
        this.setModifiers(Modifier.setPublic(this.getModifiers()));
        this.hasConstructor = isInterface;
    }
    
    @Override
    protected void extendToString(final StringBuffer buffer) {
        if (this.hasConstructor) {
            buffer.append("hasConstructor ");
        }
        super.extendToString(buffer);
    }
    
    @Override
    public void addConstructor(final CtConstructor c) throws CannotCompileException {
        this.hasConstructor = true;
        super.addConstructor(c);
    }
    
    @Override
    public void toBytecode(final DataOutputStream out) throws CannotCompileException, IOException {
        if (!this.hasConstructor) {
            try {
                this.inheritAllConstructors();
                this.hasConstructor = true;
            }
            catch (NotFoundException e) {
                throw new CannotCompileException(e);
            }
        }
        super.toBytecode(out);
    }
    
    public void inheritAllConstructors() throws CannotCompileException, NotFoundException {
        final CtClass superclazz = this.getSuperclass();
        final CtConstructor[] cs = superclazz.getDeclaredConstructors();
        int n = 0;
        for (int i = 0; i < cs.length; ++i) {
            final CtConstructor c = cs[i];
            final int mod = c.getModifiers();
            if (this.isInheritable(mod, superclazz)) {
                final CtConstructor cons = CtNewConstructor.make(c.getParameterTypes(), c.getExceptionTypes(), this);
                cons.setModifiers(mod & 0x7);
                this.addConstructor(cons);
                ++n;
            }
        }
        if (n < 1) {
            throw new CannotCompileException("no inheritable constructor in " + superclazz.getName());
        }
    }
    
    private boolean isInheritable(final int mod, final CtClass superclazz) {
        if (Modifier.isPrivate(mod)) {
            return false;
        }
        if (!Modifier.isPackage(mod)) {
            return true;
        }
        final String pname = this.getPackageName();
        final String pname2 = superclazz.getPackageName();
        if (pname == null) {
            return pname2 == null;
        }
        return pname.equals(pname2);
    }
}
