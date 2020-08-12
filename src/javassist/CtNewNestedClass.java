// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import javassist.bytecode.ClassFile;
import javassist.bytecode.InnerClassesAttribute;

class CtNewNestedClass extends CtNewClass
{
    CtNewNestedClass(final String realName, final ClassPool cp, final boolean isInterface, final CtClass superclass) {
        super(realName, cp, isInterface, superclass);
    }
    
    @Override
    public void setModifiers(int mod) {
        mod &= 0xFFFFFFF7;
        super.setModifiers(mod);
        updateInnerEntry(mod, this.getName(), this, true);
    }
    
    private static void updateInnerEntry(final int mod, final String name, final CtClass clazz, final boolean outer) {
        final ClassFile cf = clazz.getClassFile2();
        final InnerClassesAttribute ica = (InnerClassesAttribute)cf.getAttribute("InnerClasses");
        if (ica == null) {
            return;
        }
        for (int n = ica.tableLength(), i = 0; i < n; ++i) {
            if (name.equals(ica.innerClass(i))) {
                final int acc = ica.accessFlags(i) & 0x8;
                ica.setAccessFlags(i, mod | acc);
                final String outName = ica.outerClass(i);
                if (outName == null || !outer) {
                    break;
                }
                try {
                    final CtClass parent = clazz.getClassPool().get(outName);
                    updateInnerEntry(mod, name, parent, false);
                    break;
                }
                catch (NotFoundException e) {
                    throw new RuntimeException("cannot find the declaring class: " + outName);
                }
            }
        }
    }
}
