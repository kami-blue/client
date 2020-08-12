// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.util;

import org.spongepowered.asm.lib.TypeReference;
import java.util.HashMap;
import org.spongepowered.asm.lib.Handle;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.Attribute;
import org.spongepowered.asm.lib.TypePath;
import org.spongepowered.asm.lib.signature.SignatureVisitor;
import org.spongepowered.asm.lib.signature.SignatureReader;
import org.spongepowered.asm.lib.ClassVisitor;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.InputStream;
import org.spongepowered.asm.lib.ClassReader;
import java.io.FileInputStream;
import org.spongepowered.asm.lib.Label;
import java.util.Map;

public class Textifier extends Printer
{
    public static final int INTERNAL_NAME = 0;
    public static final int FIELD_DESCRIPTOR = 1;
    public static final int FIELD_SIGNATURE = 2;
    public static final int METHOD_DESCRIPTOR = 3;
    public static final int METHOD_SIGNATURE = 4;
    public static final int CLASS_SIGNATURE = 5;
    public static final int TYPE_DECLARATION = 6;
    public static final int CLASS_DECLARATION = 7;
    public static final int PARAMETERS_DECLARATION = 8;
    public static final int HANDLE_DESCRIPTOR = 9;
    protected String tab;
    protected String tab2;
    protected String tab3;
    protected String ltab;
    protected Map<Label, String> labelNames;
    private int access;
    private int valueNumber;
    
    public Textifier() {
        this(327680);
        if (this.getClass() != Textifier.class) {
            throw new IllegalStateException();
        }
    }
    
    protected Textifier(final int api) {
        super(api);
        this.tab = "  ";
        this.tab2 = "    ";
        this.tab3 = "      ";
        this.ltab = "   ";
        this.valueNumber = 0;
    }
    
