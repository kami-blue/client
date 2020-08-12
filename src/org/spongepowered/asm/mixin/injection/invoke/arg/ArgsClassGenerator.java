// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.invoke.arg;

import org.spongepowered.asm.util.SignaturePrinter;
import org.spongepowered.asm.lib.Label;
import org.spongepowered.asm.util.asm.MethodVisitorEx;
import org.spongepowered.asm.util.Bytecode;
import org.spongepowered.asm.lib.MethodVisitor;
import org.spongepowered.asm.lib.ClassVisitor;
import org.spongepowered.asm.lib.util.CheckClassAdapter;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.lib.ClassWriter;
import org.spongepowered.asm.lib.Type;
import java.util.HashMap;
import com.google.common.collect.HashBiMap;
import java.util.Map;
import com.google.common.collect.BiMap;
import org.spongepowered.asm.mixin.transformer.ext.IClassGenerator;

public final class ArgsClassGenerator implements IClassGenerator
{
    public static final String ARGS_NAME;
    public static final String ARGS_REF;
    public static final String GETTER_PREFIX = "$";
    private static final String CLASS_NAME_BASE = "org.spongepowered.asm.synthetic.args.Args$";
    private static final String OBJECT = "java/lang/Object";
    private static final String OBJECT_ARRAY = "[Ljava/lang/Object;";
    private static final String VALUES_FIELD = "values";
    private static final String CTOR_DESC = "([Ljava/lang/Object;)V";
    private static final String SET = "set";
    private static final String SET_DESC = "(ILjava/lang/Object;)V";
    private static final String SETALL = "setAll";
    private static final String SETALL_DESC = "([Ljava/lang/Object;)V";
    private static final String NPE = "java/lang/NullPointerException";
    private static final String NPE_CTOR_DESC = "(Ljava/lang/String;)V";
    private static final String AIOOBE = "org/spongepowered/asm/mixin/injection/invoke/arg/ArgumentIndexOutOfBoundsException";
    private static final String AIOOBE_CTOR_DESC = "(I)V";
    private static final String ACE = "org/spongepowered/asm/mixin/injection/invoke/arg/ArgumentCountException";
    private static final String ACE_CTOR_DESC = "(IILjava/lang/String;)V";
    private int nextIndex;
    private final BiMap<String, String> classNames;
    private final Map<String, byte[]> classBytes;
    
    public ArgsClassGenerator() {
        this.nextIndex = 1;
        this.classNames = (BiMap<String, String>)HashBiMap.create();
        this.classBytes = new HashMap<String, byte[]>();
    }
    
    public String getClassName(final String desc) {
        if (!desc.endsWith(")V")) {
            throw new IllegalArgumentException("Invalid @ModifyArgs method descriptor");
        }
        String name = (String)this.classNames.get((Object)desc);
        if (name == null) {
            name = String.format("%s%d", "org.spongepowered.asm.synthetic.args.Args$", this.nextIndex++);
            this.classNames.put((Object)desc, (Object)name);
        }
        return name;
    }
    
    public String getClassRef(final String desc) {
        return this.getClassName(desc).replace('.', '/');
    }
    
    @Override
    public byte[] generate(final String name) {
        return this.getBytes(name);
    }
    
    public byte[] getBytes(final String name) {
        byte[] bytes = this.classBytes.get(name);
        if (bytes == null) {
            final String desc = (String)this.classNames.inverse().get((Object)name);
            if (desc == null) {
                return null;
            }
            bytes = this.generateClass(name, desc);
            this.classBytes.put(name, bytes);
        }
        return bytes;
    }
    
    private byte[] generateClass(final String name, final String desc) {
        final String ref = name.replace('.', '/');
        final Type[] args = Type.getArgumentTypes(desc);
        ClassVisitor visitor;
        final ClassWriter writer = (ClassWriter)(visitor = new ClassWriter(2));
        if (MixinEnvironment.getCurrentEnvironment().getOption(MixinEnvironment.Option.DEBUG_VERIFY)) {
            visitor = new CheckClassAdapter(writer);
        }
        visitor.visit(50, 4129, ref, null, ArgsClassGenerator.ARGS_REF, null);
        visitor.visitSource(name.substring(name.lastIndexOf(46) + 1) + ".java", null);
        this.generateCtor(ref, desc, args, visitor);
        this.generateToString(ref, desc, args, visitor);
        this.generateFactory(ref, desc, args, visitor);
        this.generateSetters(ref, desc, args, visitor);
        this.generateGetters(ref, desc, args, visitor);
        visitor.visitEnd();
        return writer.toByteArray();
    }
    
