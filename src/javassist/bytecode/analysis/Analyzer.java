// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.analysis;

import java.util.Iterator;
import javassist.bytecode.Descriptor;
import javassist.bytecode.ExceptionTable;
import javassist.ClassPool;
import javassist.bytecode.ConstPool;
import javassist.NotFoundException;
import javassist.CtMethod;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.MethodInfo;
import javassist.CtClass;
import javassist.bytecode.Opcode;

public class Analyzer implements Opcode
{
    private final SubroutineScanner scanner;
    private CtClass clazz;
    private ExceptionInfo[] exceptions;
    private Frame[] frames;
    private Subroutine[] subroutines;
    
    public Analyzer() {
        this.scanner = new SubroutineScanner();
    }
    
    public Frame[] analyze(final CtClass clazz, final MethodInfo method) throws BadBytecode {
        this.clazz = clazz;
        final CodeAttribute codeAttribute = method.getCodeAttribute();
        if (codeAttribute == null) {
            return null;
        }
        final int maxLocals = codeAttribute.getMaxLocals();
        final int maxStack = codeAttribute.getMaxStack();
        final int codeLength = codeAttribute.getCodeLength();
        final CodeIterator iter = codeAttribute.iterator();
        final IntQueue queue = new IntQueue();
        this.exceptions = this.buildExceptionInfo(method);
        this.subroutines = this.scanner.scan(method);
        final Executor executor = new Executor(clazz.getClassPool(), method.getConstPool());
        (this.frames = new Frame[codeLength])[iter.lookAhead()] = this.firstFrame(method, maxLocals, maxStack);
        queue.add(iter.next());
        while (!queue.isEmpty()) {
            this.analyzeNextEntry(method, iter, queue, executor);
        }
        return this.frames;
    }
    
    public Frame[] analyze(final CtMethod method) throws BadBytecode {
        return this.analyze(method.getDeclaringClass(), method.getMethodInfo2());
    }
    
    private void analyzeNextEntry(final MethodInfo method, final CodeIterator iter, final IntQueue queue, final Executor executor) throws BadBytecode {
        final int pos = queue.take();
        iter.move(pos);
        iter.next();
        final Frame frame = this.frames[pos].copy();
        final Subroutine subroutine = this.subroutines[pos];
        try {
            executor.execute(method, pos, iter, frame, subroutine);
        }
        catch (RuntimeException e) {
            throw new BadBytecode(e.getMessage() + "[pos = " + pos + "]", e);
        }
        final int opcode = iter.byteAt(pos);
        if (opcode == 170) {
            this.mergeTableSwitch(queue, pos, iter, frame);
        }
        else if (opcode == 171) {
            this.mergeLookupSwitch(queue, pos, iter, frame);
        }
        else if (opcode == 169) {
            this.mergeRet(queue, iter, pos, frame, subroutine);
        }
        else if (Util.isJumpInstruction(opcode)) {
            final int target = Util.getJumpTarget(pos, iter);
            if (Util.isJsr(opcode)) {
                this.mergeJsr(queue, this.frames[pos], this.subroutines[target], pos, this.lookAhead(iter, pos));
            }
            else if (!Util.isGoto(opcode)) {
                this.merge(queue, frame, this.lookAhead(iter, pos));
            }
            this.merge(queue, frame, target);
        }
        else if (opcode != 191 && !Util.isReturn(opcode)) {
            this.merge(queue, frame, this.lookAhead(iter, pos));
        }
        this.mergeExceptionHandlers(queue, method, pos, frame);
    }
    
    private ExceptionInfo[] buildExceptionInfo(final MethodInfo method) {
        final ConstPool constPool = method.getConstPool();
        final ClassPool classes = this.clazz.getClassPool();
        final ExceptionTable table = method.getCodeAttribute().getExceptionTable();
        final ExceptionInfo[] exceptions = new ExceptionInfo[table.size()];
        for (int i = 0; i < table.size(); ++i) {
            final int index = table.catchType(i);
            Type type;
            try {
                type = ((index == 0) ? Type.THROWABLE : Type.get(classes.get(constPool.getClassInfo(index))));
            }
            catch (NotFoundException e) {
                throw new IllegalStateException(e.getMessage());
            }
            exceptions[i] = new ExceptionInfo(table.startPc(i), table.endPc(i), table.handlerPc(i), type);
        }
        return exceptions;
    }
    
