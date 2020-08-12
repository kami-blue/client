// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

class ExceptionTableEntry
{
    int startPc;
    int endPc;
    int handlerPc;
    int catchType;
    
    ExceptionTableEntry(final int start, final int end, final int handle, final int type) {
        this.startPc = start;
        this.endPc = end;
        this.handlerPc = handle;
        this.catchType = type;
    }
}