    private void generateCtor(final String ref, final String desc, final Type[] args, final ClassVisitor writer) {
        final MethodVisitor ctor = writer.visitMethod(2, "<init>", "([Ljava/lang/Object;)V", null, null);
        ctor.visitCode();
        ctor.visitVarInsn(25, 0);
        ctor.visitVarInsn(25, 1);
        ctor.visitMethodInsn(183, ArgsClassGenerator.ARGS_REF, "<init>", "([Ljava/lang/Object;)V", false);
        ctor.visitInsn(177);
        ctor.visitMaxs(2, 2);
        ctor.visitEnd();
    }
    
    private void generateToString(final String ref, final String desc, final Type[] args, final ClassVisitor writer) {
        final MethodVisitor toString = writer.visitMethod(1, "toString", "()Ljava/lang/String;", null, null);
        toString.visitCode();
        toString.visitLdcInsn("Args" + getSignature(args));
        toString.visitInsn(176);
        toString.visitMaxs(1, 1);
        toString.visitEnd();
    }
    
    private void generateFactory(final String ref, final String desc, final Type[] args, final ClassVisitor writer) {
        final String factoryDesc = Bytecode.changeDescriptorReturnType(desc, "L" + ref + ";");
        final MethodVisitorEx of = new MethodVisitorEx(writer.visitMethod(9, "of", factoryDesc, null, null));
        of.visitCode();
        of.visitTypeInsn(187, ref);
        of.visitInsn(89);
        of.visitConstant((byte)args.length);
        of.visitTypeInsn(189, "java/lang/Object");
        byte argIndex = 0;
        for (final Type arg : args) {
            of.visitInsn(89);
            of.visitConstant(argIndex);
            final MethodVisitorEx methodVisitorEx = of;
            final int opcode = arg.getOpcode(21);
            final byte var = argIndex;
            ++argIndex;
            methodVisitorEx.visitVarInsn(opcode, var);
            box(of, arg);
            of.visitInsn(83);
        }
        of.visitMethodInsn(183, ref, "<init>", "([Ljava/lang/Object;)V", false);
        of.visitInsn(176);
        of.visitMaxs(6, Bytecode.getArgsSize(args));
        of.visitEnd();
    }
    
    private void generateGetters(final String ref, final String desc, final Type[] args, final ClassVisitor writer) {
        byte argIndex = 0;
        for (final Type arg : args) {
            final String name = "$" + argIndex;
            final String sig = "()" + arg.getDescriptor();
            final MethodVisitorEx get = new MethodVisitorEx(writer.visitMethod(1, name, sig, null, null));
            get.visitCode();
            get.visitVarInsn(25, 0);
            get.visitFieldInsn(180, ref, "values", "[Ljava/lang/Object;");
            get.visitConstant(argIndex);
            get.visitInsn(50);
            unbox(get, arg);
            get.visitInsn(arg.getOpcode(172));
            get.visitMaxs(2, 1);
            get.visitEnd();
            ++argIndex;
        }
    }
    
    private void generateSetters(final String ref, final String desc, final Type[] args, final ClassVisitor writer) {
        this.generateIndexedSetter(ref, desc, args, writer);
        this.generateMultiSetter(ref, desc, args, writer);
    }
    
    private void generateIndexedSetter(final String ref, final String desc, final Type[] args, final ClassVisitor writer) {
        final MethodVisitorEx set = new MethodVisitorEx(writer.visitMethod(1, "set", "(ILjava/lang/Object;)V", null, null));
        set.visitCode();
        final Label store = new Label();
        final Label checkNull = new Label();
        final Label[] labels = new Label[args.length];
        for (int label = 0; label < labels.length; ++label) {
            labels[label] = new Label();
        }
        set.visitVarInsn(25, 0);
        set.visitFieldInsn(180, ref, "values", "[Ljava/lang/Object;");
        for (byte index = 0; index < args.length; ++index) {
            set.visitVarInsn(21, 1);
            set.visitConstant(index);
            set.visitJumpInsn(159, labels[index]);
        }
        throwAIOOBE(set, 1);
        for (int index2 = 0; index2 < args.length; ++index2) {
            final String boxingType = Bytecode.getBoxingType(args[index2]);
            set.visitLabel(labels[index2]);
            set.visitVarInsn(21, 1);
            set.visitVarInsn(25, 2);
            set.visitTypeInsn(192, (boxingType != null) ? boxingType : args[index2].getInternalName());
            set.visitJumpInsn(167, (boxingType != null) ? checkNull : store);
        }
        set.visitLabel(checkNull);
        set.visitInsn(89);
        set.visitJumpInsn(199, store);
        throwNPE(set, "Argument with primitive type cannot be set to NULL");
        set.visitLabel(store);
        set.visitInsn(83);
        set.visitInsn(177);
        set.visitMaxs(6, 3);
        set.visitEnd();
    }
    
