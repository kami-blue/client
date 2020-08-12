// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.analysis;

import javassist.bytecode.BadBytecode;
import javassist.bytecode.ExceptionTable;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.MethodInfo;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import javassist.bytecode.Opcode;

public class SubroutineScanner implements Opcode
{
    private Subroutine[] subroutines;
    Map subTable;
    Set done;
    
    public SubroutineScanner() {
        this.subTable = new HashMap();
        this.done = new HashSet();
    }
    
    public Subroutine[] scan(final MethodInfo method) throws BadBytecode {
        final CodeAttribute code = method.getCodeAttribute();
        final CodeIterator iter = code.iterator();
        this.subroutines = new Subroutine[code.getCodeLength()];
        this.subTable.clear();
        this.done.clear();
        this.scan(0, iter, null);
        final ExceptionTable exceptions = code.getExceptionTable();
        for (int i = 0; i < exceptions.size(); ++i) {
            final int handler = exceptions.handlerPc(i);
            this.scan(handler, iter, this.subroutines[exceptions.startPc(i)]);
        }
        return this.subroutines;
    }
    
    private void scan(int pos, final CodeIterator iter, final Subroutine sub) throws BadBytecode {
        if (this.done.contains(new Integer(pos))) {
            return;
        }
        this.done.add(new Integer(pos));
        final int old = iter.lookAhead();
        iter.move(pos);
        boolean next;
        do {
            pos = iter.next();
            next = (this.scanOp(pos, iter, sub) && iter.hasNext());
        } while (next);
        iter.move(old);
    }
    
    private boolean scanOp(final int pos, final CodeIterator iter, final Subroutine sub) throws BadBytecode {
        this.subroutines[pos] = sub;
        final int opcode = iter.byteAt(pos);
        if (opcode == 170) {
            this.scanTableSwitch(pos, iter, sub);
            return false;
        }
        if (opcode == 171) {
            this.scanLookupSwitch(pos, iter, sub);
            return false;
        }
        if (Util.isReturn(opcode) || opcode == 169 || opcode == 191) {
            return false;
        }
        if (Util.isJumpInstruction(opcode)) {
            final int target = Util.getJumpTarget(pos, iter);
            if (opcode == 168 || opcode == 201) {
                Subroutine s = this.subTable.get(new Integer(target));
                if (s == null) {
                    s = new Subroutine(target, pos);
                    this.subTable.put(new Integer(target), s);
                    this.scan(target, iter, s);
                }
                else {
                    s.addCaller(pos);
                }
            }
            else {
                this.scan(target, iter, sub);
                if (Util.isGoto(opcode)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private void scanLookupSwitch(final int pos, final CodeIterator iter, final Subroutine sub) throws BadBytecode {
        int index = (pos & 0xFFFFFFFC) + 4;
        this.scan(pos + iter.s32bitAt(index), iter, sub);
        index += 4;
        final int npairs = iter.s32bitAt(index);
        final int n = npairs * 8;
        for (index += 4, final int end = n + index, index += 4; index < end; index += 8) {
            final int target = iter.s32bitAt(index) + pos;
            this.scan(target, iter, sub);
        }
    }
    
    private void scanTableSwitch(final int pos, final CodeIterator iter, final Subroutine sub) throws BadBytecode {
        int index = (pos & 0xFFFFFFFC) + 4;
        this.scan(pos + iter.s32bitAt(index), iter, sub);
        index += 4;
        final int low = iter.s32bitAt(index);
        index += 4;
        final int high = iter.s32bitAt(index);
        final int n = (high - low + 1) * 4;
        index += 4;
        for (int end = n + index; index < end; index += 4) {
            final int target = iter.s32bitAt(index) + pos;
            this.scan(target, iter, sub);
        }
    }
}
