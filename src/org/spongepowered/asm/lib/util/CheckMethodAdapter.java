// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.util;

import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.Opcodes;
import java.util.Iterator;
import org.spongepowered.asm.lib.Handle;
import org.spongepowered.asm.lib.Attribute;
import org.spongepowered.asm.lib.TypePath;
import org.spongepowered.asm.lib.AnnotationVisitor;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.spongepowered.asm.lib.tree.analysis.Interpreter;
import org.spongepowered.asm.lib.tree.analysis.BasicValue;
import org.spongepowered.asm.lib.tree.analysis.Analyzer;
import org.spongepowered.asm.lib.tree.analysis.BasicVerifier;
import org.spongepowered.asm.lib.tree.MethodNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import org.spongepowered.asm.lib.Label;
import java.util.Map;
import org.spongepowered.asm.lib.MethodVisitor;

public class CheckMethodAdapter extends MethodVisitor
{
    public int version;
    private int access;
    private boolean startCode;
    private boolean endCode;
    private boolean endMethod;
    private int insnCount;
    private final Map<Label, Integer> labels;
    private Set<Label> usedLabels;
    private int expandedFrames;
    private int compressedFrames;
    private int lastFrame;
    private List<Label> handlers;
    private static final int[] TYPE;
    private static Field labelStatusField;
    
    public CheckMethodAdapter(final MethodVisitor mv) {
        this(mv, new HashMap<Label, Integer>());
    }
    
    public CheckMethodAdapter(final MethodVisitor mv, final Map<Label, Integer> labels) {
        this(327680, mv, labels);
        if (this.getClass() != CheckMethodAdapter.class) {
            throw new IllegalStateException();
        }
    }
    
    protected CheckMethodAdapter(final int api, final MethodVisitor mv, final Map<Label, Integer> labels) {
        super(api, mv);
        this.lastFrame = -1;
        this.labels = labels;
        this.usedLabels = new HashSet<Label>();
        this.handlers = new ArrayList<Label>();
    }
    
    public CheckMethodAdapter(final int access, final String name, final String desc, final MethodVisitor cmv, final Map<Label, Integer> labels) {
        this(new MethodNode(327680, access, name, desc, null, null) {
            @Override
            public void visitEnd() {
                final Analyzer<BasicValue> a = new Analyzer<BasicValue>(new BasicVerifier());
                try {
                    a.analyze("dummy", this);
                }
                catch (Exception e) {
                    if (e instanceof IndexOutOfBoundsException && this.maxLocals == 0 && this.maxStack == 0) {
                        throw new RuntimeException("Data flow checking option requires valid, non zero maxLocals and maxStack values.");
                    }
                    e.printStackTrace();
                    final StringWriter sw = new StringWriter();
                    final PrintWriter pw = new PrintWriter(sw, true);
                    CheckClassAdapter.printAnalyzerResult(this, a, pw);
                    pw.close();
                    throw new RuntimeException(e.getMessage() + ' ' + sw.toString());
                }
                this.accept(cmv);
            }
        }, labels);
        this.access = access;
    }
    
    @Override
    public void visitParameter(final String name, final int access) {
        if (name != null) {
            checkUnqualifiedName(this.version, name, "name");
        }
        CheckClassAdapter.checkAccess(access, 36880);
        super.visitParameter(name, access);
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        this.checkEndMethod();
        checkDesc(desc, false);
        return new CheckAnnotationAdapter(super.visitAnnotation(desc, visible));
    }
    
