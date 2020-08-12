// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.CodeIterator;
import javassist.expr.ExprEditor;
import javassist.bytecode.StackMap;
import javassist.bytecode.StackMapTable;
import javassist.bytecode.LocalVariableTypeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.Bytecode;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;
import javassist.bytecode.ExceptionsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.Descriptor;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.ConstPool;
import javassist.bytecode.BadBytecode;
import java.util.Map;
import javassist.bytecode.MethodInfo;

public abstract class CtBehavior extends CtMember
{
    protected MethodInfo methodInfo;
    
    protected CtBehavior(final CtClass clazz, final MethodInfo minfo) {
        super(clazz);
        this.methodInfo = minfo;
    }
    
    void copy(final CtBehavior src, final boolean isCons, ClassMap map) throws CannotCompileException {
        final CtClass declaring = this.declaringClass;
        final MethodInfo srcInfo = src.methodInfo;
        final CtClass srcClass = src.getDeclaringClass();
        final ConstPool cp = declaring.getClassFile2().getConstPool();
        map = new ClassMap(map);
        map.put(srcClass.getName(), declaring.getName());
        try {
            boolean patch = false;
            final CtClass srcSuper = srcClass.getSuperclass();
            final CtClass destSuper = declaring.getSuperclass();
            String destSuperName = null;
            if (srcSuper != null && destSuper != null) {
                final String srcSuperName = srcSuper.getName();
                destSuperName = destSuper.getName();
                if (!srcSuperName.equals(destSuperName)) {
                    if (srcSuperName.equals("java.lang.Object")) {
                        patch = true;
                    }
                    else {
                        map.putIfNone(srcSuperName, destSuperName);
                    }
                }
            }
            this.methodInfo = new MethodInfo(cp, srcInfo.getName(), srcInfo, map);
            if (isCons && patch) {
                this.methodInfo.setSuperclass(destSuperName);
            }
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        catch (BadBytecode e2) {
            throw new CannotCompileException(e2);
        }
    }
    
    @Override
    protected void extendToString(final StringBuffer buffer) {
        buffer.append(' ');
        buffer.append(this.getName());
        buffer.append(' ');
        buffer.append(this.methodInfo.getDescriptor());
    }
    
    public abstract String getLongName();
    
    public MethodInfo getMethodInfo() {
        this.declaringClass.checkModify();
        return this.methodInfo;
    }
    
    public MethodInfo getMethodInfo2() {
        return this.methodInfo;
    }
    
    @Override
    public int getModifiers() {
        return AccessFlag.toModifier(this.methodInfo.getAccessFlags());
    }
    
    @Override
    public void setModifiers(final int mod) {
        this.declaringClass.checkModify();
        this.methodInfo.setAccessFlags(AccessFlag.of(mod));
    }
    
    @Override
    public boolean hasAnnotation(final String typeName) {
        final MethodInfo mi = this.getMethodInfo2();
        final AnnotationsAttribute ainfo = (AnnotationsAttribute)mi.getAttribute("RuntimeInvisibleAnnotations");
        final AnnotationsAttribute ainfo2 = (AnnotationsAttribute)mi.getAttribute("RuntimeVisibleAnnotations");
        return CtClassType.hasAnnotationType(typeName, this.getDeclaringClass().getClassPool(), ainfo, ainfo2);
    }
    
    @Override
    public Object getAnnotation(final Class clz) throws ClassNotFoundException {
        final MethodInfo mi = this.getMethodInfo2();
        final AnnotationsAttribute ainfo = (AnnotationsAttribute)mi.getAttribute("RuntimeInvisibleAnnotations");
        final AnnotationsAttribute ainfo2 = (AnnotationsAttribute)mi.getAttribute("RuntimeVisibleAnnotations");
        return CtClassType.getAnnotationType(clz, this.getDeclaringClass().getClassPool(), ainfo, ainfo2);
    }
    
    @Override
    public Object[] getAnnotations() throws ClassNotFoundException {
        return this.getAnnotations(false);
    }
    
    @Override
    public Object[] getAvailableAnnotations() {
        try {
            return this.getAnnotations(true);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    
    private Object[] getAnnotations(final boolean ignoreNotFound) throws ClassNotFoundException {
        final MethodInfo mi = this.getMethodInfo2();
        final AnnotationsAttribute ainfo = (AnnotationsAttribute)mi.getAttribute("RuntimeInvisibleAnnotations");
        final AnnotationsAttribute ainfo2 = (AnnotationsAttribute)mi.getAttribute("RuntimeVisibleAnnotations");
        return CtClassType.toAnnotationType(ignoreNotFound, this.getDeclaringClass().getClassPool(), ainfo, ainfo2);
    }
    
    public Object[][] getParameterAnnotations() throws ClassNotFoundException {
        return this.getParameterAnnotations(false);
    }
    
    public Object[][] getAvailableParameterAnnotations() {
        try {
            return this.getParameterAnnotations(true);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    
    Object[][] getParameterAnnotations(final boolean ignoreNotFound) throws ClassNotFoundException {
        final MethodInfo mi = this.getMethodInfo2();
        final ParameterAnnotationsAttribute ainfo = (ParameterAnnotationsAttribute)mi.getAttribute("RuntimeInvisibleParameterAnnotations");
        final ParameterAnnotationsAttribute ainfo2 = (ParameterAnnotationsAttribute)mi.getAttribute("RuntimeVisibleParameterAnnotations");
        return CtClassType.toAnnotationType(ignoreNotFound, this.getDeclaringClass().getClassPool(), ainfo, ainfo2, mi);
    }
    
    public CtClass[] getParameterTypes() throws NotFoundException {
        return Descriptor.getParameterTypes(this.methodInfo.getDescriptor(), this.declaringClass.getClassPool());
    }
    
    CtClass getReturnType0() throws NotFoundException {
        return Descriptor.getReturnType(this.methodInfo.getDescriptor(), this.declaringClass.getClassPool());
    }
    
    @Override
    public String getSignature() {
        return this.methodInfo.getDescriptor();
    }
    
    @Override
    public String getGenericSignature() {
        final SignatureAttribute sa = (SignatureAttribute)this.methodInfo.getAttribute("Signature");
        return (sa == null) ? null : sa.getSignature();
    }
    
    @Override
    public void setGenericSignature(final String sig) {
        this.declaringClass.checkModify();
        this.methodInfo.addAttribute(new SignatureAttribute(this.methodInfo.getConstPool(), sig));
    }
    
    public CtClass[] getExceptionTypes() throws NotFoundException {
        final ExceptionsAttribute ea = this.methodInfo.getExceptionsAttribute();
        String[] exceptions;
        if (ea == null) {
            exceptions = null;
        }
        else {
            exceptions = ea.getExceptions();
        }
        return this.declaringClass.getClassPool().get(exceptions);
    }
    
    public void setExceptionTypes(final CtClass[] types) throws NotFoundException {
        this.declaringClass.checkModify();
        if (types == null || types.length == 0) {
            this.methodInfo.removeExceptionsAttribute();
            return;
        }
        final String[] names = new String[types.length];
        for (int i = 0; i < types.length; ++i) {
            names[i] = types[i].getName();
        }
        ExceptionsAttribute ea = this.methodInfo.getExceptionsAttribute();
        if (ea == null) {
            ea = new ExceptionsAttribute(this.methodInfo.getConstPool());
            this.methodInfo.setExceptionsAttribute(ea);
        }
        ea.setExceptions(names);
    }
    
    public abstract boolean isEmpty();
    
    public void setBody(final String src) throws CannotCompileException {
        this.setBody(src, null, null);
    }
    
    public void setBody(final String src, final String delegateObj, final String delegateMethod) throws CannotCompileException {
        final CtClass cc = this.declaringClass;
        cc.checkModify();
        try {
            final Javac jv = new Javac(cc);
            if (delegateMethod != null) {
                jv.recordProceed(delegateObj, delegateMethod);
            }
            final Bytecode b = jv.compileBody(this, src);
            this.methodInfo.setCodeAttribute(b.toCodeAttribute());
            this.methodInfo.setAccessFlags(this.methodInfo.getAccessFlags() & 0xFFFFFBFF);
            this.methodInfo.rebuildStackMapIf6(cc.getClassPool(), cc.getClassFile2());
            this.declaringClass.rebuildClassFile();
        }
        catch (CompileError e) {
            throw new CannotCompileException(e);
        }
        catch (BadBytecode e2) {
            throw new CannotCompileException(e2);
        }
    }
    
    static void setBody0(final CtClass srcClass, final MethodInfo srcInfo, final CtClass destClass, final MethodInfo destInfo, ClassMap map) throws CannotCompileException {
        destClass.checkModify();
        map = new ClassMap(map);
        map.put(srcClass.getName(), destClass.getName());
        try {
            final CodeAttribute cattr = srcInfo.getCodeAttribute();
            if (cattr != null) {
                final ConstPool cp = destInfo.getConstPool();
                final CodeAttribute ca = (CodeAttribute)cattr.copy(cp, map);
                destInfo.setCodeAttribute(ca);
            }
        }
        catch (CodeAttribute.RuntimeCopyException e) {
            throw new CannotCompileException(e);
        }
        destInfo.setAccessFlags(destInfo.getAccessFlags() & 0xFFFFFBFF);
        destClass.rebuildClassFile();
    }
    
    @Override
    public byte[] getAttribute(final String name) {
        final AttributeInfo ai = this.methodInfo.getAttribute(name);
        if (ai == null) {
            return null;
        }
        return ai.get();
    }
    
    @Override
    public void setAttribute(final String name, final byte[] data) {
        this.declaringClass.checkModify();
        this.methodInfo.addAttribute(new AttributeInfo(this.methodInfo.getConstPool(), name, data));
    }
    
    public void useCflow(final String name) throws CannotCompileException {
        final CtClass cc = this.declaringClass;
        cc.checkModify();
        final ClassPool pool = cc.getClassPool();
        int i = 0;
        while (true) {
            final String fname = "_cflow$" + i++;
            try {
                cc.getDeclaredField(fname);
            }
            catch (NotFoundException e) {
                pool.recordCflow(name, this.declaringClass.getName(), fname);
                try {
                    final CtClass type = pool.get("javassist.runtime.Cflow");
                    final CtField field = new CtField(type, fname, cc);
                    field.setModifiers(9);
                    cc.addField(field, CtField.Initializer.byNew(type));
                    this.insertBefore(fname + ".enter();", false);
                    final String src = fname + ".exit();";
                    this.insertAfter(src, true);
                }
                catch (NotFoundException e) {
                    throw new CannotCompileException(e);
                }
            }
        }
    }
    
    public void addLocalVariable(final String name, final CtClass type) throws CannotCompileException {
        this.declaringClass.checkModify();
        final ConstPool cp = this.methodInfo.getConstPool();
        final CodeAttribute ca = this.methodInfo.getCodeAttribute();
        if (ca == null) {
            throw new CannotCompileException("no method body");
        }
        LocalVariableAttribute va = (LocalVariableAttribute)ca.getAttribute("LocalVariableTable");
        if (va == null) {
            va = new LocalVariableAttribute(cp);
            ca.getAttributes().add(va);
        }
        final int maxLocals = ca.getMaxLocals();
        final String desc = Descriptor.of(type);
        va.addEntry(0, ca.getCodeLength(), cp.addUtf8Info(name), cp.addUtf8Info(desc), maxLocals);
        ca.setMaxLocals(maxLocals + Descriptor.dataSize(desc));
    }
    
    public void insertParameter(final CtClass type) throws CannotCompileException {
        this.declaringClass.checkModify();
        final String desc = this.methodInfo.getDescriptor();
        final String desc2 = Descriptor.insertParameter(type, desc);
        try {
            this.addParameter2(Modifier.isStatic(this.getModifiers()) ? 0 : 1, type, desc);
        }
        catch (BadBytecode e) {
            throw new CannotCompileException(e);
        }
        this.methodInfo.setDescriptor(desc2);
    }
    
    public void addParameter(final CtClass type) throws CannotCompileException {
        this.declaringClass.checkModify();
        final String desc = this.methodInfo.getDescriptor();
        final String desc2 = Descriptor.appendParameter(type, desc);
        final int offset = Modifier.isStatic(this.getModifiers()) ? 0 : 1;
        try {
            this.addParameter2(offset + Descriptor.paramSize(desc), type, desc);
        }
        catch (BadBytecode e) {
            throw new CannotCompileException(e);
        }
        this.methodInfo.setDescriptor(desc2);
    }
    
    private void addParameter2(final int where, final CtClass type, final String desc) throws BadBytecode {
        final CodeAttribute ca = this.methodInfo.getCodeAttribute();
        if (ca != null) {
            int size = 1;
            char typeDesc = 'L';
            int classInfo = 0;
            if (type.isPrimitive()) {
                final CtPrimitiveType cpt = (CtPrimitiveType)type;
                size = cpt.getDataSize();
                typeDesc = cpt.getDescriptor();
            }
            else {
                classInfo = this.methodInfo.getConstPool().addClassInfo(type);
            }
            ca.insertLocalVar(where, size);
            final LocalVariableAttribute va = (LocalVariableAttribute)ca.getAttribute("LocalVariableTable");
            if (va != null) {
                va.shiftIndex(where, size);
            }
            final LocalVariableTypeAttribute lvta = (LocalVariableTypeAttribute)ca.getAttribute("LocalVariableTypeTable");
            if (lvta != null) {
                lvta.shiftIndex(where, size);
            }
            final StackMapTable smt = (StackMapTable)ca.getAttribute("StackMapTable");
            if (smt != null) {
                smt.insertLocal(where, StackMapTable.typeTagOf(typeDesc), classInfo);
            }
            final StackMap sm = (StackMap)ca.getAttribute("StackMap");
            if (sm != null) {
                sm.insertLocal(where, StackMapTable.typeTagOf(typeDesc), classInfo);
            }
        }
    }
    
    public void instrument(final CodeConverter converter) throws CannotCompileException {
        this.declaringClass.checkModify();
        final ConstPool cp = this.methodInfo.getConstPool();
        converter.doit(this.getDeclaringClass(), this.methodInfo, cp);
    }
    
    public void instrument(final ExprEditor editor) throws CannotCompileException {
        if (this.declaringClass.isFrozen()) {
            this.declaringClass.checkModify();
        }
        if (editor.doit(this.declaringClass, this.methodInfo)) {
            this.declaringClass.checkModify();
        }
    }
    
    public void insertBefore(final String src) throws CannotCompileException {
        this.insertBefore(src, true);
    }
    
    private void insertBefore(final String src, final boolean rebuild) throws CannotCompileException {
        final CtClass cc = this.declaringClass;
        cc.checkModify();
        final CodeAttribute ca = this.methodInfo.getCodeAttribute();
        if (ca == null) {
            throw new CannotCompileException("no method body");
        }
        final CodeIterator iterator = ca.iterator();
        final Javac jv = new Javac(cc);
        try {
            final int nvars = jv.recordParams(this.getParameterTypes(), Modifier.isStatic(this.getModifiers()));
            jv.recordParamNames(ca, nvars);
            jv.recordLocalVariables(ca, 0);
            jv.recordType(this.getReturnType0());
            jv.compileStmnt(src);
            final Bytecode b = jv.getBytecode();
            final int stack = b.getMaxStack();
            final int locals = b.getMaxLocals();
            if (stack > ca.getMaxStack()) {
                ca.setMaxStack(stack);
            }
            if (locals > ca.getMaxLocals()) {
                ca.setMaxLocals(locals);
            }
            final int pos = iterator.insertEx(b.get());
            iterator.insert(b.getExceptionTable(), pos);
            if (rebuild) {
                this.methodInfo.rebuildStackMapIf6(cc.getClassPool(), cc.getClassFile2());
            }
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
    
    public void insertAfter(final String src) throws CannotCompileException {
        this.insertAfter(src, false);
    }
    
    public void insertAfter(final String src, final boolean asFinally) throws CannotCompileException {
        final CtClass cc = this.declaringClass;
        cc.checkModify();
        final ConstPool pool = this.methodInfo.getConstPool();
        final CodeAttribute ca = this.methodInfo.getCodeAttribute();
        if (ca == null) {
            throw new CannotCompileException("no method body");
        }
        final CodeIterator iterator = ca.iterator();
        final int retAddr = ca.getMaxLocals();
        final Bytecode b = new Bytecode(pool, 0, retAddr + 1);
        b.setStackDepth(ca.getMaxStack() + 1);
        final Javac jv = new Javac(b, cc);
        try {
            final int nvars = jv.recordParams(this.getParameterTypes(), Modifier.isStatic(this.getModifiers()));
            jv.recordParamNames(ca, nvars);
            final CtClass rtype = this.getReturnType0();
            final int varNo = jv.recordReturnType(rtype, true);
            jv.recordLocalVariables(ca, 0);
            int handlerLen = this.insertAfterHandler(asFinally, b, rtype, varNo, jv, src);
            int handlerPos = iterator.getCodeLength();
            if (asFinally) {
                ca.getExceptionTable().add(this.getStartPosOfBody(ca), handlerPos, handlerPos, 0);
            }
            int adviceLen = 0;
            int advicePos = 0;
            boolean noReturn = true;
            while (iterator.hasNext()) {
                final int pos = iterator.next();
                if (pos >= handlerPos) {
                    break;
                }
                final int c = iterator.byteAt(pos);
                if (c != 176 && c != 172 && c != 174 && c != 173 && c != 175 && c != 177) {
                    continue;
                }
                if (noReturn) {
                    adviceLen = this.insertAfterAdvice(b, jv, src, pool, rtype, varNo);
                    handlerPos = iterator.append(b.get());
                    iterator.append(b.getExceptionTable(), handlerPos);
                    advicePos = iterator.getCodeLength() - adviceLen;
                    handlerLen = advicePos - handlerPos;
                    noReturn = false;
                }
                this.insertGoto(iterator, advicePos, pos);
                advicePos = iterator.getCodeLength() - adviceLen;
                handlerPos = advicePos - handlerLen;
            }
            if (noReturn) {
                handlerPos = iterator.append(b.get());
                iterator.append(b.getExceptionTable(), handlerPos);
            }
            ca.setMaxStack(b.getMaxStack());
            ca.setMaxLocals(b.getMaxLocals());
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
    
    private int insertAfterAdvice(final Bytecode code, final Javac jv, final String src, final ConstPool cp, final CtClass rtype, final int varNo) throws CompileError {
        final int pc = code.currentPc();
        if (rtype == CtClass.voidType) {
            code.addOpcode(1);
            code.addAstore(varNo);
            jv.compileStmnt(src);
            code.addOpcode(177);
            if (code.getMaxLocals() < 1) {
                code.setMaxLocals(1);
            }
        }
        else {
            code.addStore(varNo, rtype);
            jv.compileStmnt(src);
            code.addLoad(varNo, rtype);
            if (rtype.isPrimitive()) {
                code.addOpcode(((CtPrimitiveType)rtype).getReturnOp());
            }
            else {
                code.addOpcode(176);
            }
        }
        return code.currentPc() - pc;
    }
    
    private void insertGoto(final CodeIterator iterator, final int subr, int pos) throws BadBytecode {
        iterator.setMark(subr);
        iterator.writeByte(0, pos);
        final boolean wide = subr + 2 - pos > 32767;
        final int len = wide ? 4 : 2;
        final CodeIterator.Gap gap = iterator.insertGapAt(pos, len, false);
        pos = gap.position + gap.length - len;
        final int offset = iterator.getMark() - pos;
        if (wide) {
            iterator.writeByte(200, pos);
            iterator.write32bit(offset, pos + 1);
        }
        else if (offset <= 32767) {
            iterator.writeByte(167, pos);
            iterator.write16bit(offset, pos + 1);
        }
        else {
            if (gap.length < 4) {
                final CodeIterator.Gap gap2 = iterator.insertGapAt(gap.position, 2, false);
                pos = gap2.position + gap2.length + gap.length - 4;
            }
            iterator.writeByte(200, pos);
            iterator.write32bit(iterator.getMark() - pos, pos + 1);
        }
    }
    
    private int insertAfterHandler(final boolean asFinally, final Bytecode b, final CtClass rtype, final int returnVarNo, final Javac javac, final String src) throws CompileError {
        if (!asFinally) {
            return 0;
        }
        final int var = b.getMaxLocals();
        b.incMaxLocals(1);
        final int pc = b.currentPc();
        b.addAstore(var);
        if (rtype.isPrimitive()) {
            final char c = ((CtPrimitiveType)rtype).getDescriptor();
            if (c == 'D') {
                b.addDconst(0.0);
                b.addDstore(returnVarNo);
            }
            else if (c == 'F') {
                b.addFconst(0.0f);
                b.addFstore(returnVarNo);
            }
            else if (c == 'J') {
                b.addLconst(0L);
                b.addLstore(returnVarNo);
            }
            else if (c == 'V') {
                b.addOpcode(1);
                b.addAstore(returnVarNo);
            }
            else {
                b.addIconst(0);
                b.addIstore(returnVarNo);
            }
        }
        else {
            b.addOpcode(1);
            b.addAstore(returnVarNo);
        }
        javac.compileStmnt(src);
        b.addAload(var);
        b.addOpcode(191);
        return b.currentPc() - pc;
    }
    
    public void addCatch(final String src, final CtClass exceptionType) throws CannotCompileException {
        this.addCatch(src, exceptionType, "$e");
    }
    
    public void addCatch(final String src, final CtClass exceptionType, final String exceptionName) throws CannotCompileException {
        final CtClass cc = this.declaringClass;
        cc.checkModify();
        final ConstPool cp = this.methodInfo.getConstPool();
        final CodeAttribute ca = this.methodInfo.getCodeAttribute();
        final CodeIterator iterator = ca.iterator();
        final Bytecode b = new Bytecode(cp, ca.getMaxStack(), ca.getMaxLocals());
        b.setStackDepth(1);
        final Javac jv = new Javac(b, cc);
        try {
            jv.recordParams(this.getParameterTypes(), Modifier.isStatic(this.getModifiers()));
            final int var = jv.recordVariable(exceptionType, exceptionName);
            b.addAstore(var);
            jv.compileStmnt(src);
            final int stack = b.getMaxStack();
            final int locals = b.getMaxLocals();
            if (stack > ca.getMaxStack()) {
                ca.setMaxStack(stack);
            }
            if (locals > ca.getMaxLocals()) {
                ca.setMaxLocals(locals);
            }
            final int len = iterator.getCodeLength();
            final int pos = iterator.append(b.get());
            ca.getExceptionTable().add(this.getStartPosOfBody(ca), len, len, cp.addClassInfo(exceptionType));
            iterator.append(b.getExceptionTable(), pos);
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
    
    int getStartPosOfBody(final CodeAttribute ca) throws CannotCompileException {
        return 0;
    }
    
    public int insertAt(final int lineNum, final String src) throws CannotCompileException {
        return this.insertAt(lineNum, true, src);
    }
    
    public int insertAt(int lineNum, final boolean modify, final String src) throws CannotCompileException {
        final CodeAttribute ca = this.methodInfo.getCodeAttribute();
        if (ca == null) {
            throw new CannotCompileException("no method body");
        }
        final LineNumberAttribute ainfo = (LineNumberAttribute)ca.getAttribute("LineNumberTable");
        if (ainfo == null) {
            throw new CannotCompileException("no line number info");
        }
        final LineNumberAttribute.Pc pc = ainfo.toNearPc(lineNum);
        lineNum = pc.line;
        int index = pc.index;
        if (!modify) {
            return lineNum;
        }
        final CtClass cc = this.declaringClass;
        cc.checkModify();
        final CodeIterator iterator = ca.iterator();
        final Javac jv = new Javac(cc);
        try {
            jv.recordLocalVariables(ca, index);
            jv.recordParams(this.getParameterTypes(), Modifier.isStatic(this.getModifiers()));
            jv.setMaxLocals(ca.getMaxLocals());
            jv.compileStmnt(src);
            final Bytecode b = jv.getBytecode();
            final int locals = b.getMaxLocals();
            final int stack = b.getMaxStack();
            ca.setMaxLocals(locals);
            if (stack > ca.getMaxStack()) {
                ca.setMaxStack(stack);
            }
            index = iterator.insertAt(index, b.get());
            iterator.insert(b.getExceptionTable(), index);
            this.methodInfo.rebuildStackMapIf6(cc.getClassPool(), cc.getClassFile2());
            return lineNum;
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
}
