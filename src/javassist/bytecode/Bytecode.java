// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import javassist.CtPrimitiveType;
import javassist.CtClass;

public class Bytecode extends ByteVector implements Cloneable, Opcode
{
    public static final CtClass THIS;
    ConstPool constPool;
    int maxStack;
    int maxLocals;
    ExceptionTable tryblocks;
    private int stackDepth;
    
    public Bytecode(final ConstPool cp, final int stacksize, final int localvars) {
        this.constPool = cp;
        this.maxStack = stacksize;
        this.maxLocals = localvars;
        this.tryblocks = new ExceptionTable(cp);
        this.stackDepth = 0;
    }
    
    public Bytecode(final ConstPool cp) {
        this(cp, 0, 0);
    }
    
    @Override
    public Object clone() {
        try {
            final Bytecode bc = (Bytecode)super.clone();
            bc.tryblocks = (ExceptionTable)this.tryblocks.clone();
            return bc;
        }
        catch (CloneNotSupportedException cnse) {
            throw new RuntimeException(cnse);
        }
    }
    
    public ConstPool getConstPool() {
        return this.constPool;
    }
    
    public ExceptionTable getExceptionTable() {
        return this.tryblocks;
    }
    
    public CodeAttribute toCodeAttribute() {
        return new CodeAttribute(this.constPool, this.maxStack, this.maxLocals, this.get(), this.tryblocks);
    }
    
    public int length() {
        return this.getSize();
    }
    
    public byte[] get() {
        return this.copy();
    }
    
    public int getMaxStack() {
        return this.maxStack;
    }
    
    public void setMaxStack(final int size) {
        this.maxStack = size;
    }
    
    public int getMaxLocals() {
        return this.maxLocals;
    }
    
    public void setMaxLocals(final int size) {
        this.maxLocals = size;
    }
    
    public void setMaxLocals(final boolean isStatic, final CtClass[] params, int locals) {
        if (!isStatic) {
            ++locals;
        }
        if (params != null) {
            final CtClass doubleType = CtClass.doubleType;
            final CtClass longType = CtClass.longType;
            for (final CtClass type : params) {
                if (type == doubleType || type == longType) {
                    locals += 2;
                }
                else {
                    ++locals;
                }
            }
        }
        this.maxLocals = locals;
    }
    
    public void incMaxLocals(final int diff) {
        this.maxLocals += diff;
    }
    
    public void addExceptionHandler(final int start, final int end, final int handler, final CtClass type) {
        this.addExceptionHandler(start, end, handler, this.constPool.addClassInfo(type));
    }
    
    public void addExceptionHandler(final int start, final int end, final int handler, final String type) {
        this.addExceptionHandler(start, end, handler, this.constPool.addClassInfo(type));
    }
    
    public void addExceptionHandler(final int start, final int end, final int handler, final int type) {
        this.tryblocks.add(start, end, handler, type);
    }
    
    public int currentPc() {
        return this.getSize();
    }
    
    @Override
    public int read(final int offset) {
        return super.read(offset);
    }
    
    public int read16bit(final int offset) {
        final int v1 = this.read(offset);
        final int v2 = this.read(offset + 1);
        return (v1 << 8) + (v2 & 0xFF);
    }
    
    public int read32bit(final int offset) {
        final int v1 = this.read16bit(offset);
        final int v2 = this.read16bit(offset + 2);
        return (v1 << 16) + (v2 & 0xFFFF);
    }
    
    @Override
    public void write(final int offset, final int value) {
        super.write(offset, value);
    }
    
    public void write16bit(final int offset, final int value) {
        this.write(offset, value >> 8);
        this.write(offset + 1, value);
    }
    
    public void write32bit(final int offset, final int value) {
        this.write16bit(offset, value >> 16);
        this.write16bit(offset + 2, value);
    }
    
    @Override
    public void add(final int code) {
        super.add(code);
    }
    
    public void add32bit(final int value) {
        this.add(value >> 24, value >> 16, value >> 8, value);
    }
    
    @Override
    public void addGap(final int length) {
        super.addGap(length);
    }
    
    public void addOpcode(final int code) {
        this.add(code);
        this.growStack(Bytecode.STACK_GROW[code]);
    }
    
    public void growStack(final int diff) {
        this.setStackDepth(this.stackDepth + diff);
    }
    
    public int getStackDepth() {
        return this.stackDepth;
    }
    
