// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib;

class MethodWriter extends MethodVisitor
{
    static final int ACC_CONSTRUCTOR = 524288;
    static final int SAME_FRAME = 0;
    static final int SAME_LOCALS_1_STACK_ITEM_FRAME = 64;
    static final int RESERVED = 128;
    static final int SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED = 247;
    static final int CHOP_FRAME = 248;
    static final int SAME_FRAME_EXTENDED = 251;
    static final int APPEND_FRAME = 252;
    static final int FULL_FRAME = 255;
    static final int FRAMES = 0;
    static final int INSERTED_FRAMES = 1;
    static final int MAXS = 2;
    static final int NOTHING = 3;
    final ClassWriter cw;
    private int access;
    private final int name;
    private final int desc;
    private final String descriptor;
    String signature;
    int classReaderOffset;
    int classReaderLength;
    int exceptionCount;
    int[] exceptions;
    private ByteVector annd;
    private AnnotationWriter anns;
    private AnnotationWriter ianns;
    private AnnotationWriter tanns;
    private AnnotationWriter itanns;
    private AnnotationWriter[] panns;
    private AnnotationWriter[] ipanns;
    private int synthetics;
    private Attribute attrs;
    private ByteVector code;
    private int maxStack;
    private int maxLocals;
    private int currentLocals;
    private int frameCount;
    private ByteVector stackMap;
    private int previousFrameOffset;
    private int[] previousFrame;
    private int[] frame;
    private int handlerCount;
    private Handler firstHandler;
    private Handler lastHandler;
    private int methodParametersCount;
    private ByteVector methodParameters;
    private int localVarCount;
    private ByteVector localVar;
    private int localVarTypeCount;
    private ByteVector localVarType;
    private int lineNumberCount;
    private ByteVector lineNumber;
    private int lastCodeOffset;
    private AnnotationWriter ctanns;
    private AnnotationWriter ictanns;
    private Attribute cattrs;
    private int subroutines;
    private final int compute;
    private Label labels;
    private Label previousBlock;
    private Label currentBlock;
    private int stackSize;
    private int maxStackSize;
    
    MethodWriter(final ClassWriter cw, final int access, final String name, final String desc, final String signature, final String[] exceptions, final int compute) {
        super(327680);
        this.code = new ByteVector();
        if (cw.firstMethod == null) {
            cw.firstMethod = this;
        }
        else {
            cw.lastMethod.mv = this;
        }
        cw.lastMethod = this;
        this.cw = cw;
        this.access = access;
        if ("<init>".equals(name)) {
            this.access |= 0x80000;
        }
        this.name = cw.newUTF8(name);
        this.desc = cw.newUTF8(desc);
        this.descriptor = desc;
        this.signature = signature;
        if (exceptions != null && exceptions.length > 0) {
            this.exceptionCount = exceptions.length;
            this.exceptions = new int[this.exceptionCount];
            for (int i = 0; i < this.exceptionCount; ++i) {
                this.exceptions[i] = cw.newClass(exceptions[i]);
            }
        }
        if ((this.compute = compute) != 3) {
            int size = Type.getArgumentsAndReturnSizes(this.descriptor) >> 2;
            if ((access & 0x8) != 0x0) {
                --size;
            }
            this.maxLocals = size;
            this.currentLocals = size;
            this.labels = new Label();
            final Label labels = this.labels;
            labels.status |= 0x8;
            this.visitLabel(this.labels);
        }
    }
    