    @Override
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        this.checkEndMethod();
        final int sort = typeRef >>> 24;
        if (sort != 1 && sort != 18 && sort != 20 && sort != 21 && sort != 22 && sort != 23) {
            throw new IllegalArgumentException("Invalid type reference sort 0x" + Integer.toHexString(sort));
        }
        CheckClassAdapter.checkTypeRefAndPath(typeRef, typePath);
        checkDesc(desc, false);
        return new CheckAnnotationAdapter(super.visitTypeAnnotation(typeRef, typePath, desc, visible));
    }
    
    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        this.checkEndMethod();
        return new CheckAnnotationAdapter(super.visitAnnotationDefault(), false);
    }
    
    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
        this.checkEndMethod();
        checkDesc(desc, false);
        return new CheckAnnotationAdapter(super.visitParameterAnnotation(parameter, desc, visible));
    }
    
    @Override
    public void visitAttribute(final Attribute attr) {
        this.checkEndMethod();
        if (attr == null) {
            throw new IllegalArgumentException("Invalid attribute (must not be null)");
        }
        super.visitAttribute(attr);
    }
    
    @Override
    public void visitCode() {
        if ((this.access & 0x400) != 0x0) {
            throw new RuntimeException("Abstract methods cannot have code");
        }
        this.startCode = true;
        super.visitCode();
    }
    
    @Override
    public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
        if (this.insnCount == this.lastFrame) {
            throw new IllegalStateException("At most one frame can be visited at a given code location.");
        }
        this.lastFrame = this.insnCount;
        int mLocal = 0;
        int mStack = 0;
        switch (type) {
            case -1:
            case 0: {
                mLocal = Integer.MAX_VALUE;
                mStack = Integer.MAX_VALUE;
                break;
            }
            case 3: {
                mLocal = 0;
                mStack = 0;
                break;
            }
            case 4: {
                mLocal = 0;
                mStack = 1;
                break;
            }
            case 1:
            case 2: {
                mLocal = 3;
                mStack = 0;
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid frame type " + type);
            }
        }
        if (nLocal > mLocal) {
            throw new IllegalArgumentException("Invalid nLocal=" + nLocal + " for frame type " + type);
        }
        if (nStack > mStack) {
            throw new IllegalArgumentException("Invalid nStack=" + nStack + " for frame type " + type);
        }
        if (type != 2) {
            if (nLocal > 0 && (local == null || local.length < nLocal)) {
                throw new IllegalArgumentException("Array local[] is shorter than nLocal");
            }
            for (int i = 0; i < nLocal; ++i) {
                this.checkFrameValue(local[i]);
            }
        }
        if (nStack > 0 && (stack == null || stack.length < nStack)) {
            throw new IllegalArgumentException("Array stack[] is shorter than nStack");
        }
        for (int i = 0; i < nStack; ++i) {
            this.checkFrameValue(stack[i]);
        }
        if (type == -1) {
            ++this.expandedFrames;
        }
        else {
            ++this.compressedFrames;
        }
        if (this.expandedFrames > 0 && this.compressedFrames > 0) {
            throw new RuntimeException("Expanded and compressed frames must not be mixed.");
        }
        super.visitFrame(type, nLocal, local, nStack, stack);
    }
    
    @Override
    public void visitInsn(final int opcode) {
        this.checkStartCode();
        this.checkEndCode();
        checkOpcode(opcode, 0);
        super.visitInsn(opcode);
        ++this.insnCount;
    }
    
    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        this.checkStartCode();
        this.checkEndCode();
        checkOpcode(opcode, 1);
        switch (opcode) {
            case 16: {
                checkSignedByte(operand, "Invalid operand");
                break;
            }
            case 17: {
                checkSignedShort(operand, "Invalid operand");
                break;
            }
            default: {
                if (operand < 4 || operand > 11) {
                    throw new IllegalArgumentException("Invalid operand (must be an array type code T_...): " + operand);
                }
                break;
            }
        }
        super.visitIntInsn(opcode, operand);
        ++this.insnCount;
    }
    
    @Override
    public void visitVarInsn(final int opcode, final int var) {
        this.checkStartCode();
        this.checkEndCode();
        checkOpcode(opcode, 2);
        checkUnsignedShort(var, "Invalid variable index");
        super.visitVarInsn(opcode, var);
        ++this.insnCount;
    }
    
    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        this.checkStartCode();
        this.checkEndCode();
        checkOpcode(opcode, 3);
        checkInternalName(type, "type");
        if (opcode == 187 && type.charAt(0) == '[') {
            throw new IllegalArgumentException("NEW cannot be used to create arrays: " + type);
        }
        super.visitTypeInsn(opcode, type);
        ++this.insnCount;
    }
    
    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        this.checkStartCode();
        this.checkEndCode();
        checkOpcode(opcode, 4);
        checkInternalName(owner, "owner");
        checkUnqualifiedName(this.version, name, "name");
        checkDesc(desc, false);
        super.visitFieldInsn(opcode, owner, name, desc);
        ++this.insnCount;
    }
    
    @Deprecated
    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
        if (this.api >= 327680) {
            super.visitMethodInsn(opcode, owner, name, desc);
            return;
        }
        this.doVisitMethodInsn(opcode, owner, name, desc, opcode == 185);
    }
    
    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean itf) {
        if (this.api < 327680) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            return;
        }
        this.doVisitMethodInsn(opcode, owner, name, desc, itf);
    }
    
    private void doVisitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean itf) {
        this.checkStartCode();
        this.checkEndCode();
        checkOpcode(opcode, 5);
        if (opcode != 183 || !"<init>".equals(name)) {
            checkMethodIdentifier(this.version, name, "name");
        }
        checkInternalName(owner, "owner");
        checkMethodDesc(desc);
        if (opcode == 182 && itf) {
            throw new IllegalArgumentException("INVOKEVIRTUAL can't be used with interfaces");
        }
        if (opcode == 185 && !itf) {
            throw new IllegalArgumentException("INVOKEINTERFACE can't be used with classes");
        }
        if (opcode == 183 && itf && (this.version & 0xFFFF) < 52) {
            throw new IllegalArgumentException("INVOKESPECIAL can't be used with interfaces prior to Java 8");
        }
        if (this.mv != null) {
            this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
        ++this.insnCount;
    }
    
    @Override
    public void visitInvokeDynamicInsn(final String name, final String desc, final Handle bsm, final Object... bsmArgs) {
        this.checkStartCode();
        this.checkEndCode();
        checkMethodIdentifier(this.version, name, "name");
        checkMethodDesc(desc);
        if (bsm.getTag() != 6 && bsm.getTag() != 8) {
            throw new IllegalArgumentException("invalid handle tag " + bsm.getTag());
        }
        for (int i = 0; i < bsmArgs.length; ++i) {
            this.checkLDCConstant(bsmArgs[i]);
        }
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        ++this.insnCount;
    }
    
    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        this.checkStartCode();
        this.checkEndCode();
        checkOpcode(opcode, 6);
        this.checkLabel(label, false, "label");
        checkNonDebugLabel(label);
        super.visitJumpInsn(opcode, label);
        this.usedLabels.add(label);
        ++this.insnCount;
    }
    
    @Override
    public void visitLabel(final Label label) {
        this.checkStartCode();
        this.checkEndCode();
        this.checkLabel(label, false, "label");
        if (this.labels.get(label) != null) {
            throw new IllegalArgumentException("Already visited label");
        }
        this.labels.put(label, this.insnCount);
        super.visitLabel(label);
    }
    
    @Override
    public void visitLdcInsn(final Object cst) {
        this.checkStartCode();
        this.checkEndCode();
        this.checkLDCConstant(cst);
        super.visitLdcInsn(cst);
        ++this.insnCount;
    }
    
    @Override
    public void visitIincInsn(final int var, final int increment) {
        this.checkStartCode();
        this.checkEndCode();
        checkUnsignedShort(var, "Invalid variable index");
        checkSignedShort(increment, "Invalid increment");
        super.visitIincInsn(var, increment);
        ++this.insnCount;
    }
    
    @Override
    public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
        this.checkStartCode();
        this.checkEndCode();
        if (max < min) {
            throw new IllegalArgumentException("Max = " + max + " must be greater than or equal to min = " + min);
        }
        this.checkLabel(dflt, false, "default label");
        checkNonDebugLabel(dflt);
        if (labels == null || labels.length != max - min + 1) {
            throw new IllegalArgumentException("There must be max - min + 1 labels");
        }
        for (int i = 0; i < labels.length; ++i) {
            this.checkLabel(labels[i], false, "label at index " + i);
            checkNonDebugLabel(labels[i]);
        }
        super.visitTableSwitchInsn(min, max, dflt, labels);
        for (int i = 0; i < labels.length; ++i) {
            this.usedLabels.add(labels[i]);
        }
        ++this.insnCount;
    }
    
    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
        this.checkEndCode();
        this.checkStartCode();
        this.checkLabel(dflt, false, "default label");
        checkNonDebugLabel(dflt);
        if (keys == null || labels == null || keys.length != labels.length) {
            throw new IllegalArgumentException("There must be the same number of keys and labels");
        }
        for (int i = 0; i < labels.length; ++i) {
            this.checkLabel(labels[i], false, "label at index " + i);
            checkNonDebugLabel(labels[i]);
        }
        super.visitLookupSwitchInsn(dflt, keys, labels);
        this.usedLabels.add(dflt);
        for (int i = 0; i < labels.length; ++i) {
            this.usedLabels.add(labels[i]);
        }
        ++this.insnCount;
    }
    
    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        this.checkStartCode();
        this.checkEndCode();
        checkDesc(desc, false);
        if (desc.charAt(0) != '[') {
            throw new IllegalArgumentException("Invalid descriptor (must be an array type descriptor): " + desc);
        }
        if (dims < 1) {
            throw new IllegalArgumentException("Invalid dimensions (must be greater than 0): " + dims);
        }
        if (dims > desc.lastIndexOf(91) + 1) {
            throw new IllegalArgumentException("Invalid dimensions (must not be greater than dims(desc)): " + dims);
        }
        super.visitMultiANewArrayInsn(desc, dims);
        ++this.insnCount;
    }
    
    @Override
    public AnnotationVisitor visitInsnAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        this.checkStartCode();
        this.checkEndCode();
        final int sort = typeRef >>> 24;
        if (sort != 67 && sort != 68 && sort != 69 && sort != 70 && sort != 71 && sort != 72 && sort != 73 && sort != 74 && sort != 75) {
            throw new IllegalArgumentException("Invalid type reference sort 0x" + Integer.toHexString(sort));
        }
        CheckClassAdapter.checkTypeRefAndPath(typeRef, typePath);
        checkDesc(desc, false);
        return new CheckAnnotationAdapter(super.visitInsnAnnotation(typeRef, typePath, desc, visible));
    }
    
    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        this.checkStartCode();
        this.checkEndCode();
        this.checkLabel(start, false, "start label");
        this.checkLabel(end, false, "end label");
        this.checkLabel(handler, false, "handler label");
        checkNonDebugLabel(start);
        checkNonDebugLabel(end);
        checkNonDebugLabel(handler);
        if (this.labels.get(start) != null || this.labels.get(end) != null || this.labels.get(handler) != null) {
            throw new IllegalStateException("Try catch blocks must be visited before their labels");
        }
        if (type != null) {
            checkInternalName(type, "type");
        }
        super.visitTryCatchBlock(start, end, handler, type);
        this.handlers.add(start);
        this.handlers.add(end);
    }
    
    @Override
    public AnnotationVisitor visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        this.checkStartCode();
        this.checkEndCode();
        final int sort = typeRef >>> 24;
        if (sort != 66) {
            throw new IllegalArgumentException("Invalid type reference sort 0x" + Integer.toHexString(sort));
        }
        CheckClassAdapter.checkTypeRefAndPath(typeRef, typePath);
        checkDesc(desc, false);
        return new CheckAnnotationAdapter(super.visitTryCatchAnnotation(typeRef, typePath, desc, visible));
    }
    
    @Override
    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {
        this.checkStartCode();
        this.checkEndCode();
        checkUnqualifiedName(this.version, name, "name");
        checkDesc(desc, false);
        this.checkLabel(start, true, "start label");
        this.checkLabel(end, true, "end label");
        checkUnsignedShort(index, "Invalid variable index");
        final int s = this.labels.get(start);
        final int e = this.labels.get(end);
        if (e < s) {
            throw new IllegalArgumentException("Invalid start and end labels (end must be greater than start)");
        }
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }
    
    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(final int typeRef, final TypePath typePath, final Label[] start, final Label[] end, final int[] index, final String desc, final boolean visible) {
        this.checkStartCode();
        this.checkEndCode();
        final int sort = typeRef >>> 24;
        if (sort != 64 && sort != 65) {
            throw new IllegalArgumentException("Invalid type reference sort 0x" + Integer.toHexString(sort));
        }
        CheckClassAdapter.checkTypeRefAndPath(typeRef, typePath);
        checkDesc(desc, false);
        if (start == null || end == null || index == null || end.length != start.length || index.length != start.length) {
            throw new IllegalArgumentException("Invalid start, end and index arrays (must be non null and of identical length");
        }
        for (int i = 0; i < start.length; ++i) {
            this.checkLabel(start[i], true, "start label");
            this.checkLabel(end[i], true, "end label");
            checkUnsignedShort(index[i], "Invalid variable index");
            final int s = this.labels.get(start[i]);
            final int e = this.labels.get(end[i]);
            if (e < s) {
                throw new IllegalArgumentException("Invalid start and end labels (end must be greater than start)");
            }
        }
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
    }
    
    @Override
    public void visitLineNumber(final int line, final Label start) {
        this.checkStartCode();
        this.checkEndCode();
        checkUnsignedShort(line, "Invalid line number");
        this.checkLabel(start, true, "start label");
        super.visitLineNumber(line, start);
    }
    
    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        this.checkStartCode();
        this.checkEndCode();
        this.endCode = true;
        for (final Label l : this.usedLabels) {
            if (this.labels.get(l) == null) {
                throw new IllegalStateException("Undefined label used");
            }
        }
        int i = 0;
        while (i < this.handlers.size()) {
            final Integer start = this.labels.get(this.handlers.get(i++));
            final Integer end = this.labels.get(this.handlers.get(i++));
            if (start == null || end == null) {
                throw new IllegalStateException("Undefined try catch block labels");
            }
            if (end <= start) {
                throw new IllegalStateException("Emty try catch block handler range");
            }
        }
        checkUnsignedShort(maxStack, "Invalid max stack");
        checkUnsignedShort(maxLocals, "Invalid max locals");
        super.visitMaxs(maxStack, maxLocals);
    }
    
    @Override
    public void visitEnd() {
        this.checkEndMethod();
        this.endMethod = true;
        super.visitEnd();
    }
    
    void checkStartCode() {
        if (!this.startCode) {
            throw new IllegalStateException("Cannot visit instructions before visitCode has been called.");
        }
    }
    
    void checkEndCode() {
        if (this.endCode) {
            throw new IllegalStateException("Cannot visit instructions after visitMaxs has been called.");
        }
    }
    
    void checkEndMethod() {
        if (this.endMethod) {
            throw new IllegalStateException("Cannot visit elements after visitEnd has been called.");
        }
    }
    
    void checkFrameValue(final Object value) {
        if (value == Opcodes.TOP || value == Opcodes.INTEGER || value == Opcodes.FLOAT || value == Opcodes.LONG || value == Opcodes.DOUBLE || value == Opcodes.NULL || value == Opcodes.UNINITIALIZED_THIS) {
            return;
        }
        if (value instanceof String) {
            checkInternalName((String)value, "Invalid stack frame value");
            return;
        }
        if (!(value instanceof Label)) {
            throw new IllegalArgumentException("Invalid stack frame value: " + value);
        }
        this.usedLabels.add((Label)value);
    }
    
    static void checkOpcode(final int opcode, final int type) {
        if (opcode < 0 || opcode > 199 || CheckMethodAdapter.TYPE[opcode] != type) {
            throw new IllegalArgumentException("Invalid opcode: " + opcode);
        }
    }
    
    static void checkSignedByte(final int value, final String msg) {
        if (value < -128 || value > 127) {
            throw new IllegalArgumentException(msg + " (must be a signed byte): " + value);
        }
    }
    
    static void checkSignedShort(final int value, final String msg) {
        if (value < -32768 || value > 32767) {
            throw new IllegalArgumentException(msg + " (must be a signed short): " + value);
        }
    }
    
    static void checkUnsignedShort(final int value, final String msg) {
        if (value < 0 || value > 65535) {
            throw new IllegalArgumentException(msg + " (must be an unsigned short): " + value);
        }
    }
    
    static void checkConstant(final Object cst) {
        if (!(cst instanceof Integer) && !(cst instanceof Float) && !(cst instanceof Long) && !(cst instanceof Double) && !(cst instanceof String)) {
            throw new IllegalArgumentException("Invalid constant: " + cst);
        }
    }
    
    void checkLDCConstant(final Object cst) {
        if (cst instanceof Type) {
            final int s = ((Type)cst).getSort();
            if (s != 10 && s != 9 && s != 11) {
                throw new IllegalArgumentException("Illegal LDC constant value");
            }
            if (s != 11 && (this.version & 0xFFFF) < 49) {
                throw new IllegalArgumentException("ldc of a constant class requires at least version 1.5");
            }
            if (s == 11 && (this.version & 0xFFFF) < 51) {
                throw new IllegalArgumentException("ldc of a method type requires at least version 1.7");
            }
        }
        else if (cst instanceof Handle) {
            if ((this.version & 0xFFFF) < 51) {
                throw new IllegalArgumentException("ldc of a handle requires at least version 1.7");
            }
            final int tag = ((Handle)cst).getTag();
            if (tag < 1 || tag > 9) {
                throw new IllegalArgumentException("invalid handle tag " + tag);
            }
        }
        else {
            checkConstant(cst);
        }
    }
    
    static void checkUnqualifiedName(final int version, final String name, final String msg) {
        if ((version & 0xFFFF) < 49) {
            checkIdentifier(name, msg);
        }
        else {
            for (int i = 0; i < name.length(); ++i) {
                if (".;[/".indexOf(name.charAt(i)) != -1) {
                    throw new IllegalArgumentException("Invalid " + msg + " (must be a valid unqualified name): " + name);
                }
            }
        }
    }
    
    static void checkIdentifier(final String name, final String msg) {
        checkIdentifier(name, 0, -1, msg);
    }
    
    static void checkIdentifier(final String name, final int start, final int end, final String msg) {
        if (name != null) {
            if (end == -1) {
                if (name.length() <= start) {
                    throw new IllegalArgumentException("Invalid " + msg + " (must not be null or empty)");
                }
            }
            else if (end <= start) {
                throw new IllegalArgumentException("Invalid " + msg + " (must not be null or empty)");
            }
            if (!Character.isJavaIdentifierStart(name.charAt(start))) {
                throw new IllegalArgumentException("Invalid " + msg + " (must be a valid Java identifier): " + name);
            }
            for (int max = (end == -1) ? name.length() : end, i = start + 1; i < max; ++i) {
                if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                    throw new IllegalArgumentException("Invalid " + msg + " (must be a valid Java identifier): " + name);
                }
            }
            return;
        }
        throw new IllegalArgumentException("Invalid " + msg + " (must not be null or empty)");
    }
    
    static void checkMethodIdentifier(final int version, final String name, final String msg) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Invalid " + msg + " (must not be null or empty)");
        }
        if ((version & 0xFFFF) >= 49) {
            for (int i = 0; i < name.length(); ++i) {
                if (".;[/<>".indexOf(name.charAt(i)) != -1) {
                    throw new IllegalArgumentException("Invalid " + msg + " (must be a valid unqualified name): " + name);
                }
            }
            return;
        }
        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            throw new IllegalArgumentException("Invalid " + msg + " (must be a '<init>', '<clinit>' or a valid Java identifier): " + name);
        }
        for (int i = 1; i < name.length(); ++i) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                throw new IllegalArgumentException("Invalid " + msg + " (must be '<init>' or '<clinit>' or a valid Java identifier): " + name);
            }
        }
    }
    
    static void checkInternalName(final String name, final String msg) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Invalid " + msg + " (must not be null or empty)");
        }
        if (name.charAt(0) == '[') {
            checkDesc(name, false);
        }
        else {
            checkInternalName(name, 0, -1, msg);
        }
    }
    
    static void checkInternalName(final String name, final int start, final int end, final String msg) {
        final int max = (end == -1) ? name.length() : end;
        try {
            int begin = start;
            int slash;
            do {
                slash = name.indexOf(47, begin + 1);
                if (slash == -1 || slash > max) {
                    slash = max;
                }
                checkIdentifier(name, begin, slash, null);
                begin = slash + 1;
            } while (slash != max);
        }
        catch (IllegalArgumentException unused) {
            throw new IllegalArgumentException("Invalid " + msg + " (must be a fully qualified class name in internal form): " + name);
        }
    }
    
    static void checkDesc(final String desc, final boolean canBeVoid) {
        final int end = checkDesc(desc, 0, canBeVoid);
        if (end != desc.length()) {
            throw new IllegalArgumentException("Invalid descriptor: " + desc);
        }
    }
    
    static int checkDesc(final String desc, final int start, final boolean canBeVoid) {
        if (desc == null || start >= desc.length()) {
            throw new IllegalArgumentException("Invalid type descriptor (must not be null or empty)");
        }
        switch (desc.charAt(start)) {
            case 'V': {
                if (canBeVoid) {
                    return start + 1;
                }
                throw new IllegalArgumentException("Invalid descriptor: " + desc);
            }
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z': {
                return start + 1;
            }
            case '[': {
                int index;
                for (index = start + 1; index < desc.length() && desc.charAt(index) == '['; ++index) {}
                if (index < desc.length()) {
                    return checkDesc(desc, index, false);
                }
                throw new IllegalArgumentException("Invalid descriptor: " + desc);
            }
            case 'L': {
                final int index = desc.indexOf(59, start);
                if (index == -1 || index - start < 2) {
                    throw new IllegalArgumentException("Invalid descriptor: " + desc);
                }
                try {
                    checkInternalName(desc, start + 1, index, null);
                }
                catch (IllegalArgumentException unused) {
                    throw new IllegalArgumentException("Invalid descriptor: " + desc);
                }
                return index + 1;
            }
            default: {
                throw new IllegalArgumentException("Invalid descriptor: " + desc);
            }
        }
    }
    
    static void checkMethodDesc(final String desc) {
        if (desc == null || desc.length() == 0) {
            throw new IllegalArgumentException("Invalid method descriptor (must not be null or empty)");
        }
        if (desc.charAt(0) != '(' || desc.length() < 3) {
            throw new IllegalArgumentException("Invalid descriptor: " + desc);
        }
        int start = 1;
        Label_0143: {
            if (desc.charAt(start) != ')') {
                while (desc.charAt(start) != 'V') {
                    start = checkDesc(desc, start, false);
                    if (start >= desc.length() || desc.charAt(start) == ')') {
                        break Label_0143;
                    }
                }
                throw new IllegalArgumentException("Invalid descriptor: " + desc);
            }
        }
        start = checkDesc(desc, start + 1, true);
        if (start != desc.length()) {
            throw new IllegalArgumentException("Invalid descriptor: " + desc);
        }
    }
    
    void checkLabel(final Label label, final boolean checkVisited, final String msg) {
        if (label == null) {
            throw new IllegalArgumentException("Invalid " + msg + " (must not be null)");
        }
        if (checkVisited && this.labels.get(label) == null) {
            throw new IllegalArgumentException("Invalid " + msg + " (must be visited first)");
        }
    }
    
    private static void checkNonDebugLabel(final Label label) {
        final Field f = getLabelStatusField();
        int status = 0;
        try {
            status = (int)((f == null) ? 0 : f.get(label));
        }
        catch (IllegalAccessException e) {
            throw new Error("Internal error");
        }
        if ((status & 0x1) != 0x0) {
            throw new IllegalArgumentException("Labels used for debug info cannot be reused for control flow");
        }
    }
    
    private static Field getLabelStatusField() {
        if (CheckMethodAdapter.labelStatusField == null) {
            CheckMethodAdapter.labelStatusField = getLabelField("a");
            if (CheckMethodAdapter.labelStatusField == null) {
                CheckMethodAdapter.labelStatusField = getLabelField("status");
            }
        }
        return CheckMethodAdapter.labelStatusField;
    }
    
    private static Field getLabelField(final String name) {
        try {
            final Field f = Label.class.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        }
        catch (NoSuchFieldException e) {
            return null;
        }
    }
    
    static {
        final String s = "BBBBBBBBBBBBBBBBCCIAADDDDDAAAAAAAAAAAAAAAAAAAABBBBBBBBDDDDDAAAAAAAAAAAAAAAAAAAABBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBJBBBBBBBBBBBBBBBBBBBBHHHHHHHHHHHHHHHHDKLBBBBBBFFFFGGGGAECEBBEEBBAMHHAA";
        TYPE = new int[s.length()];
        for (int i = 0; i < CheckMethodAdapter.TYPE.length; ++i) {
            CheckMethodAdapter.TYPE[i] = s.charAt(i) - 'A' - 1;
        }
    }
}
