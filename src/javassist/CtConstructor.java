// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import javassist.compiler.CompileError;
import javassist.compiler.Javac;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.MethodInfo;

public final class CtConstructor extends CtBehavior
{
    protected CtConstructor(final MethodInfo minfo, final CtClass declaring) {
        super(declaring, minfo);
    }
    
    public CtConstructor(final CtClass[] parameters, final CtClass declaring) {
        this((MethodInfo)null, declaring);
        final ConstPool cp = declaring.getClassFile2().getConstPool();
        final String desc = Descriptor.ofConstructor(parameters);
        this.methodInfo = new MethodInfo(cp, "<init>", desc);
        this.setModifiers(1);
    }
    
    public CtConstructor(final CtConstructor src, final CtClass declaring, final ClassMap map) throws CannotCompileException {
        this((MethodInfo)null, declaring);
        this.copy(src, true, map);
    }
    
    public boolean isConstructor() {
        return this.methodInfo.isConstructor();
    }
    
    public boolean isClassInitializer() {
        return this.methodInfo.isStaticInitializer();
    }
    
    @Override
    public String getLongName() {
        return this.getDeclaringClass().getName() + (this.isConstructor() ? Descriptor.toString(this.getSignature()) : ".<clinit>()");
    }
    
    @Override
    public String getName() {
        if (this.methodInfo.isStaticInitializer()) {
            return "<clinit>";
        }
        return this.declaringClass.getSimpleName();
    }
    
    @Override
    public boolean isEmpty() {
        final CodeAttribute ca = this.getMethodInfo2().getCodeAttribute();
        if (ca == null) {
            return false;
        }
        final ConstPool cp = ca.getConstPool();
        final CodeIterator it = ca.iterator();
        try {
            final int op0 = it.byteAt(it.next());
            final int pos;
            final int desc;
            return op0 == 177 || (op0 == 42 && it.byteAt(pos = it.next()) == 183 && (desc = cp.isConstructor(this.getSuperclassName(), it.u16bitAt(pos + 1))) != 0 && "()V".equals(cp.getUtf8Info(desc)) && it.byteAt(it.next()) == 177 && !it.hasNext());
        }
        catch (BadBytecode badBytecode) {
            return false;
        }
    }
    
    private String getSuperclassName() {
        final ClassFile cf = this.declaringClass.getClassFile2();
        return cf.getSuperclass();
    }
    
    public boolean callsSuper() throws CannotCompileException {
        final CodeAttribute codeAttr = this.methodInfo.getCodeAttribute();
        if (codeAttr != null) {
            final CodeIterator it = codeAttr.iterator();
            try {
                final int index = it.skipSuperConstructor();
                return index >= 0;
            }
            catch (BadBytecode e) {
                throw new CannotCompileException(e);
            }
        }
        return false;
    }
    
    @Override
    public void setBody(String src) throws CannotCompileException {
        if (src == null) {
            if (this.isClassInitializer()) {
                src = ";";
            }
            else {
                src = "super();";
            }
        }
        super.setBody(src);
    }
    
    public void setBody(final CtConstructor src, final ClassMap map) throws CannotCompileException {
        CtBehavior.setBody0(src.declaringClass, src.methodInfo, this.declaringClass, this.methodInfo, map);
    }
    
    public void insertBeforeBody(final String src) throws CannotCompileException {
        final CtClass cc = this.declaringClass;
        cc.checkModify();
        if (this.isClassInitializer()) {
            throw new CannotCompileException("class initializer");
        }
        final CodeAttribute ca = this.methodInfo.getCodeAttribute();
        final CodeIterator iterator = ca.iterator();
        final Bytecode b = new Bytecode(this.methodInfo.getConstPool(), ca.getMaxStack(), ca.getMaxLocals());
        b.setStackDepth(ca.getMaxStack());
        final Javac jv = new Javac(b, cc);
        try {
            jv.recordParams(this.getParameterTypes(), false);
            jv.compileStmnt(src);
            ca.setMaxStack(b.getMaxStack());
            ca.setMaxLocals(b.getMaxLocals());
            iterator.skipConstructor();
            final int pos = iterator.insertEx(b.get());
            iterator.insert(b.getExceptionTable(), pos);
            this.methodInfo.rebuildStackMapIf6(cc.getClassPool(), cc.getClassFile2());
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        catch (CompileError e2) {
            throw new CannotCompileException(e2);
        }
        catch (BadBytecode e3) {
            throw new CannotCompileException(e3);
        }
    }
    
    @Override
    int getStartPosOfBody(final CodeAttribute ca) throws CannotCompileException {
        final CodeIterator ci = ca.iterator();
        try {
            ci.skipConstructor();
            return ci.next();
        }
        catch (BadBytecode e) {
            throw new CannotCompileException(e);
        }
    }
    
    public CtMethod toMethod(final String name, final CtClass declaring) throws CannotCompileException {
        return this.toMethod(name, declaring, null);
    }
    
    public CtMethod toMethod(final String name, final CtClass declaring, final ClassMap map) throws CannotCompileException {
        final CtMethod method = new CtMethod(null, declaring);
        method.copy(this, false, map);
        if (this.isConstructor()) {
            final MethodInfo minfo = method.getMethodInfo2();
            final CodeAttribute ca = minfo.getCodeAttribute();
            if (ca != null) {
                removeConsCall(ca);
                try {
                    this.methodInfo.rebuildStackMapIf6(declaring.getClassPool(), declaring.getClassFile2());
                }
                catch (BadBytecode e) {
                    throw new CannotCompileException(e);
                }
            }
        }
        method.setName(name);
        return method;
    }
    
    private static void removeConsCall(final CodeAttribute ca) throws CannotCompileException {
        final CodeIterator iterator = ca.iterator();
        try {
            int pos = iterator.skipConstructor();
            if (pos >= 0) {
                final int mref = iterator.u16bitAt(pos + 1);
                final String desc = ca.getConstPool().getMethodrefType(mref);
                final int num = Descriptor.numOfParameters(desc) + 1;
                if (num > 3) {
                    pos = iterator.insertGapAt(pos, num - 3, false).position;
                }
                iterator.writeByte(87, pos++);
                iterator.writeByte(0, pos);
                iterator.writeByte(0, pos + 1);
                final Descriptor.Iterator it = new Descriptor.Iterator(desc);
                while (true) {
                    it.next();
                    if (!it.isParameter()) {
                        break;
                    }
                    iterator.writeByte(it.is2byte() ? 88 : 87, pos++);
                }
            }
        }
        catch (BadBytecode e) {
            throw new CannotCompileException(e);
        }
    }
}