    @Override
    public void visitParameter(final String name, final int access) {
        if (this.methodParameters == null) {
            this.methodParameters = new ByteVector();
        }
        ++this.methodParametersCount;
        this.methodParameters.putShort((name == null) ? 0 : this.cw.newUTF8(name)).putShort(access);
    }
    
    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        this.annd = new ByteVector();
        return new AnnotationWriter(this.cw, false, this.annd, null, 0);
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        final ByteVector bv = new ByteVector();
        bv.putShort(this.cw.newUTF8(desc)).putShort(0);
        final AnnotationWriter aw = new AnnotationWriter(this.cw, true, bv, bv, 2);
        if (visible) {
            aw.next = this.anns;
            this.anns = aw;
        }
        else {
            aw.next = this.ianns;
            this.ianns = aw;
        }
        return aw;
    }
    
    @Override
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        final ByteVector bv = new ByteVector();
        AnnotationWriter.putTarget(typeRef, typePath, bv);
        bv.putShort(this.cw.newUTF8(desc)).putShort(0);
        final AnnotationWriter aw = new AnnotationWriter(this.cw, true, bv, bv, bv.length - 2);
        if (visible) {
            aw.next = this.tanns;
            this.tanns = aw;
        }
        else {
            aw.next = this.itanns;
            this.itanns = aw;
        }
        return aw;
    }
    
    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
        final ByteVector bv = new ByteVector();
        if ("Ljava/lang/Synthetic;".equals(desc)) {
            this.synthetics = Math.max(this.synthetics, parameter + 1);
            return new AnnotationWriter(this.cw, false, bv, null, 0);
        }
        bv.putShort(this.cw.newUTF8(desc)).putShort(0);
        final AnnotationWriter aw = new AnnotationWriter(this.cw, true, bv, bv, 2);
        if (visible) {
            if (this.panns == null) {
                this.panns = new AnnotationWriter[Type.getArgumentTypes(this.descriptor).length];
            }
            aw.next = this.panns[parameter];
            this.panns[parameter] = aw;
        }
        else {
            if (this.ipanns == null) {
                this.ipanns = new AnnotationWriter[Type.getArgumentTypes(this.descriptor).length];
            }
            aw.next = this.ipanns[parameter];
            this.ipanns[parameter] = aw;
        }
        return aw;
    }
    
    @Override
    public void visitAttribute(final Attribute attr) {
        if (attr.isCodeAttribute()) {
            attr.next = this.cattrs;
            this.cattrs = attr;
        }
        else {
            attr.next = this.attrs;
            this.attrs = attr;
        }
    }
    
    @Override
    public void visitCode() {
    }
    
    @Override
    public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
        if (this.compute == 0) {
            return;
        }
        if (this.compute == 1) {
            if (this.currentBlock.frame == null) {
                this.currentBlock.frame = new CurrentFrame();
                this.currentBlock.frame.owner = this.currentBlock;
                this.currentBlock.frame.initInputFrame(this.cw, this.access, Type.getArgumentTypes(this.descriptor), nLocal);
                this.visitImplicitFirstFrame();
            }
            else {
                if (type == -1) {
                    this.currentBlock.frame.set(this.cw, nLocal, local, nStack, stack);
                }
                this.visitFrame(this.currentBlock.frame);
            }
        }
        else if (type == -1) {
            if (this.previousFrame == null) {
                this.visitImplicitFirstFrame();
            }
            this.currentLocals = nLocal;
            int frameIndex = this.startFrame(this.code.length, nLocal, nStack);
            for (int i = 0; i < nLocal; ++i) {
                if (local[i] instanceof String) {
                    this.frame[frameIndex++] = (0x1700000 | this.cw.addType((String)local[i]));
                }
                else if (local[i] instanceof Integer) {
                    this.frame[frameIndex++] = (int)local[i];
                }
                else {
                    this.frame[frameIndex++] = (0x1800000 | this.cw.addUninitializedType("", ((Label)local[i]).position));
                }
            }
            for (int i = 0; i < nStack; ++i) {
                if (stack[i] instanceof String) {
                    this.frame[frameIndex++] = (0x1700000 | this.cw.addType((String)stack[i]));
                }
                else if (stack[i] instanceof Integer) {
                    this.frame[frameIndex++] = (int)stack[i];
                }
                else {
                    this.frame[frameIndex++] = (0x1800000 | this.cw.addUninitializedType("", ((Label)stack[i]).position));
                }
            }
            this.endFrame();
        }
        else {
            int delta;
            if (this.stackMap == null) {
                this.stackMap = new ByteVector();
                delta = this.code.length;
            }
            else {
                delta = this.code.length - this.previousFrameOffset - 1;
                if (delta < 0) {
                    if (type == 3) {
                        return;
                    }
                    throw new IllegalStateException();
                }
            }
            switch (type) {
                case 0: {
                    this.currentLocals = nLocal;
                    this.stackMap.putByte(255).putShort(delta).putShort(nLocal);
                    for (int i = 0; i < nLocal; ++i) {
                        this.writeFrameType(local[i]);
                    }
                    this.stackMap.putShort(nStack);
                    for (int i = 0; i < nStack; ++i) {
                        this.writeFrameType(stack[i]);
                    }
                    break;
                }
                case 1: {
                    this.currentLocals += nLocal;
                    this.stackMap.putByte(251 + nLocal).putShort(delta);
                    for (int i = 0; i < nLocal; ++i) {
                        this.writeFrameType(local[i]);
                    }
                    break;
                }
                case 2: {
                    this.currentLocals -= nLocal;
                    this.stackMap.putByte(251 - nLocal).putShort(delta);
                    break;
                }
                case 3: {
                    if (delta < 64) {
                        this.stackMap.putByte(delta);
                        break;
                    }
                    this.stackMap.putByte(251).putShort(delta);
                    break;
                }
                case 4: {
                    if (delta < 64) {
                        this.stackMap.putByte(64 + delta);
                    }
                    else {
                        this.stackMap.putByte(247).putShort(delta);
                    }
                    this.writeFrameType(stack[0]);
                    break;
                }
            }
            this.previousFrameOffset = this.code.length;
            ++this.frameCount;
        }
        this.maxStack = Math.max(this.maxStack, nStack);
        this.maxLocals = Math.max(this.maxLocals, this.currentLocals);
    }
    
    @Override
    public void visitInsn(final int opcode) {
        this.lastCodeOffset = this.code.length;
        this.code.putByte(opcode);
        if (this.currentBlock != null) {
            if (this.compute == 0 || this.compute == 1) {
                this.currentBlock.frame.execute(opcode, 0, null, null);
            }
            else {
                final int size = this.stackSize + Frame.SIZE[opcode];
                if (size > this.maxStackSize) {
                    this.maxStackSize = size;
                }
                this.stackSize = size;
            }
            if ((opcode >= 172 && opcode <= 177) || opcode == 191) {
                this.noSuccessor();
            }
        }
    }
    
    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        this.lastCodeOffset = this.code.length;
        if (this.currentBlock != null) {
            if (this.compute == 0 || this.compute == 1) {
                this.currentBlock.frame.execute(opcode, operand, null, null);
            }
            else if (opcode != 188) {
                final int size = this.stackSize + 1;
                if (size > this.maxStackSize) {
                    this.maxStackSize = size;
                }
                this.stackSize = size;
            }
        }
        if (opcode == 17) {
            this.code.put12(opcode, operand);
        }
        else {
            this.code.put11(opcode, operand);
        }
    }
    
    @Override
    public void visitVarInsn(final int opcode, final int var) {
        this.lastCodeOffset = this.code.length;
        if (this.currentBlock != null) {
            if (this.compute == 0 || this.compute == 1) {
                this.currentBlock.frame.execute(opcode, var, null, null);
            }
            else if (opcode == 169) {
                final Label currentBlock = this.currentBlock;
                currentBlock.status |= 0x100;
                this.currentBlock.inputStackTop = this.stackSize;
                this.noSuccessor();
            }
            else {
                final int size = this.stackSize + Frame.SIZE[opcode];
                if (size > this.maxStackSize) {
                    this.maxStackSize = size;
                }
                this.stackSize = size;
            }
        }
        if (this.compute != 3) {
            int n;
            if (opcode == 22 || opcode == 24 || opcode == 55 || opcode == 57) {
                n = var + 2;
            }
            else {
                n = var + 1;
            }
            if (n > this.maxLocals) {
                this.maxLocals = n;
            }
        }
        if (var < 4 && opcode != 169) {
            int opt;
            if (opcode < 54) {
                opt = 26 + (opcode - 21 << 2) + var;
            }
            else {
                opt = 59 + (opcode - 54 << 2) + var;
            }
            this.code.putByte(opt);
        }
        else if (var >= 256) {
            this.code.putByte(196).put12(opcode, var);
        }
        else {
            this.code.put11(opcode, var);
        }
        if (opcode >= 54 && this.compute == 0 && this.handlerCount > 0) {
            this.visitLabel(new Label());
        }
    }
    
    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        this.lastCodeOffset = this.code.length;
        final Item i = this.cw.newClassItem(type);
        if (this.currentBlock != null) {
            if (this.compute == 0 || this.compute == 1) {
                this.currentBlock.frame.execute(opcode, this.code.length, this.cw, i);
            }
            else if (opcode == 187) {
                final int size = this.stackSize + 1;
                if (size > this.maxStackSize) {
                    this.maxStackSize = size;
                }
                this.stackSize = size;
            }
        }
        this.code.put12(opcode, i.index);
    }
    
    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        this.lastCodeOffset = this.code.length;
        final Item i = this.cw.newFieldItem(owner, name, desc);
        if (this.currentBlock != null) {
            if (this.compute == 0 || this.compute == 1) {
                this.currentBlock.frame.execute(opcode, 0, this.cw, i);
            }
            else {
                final char c = desc.charAt(0);
                int size = 0;
                switch (opcode) {
                    case 178: {
                        size = this.stackSize + ((c == 'D' || c == 'J') ? 2 : 1);
                        break;
                    }
                    case 179: {
                        size = this.stackSize + ((c == 'D' || c == 'J') ? -2 : -1);
                        break;
                    }
                    case 180: {
                        size = this.stackSize + ((c == 'D' || c == 'J') ? 1 : 0);
                        break;
                    }
                    default: {
                        size = this.stackSize + ((c == 'D' || c == 'J') ? -3 : -2);
                        break;
                    }
                }
                if (size > this.maxStackSize) {
                    this.maxStackSize = size;
                }
                this.stackSize = size;
            }
        }
        this.code.put12(opcode, i.index);
    }
    
    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean itf) {
        this.lastCodeOffset = this.code.length;
        final Item i = this.cw.newMethodItem(owner, name, desc, itf);
        int argSize = i.intVal;
        if (this.currentBlock != null) {
            if (this.compute == 0 || this.compute == 1) {
                this.currentBlock.frame.execute(opcode, 0, this.cw, i);
            }
            else {
                if (argSize == 0) {
                    argSize = Type.getArgumentsAndReturnSizes(desc);
                    i.intVal = argSize;
                }
                int size;
                if (opcode == 184) {
                    size = this.stackSize - (argSize >> 2) + (argSize & 0x3) + 1;
                }
                else {
                    size = this.stackSize - (argSize >> 2) + (argSize & 0x3);
                }
                if (size > this.maxStackSize) {
                    this.maxStackSize = size;
                }
                this.stackSize = size;
            }
        }
        if (opcode == 185) {
            if (argSize == 0) {
                argSize = Type.getArgumentsAndReturnSizes(desc);
                i.intVal = argSize;
            }
            this.code.put12(185, i.index).put11(argSize >> 2, 0);
        }
        else {
            this.code.put12(opcode, i.index);
        }
    }
    
    @Override
    public void visitInvokeDynamicInsn(final String name, final String desc, final Handle bsm, final Object... bsmArgs) {
        this.lastCodeOffset = this.code.length;
        final Item i = this.cw.newInvokeDynamicItem(name, desc, bsm, bsmArgs);
        int argSize = i.intVal;
        if (this.currentBlock != null) {
            if (this.compute == 0 || this.compute == 1) {
                this.currentBlock.frame.execute(186, 0, this.cw, i);
            }
            else {
                if (argSize == 0) {
                    argSize = Type.getArgumentsAndReturnSizes(desc);
                    i.intVal = argSize;
                }
                final int size = this.stackSize - (argSize >> 2) + (argSize & 0x3) + 1;
                if (size > this.maxStackSize) {
                    this.maxStackSize = size;
                }
                this.stackSize = size;
            }
        }
        this.code.put12(186, i.index);
        this.code.putShort(0);
    }
    
    @Override
    public void visitJumpInsn(int opcode, final Label label) {
        final boolean isWide = opcode >= 200;
        opcode = (isWide ? (opcode - 33) : opcode);
        this.lastCodeOffset = this.code.length;
        Label nextInsn = null;
        if (this.currentBlock != null) {
            if (this.compute == 0) {
                this.currentBlock.frame.execute(opcode, 0, null, null);
                final Label first = label.getFirst();
                first.status |= 0x10;
                this.addSuccessor(0, label);
                if (opcode != 167) {
                    nextInsn = new Label();
                }
            }
            else if (this.compute == 1) {
                this.currentBlock.frame.execute(opcode, 0, null, null);
            }
            else if (opcode == 168) {
                if ((label.status & 0x200) == 0x0) {
                    label.status |= 0x200;
                    ++this.subroutines;
                }
                final Label currentBlock = this.currentBlock;
                currentBlock.status |= 0x80;
                this.addSuccessor(this.stackSize + 1, label);
                nextInsn = new Label();
            }
            else {
                this.addSuccessor(this.stackSize += Frame.SIZE[opcode], label);
            }
        }
        if ((label.status & 0x2) != 0x0 && label.position - this.code.length < -32768) {
            if (opcode == 167) {
                this.code.putByte(200);
            }
            else if (opcode == 168) {
                this.code.putByte(201);
            }
            else {
                if (nextInsn != null) {
                    final Label label2 = nextInsn;
                    label2.status |= 0x10;
                }
                this.code.putByte((opcode <= 166) ? ((opcode + 1 ^ 0x1) - 1) : (opcode ^ 0x1));
                this.code.putShort(8);
                this.code.putByte(200);
            }
            label.put(this, this.code, this.code.length - 1, true);
        }
        else if (isWide) {
            this.code.putByte(opcode + 33);
            label.put(this, this.code, this.code.length - 1, true);
        }
        else {
            this.code.putByte(opcode);
            label.put(this, this.code, this.code.length - 1, false);
        }
        if (this.currentBlock != null) {
            if (nextInsn != null) {
                this.visitLabel(nextInsn);
            }
            if (opcode == 167) {
                this.noSuccessor();
            }
        }
    }
    
    @Override
    public void visitLabel(final Label label) {
        final ClassWriter cw = this.cw;
        cw.hasAsmInsns |= label.resolve(this, this.code.length, this.code.data);
        if ((label.status & 0x1) != 0x0) {
            return;
        }
        if (this.compute == 0) {
            if (this.currentBlock != null) {
                if (label.position == this.currentBlock.position) {
                    final Label currentBlock = this.currentBlock;
                    currentBlock.status |= (label.status & 0x10);
                    label.frame = this.currentBlock.frame;
                    return;
                }
                this.addSuccessor(0, label);
            }
            this.currentBlock = label;
            if (label.frame == null) {
                label.frame = new Frame();
                label.frame.owner = label;
            }
            if (this.previousBlock != null) {
                if (label.position == this.previousBlock.position) {
                    final Label previousBlock = this.previousBlock;
                    previousBlock.status |= (label.status & 0x10);
                    label.frame = this.previousBlock.frame;
                    this.currentBlock = this.previousBlock;
                    return;
                }
                this.previousBlock.successor = label;
            }
            this.previousBlock = label;
        }
        else if (this.compute == 1) {
            if (this.currentBlock == null) {
                this.currentBlock = label;
            }
            else {
                this.currentBlock.frame.owner = label;
            }
        }
        else if (this.compute == 2) {
            if (this.currentBlock != null) {
                this.currentBlock.outputStackMax = this.maxStackSize;
                this.addSuccessor(this.stackSize, label);
            }
            this.currentBlock = label;
            this.stackSize = 0;
            this.maxStackSize = 0;
            if (this.previousBlock != null) {
                this.previousBlock.successor = label;
            }
            this.previousBlock = label;
        }
    }
    
    @Override
    public void visitLdcInsn(final Object cst) {
        this.lastCodeOffset = this.code.length;
        final Item i = this.cw.newConstItem(cst);
        if (this.currentBlock != null) {
            if (this.compute == 0 || this.compute == 1) {
                this.currentBlock.frame.execute(18, 0, this.cw, i);
            }
            else {
                int size;
                if (i.type == 5 || i.type == 6) {
                    size = this.stackSize + 2;
                }
                else {
                    size = this.stackSize + 1;
                }
                if (size > this.maxStackSize) {
                    this.maxStackSize = size;
                }
                this.stackSize = size;
            }
        }
        final int index = i.index;
        if (i.type == 5 || i.type == 6) {
            this.code.put12(20, index);
        }
        else if (index >= 256) {
            this.code.put12(19, index);
        }
        else {
            this.code.put11(18, index);
        }
    }
    
    @Override
    public void visitIincInsn(final int var, final int increment) {
        this.lastCodeOffset = this.code.length;
        if (this.currentBlock != null && (this.compute == 0 || this.compute == 1)) {
            this.currentBlock.frame.execute(132, var, null, null);
        }
        if (this.compute != 3) {
            final int n = var + 1;
            if (n > this.maxLocals) {
                this.maxLocals = n;
            }
        }
        if (var > 255 || increment > 127 || increment < -128) {
            this.code.putByte(196).put12(132, var).putShort(increment);
        }
        else {
            this.code.putByte(132).put11(var, increment);
        }
    }
    
    @Override
    public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
        this.lastCodeOffset = this.code.length;
        final int source = this.code.length;
        this.code.putByte(170);
        this.code.putByteArray(null, 0, (4 - this.code.length % 4) % 4);
        dflt.put(this, this.code, source, true);
        this.code.putInt(min).putInt(max);
        for (int i = 0; i < labels.length; ++i) {
            labels[i].put(this, this.code, source, true);
        }
        this.visitSwitchInsn(dflt, labels);
    }
    
    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
        this.lastCodeOffset = this.code.length;
        final int source = this.code.length;
        this.code.putByte(171);
        this.code.putByteArray(null, 0, (4 - this.code.length % 4) % 4);
        dflt.put(this, this.code, source, true);
        this.code.putInt(labels.length);
        for (int i = 0; i < labels.length; ++i) {
            this.code.putInt(keys[i]);
            labels[i].put(this, this.code, source, true);
        }
        this.visitSwitchInsn(dflt, labels);
    }
    
    private void visitSwitchInsn(final Label dflt, final Label[] labels) {
        if (this.currentBlock != null) {
            if (this.compute == 0) {
                this.currentBlock.frame.execute(171, 0, null, null);
                this.addSuccessor(0, dflt);
                final Label first = dflt.getFirst();
                first.status |= 0x10;
                for (int i = 0; i < labels.length; ++i) {
                    this.addSuccessor(0, labels[i]);
                    final Label first2 = labels[i].getFirst();
                    first2.status |= 0x10;
                }
            }
            else {
                this.addSuccessor(--this.stackSize, dflt);
                for (int i = 0; i < labels.length; ++i) {
                    this.addSuccessor(this.stackSize, labels[i]);
                }
            }
            this.noSuccessor();
        }
    }
    
    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        this.lastCodeOffset = this.code.length;
        final Item i = this.cw.newClassItem(desc);
        if (this.currentBlock != null) {
            if (this.compute == 0 || this.compute == 1) {
                this.currentBlock.frame.execute(197, dims, this.cw, i);
            }
            else {
                this.stackSize += 1 - dims;
            }
        }
        this.code.put12(197, i.index).putByte(dims);
    }
    
    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        final ByteVector bv = new ByteVector();
        typeRef = ((typeRef & 0xFF0000FF) | this.lastCodeOffset << 8);
        AnnotationWriter.putTarget(typeRef, typePath, bv);
        bv.putShort(this.cw.newUTF8(desc)).putShort(0);
        final AnnotationWriter aw = new AnnotationWriter(this.cw, true, bv, bv, bv.length - 2);
        if (visible) {
            aw.next = this.ctanns;
            this.ctanns = aw;
        }
        else {
            aw.next = this.ictanns;
            this.ictanns = aw;
        }
        return aw;
    }
    
    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        ++this.handlerCount;
        final Handler h = new Handler();
        h.start = start;
        h.end = end;
        h.handler = handler;
        h.desc = type;
        h.type = ((type != null) ? this.cw.newClass(type) : 0);
        if (this.lastHandler == null) {
            this.firstHandler = h;
        }
        else {
            this.lastHandler.next = h;
        }
        this.lastHandler = h;
    }
    
    @Override
    public AnnotationVisitor visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        final ByteVector bv = new ByteVector();
        AnnotationWriter.putTarget(typeRef, typePath, bv);
        bv.putShort(this.cw.newUTF8(desc)).putShort(0);
        final AnnotationWriter aw = new AnnotationWriter(this.cw, true, bv, bv, bv.length - 2);
        if (visible) {
            aw.next = this.ctanns;
            this.ctanns = aw;
        }
        else {
            aw.next = this.ictanns;
            this.ictanns = aw;
        }
        return aw;
    }
    
    @Override
    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {
        if (signature != null) {
            if (this.localVarType == null) {
                this.localVarType = new ByteVector();
            }
            ++this.localVarTypeCount;
            this.localVarType.putShort(start.position).putShort(end.position - start.position).putShort(this.cw.newUTF8(name)).putShort(this.cw.newUTF8(signature)).putShort(index);
        }
        if (this.localVar == null) {
            this.localVar = new ByteVector();
        }
        ++this.localVarCount;
        this.localVar.putShort(start.position).putShort(end.position - start.position).putShort(this.cw.newUTF8(name)).putShort(this.cw.newUTF8(desc)).putShort(index);
        if (this.compute != 3) {
            final char c = desc.charAt(0);
            final int n = index + ((c == 'J' || c == 'D') ? 2 : 1);
            if (n > this.maxLocals) {
                this.maxLocals = n;
            }
        }
    }
    
    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(final int typeRef, final TypePath typePath, final Label[] start, final Label[] end, final int[] index, final String desc, final boolean visible) {
        final ByteVector bv = new ByteVector();
        bv.putByte(typeRef >>> 24).putShort(start.length);
        for (int i = 0; i < start.length; ++i) {
            bv.putShort(start[i].position).putShort(end[i].position - start[i].position).putShort(index[i]);
        }
        if (typePath == null) {
            bv.putByte(0);
        }
        else {
            final int length = typePath.b[typePath.offset] * 2 + 1;
            bv.putByteArray(typePath.b, typePath.offset, length);
        }
        bv.putShort(this.cw.newUTF8(desc)).putShort(0);
        final AnnotationWriter aw = new AnnotationWriter(this.cw, true, bv, bv, bv.length - 2);
        if (visible) {
            aw.next = this.ctanns;
            this.ctanns = aw;
        }
        else {
            aw.next = this.ictanns;
            this.ictanns = aw;
        }
        return aw;
    }
    
    @Override
    public void visitLineNumber(final int line, final Label start) {
        if (this.lineNumber == null) {
            this.lineNumber = new ByteVector();
        }
        ++this.lineNumberCount;
        this.lineNumber.putShort(start.position);
        this.lineNumber.putShort(line);
    }
    
    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        if (this.compute == 0) {
            for (Handler handler = this.firstHandler; handler != null; handler = handler.next) {
                Label l = handler.start.getFirst();
                final Label h = handler.handler.getFirst();
                final Label e = handler.end.getFirst();
                final String t = (handler.desc == null) ? "java/lang/Throwable" : handler.desc;
                final int kind = 0x1700000 | this.cw.addType(t);
                final Label label = h;
                label.status |= 0x10;
                while (l != e) {
                    final Edge b = new Edge();
                    b.info = kind;
                    b.successor = h;
                    b.next = l.successors;
                    l.successors = b;
                    l = l.successor;
                }
            }
            Frame f = this.labels.frame;
            f.initInputFrame(this.cw, this.access, Type.getArgumentTypes(this.descriptor), this.maxLocals);
            this.visitFrame(f);
            int max = 0;
            Label changed = this.labels;
            while (changed != null) {
                final Label i = changed;
                changed = changed.next;
                i.next = null;
                f = i.frame;
                if ((i.status & 0x10) != 0x0) {
                    final Label label2 = i;
                    label2.status |= 0x20;
                }
                final Label label3 = i;
                label3.status |= 0x40;
                final int blockMax = f.inputStack.length + i.outputStackMax;
                if (blockMax > max) {
                    max = blockMax;
                }
                for (Edge e2 = i.successors; e2 != null; e2 = e2.next) {
                    final Label n = e2.successor.getFirst();
                    final boolean change = f.merge(this.cw, n.frame, e2.info);
                    if (change && n.next == null) {
                        n.next = changed;
                        changed = n;
                    }
                }
            }
            for (Label i = this.labels; i != null; i = i.successor) {
                f = i.frame;
                if ((i.status & 0x20) != 0x0) {
                    this.visitFrame(f);
                }
                if ((i.status & 0x40) == 0x0) {
                    final Label k = i.successor;
                    final int start = i.position;
                    final int end = ((k == null) ? this.code.length : k.position) - 1;
                    if (end >= start) {
                        max = Math.max(max, 1);
                        for (int j = start; j < end; ++j) {
                            this.code.data[j] = 0;
                        }
                        this.code.data[end] = -65;
                        final int frameIndex = this.startFrame(start, 0, 1);
                        this.frame[frameIndex] = (0x1700000 | this.cw.addType("java/lang/Throwable"));
                        this.endFrame();
                        this.firstHandler = Handler.remove(this.firstHandler, i, k);
                    }
                }
            }
            Handler handler = this.firstHandler;
            this.handlerCount = 0;
            while (handler != null) {
                ++this.handlerCount;
                handler = handler.next;
            }
            this.maxStack = max;
        }
        else if (this.compute == 2) {
            for (Handler handler = this.firstHandler; handler != null; handler = handler.next) {
                Label l = handler.start;
                final Label h = handler.handler;
                for (Label e = handler.end; l != e; l = l.successor) {
                    final Edge b2 = new Edge();
                    b2.info = Integer.MAX_VALUE;
                    b2.successor = h;
                    if ((l.status & 0x80) == 0x0) {
                        b2.next = l.successors;
                        l.successors = b2;
                    }
                    else {
                        b2.next = l.successors.next.next;
                        l.successors.next.next = b2;
                    }
                }
            }
            if (this.subroutines > 0) {
                int id = 0;
                this.labels.visitSubroutine(null, 1L, this.subroutines);
                for (Label m = this.labels; m != null; m = m.successor) {
                    if ((m.status & 0x80) != 0x0) {
                        final Label subroutine = m.successors.next.successor;
                        if ((subroutine.status & 0x400) == 0x0) {
                            ++id;
                            subroutine.visitSubroutine(null, id / 32L << 32 | 1L << id % 32, this.subroutines);
                        }
                    }
                }
                for (Label m = this.labels; m != null; m = m.successor) {
                    if ((m.status & 0x80) != 0x0) {
                        for (Label L = this.labels; L != null; L = L.successor) {
                            final Label label4 = L;
                            label4.status &= 0xFFFFF7FF;
                        }
                        final Label subroutine2 = m.successors.next.successor;
                        subroutine2.visitSubroutine(m, 0L, this.subroutines);
                    }
                }
            }
            int max2 = 0;
            Label stack = this.labels;
            while (stack != null) {
                Label l2 = stack;
                stack = stack.next;
                final int start2 = l2.inputStackTop;
                final int blockMax = start2 + l2.outputStackMax;
                if (blockMax > max2) {
                    max2 = blockMax;
                }
                Edge b = l2.successors;
                if ((l2.status & 0x80) != 0x0) {
                    b = b.next;
                }
                while (b != null) {
                    l2 = b.successor;
                    if ((l2.status & 0x8) == 0x0) {
                        l2.inputStackTop = ((b.info == Integer.MAX_VALUE) ? 1 : (start2 + b.info));
                        final Label label5 = l2;
                        label5.status |= 0x8;
                        l2.next = stack;
                        stack = l2;
                    }
                    b = b.next;
                }
            }
            this.maxStack = Math.max(maxStack, max2);
        }
        else {
            this.maxStack = maxStack;
            this.maxLocals = maxLocals;
        }
    }
    
    @Override
    public void visitEnd() {
    }
    
    private void addSuccessor(final int info, final Label successor) {
        final Edge b = new Edge();
        b.info = info;
        b.successor = successor;
        b.next = this.currentBlock.successors;
        this.currentBlock.successors = b;
    }
    
    private void noSuccessor() {
        if (this.compute == 0) {
            final Label l = new Label();
            l.frame = new Frame();
            (l.frame.owner = l).resolve(this, this.code.length, this.code.data);
            this.previousBlock.successor = l;
            this.previousBlock = l;
        }
        else {
            this.currentBlock.outputStackMax = this.maxStackSize;
        }
        if (this.compute != 1) {
            this.currentBlock = null;
        }
    }
    
    private void visitFrame(final Frame f) {
        int nTop = 0;
        int nLocal = 0;
        int nStack = 0;
        final int[] locals = f.inputLocals;
        final int[] stacks = f.inputStack;
        for (int i = 0; i < locals.length; ++i) {
            final int t = locals[i];
            if (t == 16777216) {
                ++nTop;
            }
            else {
                nLocal += nTop + 1;
                nTop = 0;
            }
            if (t == 16777220 || t == 16777219) {
                ++i;
            }
        }
        for (int i = 0; i < stacks.length; ++i) {
            final int t = stacks[i];
            ++nStack;
            if (t == 16777220 || t == 16777219) {
                ++i;
            }
        }
        int frameIndex = this.startFrame(f.owner.position, nLocal, nStack);
        int i = 0;
        while (nLocal > 0) {
            final int t = locals[i];
            this.frame[frameIndex++] = t;
            if (t == 16777220 || t == 16777219) {
                ++i;
            }
            ++i;
            --nLocal;
        }
        for (i = 0; i < stacks.length; ++i) {
            final int t = stacks[i];
            this.frame[frameIndex++] = t;
            if (t == 16777220 || t == 16777219) {
                ++i;
            }
        }
        this.endFrame();
    }
    
    private void visitImplicitFirstFrame() {
        int frameIndex = this.startFrame(0, this.descriptor.length() + 1, 0);
        if ((this.access & 0x8) == 0x0) {
            if ((this.access & 0x80000) == 0x0) {
                this.frame[frameIndex++] = (0x1700000 | this.cw.addType(this.cw.thisName));
            }
            else {
                this.frame[frameIndex++] = 6;
            }
        }
        int i = 1;
        while (true) {
            final int j = i;
            switch (this.descriptor.charAt(i++)) {
                case 'B':
                case 'C':
                case 'I':
                case 'S':
                case 'Z': {
                    this.frame[frameIndex++] = 1;
                    continue;
                }
                case 'F': {
                    this.frame[frameIndex++] = 2;
                    continue;
                }
                case 'J': {
                    this.frame[frameIndex++] = 4;
                    continue;
                }
                case 'D': {
                    this.frame[frameIndex++] = 3;
                    continue;
                }
                case '[': {
                    while (this.descriptor.charAt(i) == '[') {
                        ++i;
                    }
                    if (this.descriptor.charAt(i) == 'L') {
                        ++i;
                        while (this.descriptor.charAt(i) != ';') {
                            ++i;
                        }
                    }
                    this.frame[frameIndex++] = (0x1700000 | this.cw.addType(this.descriptor.substring(j, ++i)));
                    continue;
                }
                case 'L': {
                    while (this.descriptor.charAt(i) != ';') {
                        ++i;
                    }
                    this.frame[frameIndex++] = (0x1700000 | this.cw.addType(this.descriptor.substring(j + 1, i++)));
                    continue;
                }
                default: {
                    this.frame[1] = frameIndex - 3;
                    this.endFrame();
                }
            }
        }
    }
    
    private int startFrame(final int offset, final int nLocal, final int nStack) {
        final int n = 3 + nLocal + nStack;
        if (this.frame == null || this.frame.length < n) {
            this.frame = new int[n];
        }
        this.frame[0] = offset;
        this.frame[1] = nLocal;
        this.frame[2] = nStack;
        return 3;
    }
    
    private void endFrame() {
        if (this.previousFrame != null) {
            if (this.stackMap == null) {
                this.stackMap = new ByteVector();
            }
            this.writeFrame();
            ++this.frameCount;
        }
        this.previousFrame = this.frame;
        this.frame = null;
    }
    
    private void writeFrame() {
        final int clocalsSize = this.frame[1];
        final int cstackSize = this.frame[2];
        if ((this.cw.version & 0xFFFF) < 50) {
            this.stackMap.putShort(this.frame[0]).putShort(clocalsSize);
            this.writeFrameTypes(3, 3 + clocalsSize);
            this.stackMap.putShort(cstackSize);
            this.writeFrameTypes(3 + clocalsSize, 3 + clocalsSize + cstackSize);
            return;
        }
        int localsSize = this.previousFrame[1];
        int type = 255;
        int k = 0;
        int delta;
        if (this.frameCount == 0) {
            delta = this.frame[0];
        }
        else {
            delta = this.frame[0] - this.previousFrame[0] - 1;
        }
        if (cstackSize == 0) {
            k = clocalsSize - localsSize;
            switch (k) {
                case -3:
                case -2:
                case -1: {
                    type = 248;
                    localsSize = clocalsSize;
                    break;
                }
                case 0: {
                    type = ((delta < 64) ? 0 : 251);
                    break;
                }
                case 1:
                case 2:
                case 3: {
                    type = 252;
                    break;
                }
            }
        }
        else if (clocalsSize == localsSize && cstackSize == 1) {
            type = ((delta < 63) ? 64 : 247);
        }
        if (type != 255) {
            int l = 3;
            for (int j = 0; j < localsSize; ++j) {
                if (this.frame[l] != this.previousFrame[l]) {
                    type = 255;
                    break;
                }
                ++l;
            }
        }
        switch (type) {
            case 0: {
                this.stackMap.putByte(delta);
                break;
            }
            case 64: {
                this.stackMap.putByte(64 + delta);
                this.writeFrameTypes(3 + clocalsSize, 4 + clocalsSize);
                break;
            }
            case 247: {
                this.stackMap.putByte(247).putShort(delta);
                this.writeFrameTypes(3 + clocalsSize, 4 + clocalsSize);
                break;
            }
            case 251: {
                this.stackMap.putByte(251).putShort(delta);
                break;
            }
            case 248: {
                this.stackMap.putByte(251 + k).putShort(delta);
                break;
            }
            case 252: {
                this.stackMap.putByte(251 + k).putShort(delta);
                this.writeFrameTypes(3 + localsSize, 3 + clocalsSize);
                break;
            }
            default: {
                this.stackMap.putByte(255).putShort(delta).putShort(clocalsSize);
                this.writeFrameTypes(3, 3 + clocalsSize);
                this.stackMap.putShort(cstackSize);
                this.writeFrameTypes(3 + clocalsSize, 3 + clocalsSize + cstackSize);
                break;
            }
        }
    }
    
    private void writeFrameTypes(final int start, final int end) {
        for (int i = start; i < end; ++i) {
            final int t = this.frame[i];
            int d = t & 0xF0000000;
            if (d == 0) {
                final int v = t & 0xFFFFF;
                switch (t & 0xFF00000) {
                    case 24117248: {
                        this.stackMap.putByte(7).putShort(this.cw.newClass(this.cw.typeTable[v].strVal1));
                        break;
                    }
                    case 25165824: {
                        this.stackMap.putByte(8).putShort(this.cw.typeTable[v].intVal);
                        break;
                    }
                    default: {
                        this.stackMap.putByte(v);
                        break;
                    }
                }
            }
            else {
                final StringBuilder sb = new StringBuilder();
                d >>= 28;
                while (d-- > 0) {
                    sb.append('[');
                }
                if ((t & 0xFF00000) == 0x1700000) {
                    sb.append('L');
                    sb.append(this.cw.typeTable[t & 0xFFFFF].strVal1);
                    sb.append(';');
                }
                else {
                    switch (t & 0xF) {
                        case 1: {
                            sb.append('I');
                            break;
                        }
                        case 2: {
                            sb.append('F');
                            break;
                        }
                        case 3: {
                            sb.append('D');
                            break;
                        }
                        case 9: {
                            sb.append('Z');
                            break;
                        }
                        case 10: {
                            sb.append('B');
                            break;
                        }
                        case 11: {
                            sb.append('C');
                            break;
                        }
                        case 12: {
                            sb.append('S');
                            break;
                        }
                        default: {
                            sb.append('J');
                            break;
                        }
                    }
                }
                this.stackMap.putByte(7).putShort(this.cw.newClass(sb.toString()));
            }
        }
    }
    
    private void writeFrameType(final Object type) {
        if (type instanceof String) {
            this.stackMap.putByte(7).putShort(this.cw.newClass((String)type));
        }
        else if (type instanceof Integer) {
            this.stackMap.putByte((int)type);
        }
        else {
            this.stackMap.putByte(8).putShort(((Label)type).position);
        }
    }
    
    final int getSize() {
        if (this.classReaderOffset != 0) {
            return 6 + this.classReaderLength;
        }
        int size = 8;
        if (this.code.length > 0) {
            if (this.code.length > 65535) {
                throw new RuntimeException("Method code too large!");
            }
            this.cw.newUTF8("Code");
            size += 18 + this.code.length + 8 * this.handlerCount;
            if (this.localVar != null) {
                this.cw.newUTF8("LocalVariableTable");
                size += 8 + this.localVar.length;
            }
            if (this.localVarType != null) {
                this.cw.newUTF8("LocalVariableTypeTable");
                size += 8 + this.localVarType.length;
            }
            if (this.lineNumber != null) {
                this.cw.newUTF8("LineNumberTable");
                size += 8 + this.lineNumber.length;
            }
            if (this.stackMap != null) {
                final boolean zip = (this.cw.version & 0xFFFF) >= 50;
                this.cw.newUTF8(zip ? "StackMapTable" : "StackMap");
                size += 8 + this.stackMap.length;
            }
            if (this.ctanns != null) {
                this.cw.newUTF8("RuntimeVisibleTypeAnnotations");
                size += 8 + this.ctanns.getSize();
            }
            if (this.ictanns != null) {
                this.cw.newUTF8("RuntimeInvisibleTypeAnnotations");
                size += 8 + this.ictanns.getSize();
            }
            if (this.cattrs != null) {
                size += this.cattrs.getSize(this.cw, this.code.data, this.code.length, this.maxStack, this.maxLocals);
            }
        }
        if (this.exceptionCount > 0) {
            this.cw.newUTF8("Exceptions");
            size += 8 + 2 * this.exceptionCount;
        }
        if ((this.access & 0x1000) != 0x0 && ((this.cw.version & 0xFFFF) < 49 || (this.access & 0x40000) != 0x0)) {
            this.cw.newUTF8("Synthetic");
            size += 6;
        }
        if ((this.access & 0x20000) != 0x0) {
            this.cw.newUTF8("Deprecated");
            size += 6;
        }
        if (this.signature != null) {
            this.cw.newUTF8("Signature");
            this.cw.newUTF8(this.signature);
            size += 8;
        }
        if (this.methodParameters != null) {
            this.cw.newUTF8("MethodParameters");
            size += 7 + this.methodParameters.length;
        }
        if (this.annd != null) {
            this.cw.newUTF8("AnnotationDefault");
            size += 6 + this.annd.length;
        }
        if (this.anns != null) {
            this.cw.newUTF8("RuntimeVisibleAnnotations");
            size += 8 + this.anns.getSize();
        }
        if (this.ianns != null) {
            this.cw.newUTF8("RuntimeInvisibleAnnotations");
            size += 8 + this.ianns.getSize();
        }
        if (this.tanns != null) {
            this.cw.newUTF8("RuntimeVisibleTypeAnnotations");
            size += 8 + this.tanns.getSize();
        }
        if (this.itanns != null) {
            this.cw.newUTF8("RuntimeInvisibleTypeAnnotations");
            size += 8 + this.itanns.getSize();
        }
        if (this.panns != null) {
            this.cw.newUTF8("RuntimeVisibleParameterAnnotations");
            size += 7 + 2 * (this.panns.length - this.synthetics);
            for (int i = this.panns.length - 1; i >= this.synthetics; --i) {
                size += ((this.panns[i] == null) ? 0 : this.panns[i].getSize());
            }
        }
        if (this.ipanns != null) {
            this.cw.newUTF8("RuntimeInvisibleParameterAnnotations");
            size += 7 + 2 * (this.ipanns.length - this.synthetics);
            for (int i = this.ipanns.length - 1; i >= this.synthetics; --i) {
                size += ((this.ipanns[i] == null) ? 0 : this.ipanns[i].getSize());
            }
        }
        if (this.attrs != null) {
            size += this.attrs.getSize(this.cw, null, 0, -1, -1);
        }
        return size;
    }
    
    final void put(final ByteVector out) {
        final int FACTOR = 64;
        final int mask = 0xE0000 | (this.access & 0x40000) / 64;
        out.putShort(this.access & ~mask).putShort(this.name).putShort(this.desc);
        if (this.classReaderOffset != 0) {
            out.putByteArray(this.cw.cr.b, this.classReaderOffset, this.classReaderLength);
            return;
        }
        int attributeCount = 0;
        if (this.code.length > 0) {
            ++attributeCount;
        }
        if (this.exceptionCount > 0) {
            ++attributeCount;
        }
        if ((this.access & 0x1000) != 0x0 && ((this.cw.version & 0xFFFF) < 49 || (this.access & 0x40000) != 0x0)) {
            ++attributeCount;
        }
        if ((this.access & 0x20000) != 0x0) {
            ++attributeCount;
        }
        if (this.signature != null) {
            ++attributeCount;
        }
        if (this.methodParameters != null) {
            ++attributeCount;
        }
        if (this.annd != null) {
            ++attributeCount;
        }
        if (this.anns != null) {
            ++attributeCount;
        }
        if (this.ianns != null) {
            ++attributeCount;
        }
        if (this.tanns != null) {
            ++attributeCount;
        }
        if (this.itanns != null) {
            ++attributeCount;
        }
        if (this.panns != null) {
            ++attributeCount;
        }
        if (this.ipanns != null) {
            ++attributeCount;
        }
        if (this.attrs != null) {
            attributeCount += this.attrs.getCount();
        }
        out.putShort(attributeCount);
        if (this.code.length > 0) {
            int size = 12 + this.code.length + 8 * this.handlerCount;
            if (this.localVar != null) {
                size += 8 + this.localVar.length;
            }
            if (this.localVarType != null) {
                size += 8 + this.localVarType.length;
            }
            if (this.lineNumber != null) {
                size += 8 + this.lineNumber.length;
            }
            if (this.stackMap != null) {
                size += 8 + this.stackMap.length;
            }
            if (this.ctanns != null) {
                size += 8 + this.ctanns.getSize();
            }
            if (this.ictanns != null) {
                size += 8 + this.ictanns.getSize();
            }
            if (this.cattrs != null) {
                size += this.cattrs.getSize(this.cw, this.code.data, this.code.length, this.maxStack, this.maxLocals);
            }
            out.putShort(this.cw.newUTF8("Code")).putInt(size);
            out.putShort(this.maxStack).putShort(this.maxLocals);
            out.putInt(this.code.length).putByteArray(this.code.data, 0, this.code.length);
            out.putShort(this.handlerCount);
            if (this.handlerCount > 0) {
                for (Handler h = this.firstHandler; h != null; h = h.next) {
                    out.putShort(h.start.position).putShort(h.end.position).putShort(h.handler.position).putShort(h.type);
                }
            }
            attributeCount = 0;
            if (this.localVar != null) {
                ++attributeCount;
            }
            if (this.localVarType != null) {
                ++attributeCount;
            }
            if (this.lineNumber != null) {
                ++attributeCount;
            }
            if (this.stackMap != null) {
                ++attributeCount;
            }
            if (this.ctanns != null) {
                ++attributeCount;
            }
            if (this.ictanns != null) {
                ++attributeCount;
            }
            if (this.cattrs != null) {
                attributeCount += this.cattrs.getCount();
            }
            out.putShort(attributeCount);
            if (this.localVar != null) {
                out.putShort(this.cw.newUTF8("LocalVariableTable"));
                out.putInt(this.localVar.length + 2).putShort(this.localVarCount);
                out.putByteArray(this.localVar.data, 0, this.localVar.length);
            }
            if (this.localVarType != null) {
                out.putShort(this.cw.newUTF8("LocalVariableTypeTable"));
                out.putInt(this.localVarType.length + 2).putShort(this.localVarTypeCount);
                out.putByteArray(this.localVarType.data, 0, this.localVarType.length);
            }
            if (this.lineNumber != null) {
                out.putShort(this.cw.newUTF8("LineNumberTable"));
                out.putInt(this.lineNumber.length + 2).putShort(this.lineNumberCount);
                out.putByteArray(this.lineNumber.data, 0, this.lineNumber.length);
            }
            if (this.stackMap != null) {
                final boolean zip = (this.cw.version & 0xFFFF) >= 50;
                out.putShort(this.cw.newUTF8(zip ? "StackMapTable" : "StackMap"));
                out.putInt(this.stackMap.length + 2).putShort(this.frameCount);
                out.putByteArray(this.stackMap.data, 0, this.stackMap.length);
            }
            if (this.ctanns != null) {
                out.putShort(this.cw.newUTF8("RuntimeVisibleTypeAnnotations"));
                this.ctanns.put(out);
            }
            if (this.ictanns != null) {
                out.putShort(this.cw.newUTF8("RuntimeInvisibleTypeAnnotations"));
                this.ictanns.put(out);
            }
            if (this.cattrs != null) {
                this.cattrs.put(this.cw, this.code.data, this.code.length, this.maxLocals, this.maxStack, out);
            }
        }
        if (this.exceptionCount > 0) {
            out.putShort(this.cw.newUTF8("Exceptions")).putInt(2 * this.exceptionCount + 2);
            out.putShort(this.exceptionCount);
            for (int i = 0; i < this.exceptionCount; ++i) {
                out.putShort(this.exceptions[i]);
            }
        }
        if ((this.access & 0x1000) != 0x0 && ((this.cw.version & 0xFFFF) < 49 || (this.access & 0x40000) != 0x0)) {
            out.putShort(this.cw.newUTF8("Synthetic")).putInt(0);
        }
        if ((this.access & 0x20000) != 0x0) {
            out.putShort(this.cw.newUTF8("Deprecated")).putInt(0);
        }
        if (this.signature != null) {
            out.putShort(this.cw.newUTF8("Signature")).putInt(2).putShort(this.cw.newUTF8(this.signature));
        }
        if (this.methodParameters != null) {
            out.putShort(this.cw.newUTF8("MethodParameters"));
            out.putInt(this.methodParameters.length + 1).putByte(this.methodParametersCount);
            out.putByteArray(this.methodParameters.data, 0, this.methodParameters.length);
        }
        if (this.annd != null) {
            out.putShort(this.cw.newUTF8("AnnotationDefault"));
            out.putInt(this.annd.length);
            out.putByteArray(this.annd.data, 0, this.annd.length);
        }
        if (this.anns != null) {
            out.putShort(this.cw.newUTF8("RuntimeVisibleAnnotations"));
            this.anns.put(out);
        }
        if (this.ianns != null) {
            out.putShort(this.cw.newUTF8("RuntimeInvisibleAnnotations"));
            this.ianns.put(out);
        }
        if (this.tanns != null) {
            out.putShort(this.cw.newUTF8("RuntimeVisibleTypeAnnotations"));
            this.tanns.put(out);
        }
        if (this.itanns != null) {
            out.putShort(this.cw.newUTF8("RuntimeInvisibleTypeAnnotations"));
            this.itanns.put(out);
        }
        if (this.panns != null) {
            out.putShort(this.cw.newUTF8("RuntimeVisibleParameterAnnotations"));
            AnnotationWriter.put(this.panns, this.synthetics, out);
        }
        if (this.ipanns != null) {
            out.putShort(this.cw.newUTF8("RuntimeInvisibleParameterAnnotations"));
            AnnotationWriter.put(this.ipanns, this.synthetics, out);
        }
        if (this.attrs != null) {
            this.attrs.put(this.cw, null, 0, -1, -1, out);
        }
    }
}