    private Frame firstFrame(final MethodInfo method, final int maxLocals, final int maxStack) {
        int pos = 0;
        final Frame first = new Frame(maxLocals, maxStack);
        if ((method.getAccessFlags() & 0x8) == 0x0) {
            first.setLocal(pos++, Type.get(this.clazz));
        }
        CtClass[] parameters;
        try {
            parameters = Descriptor.getParameterTypes(method.getDescriptor(), this.clazz.getClassPool());
        }
        catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < parameters.length; ++i) {
            final Type type = this.zeroExtend(Type.get(parameters[i]));
            first.setLocal(pos++, type);
            if (type.getSize() == 2) {
                first.setLocal(pos++, Type.TOP);
            }
        }
        return first;
    }
    
    private int getNext(final CodeIterator iter, final int of, final int restore) throws BadBytecode {
        iter.move(of);
        iter.next();
        final int next = iter.lookAhead();
        iter.move(restore);
        iter.next();
        return next;
    }
    
    private int lookAhead(final CodeIterator iter, final int pos) throws BadBytecode {
        if (!iter.hasNext()) {
            throw new BadBytecode("Execution falls off end! [pos = " + pos + "]");
        }
        return iter.lookAhead();
    }
    
    private void merge(final IntQueue queue, final Frame frame, final int target) {
        final Frame old = this.frames[target];
        boolean changed;
        if (old == null) {
            this.frames[target] = frame.copy();
            changed = true;
        }
        else {
            changed = old.merge(frame);
        }
        if (changed) {
            queue.add(target);
        }
    }
    
    private void mergeExceptionHandlers(final IntQueue queue, final MethodInfo method, final int pos, final Frame frame) {
        for (int i = 0; i < this.exceptions.length; ++i) {
            final ExceptionInfo exception = this.exceptions[i];
            if (pos >= exception.start && pos < exception.end) {
                final Frame newFrame = frame.copy();
                newFrame.clearStack();
                newFrame.push(exception.type);
                this.merge(queue, newFrame, exception.handler);
            }
        }
    }
    
    private void mergeJsr(final IntQueue queue, final Frame frame, final Subroutine sub, final int pos, final int next) throws BadBytecode {
        if (sub == null) {
            throw new BadBytecode("No subroutine at jsr target! [pos = " + pos + "]");
        }
        Frame old = this.frames[next];
        boolean changed = false;
        if (old == null) {
            final Frame[] frames = this.frames;
            final Frame copy = frame.copy();
            frames[next] = copy;
            old = copy;
            changed = true;
        }
        else {
            for (int i = 0; i < frame.localsLength(); ++i) {
                if (!sub.isAccessed(i)) {
                    final Type oldType = old.getLocal(i);
                    Type newType = frame.getLocal(i);
                    if (oldType == null) {
                        old.setLocal(i, newType);
                        changed = true;
                    }
                    else {
                        newType = oldType.merge(newType);
                        old.setLocal(i, newType);
                        if (!newType.equals(oldType) || newType.popChanged()) {
                            changed = true;
                        }
                    }
                }
            }
        }
        if (!old.isJsrMerged()) {
            old.setJsrMerged(true);
            changed = true;
        }
        if (changed && old.isRetMerged()) {
            queue.add(next);
        }
    }
    
    private void mergeLookupSwitch(final IntQueue queue, final int pos, final CodeIterator iter, final Frame frame) throws BadBytecode {
        int index = (pos & 0xFFFFFFFC) + 4;
        this.merge(queue, frame, pos + iter.s32bitAt(index));
        index += 4;
        final int npairs = iter.s32bitAt(index);
        final int n = npairs * 8;
        for (index += 4, final int end = n + index, index += 4; index < end; index += 8) {
            final int target = iter.s32bitAt(index) + pos;
            this.merge(queue, frame, target);
        }
    }
    
    private void mergeRet(final IntQueue queue, final CodeIterator iter, final int pos, final Frame frame, final Subroutine subroutine) throws BadBytecode {
        if (subroutine == null) {
            throw new BadBytecode("Ret on no subroutine! [pos = " + pos + "]");
        }
        for (final int caller : subroutine.callers()) {
            final int returnLoc = this.getNext(iter, caller, pos);
            boolean changed = false;
            Frame old = this.frames[returnLoc];
            if (old == null) {
                final Frame[] frames = this.frames;
                final int n = returnLoc;
                final Frame copyStack = frame.copyStack();
                frames[n] = copyStack;
                old = copyStack;
                changed = true;
            }
            else {
                changed = old.mergeStack(frame);
            }
            for (final int index : subroutine.accessed()) {
                final Type oldType = old.getLocal(index);
                final Type newType = frame.getLocal(index);
                if (oldType != newType) {
                    old.setLocal(index, newType);
                    changed = true;
                }
            }
            if (!old.isRetMerged()) {
                old.setRetMerged(true);
                changed = true;
            }
            if (changed && old.isJsrMerged()) {
                queue.add(returnLoc);
            }
        }
    }
    
    private void mergeTableSwitch(final IntQueue queue, final int pos, final CodeIterator iter, final Frame frame) throws BadBytecode {
        int index = (pos & 0xFFFFFFFC) + 4;
        this.merge(queue, frame, pos + iter.s32bitAt(index));
        index += 4;
        final int low = iter.s32bitAt(index);
        index += 4;
        final int high = iter.s32bitAt(index);
        final int n = (high - low + 1) * 4;
        index += 4;
        for (int end = n + index; index < end; index += 4) {
            final int target = iter.s32bitAt(index) + pos;
            this.merge(queue, frame, target);
        }
    }
    
    private Type zeroExtend(final Type type) {
        if (type == Type.SHORT || type == Type.BYTE || type == Type.CHAR || type == Type.BOOLEAN) {
            return Type.INTEGER;
        }
        return type;
    }
    
    private static class ExceptionInfo
    {
        private int end;
        private int handler;
        private int start;
        private Type type;
        
        private ExceptionInfo(final int start, final int end, final int handler, final Type type) {
            this.start = start;
            this.end = end;
            this.handler = handler;
            this.type = type;
        }
    }
}