    public void setStackDepth(final int depth) {
        this.stackDepth = depth;
        if (this.stackDepth > this.maxStack) {
            this.maxStack = this.stackDepth;
        }
    }
    
    public void addIndex(final int index) {
        this.add(index >> 8, index);
    }
    
    public void addAload(final int n) {
        if (n < 4) {
            this.addOpcode(42 + n);
        }
        else if (n < 256) {
            this.addOpcode(25);
            this.add(n);
        }
        else {
            this.addOpcode(196);
            this.addOpcode(25);
            this.addIndex(n);
        }
    }
    
    public void addAstore(final int n) {
        if (n < 4) {
            this.addOpcode(75 + n);
        }
        else if (n < 256) {
            this.addOpcode(58);
            this.add(n);
        }
        else {
            this.addOpcode(196);
            this.addOpcode(58);
            this.addIndex(n);
        }
    }
    
    public void addIconst(final int n) {
        if (n < 6 && -2 < n) {
            this.addOpcode(3 + n);
        }
        else if (n <= 127 && -128 <= n) {
            this.addOpcode(16);
            this.add(n);
        }
        else if (n <= 32767 && -32768 <= n) {
            this.addOpcode(17);
            this.add(n >> 8);
            this.add(n);
        }
        else {
            this.addLdc(this.constPool.addIntegerInfo(n));
        }
    }
    
    public void addConstZero(final CtClass type) {
        if (type.isPrimitive()) {
            if (type == CtClass.longType) {
                this.addOpcode(9);
            }
            else if (type == CtClass.floatType) {
                this.addOpcode(11);
            }
            else if (type == CtClass.doubleType) {
                this.addOpcode(14);
            }
            else {
                if (type == CtClass.voidType) {
                    throw new RuntimeException("void type?");
                }
                this.addOpcode(3);
            }
        }
        else {
            this.addOpcode(1);
        }
    }
    
    public void addIload(final int n) {
        if (n < 4) {
            this.addOpcode(26 + n);
        }
        else if (n < 256) {
            this.addOpcode(21);
            this.add(n);
        }
        else {
            this.addOpcode(196);
            this.addOpcode(21);
            this.addIndex(n);
        }
    }
    
    public void addIstore(final int n) {
        if (n < 4) {
            this.addOpcode(59 + n);
        }
        else if (n < 256) {
            this.addOpcode(54);
            this.add(n);
        }
        else {
            this.addOpcode(196);
            this.addOpcode(54);
            this.addIndex(n);
        }
    }
    
    public void addLconst(final long n) {
        if (n == 0L || n == 1L) {
            this.addOpcode(9 + (int)n);
        }
        else {
            this.addLdc2w(n);
        }
    }
    
    public void addLload(final int n) {
        if (n < 4) {
            this.addOpcode(30 + n);
        }
        else if (n < 256) {
            this.addOpcode(22);
            this.add(n);
        }
        else {
            this.addOpcode(196);
            this.addOpcode(22);
            this.addIndex(n);
        }
    }
    
    public void addLstore(final int n) {
        if (n < 4) {
            this.addOpcode(63 + n);
        }
        else if (n < 256) {
            this.addOpcode(55);
            this.add(n);
        }
        else {
            this.addOpcode(196);
            this.addOpcode(55);
            this.addIndex(n);
        }
    }
    
    public void addDconst(final double d) {
        if (d == 0.0 || d == 1.0) {
            this.addOpcode(14 + (int)d);
        }
        else {
            this.addLdc2w(d);
        }
    }
    
    public void addDload(final int n) {
        if (n < 4) {
            this.addOpcode(38 + n);
        }
        else if (n < 256) {
            this.addOpcode(24);
            this.add(n);
        }
        else {
            this.addOpcode(196);
            this.addOpcode(24);
            this.addIndex(n);
        }
    }
    
    public void addDstore(final int n) {
        if (n < 4) {
            this.addOpcode(71 + n);
        }
        else if (n < 256) {
            this.addOpcode(57);
            this.add(n);
        }
        else {
            this.addOpcode(196);
            this.addOpcode(57);
            this.addIndex(n);
        }
    }
    
    public void addFconst(final float f) {
        if (f == 0.0f || f == 1.0f || f == 2.0f) {
            this.addOpcode(11 + (int)f);
        }
        else {
            this.addLdc(this.constPool.addFloatInfo(f));
        }
    }
    
