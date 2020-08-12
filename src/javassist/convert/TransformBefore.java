// 
// Decompiled by Procyon v0.5.36
// 

package javassist.convert;

import javassist.bytecode.Bytecode;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Descriptor;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.NotFoundException;
import javassist.CtMethod;
import javassist.CtClass;

public class TransformBefore extends TransformCall
{
    protected CtClass[] parameterTypes;
    protected int locals;
    protected int maxLocals;
    protected byte[] saveCode;
    protected byte[] loadCode;
    
    public TransformBefore(final Transformer next, final CtMethod origMethod, final CtMethod beforeMethod) throws NotFoundException {
        super(next, origMethod, beforeMethod);
        this.methodDescriptor = origMethod.getMethodInfo2().getDescriptor();
        this.parameterTypes = origMethod.getParameterTypes();
        this.locals = 0;
        this.maxLocals = 0;
        final byte[] array = null;
        this.loadCode = array;
        this.saveCode = array;
    }
    
    @Override
    public void initialize(final ConstPool cp, final CodeAttribute attr) {
        super.initialize(cp, attr);
        this.locals = 0;
        this.maxLocals = attr.getMaxLocals();
        final byte[] array = null;
        this.loadCode = array;
        this.saveCode = array;
    }
    
    @Override
    protected int match(final int c, final int pos, final CodeIterator iterator, final int typedesc, final ConstPool cp) throws BadBytecode {
        if (this.newIndex == 0) {
            String desc = Descriptor.ofParameters(this.parameterTypes) + 'V';
            desc = Descriptor.insertParameter(this.classname, desc);
            final int nt = cp.addNameAndTypeInfo(this.newMethodname, desc);
            final int ci = cp.addClassInfo(this.newClassname);
            this.newIndex = cp.addMethodrefInfo(ci, nt);
            this.constPool = cp;
        }
        if (this.saveCode == null) {
            this.makeCode(this.parameterTypes, cp);
        }
        return this.match2(pos, iterator);
    }
    
    protected int match2(final int pos, final CodeIterator iterator) throws BadBytecode {
        iterator.move(pos);
        iterator.insert(this.saveCode);
        iterator.insert(this.loadCode);
        final int p = iterator.insertGap(3);
        iterator.writeByte(184, p);
        iterator.write16bit(this.newIndex, p + 1);
        iterator.insert(this.loadCode);
        return iterator.next();
    }
    
    @Override
    public int extraLocals() {
        return this.locals;
    }
    
    protected void makeCode(final CtClass[] paramTypes, final ConstPool cp) {
        final Bytecode save = new Bytecode(cp, 0, 0);
        final Bytecode load = new Bytecode(cp, 0, 0);
        final int var = this.maxLocals;
        final int len = (paramTypes == null) ? 0 : paramTypes.length;
        load.addAload(var);
        this.makeCode2(save, load, 0, len, paramTypes, var + 1);
        save.addAstore(var);
        this.saveCode = save.get();
        this.loadCode = load.get();
    }
    
    private void makeCode2(final Bytecode save, final Bytecode load, final int i, final int n, final CtClass[] paramTypes, final int var) {
        if (i < n) {
            final int size = load.addLoad(var, paramTypes[i]);
            this.makeCode2(save, load, i + 1, n, paramTypes, var + size);
            save.addStore(var, paramTypes[i]);
        }
        else {
            this.locals = var - this.maxLocals;
        }
    }
}
