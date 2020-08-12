// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.callback;

public enum LocalCapture
{
    NO_CAPTURE(false, false), 
    PRINT(false, true), 
    CAPTURE_FAILSOFT, 
    CAPTURE_FAILHARD, 
    CAPTURE_FAILEXCEPTION;
    
    private final boolean captureLocals;
    private final boolean printLocals;
    
    private LocalCapture() {
        this(true, false);
    }
    
    private LocalCapture(final boolean captureLocals, final boolean printLocals) {
        this.captureLocals = captureLocals;
        this.printLocals = printLocals;
    }
    
    boolean isCaptureLocals() {
        return this.captureLocals;
    }
    
    boolean isPrintLocals() {
        return this.printLocals;
    }
}