    public void addFload(final int n) {
        if (n < 4) {
            this.addOpcode(34 + n);
        }
        else if (n < 256) {
            this.addOpcode(23);
            this.add(n);
        }
        else {
            this.addOpcode(196);
            this.addOpcode(23);
            this.addIndex(n);
        }
    }
    
    public void addFstore(final int n) {
        if (n < 4) {
            this.addOpcode(67 + n);
        }
        else if (n < 256) {
            this.addOpcode(56);
            this.add(n);
        }
        else {
            this.addOpcode(196);
            this.addOpcode(56);
            this.addIndex(n);
        }
    }
    
    public int addLoad(final int n, final CtClass type) {
        if (type.isPrimitive()) {
            if (type == CtClass.booleanType || type == CtClass.charType || type == CtClass.byteType || type == CtClass.shortType || type == CtClass.intType) {
                this.addIload(n);
            }
            else {
                if (type == CtClass.longType) {
                    this.addLload(n);
                    return 2;
                }
                if (type == CtClass.floatType) {
                    this.addFload(n);
                }
                else {
                    if (type == CtClass.doubleType) {
                        this.addDload(n);
                        return 2;
                    }
                    throw new RuntimeException("void type?");
                }
            }
        }
        else {
            this.addAload(n);
        }
        return 1;
    }
    
    public int addStore(final int n, final CtClass type) {
        if (type.isPrimitive()) {
            if (type == CtClass.booleanType || type == CtClass.charType || type == CtClass.byteType || type == CtClass.shortType || type == CtClass.intType) {
                this.addIstore(n);
            }
            else {
                if (type == CtClass.longType) {
                    this.addLstore(n);
                    return 2;
                }
                if (type == CtClass.floatType) {
                    this.addFstore(n);
                }
                else {
                    if (type == CtClass.doubleType) {
                        this.addDstore(n);
                        return 2;
                    }
                    throw new RuntimeException("void type?");
                }
            }
        }
        else {
            this.addAstore(n);
        }
        return 1;
    }
    
    public int addLoadParameters(final CtClass[] params, final int offset) {
        int stacksize = 0;
        if (params != null) {
            for (int n = params.length, i = 0; i < n; ++i) {
                stacksize += this.addLoad(stacksize + offset, params[i]);
            }
        }
        return stacksize;
    }
    
    public void addCheckcast(final CtClass c) {
        this.addOpcode(192);
        this.addIndex(this.constPool.addClassInfo(c));
    }
    
    public void addCheckcast(final String classname) {
        this.addOpcode(192);
        this.addIndex(this.constPool.addClassInfo(classname));
    }
    
    public void addInstanceof(final String classname) {
        this.addOpcode(193);
        this.addIndex(this.constPool.addClassInfo(classname));
    }
    
    public void addGetfield(final CtClass c, final String name, final String type) {
        this.add(180);
        final int ci = this.constPool.addClassInfo(c);
        this.addIndex(this.constPool.addFieldrefInfo(ci, name, type));
        this.growStack(Descriptor.dataSize(type) - 1);
    }
    
    public void addGetfield(final String c, final String name, final String type) {
        this.add(180);
        final int ci = this.constPool.addClassInfo(c);
        this.addIndex(this.constPool.addFieldrefInfo(ci, name, type));
        this.growStack(Descriptor.dataSize(type) - 1);
    }
    
    public void addGetstatic(final CtClass c, final String name, final String type) {
        this.add(178);
        final int ci = this.constPool.addClassInfo(c);
        this.addIndex(this.constPool.addFieldrefInfo(ci, name, type));
        this.growStack(Descriptor.dataSize(type));
    }
    
    public void addGetstatic(final String c, final String name, final String type) {
        this.add(178);
        final int ci = this.constPool.addClassInfo(c);
        this.addIndex(this.constPool.addFieldrefInfo(ci, name, type));
        this.growStack(Descriptor.dataSize(type));
    }
    
    public void addInvokespecial(final CtClass clazz, final String name, final CtClass returnType, final CtClass[] paramTypes) {
        final String desc = Descriptor.ofMethod(returnType, paramTypes);
        this.addInvokespecial(clazz, name, desc);
    }
    
    public void addInvokespecial(final CtClass clazz, final String name, final String desc) {
        final boolean isInterface = clazz != null && clazz.isInterface();
        this.addInvokespecial(isInterface, this.constPool.addClassInfo(clazz), name, desc);
    }
    
