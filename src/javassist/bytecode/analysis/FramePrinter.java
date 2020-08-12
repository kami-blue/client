// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.analysis;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.InstructionPrinter;
import javassist.bytecode.BadBytecode;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import javassist.Modifier;
import javassist.CtMethod;
import javassist.CtClass;
import java.io.PrintStream;

public final class FramePrinter
{
    private final PrintStream stream;
    
    public FramePrinter(final PrintStream stream) {
        this.stream = stream;
    }
    
    public static void print(final CtClass clazz, final PrintStream stream) {
        new FramePrinter(stream).print(clazz);
    }
    
    public void print(final CtClass clazz) {
        final CtMethod[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; ++i) {
            this.print(methods[i]);
        }
    }
    
    private String getMethodString(final CtMethod method) {
        try {
            return Modifier.toString(method.getModifiers()) + " " + method.getReturnType().getName() + " " + method.getName() + Descriptor.toString(method.getSignature()) + ";";
        }
        catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void print(final CtMethod method) {
        this.stream.println("\n" + this.getMethodString(method));
        final MethodInfo info = method.getMethodInfo2();
        final ConstPool pool = info.getConstPool();
        final CodeAttribute code = info.getCodeAttribute();
        if (code == null) {
            return;
        }
        Frame[] frames;
        try {
            frames = new Analyzer().analyze(method.getDeclaringClass(), info);
        }
        catch (BadBytecode e) {
            throw new RuntimeException(e);
        }
        final int spacing = String.valueOf(code.getCodeLength()).length();
        final CodeIterator iterator = code.iterator();
        while (iterator.hasNext()) {
            int pos;
            try {
                pos = iterator.next();
            }
            catch (BadBytecode e2) {
                throw new RuntimeException(e2);
            }
            this.stream.println(pos + ": " + InstructionPrinter.instructionString(iterator, pos, pool));
            this.addSpacing(spacing + 3);
            final Frame frame = frames[pos];
            if (frame == null) {
                this.stream.println("--DEAD CODE--");
            }
            else {
                this.printStack(frame);
                this.addSpacing(spacing + 3);
                this.printLocals(frame);
            }
        }
    }
    
    private void printStack(final Frame frame) {
        this.stream.print("stack [");
        for (int top = frame.getTopIndex(), i = 0; i <= top; ++i) {
            if (i > 0) {
                this.stream.print(", ");
            }
            final Type type = frame.getStack(i);
            this.stream.print(type);
        }
        this.stream.println("]");
    }
    
    private void printLocals(final Frame frame) {
        this.stream.print("locals [");
        for (int length = frame.localsLength(), i = 0; i < length; ++i) {
            if (i > 0) {
                this.stream.print(", ");
            }
            final Type type = frame.getLocal(i);
            this.stream.print((type == null) ? "empty" : type.toString());
        }
        this.stream.println("]");
    }
    
    private void addSpacing(int count) {
        while (count-- > 0) {
            this.stream.print(' ');
        }
    }
}