    public static void main(final String[] args) throws Exception {
        int i = 0;
        int flags = 2;
        boolean ok = true;
        if (args.length < 1 || args.length > 2) {
            ok = false;
        }
        if (ok && "-debug".equals(args[0])) {
            i = 1;
            flags = 0;
            if (args.length != 2) {
                ok = false;
            }
        }
        if (!ok) {
            System.err.println("Prints a disassembled view of the given class.");
            System.err.println("Usage: Textifier [-debug] <fully qualified class name or class file name>");
            return;
        }
        ClassReader cr;
        if (args[i].endsWith(".class") || args[i].indexOf(92) > -1 || args[i].indexOf(47) > -1) {
            cr = new ClassReader(new FileInputStream(args[i]));
        }
        else {
            cr = new ClassReader(args[i]);
        }
        cr.accept(new TraceClassVisitor(new PrintWriter(System.out)), flags);
    }
    
    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        this.access = access;
        final int major = version & 0xFFFF;
        final int minor = version >>> 16;
        this.buf.setLength(0);
        this.buf.append("// class version ").append(major).append('.').append(minor).append(" (").append(version).append(")\n");
        if ((access & 0x20000) != 0x0) {
            this.buf.append("// DEPRECATED\n");
        }
        this.buf.append("// access flags 0x").append(Integer.toHexString(access).toUpperCase()).append('\n');
        this.appendDescriptor(5, signature);
        if (signature != null) {
            final TraceSignatureVisitor sv = new TraceSignatureVisitor(access);
            final SignatureReader r = new SignatureReader(signature);
            r.accept(sv);
            this.buf.append("// declaration: ").append(name).append(sv.getDeclaration()).append('\n');
        }
        this.appendAccess(access & 0xFFFFFFDF);
        if ((access & 0x2000) != 0x0) {
            this.buf.append("@interface ");
        }
        else if ((access & 0x200) != 0x0) {
            this.buf.append("interface ");
        }
        else if ((access & 0x4000) == 0x0) {
            this.buf.append("class ");
        }
        this.appendDescriptor(0, name);
        if (superName != null && !"java/lang/Object".equals(superName)) {
            this.buf.append(" extends ");
            this.appendDescriptor(0, superName);
            this.buf.append(' ');
        }
        if (interfaces != null && interfaces.length > 0) {
            this.buf.append(" implements ");
            for (int i = 0; i < interfaces.length; ++i) {
                this.appendDescriptor(0, interfaces[i]);
                this.buf.append(' ');
            }
        }
        this.buf.append(" {\n\n");
        this.text.add(this.buf.toString());
    }
    
    @Override
    public void visitSource(final String file, final String debug) {
        this.buf.setLength(0);
        if (file != null) {
            this.buf.append(this.tab).append("// compiled from: ").append(file).append('\n');
        }
        if (debug != null) {
            this.buf.append(this.tab).append("// debug info: ").append(debug).append('\n');
        }
        if (this.buf.length() > 0) {
            this.text.add(this.buf.toString());
        }
    }
    
    @Override
    public void visitOuterClass(final String owner, final String name, final String desc) {
        this.buf.setLength(0);
        this.buf.append(this.tab).append("OUTERCLASS ");
        this.appendDescriptor(0, owner);
        this.buf.append(' ');
        if (name != null) {
            this.buf.append(name).append(' ');
        }
        this.appendDescriptor(3, desc);
        this.buf.append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public Textifier visitClassAnnotation(final String desc, final boolean visible) {
        this.text.add("\n");
        return this.visitAnnotation(desc, visible);
    }
    
    @Override
    public Printer visitClassTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        this.text.add("\n");
        return this.visitTypeAnnotation(typeRef, typePath, desc, visible);
    }
    
    @Override
    public void visitClassAttribute(final Attribute attr) {
        this.text.add("\n");
        this.visitAttribute(attr);
    }
    
    @Override
    public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
        this.buf.setLength(0);
        this.buf.append(this.tab).append("// access flags 0x");
        this.buf.append(Integer.toHexString(access & 0xFFFFFFDF).toUpperCase()).append('\n');
        this.buf.append(this.tab);
        this.appendAccess(access);
        this.buf.append("INNERCLASS ");
        this.appendDescriptor(0, name);
        this.buf.append(' ');
        this.appendDescriptor(0, outerName);
        this.buf.append(' ');
        this.appendDescriptor(0, innerName);
        this.buf.append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public Textifier visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        this.buf.setLength(0);
        this.buf.append('\n');
        if ((access & 0x20000) != 0x0) {
            this.buf.append(this.tab).append("// DEPRECATED\n");
        }
        this.buf.append(this.tab).append("// access flags 0x").append(Integer.toHexString(access).toUpperCase()).append('\n');
        if (signature != null) {
            this.buf.append(this.tab);
            this.appendDescriptor(2, signature);
            final TraceSignatureVisitor sv = new TraceSignatureVisitor(0);
            final SignatureReader r = new SignatureReader(signature);
            r.acceptType(sv);
            this.buf.append(this.tab).append("// declaration: ").append(sv.getDeclaration()).append('\n');
        }
        this.buf.append(this.tab);
        this.appendAccess(access);
        this.appendDescriptor(1, desc);
        this.buf.append(' ').append(name);
        if (value != null) {
            this.buf.append(" = ");
            if (value instanceof String) {
                this.buf.append('\"').append(value).append('\"');
            }
            else {
                this.buf.append(value);
            }
        }
        this.buf.append('\n');
        this.text.add(this.buf.toString());
        final Textifier t = this.createTextifier();
        this.text.add(t.getText());
        return t;
    }
    
    @Override
    public Textifier visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        this.buf.setLength(0);
        this.buf.append('\n');
        if ((access & 0x20000) != 0x0) {
            this.buf.append(this.tab).append("// DEPRECATED\n");
        }
        this.buf.append(this.tab).append("// access flags 0x").append(Integer.toHexString(access).toUpperCase()).append('\n');
        if (signature != null) {
            this.buf.append(this.tab);
            this.appendDescriptor(4, signature);
            final TraceSignatureVisitor v = new TraceSignatureVisitor(0);
            final SignatureReader r = new SignatureReader(signature);
            r.accept(v);
            final String genericDecl = v.getDeclaration();
            final String genericReturn = v.getReturnType();
            final String genericExceptions = v.getExceptions();
            this.buf.append(this.tab).append("// declaration: ").append(genericReturn).append(' ').append(name).append(genericDecl);
            if (genericExceptions != null) {
                this.buf.append(" throws ").append(genericExceptions);
            }
            this.buf.append('\n');
        }
        this.buf.append(this.tab);
        this.appendAccess(access & 0xFFFFFFBF);
        if ((access & 0x100) != 0x0) {
            this.buf.append("native ");
        }
        if ((access & 0x80) != 0x0) {
            this.buf.append("varargs ");
        }
        if ((access & 0x40) != 0x0) {
            this.buf.append("bridge ");
        }
        if ((this.access & 0x200) != 0x0 && (access & 0x400) == 0x0 && (access & 0x8) == 0x0) {
            this.buf.append("default ");
        }
        this.buf.append(name);
        this.appendDescriptor(3, desc);
        if (exceptions != null && exceptions.length > 0) {
            this.buf.append(" throws ");
            for (int i = 0; i < exceptions.length; ++i) {
                this.appendDescriptor(0, exceptions[i]);
                this.buf.append(' ');
            }
        }
        this.buf.append('\n');
        this.text.add(this.buf.toString());
        final Textifier t = this.createTextifier();
        this.text.add(t.getText());
        return t;
    }
    
    @Override
    public void visitClassEnd() {
        this.text.add("}\n");
    }
    
    @Override
    public void visit(final String name, final Object value) {
        this.buf.setLength(0);
        this.appendComa(this.valueNumber++);
        if (name != null) {
            this.buf.append(name).append('=');
        }
        if (value instanceof String) {
            this.visitString((String)value);
        }
        else if (value instanceof Type) {
            this.visitType((Type)value);
        }
        else if (value instanceof Byte) {
            this.visitByte((byte)value);
        }
        else if (value instanceof Boolean) {
            this.visitBoolean((boolean)value);
        }
        else if (value instanceof Short) {
            this.visitShort((short)value);
        }
        else if (value instanceof Character) {
            this.visitChar((char)value);
        }
        else if (value instanceof Integer) {
            this.visitInt((int)value);
        }
        else if (value instanceof Float) {
            this.visitFloat((float)value);
        }
        else if (value instanceof Long) {
            this.visitLong((long)value);
        }
        else if (value instanceof Double) {
            this.visitDouble((double)value);
        }
        else if (value.getClass().isArray()) {
            this.buf.append('{');
            if (value instanceof byte[]) {
                final byte[] v = (byte[])value;
                for (int i = 0; i < v.length; ++i) {
                    this.appendComa(i);
                    this.visitByte(v[i]);
                }
            }
            else if (value instanceof boolean[]) {
                final boolean[] v2 = (boolean[])value;
                for (int i = 0; i < v2.length; ++i) {
                    this.appendComa(i);
                    this.visitBoolean(v2[i]);
                }
            }
            else if (value instanceof short[]) {
                final short[] v3 = (short[])value;
                for (int i = 0; i < v3.length; ++i) {
                    this.appendComa(i);
                    this.visitShort(v3[i]);
                }
            }
            else if (value instanceof char[]) {
                final char[] v4 = (char[])value;
                for (int i = 0; i < v4.length; ++i) {
                    this.appendComa(i);
                    this.visitChar(v4[i]);
                }
            }
            else if (value instanceof int[]) {
                final int[] v5 = (int[])value;
                for (int i = 0; i < v5.length; ++i) {
                    this.appendComa(i);
                    this.visitInt(v5[i]);
                }
            }
            else if (value instanceof long[]) {
                final long[] v6 = (long[])value;
                for (int i = 0; i < v6.length; ++i) {
                    this.appendComa(i);
                    this.visitLong(v6[i]);
                }
            }
            else if (value instanceof float[]) {
                final float[] v7 = (float[])value;
                for (int i = 0; i < v7.length; ++i) {
                    this.appendComa(i);
                    this.visitFloat(v7[i]);
                }
            }
            else if (value instanceof double[]) {
                final double[] v8 = (double[])value;
                for (int i = 0; i < v8.length; ++i) {
                    this.appendComa(i);
                    this.visitDouble(v8[i]);
                }
            }
            this.buf.append('}');
        }
        this.text.add(this.buf.toString());
    }
    
    private void visitInt(final int value) {
        this.buf.append(value);
    }
    
    private void visitLong(final long value) {
        this.buf.append(value).append('L');
    }
    
    private void visitFloat(final float value) {
        this.buf.append(value).append('F');
    }
    
    private void visitDouble(final double value) {
        this.buf.append(value).append('D');
    }
    
    private void visitChar(final char value) {
        this.buf.append("(char)").append((int)value);
    }
    
    private void visitShort(final short value) {
        this.buf.append("(short)").append(value);
    }
    
    private void visitByte(final byte value) {
        this.buf.append("(byte)").append(value);
    }
    
    private void visitBoolean(final boolean value) {
        this.buf.append(value);
    }
    
    private void visitString(final String value) {
        Printer.appendString(this.buf, value);
    }
    
    private void visitType(final Type value) {
        this.buf.append(value.getClassName()).append(".class");
    }
    
    @Override
    public void visitEnum(final String name, final String desc, final String value) {
        this.buf.setLength(0);
        this.appendComa(this.valueNumber++);
        if (name != null) {
            this.buf.append(name).append('=');
        }
        this.appendDescriptor(1, desc);
        this.buf.append('.').append(value);
        this.text.add(this.buf.toString());
    }
    
    @Override
    public Textifier visitAnnotation(final String name, final String desc) {
        this.buf.setLength(0);
        this.appendComa(this.valueNumber++);
        if (name != null) {
            this.buf.append(name).append('=');
        }
        this.buf.append('@');
        this.appendDescriptor(1, desc);
        this.buf.append('(');
        this.text.add(this.buf.toString());
        final Textifier t = this.createTextifier();
        this.text.add(t.getText());
        this.text.add(")");
        return t;
    }
    
    @Override
    public Textifier visitArray(final String name) {
        this.buf.setLength(0);
        this.appendComa(this.valueNumber++);
        if (name != null) {
            this.buf.append(name).append('=');
        }
        this.buf.append('{');
        this.text.add(this.buf.toString());
        final Textifier t = this.createTextifier();
        this.text.add(t.getText());
        this.text.add("}");
        return t;
    }
    
    @Override
    public void visitAnnotationEnd() {
    }
    
    @Override
    public Textifier visitFieldAnnotation(final String desc, final boolean visible) {
        return this.visitAnnotation(desc, visible);
    }
    
    @Override
    public Printer visitFieldTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        return this.visitTypeAnnotation(typeRef, typePath, desc, visible);
    }
    
    @Override
    public void visitFieldAttribute(final Attribute attr) {
        this.visitAttribute(attr);
    }
    
    @Override
    public void visitFieldEnd() {
    }
    
    @Override
    public void visitParameter(final String name, final int access) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append("// parameter ");
        this.appendAccess(access);
        this.buf.append(' ').append((name == null) ? "<no name>" : name).append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public Textifier visitAnnotationDefault() {
        this.text.add(this.tab2 + "default=");
        final Textifier t = this.createTextifier();
        this.text.add(t.getText());
        this.text.add("\n");
        return t;
    }
    
    @Override
    public Textifier visitMethodAnnotation(final String desc, final boolean visible) {
        return this.visitAnnotation(desc, visible);
    }
    
    @Override
    public Printer visitMethodTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        return this.visitTypeAnnotation(typeRef, typePath, desc, visible);
    }
    
    @Override
    public Textifier visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append('@');
        this.appendDescriptor(1, desc);
        this.buf.append('(');
        this.text.add(this.buf.toString());
        final Textifier t = this.createTextifier();
        this.text.add(t.getText());
        this.text.add(visible ? ") // parameter " : ") // invisible, parameter ");
        this.text.add(parameter);
        this.text.add("\n");
        return t;
    }
    
    @Override
    public void visitMethodAttribute(final Attribute attr) {
        this.buf.setLength(0);
        this.buf.append(this.tab).append("ATTRIBUTE ");
        this.appendDescriptor(-1, attr.type);
        if (attr instanceof Textifiable) {
            ((Textifiable)attr).textify(this.buf, this.labelNames);
        }
        else {
            this.buf.append(" : unknown\n");
        }
        this.text.add(this.buf.toString());
    }
    
    @Override
    public void visitCode() {
    }
    
    @Override
    public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
        this.buf.setLength(0);
        this.buf.append(this.ltab);
        this.buf.append("FRAME ");
        switch (type) {
            case -1:
            case 0: {
                this.buf.append("FULL [");
                this.appendFrameTypes(nLocal, local);
                this.buf.append("] [");
                this.appendFrameTypes(nStack, stack);
                this.buf.append(']');
                break;
            }
            case 1: {
                this.buf.append("APPEND [");
                this.appendFrameTypes(nLocal, local);
                this.buf.append(']');
                break;
            }
            case 2: {
                this.buf.append("CHOP ").append(nLocal);
                break;
            }
            case 3: {
                this.buf.append("SAME");
                break;
            }
            case 4: {
                this.buf.append("SAME1 ");
                this.appendFrameTypes(1, stack);
                break;
            }
        }
        this.buf.append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public void visitInsn(final int opcode) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append(Textifier.OPCODES[opcode]).append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append(Textifier.OPCODES[opcode]).append(' ').append((opcode == 188) ? Textifier.TYPES[operand] : Integer.toString(operand)).append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public void visitVarInsn(final int opcode, final int var) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append(Textifier.OPCODES[opcode]).append(' ').append(var).append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append(Textifier.OPCODES[opcode]).append(' ');
        this.appendDescriptor(0, type);
        this.buf.append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append(Textifier.OPCODES[opcode]).append(' ');
        this.appendDescriptor(0, owner);
        this.buf.append('.').append(name).append(" : ");
        this.appendDescriptor(1, desc);
        this.buf.append('\n');
        this.text.add(this.buf.toString());
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
        this.buf.setLength(0);
        this.buf.append(this.tab2).append(Textifier.OPCODES[opcode]).append(' ');
        this.appendDescriptor(0, owner);
        this.buf.append('.').append(name).append(' ');
        this.appendDescriptor(3, desc);
        this.buf.append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public void visitInvokeDynamicInsn(final String name, final String desc, final Handle bsm, final Object... bsmArgs) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append("INVOKEDYNAMIC").append(' ');
        this.buf.append(name);
        this.appendDescriptor(3, desc);
        this.buf.append(" [");
        this.buf.append('\n');
        this.buf.append(this.tab3);
        this.appendHandle(bsm);
        this.buf.append('\n');
        this.buf.append(this.tab3).append("// arguments:");
        if (bsmArgs.length == 0) {
            this.buf.append(" none");
        }
        else {
            this.buf.append('\n');
            for (int i = 0; i < bsmArgs.length; ++i) {
                this.buf.append(this.tab3);
                final Object cst = bsmArgs[i];
                if (cst instanceof String) {
                    Printer.appendString(this.buf, (String)cst);
                }
                else if (cst instanceof Type) {
                    final Type type = (Type)cst;
                    if (type.getSort() == 11) {
                        this.appendDescriptor(3, type.getDescriptor());
                    }
                    else {
                        this.buf.append(type.getDescriptor()).append(".class");
                    }
                }
                else if (cst instanceof Handle) {
                    this.appendHandle((Handle)cst);
                }
                else {
                    this.buf.append(cst);
                }
                this.buf.append(", \n");
            }
            this.buf.setLength(this.buf.length() - 3);
        }
        this.buf.append('\n');
        this.buf.append(this.tab2).append("]\n");
        this.text.add(this.buf.toString());
    }
    
    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append(Textifier.OPCODES[opcode]).append(' ');
        this.appendLabel(label);
        this.buf.append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public void visitLabel(final Label label) {
        this.buf.setLength(0);
        this.buf.append(this.ltab);
        this.appendLabel(label);
        this.buf.append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public void visitLdcInsn(final Object cst) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append("LDC ");
        if (cst instanceof String) {
            Printer.appendString(this.buf, (String)cst);
        }
        else if (cst instanceof Type) {
            this.buf.append(((Type)cst).getDescriptor()).append(".class");
        }
        else {
            this.buf.append(cst);
        }
        this.buf.append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public void visitIincInsn(final int var, final int increment) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append("IINC ").append(var).append(' ').append(increment).append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append("TABLESWITCH\n");
        for (int i = 0; i < labels.length; ++i) {
            this.buf.append(this.tab3).append(min + i).append(": ");
            this.appendLabel(labels[i]);
            this.buf.append('\n');
        }
        this.buf.append(this.tab3).append("default: ");
        this.appendLabel(dflt);
        this.buf.append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append("LOOKUPSWITCH\n");
        for (int i = 0; i < labels.length; ++i) {
            this.buf.append(this.tab3).append(keys[i]).append(": ");
            this.appendLabel(labels[i]);
            this.buf.append('\n');
        }
        this.buf.append(this.tab3).append("default: ");
        this.appendLabel(dflt);
        this.buf.append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append("MULTIANEWARRAY ");
        this.appendDescriptor(1, desc);
        this.buf.append(' ').append(dims).append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public Printer visitInsnAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        return this.visitTypeAnnotation(typeRef, typePath, desc, visible);
    }
    
    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append("TRYCATCHBLOCK ");
        this.appendLabel(start);
        this.buf.append(' ');
        this.appendLabel(end);
        this.buf.append(' ');
        this.appendLabel(handler);
        this.buf.append(' ');
        this.appendDescriptor(0, type);
        this.buf.append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public Printer visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append("TRYCATCHBLOCK @");
        this.appendDescriptor(1, desc);
        this.buf.append('(');
        this.text.add(this.buf.toString());
        final Textifier t = this.createTextifier();
        this.text.add(t.getText());
        this.buf.setLength(0);
        this.buf.append(") : ");
        this.appendTypeReference(typeRef);
        this.buf.append(", ").append(typePath);
        this.buf.append(visible ? "\n" : " // invisible\n");
        this.text.add(this.buf.toString());
        return t;
    }
    
    @Override
    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append("LOCALVARIABLE ").append(name).append(' ');
        this.appendDescriptor(1, desc);
        this.buf.append(' ');
        this.appendLabel(start);
        this.buf.append(' ');
        this.appendLabel(end);
        this.buf.append(' ').append(index).append('\n');
        if (signature != null) {
            this.buf.append(this.tab2);
            this.appendDescriptor(2, signature);
            final TraceSignatureVisitor sv = new TraceSignatureVisitor(0);
            final SignatureReader r = new SignatureReader(signature);
            r.acceptType(sv);
            this.buf.append(this.tab2).append("// declaration: ").append(sv.getDeclaration()).append('\n');
        }
        this.text.add(this.buf.toString());
    }
    
    @Override
    public Printer visitLocalVariableAnnotation(final int typeRef, final TypePath typePath, final Label[] start, final Label[] end, final int[] index, final String desc, final boolean visible) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append("LOCALVARIABLE @");
        this.appendDescriptor(1, desc);
        this.buf.append('(');
        this.text.add(this.buf.toString());
        final Textifier t = this.createTextifier();
        this.text.add(t.getText());
        this.buf.setLength(0);
        this.buf.append(") : ");
        this.appendTypeReference(typeRef);
        this.buf.append(", ").append(typePath);
        for (int i = 0; i < start.length; ++i) {
            this.buf.append(" [ ");
            this.appendLabel(start[i]);
            this.buf.append(" - ");
            this.appendLabel(end[i]);
            this.buf.append(" - ").append(index[i]).append(" ]");
        }
        this.buf.append(visible ? "\n" : " // invisible\n");
        this.text.add(this.buf.toString());
        return t;
    }
    
    @Override
    public void visitLineNumber(final int line, final Label start) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append("LINENUMBER ").append(line).append(' ');
        this.appendLabel(start);
        this.buf.append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        this.buf.setLength(0);
        this.buf.append(this.tab2).append("MAXSTACK = ").append(maxStack).append('\n');
        this.text.add(this.buf.toString());
        this.buf.setLength(0);
        this.buf.append(this.tab2).append("MAXLOCALS = ").append(maxLocals).append('\n');
        this.text.add(this.buf.toString());
    }
    
    @Override
    public void visitMethodEnd() {
    }
    
    public Textifier visitAnnotation(final String desc, final boolean visible) {
        this.buf.setLength(0);
        this.buf.append(this.tab).append('@');
        this.appendDescriptor(1, desc);
        this.buf.append('(');
        this.text.add(this.buf.toString());
        final Textifier t = this.createTextifier();
        this.text.add(t.getText());
        this.text.add(visible ? ")\n" : ") // invisible\n");
        return t;
    }
    
    public Textifier visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        this.buf.setLength(0);
        this.buf.append(this.tab).append('@');
        this.appendDescriptor(1, desc);
        this.buf.append('(');
        this.text.add(this.buf.toString());
        final Textifier t = this.createTextifier();
        this.text.add(t.getText());
        this.buf.setLength(0);
        this.buf.append(") : ");
        this.appendTypeReference(typeRef);
        this.buf.append(", ").append(typePath);
        this.buf.append(visible ? "\n" : " // invisible\n");
        this.text.add(this.buf.toString());
        return t;
    }
    
    public void visitAttribute(final Attribute attr) {
        this.buf.setLength(0);
        this.buf.append(this.tab).append("ATTRIBUTE ");
        this.appendDescriptor(-1, attr.type);
        if (attr instanceof Textifiable) {
            ((Textifiable)attr).textify(this.buf, null);
        }
        else {
            this.buf.append(" : unknown\n");
        }
        this.text.add(this.buf.toString());
    }
    
    protected Textifier createTextifier() {
        return new Textifier();
    }
    
    protected void appendDescriptor(final int type, final String desc) {
        if (type == 5 || type == 2 || type == 4) {
            if (desc != null) {
                this.buf.append("// signature ").append(desc).append('\n');
            }
        }
        else {
            this.buf.append(desc);
        }
    }
    
    protected void appendLabel(final Label l) {
        if (this.labelNames == null) {
            this.labelNames = new HashMap<Label, String>();
        }
        String name = this.labelNames.get(l);
        if (name == null) {
            name = "L" + this.labelNames.size();
            this.labelNames.put(l, name);
        }
        this.buf.append(name);
    }
    
    protected void appendHandle(final Handle h) {
        final int tag = h.getTag();
        this.buf.append("// handle kind 0x").append(Integer.toHexString(tag)).append(" : ");
        boolean isMethodHandle = false;
        switch (tag) {
            case 1: {
                this.buf.append("GETFIELD");
                break;
            }
            case 2: {
                this.buf.append("GETSTATIC");
                break;
            }
            case 3: {
                this.buf.append("PUTFIELD");
                break;
            }
            case 4: {
                this.buf.append("PUTSTATIC");
                break;
            }
            case 9: {
                this.buf.append("INVOKEINTERFACE");
                isMethodHandle = true;
                break;
            }
            case 7: {
                this.buf.append("INVOKESPECIAL");
                isMethodHandle = true;
                break;
            }
            case 6: {
                this.buf.append("INVOKESTATIC");
                isMethodHandle = true;
                break;
            }
            case 5: {
                this.buf.append("INVOKEVIRTUAL");
                isMethodHandle = true;
                break;
            }
            case 8: {
                this.buf.append("NEWINVOKESPECIAL");
                isMethodHandle = true;
                break;
            }
        }
        this.buf.append('\n');
        this.buf.append(this.tab3);
        this.appendDescriptor(0, h.getOwner());
        this.buf.append('.');
        this.buf.append(h.getName());
        if (!isMethodHandle) {
            this.buf.append('(');
        }
        this.appendDescriptor(9, h.getDesc());
        if (!isMethodHandle) {
            this.buf.append(')');
        }
    }
    
    private void appendAccess(final int access) {
        if ((access & 0x1) != 0x0) {
            this.buf.append("public ");
        }
        if ((access & 0x2) != 0x0) {
            this.buf.append("private ");
        }
        if ((access & 0x4) != 0x0) {
            this.buf.append("protected ");
        }
        if ((access & 0x10) != 0x0) {
            this.buf.append("final ");
        }
        if ((access & 0x8) != 0x0) {
            this.buf.append("static ");
        }
        if ((access & 0x20) != 0x0) {
            this.buf.append("synchronized ");
        }
        if ((access & 0x40) != 0x0) {
            this.buf.append("volatile ");
        }
        if ((access & 0x80) != 0x0) {
            this.buf.append("transient ");
        }
        if ((access & 0x400) != 0x0) {
            this.buf.append("abstract ");
        }
        if ((access & 0x800) != 0x0) {
            this.buf.append("strictfp ");
        }
        if ((access & 0x1000) != 0x0) {
            this.buf.append("synthetic ");
        }
        if ((access & 0x8000) != 0x0) {
            this.buf.append("mandated ");
        }
        if ((access & 0x4000) != 0x0) {
            this.buf.append("enum ");
        }
    }
    
    private void appendComa(final int i) {
        if (i != 0) {
            this.buf.append(", ");
        }
    }
    
    private void appendTypeReference(final int typeRef) {
        final TypeReference ref = new TypeReference(typeRef);
        switch (ref.getSort()) {
            case 0: {
                this.buf.append("CLASS_TYPE_PARAMETER ").append(ref.getTypeParameterIndex());
                break;
            }
            case 1: {
                this.buf.append("METHOD_TYPE_PARAMETER ").append(ref.getTypeParameterIndex());
                break;
            }
            case 16: {
                this.buf.append("CLASS_EXTENDS ").append(ref.getSuperTypeIndex());
                break;
            }
            case 17: {
                this.buf.append("CLASS_TYPE_PARAMETER_BOUND ").append(ref.getTypeParameterIndex()).append(", ").append(ref.getTypeParameterBoundIndex());
                break;
            }
            case 18: {
                this.buf.append("METHOD_TYPE_PARAMETER_BOUND ").append(ref.getTypeParameterIndex()).append(", ").append(ref.getTypeParameterBoundIndex());
                break;
            }
            case 19: {
                this.buf.append("FIELD");
                break;
            }
            case 20: {
                this.buf.append("METHOD_RETURN");
                break;
            }
            case 21: {
                this.buf.append("METHOD_RECEIVER");
                break;
            }
            case 22: {
                this.buf.append("METHOD_FORMAL_PARAMETER ").append(ref.getFormalParameterIndex());
                break;
            }
            case 23: {
                this.buf.append("THROWS ").append(ref.getExceptionIndex());
                break;
            }
            case 64: {
                this.buf.append("LOCAL_VARIABLE");
                break;
            }
            case 65: {
                this.buf.append("RESOURCE_VARIABLE");
                break;
            }
            case 66: {
                this.buf.append("EXCEPTION_PARAMETER ").append(ref.getTryCatchBlockIndex());
                break;
            }
            case 67: {
                this.buf.append("INSTANCEOF");
                break;
            }
            case 68: {
                this.buf.append("NEW");
                break;
            }
            case 69: {
                this.buf.append("CONSTRUCTOR_REFERENCE");
                break;
            }
            case 70: {
                this.buf.append("METHOD_REFERENCE");
                break;
            }
            case 71: {
                this.buf.append("CAST ").append(ref.getTypeArgumentIndex());
                break;
            }
            case 72: {
                this.buf.append("CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT ").append(ref.getTypeArgumentIndex());
                break;
            }
            case 73: {
                this.buf.append("METHOD_INVOCATION_TYPE_ARGUMENT ").append(ref.getTypeArgumentIndex());
                break;
            }
            case 74: {
                this.buf.append("CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT ").append(ref.getTypeArgumentIndex());
                break;
            }
            case 75: {
                this.buf.append("METHOD_REFERENCE_TYPE_ARGUMENT ").append(ref.getTypeArgumentIndex());
                break;
            }
        }
    }
    
    private void appendFrameTypes(final int n, final Object[] o) {
        for (int i = 0; i < n; ++i) {
            if (i > 0) {
                this.buf.append(' ');
            }
            if (o[i] instanceof String) {
                final String desc = (String)o[i];
                if (desc.startsWith("[")) {
                    this.appendDescriptor(1, desc);
                }
                else {
                    this.appendDescriptor(0, desc);
                }
            }
            else if (o[i] instanceof Integer) {
                switch ((int)o[i]) {
                    case 0: {
                        this.appendDescriptor(1, "T");
                        break;
                    }
                    case 1: {
                        this.appendDescriptor(1, "I");
                        break;
                    }
                    case 2: {
                        this.appendDescriptor(1, "F");
                        break;
                    }
                    case 3: {
                        this.appendDescriptor(1, "D");
                        break;
                    }
                    case 4: {
                        this.appendDescriptor(1, "J");
                        break;
                    }
                    case 5: {
                        this.appendDescriptor(1, "N");
                        break;
                    }
                    case 6: {
                        this.appendDescriptor(1, "U");
                        break;
                    }
                }
            }
            else {
                this.appendLabel((Label)o[i]);
            }
        }
    }
}