    public void addInvokespecial(final String clazz, final String name, final String desc) {
        this.addInvokespecial(false, this.constPool.addClassInfo(clazz), name, desc);
    }
    
    public void addInvokespecial(final int clazz, final String name, final String desc) {
        this.addInvokespecial(false, clazz, name, desc);
    }
    
    public void addInvokespecial(final boolean isInterface, final int clazz, final String name, final String desc) {
        int index;
        if (isInterface) {
            index = this.constPool.addInterfaceMethodrefInfo(clazz, name, desc);
        }
        else {
            index = this.constPool.addMethodrefInfo(clazz, name, desc);
        }
        this.addInvokespecial(index, desc);
    }
    
    public void addInvokespecial(final int index, final String desc) {
        this.add(183);
        this.addIndex(index);
        this.growStack(Descriptor.dataSize(desc) - 1);
    }
    
    public void addInvokestatic(final CtClass clazz, final String name, final CtClass returnType, final CtClass[] paramTypes) {
        final String desc = Descriptor.ofMethod(returnType, paramTypes);
        this.addInvokestatic(clazz, name, desc);
    }
    
    public void addInvokestatic(final CtClass clazz, final String name, final String desc) {
        final boolean isInterface = clazz != Bytecode.THIS && clazz.isInterface();
        this.addInvokestatic(this.constPool.addClassInfo(clazz), name, desc, isInterface);
    }
    
    public void addInvokestatic(final String classname, final String name, final String desc) {
        this.addInvokestatic(this.constPool.addClassInfo(classname), name, desc);
    }
    
    public void addInvokestatic(final int clazz, final String name, final String desc) {
        this.addInvokestatic(clazz, name, desc, false);
    }
    
    private void addInvokestatic(final int clazz, final String name, final String desc, final boolean isInterface) {
        this.add(184);
        int index;
        if (isInterface) {
            index = this.constPool.addInterfaceMethodrefInfo(clazz, name, desc);
        }
        else {
            index = this.constPool.addMethodrefInfo(clazz, name, desc);
        }
        this.addIndex(index);
        this.growStack(Descriptor.dataSize(desc));
    }
    
    public void addInvokevirtual(final CtClass clazz, final String name, final CtClass returnType, final CtClass[] paramTypes) {
        final String desc = Descriptor.ofMethod(returnType, paramTypes);
        this.addInvokevirtual(clazz, name, desc);
    }
    
    public void addInvokevirtual(final CtClass clazz, final String name, final String desc) {
        this.addInvokevirtual(this.constPool.addClassInfo(clazz), name, desc);
    }
    
    public void addInvokevirtual(final String classname, final String name, final String desc) {
        this.addInvokevirtual(this.constPool.addClassInfo(classname), name, desc);
    }
    
    public void addInvokevirtual(final int clazz, final String name, final String desc) {
        this.add(182);
        this.addIndex(this.constPool.addMethodrefInfo(clazz, name, desc));
        this.growStack(Descriptor.dataSize(desc) - 1);
    }
    
    public void addInvokeinterface(final CtClass clazz, final String name, final CtClass returnType, final CtClass[] paramTypes, final int count) {
        final String desc = Descriptor.ofMethod(returnType, paramTypes);
        this.addInvokeinterface(clazz, name, desc, count);
    }
    
    public void addInvokeinterface(final CtClass clazz, final String name, final String desc, final int count) {
        this.addInvokeinterface(this.constPool.addClassInfo(clazz), name, desc, count);
    }
    
    public void addInvokeinterface(final String classname, final String name, final String desc, final int count) {
        this.addInvokeinterface(this.constPool.addClassInfo(classname), name, desc, count);
    }
    
    public void addInvokeinterface(final int clazz, final String name, final String desc, final int count) {
        this.add(185);
        this.addIndex(this.constPool.addInterfaceMethodrefInfo(clazz, name, desc));
        this.add(count);
        this.add(0);
        this.growStack(Descriptor.dataSize(desc) - 1);
    }
    
    public void addInvokedynamic(final int bootstrap, final String name, final String desc) {
        final int nt = this.constPool.addNameAndTypeInfo(name, desc);
        final int dyn = this.constPool.addInvokeDynamicInfo(bootstrap, nt);
        this.add(186);
        this.addIndex(dyn);
        this.add(0, 0);
        this.growStack(Descriptor.dataSize(desc));
    }
    
