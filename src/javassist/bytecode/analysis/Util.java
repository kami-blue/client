// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.analysis;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.Opcode;

public class Util implements Opcode
{
    public static int getJumpTarget(int pos, final CodeIterator iter) {
        final int opcode = iter.byteAt(pos);
        pos += ((opcode == 201 || opcode == 200) ? iter.s32bitAt(pos + 1) : iter.s16bitAt(pos + 1));
        return pos;
    }
    
    public static boolean isJumpInstruction(final int opcode) {
        return (opcode >= 153 && opcode <= 168) || opcode == 198 || opcode == 199 || opcode == 201 || opcode == 200;
    }
    
    public static boolean isGoto(final int opcode) {
        return opcode == 167 || opcode == 200;
    }
    
    public static boolean isJsr(final int opcode) {
        return opcode == 168 || opcode == 201;
    }
    
    public static boolean isReturn(final int opcode) {
        return opcode >= 172 && opcode <= 177;
    }
}