    private void generateMultiSetter(final String ref, final String desc, final Type[] args, final ClassVisitor writer) {
        final MethodVisitorEx set = new MethodVisitorEx(writer.visitMethod(1, "setAll", "([Ljava/lang/Object;)V", null, null));
        set.visitCode();
        final Label lengthOk = new Label();
        final Label nullPrimitive = new Label();
        int maxStack = 6;
        set.visitVarInsn(25, 1);
        set.visitInsn(190);
        set.visitInsn(89);
        set.visitConstant((byte)args.length);
        set.visitJumpInsn(159, lengthOk);
        set.visitTypeInsn(187, "org/spongepowered/asm/mixin/injection/invoke/arg/ArgumentCountException");
        set.visitInsn(89);
        set.visitInsn(93);
        set.visitInsn(88);
        set.visitConstant((byte)args.length);
        set.visitLdcInsn(getSignature(args));
        set.visitMethodInsn(183, "org/spongepowered/asm/mixin/injection/invoke/arg/ArgumentCountException", "<init>", "(IILjava/lang/String;)V", false);
        set.visitInsn(191);
        set.visitLabel(lengthOk);
        set.visitInsn(87);
        set.visitVarInsn(25, 0);
        set.visitFieldInsn(180, ref, "values", "[Ljava/lang/Object;");
        for (byte index = 0; index < args.length; ++index) {
            set.visitInsn(89);
            set.visitConstant(index);
            set.visitVarInsn(25, 1);
            set.visitConstant(index);
            set.visitInsn(50);
            final String boxingType = Bytecode.getBoxingType(args[index]);
            set.visitTypeInsn(192, (boxingType != null) ? boxingType : args[index].getInternalName());
            if (boxingType != null) {
                set.visitInsn(89);
                set.visitJumpInsn(198, nullPrimitive);
                maxStack = 7;
            }
            set.visitInsn(83);
        }
        set.visitInsn(177);
        set.visitLabel(nullPrimitive);
        throwNPE(set, "Argument with primitive type cannot be set to NULL");
        set.visitInsn(177);
        set.visitMaxs(maxStack, 2);
        set.visitEnd();
    }
    
    private static void throwNPE(final MethodVisitorEx method, final String message) {
        method.visitTypeInsn(187, "java/lang/NullPointerException");
        method.visitInsn(89);
        method.visitLdcInsn(message);
        method.visitMethodInsn(183, "java/lang/NullPointerException", "<init>", "(Ljava/lang/String;)V", false);
        method.visitInsn(191);
    }
    
    private static void throwAIOOBE(final MethodVisitorEx method, final int arg) {
        method.visitTypeInsn(187, "org/spongepowered/asm/mixin/injection/invoke/arg/ArgumentIndexOutOfBoundsException");
        method.visitInsn(89);
        method.visitVarInsn(21, arg);
        method.visitMethodInsn(183, "org/spongepowered/asm/mixin/injection/invoke/arg/ArgumentIndexOutOfBoundsException", "<init>", "(I)V", false);
        method.visitInsn(191);
    }
    
    private static void box(final MethodVisitor method, final Type var) {
        final String boxingType = Bytecode.getBoxingType(var);
        if (boxingType != null) {
            final String desc = String.format("(%s)L%s;", var.getDescriptor(), boxingType);
            method.visitMethodInsn(184, boxingType, "valueOf", desc, false);
        }
    }
    
    private static void unbox(final MethodVisitor method, final Type var) {
        final String boxingType = Bytecode.getBoxingType(var);
        if (boxingType != null) {
            final String unboxingMethod = Bytecode.getUnboxingMethod(var);
            final String desc = "()" + var.getDescriptor();
            method.visitTypeInsn(192, boxingType);
            method.visitMethodInsn(182, boxingType, unboxingMethod, desc, false);
        }
        else {
            method.visitTypeInsn(192, var.getInternalName());
        }
    }
    
    private static String getSignature(final Type[] args) {
        return new SignaturePrinter("", null, args).setFullyQualified(true).getFormattedArgs();
    }
    
    static {
        ARGS_NAME = Args.class.getName();
        ARGS_REF = ArgsClassGenerator.ARGS_NAME.replace('.', '/');
    }
}