    public void addLdc(final String s) {
        this.addLdc(this.constPool.addStringInfo(s));
    }
    
    public void addLdc(final int i) {
        if (i > 255) {
            this.addOpcode(19);
            this.addIndex(i);
        }
        else {
            this.addOpcode(18);
            this.add(i);
        }
    }
    
    public void addLdc2w(final long l) {
        this.addOpcode(20);
        this.addIndex(this.constPool.addLongInfo(l));
    }
    
    public void addLdc2w(final double d) {
        this.addOpcode(20);
        this.addIndex(this.constPool.addDoubleInfo(d));
    }
    
    public void addNew(final CtClass clazz) {
        this.addOpcode(187);
        this.addIndex(this.constPool.addClassInfo(clazz));
    }
    
    public void addNew(final String classname) {
        this.addOpcode(187);
        this.addIndex(this.constPool.addClassInfo(classname));
    }
    
    public void addAnewarray(final String classname) {
        this.addOpcode(189);
        this.addIndex(this.constPool.addClassInfo(classname));
    }
    
    public void addAnewarray(final CtClass clazz, final int length) {
        this.addIconst(length);
        this.addOpcode(189);
        this.addIndex(this.constPool.addClassInfo(clazz));
    }
    
    public void addNewarray(final int atype, final int length) {
        this.addIconst(length);
        this.addOpcode(188);
        this.add(atype);
    }
    
    public int addMultiNewarray(final CtClass clazz, final int[] dimensions) {
        final int len = dimensions.length;
        for (int i = 0; i < len; ++i) {
            this.addIconst(dimensions[i]);
        }
        this.growStack(len);
        return this.addMultiNewarray(clazz, len);
    }
    
    public int addMultiNewarray(final CtClass clazz, final int dim) {
        this.add(197);
        this.addIndex(this.constPool.addClassInfo(clazz));
        this.add(dim);
        this.growStack(1 - dim);
        return dim;
    }
    
    public int addMultiNewarray(final String desc, final int dim) {
        this.add(197);
        this.addIndex(this.constPool.addClassInfo(desc));
        this.add(dim);
        this.growStack(1 - dim);
        return dim;
    }
    
    public void addPutfield(final CtClass c, final String name, final String desc) {
        this.addPutfield0(c, null, name, desc);
    }
    
    public void addPutfield(final String classname, final String name, final String desc) {
        this.addPutfield0(null, classname, name, desc);
    }
    
    private void addPutfield0(final CtClass target, final String classname, final String name, final String desc) {
        this.add(181);
        final int ci = (classname == null) ? this.constPool.addClassInfo(target) : this.constPool.addClassInfo(classname);
        this.addIndex(this.constPool.addFieldrefInfo(ci, name, desc));
        this.growStack(-1 - Descriptor.dataSize(desc));
    }
    
    public void addPutstatic(final CtClass c, final String name, final String desc) {
        this.addPutstatic0(c, null, name, desc);
    }
    
    public void addPutstatic(final String classname, final String fieldName, final String desc) {
        this.addPutstatic0(null, classname, fieldName, desc);
    }
    
    private void addPutstatic0(final CtClass target, final String classname, final String fieldName, final String desc) {
        this.add(179);
        final int ci = (classname == null) ? this.constPool.addClassInfo(target) : this.constPool.addClassInfo(classname);
        this.addIndex(this.constPool.addFieldrefInfo(ci, fieldName, desc));
        this.growStack(-Descriptor.dataSize(desc));
    }
    
    public void addReturn(final CtClass type) {
        if (type == null) {
            this.addOpcode(177);
        }
        else if (type.isPrimitive()) {
            final CtPrimitiveType ptype = (CtPrimitiveType)type;
            this.addOpcode(ptype.getReturnOp());
        }
        else {
            this.addOpcode(176);
        }
    }
    
    public void addRet(final int var) {
        if (var < 256) {
            this.addOpcode(169);
            this.add(var);
        }
        else {
            this.addOpcode(196);
            this.addOpcode(169);
            this.addIndex(var);
        }
    }
    
    public void addPrintln(final String message) {
        this.addGetstatic("java.lang.System", "err", "Ljava/io/PrintStream;");
        this.addLdc(message);
        this.addInvokevirtual("java.io.PrintStream", "println", "(Ljava/lang/String;)V");
    }
    
    static {
        THIS = ConstPool.THIS;
    }
}
