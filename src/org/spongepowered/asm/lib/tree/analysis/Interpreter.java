// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.tree.analysis;

import java.util.List;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.Type;

public abstract class Interpreter<V extends Value>
{
    protected final int api;
    
    protected Interpreter(final int api) {
        this.api = api;
    }
    
    public abstract V newValue(final Type p0);
    
    public abstract V newOperation(final AbstractInsnNode p0) throws AnalyzerException;
    
    public abstract V copyOperation(final AbstractInsnNode p0, final V p1) throws AnalyzerException;
    
    public abstract V unaryOperation(final AbstractInsnNode p0, final V p1) throws AnalyzerException;
    
    public abstract V binaryOperation(final AbstractInsnNode p0, final V p1, final V p2) throws AnalyzerException;
    
    public abstract V ternaryOperation(final AbstractInsnNode p0, final V p1, final V p2, final V p3) throws AnalyzerException;
    
    public abstract V naryOperation(final AbstractInsnNode p0, final List<? extends V> p1) throws AnalyzerException;
    
    public abstract void returnOperation(final AbstractInsnNode p0, final V p1, final V p2) throws AnalyzerException;
    
    public abstract V merge(final V p0, final V p1);
}
