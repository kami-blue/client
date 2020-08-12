// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.tree.analysis;

import java.util.Map;
import org.spongepowered.asm.lib.tree.IincInsnNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.lib.tree.TableSwitchInsnNode;
import org.spongepowered.asm.lib.tree.LookupSwitchInsnNode;
import org.spongepowered.asm.lib.Type;
import java.util.HashMap;
import org.spongepowered.asm.lib.tree.JumpInsnNode;
import org.spongepowered.asm.lib.tree.LabelNode;
import java.util.ArrayList;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.TryCatchBlockNode;
import java.util.List;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.Opcodes;

public class Analyzer<V extends Value> implements Opcodes
{
    private final Interpreter<V> interpreter;
    private int n;
    private InsnList insns;
    private List<TryCatchBlockNode>[] handlers;
    private Frame<V>[] frames;
    private Subroutine[] subroutines;
    private boolean[] queued;
    private int[] queue;
    private int top;
    
    public Analyzer(final Interpreter<V> interpreter) {
        this.interpreter = interpreter;
    }
    
    public Frame<V>[] analyze(final String owner, final MethodNode m) throws AnalyzerException {
        if ((m.access & 0x500) != 0x0) {
            return this.frames = (Frame<V>[])new Frame[0];
        }
        this.n = m.instructions.size();
        this.insns = m.instructions;
        this.handlers = (List<TryCatchBlockNode>[])new List[this.n];
        this.frames = (Frame<V>[])new Frame[this.n];
        this.subroutines = new Subroutine[this.n];
        this.queued = new boolean[this.n];
        this.queue = new int[this.n];
        this.top = 0;
        for (int i = 0; i < m.tryCatchBlocks.size(); ++i) {
            final TryCatchBlockNode tcb = m.tryCatchBlocks.get(i);
            final int begin = this.insns.indexOf(tcb.start);
            for (int end = this.insns.indexOf(tcb.end), j = begin; j < end; ++j) {
                List<TryCatchBlockNode> insnHandlers = this.handlers[j];
                if (insnHandlers == null) {
                    insnHandlers = new ArrayList<TryCatchBlockNode>();
                    this.handlers[j] = insnHandlers;
                }
                insnHandlers.add(tcb);
            }
        }
        final Subroutine main = new Subroutine(null, m.maxLocals, null);
        final List<AbstractInsnNode> subroutineCalls = new ArrayList<AbstractInsnNode>();
        final Map<LabelNode, Subroutine> subroutineHeads = new HashMap<LabelNode, Subroutine>();
        this.findSubroutine(0, main, subroutineCalls);
        while (!subroutineCalls.isEmpty()) {
            final JumpInsnNode jsr = subroutineCalls.remove(0);
            Subroutine sub = subroutineHeads.get(jsr.label);
            if (sub == null) {
                sub = new Subroutine(jsr.label, m.maxLocals, jsr);
                subroutineHeads.put(jsr.label, sub);
                this.findSubroutine(this.insns.indexOf(jsr.label), sub, subroutineCalls);
            }
            else {
                sub.callers.add(jsr);
            }
        }
        for (int k = 0; k < this.n; ++k) {
            if (this.subroutines[k] != null && this.subroutines[k].start == null) {
                this.subroutines[k] = null;
            }
        }
        final Frame<V> current = this.newFrame(m.maxLocals, m.maxStack);
        final Frame<V> handler = this.newFrame(m.maxLocals, m.maxStack);
        current.setReturn(this.interpreter.newValue(Type.getReturnType(m.desc)));
        final Type[] args = Type.getArgumentTypes(m.desc);
        int local = 0;
        if ((m.access & 0x8) == 0x0) {
            final Type ctype = Type.getObjectType(owner);
            current.setLocal(local++, this.interpreter.newValue(ctype));
        }
        for (int l = 0; l < args.length; ++l) {
            current.setLocal(local++, this.interpreter.newValue(args[l]));
            if (args[l].getSize() == 2) {
                current.setLocal(local++, this.interpreter.newValue(null));
            }
        }
        while (local < m.maxLocals) {
            current.setLocal(local++, this.interpreter.newValue(null));
        }
        this.merge(0, current, null);
        this.init(owner, m);
        while (this.top > 0) {
            final int[] queue = this.queue;
            final int top = this.top - 1;
            this.top = top;
            final int insn = queue[top];
            final Frame<V> f = this.frames[insn];
            Subroutine subroutine = this.subroutines[insn];
            this.queued[insn] = false;
            AbstractInsnNode insnNode = null;
            try {
                insnNode = m.instructions.get(insn);
                final int insnOpcode = insnNode.getOpcode();
                final int insnType = insnNode.getType();
                if (insnType == 8 || insnType == 15 || insnType == 14) {
                    this.merge(insn + 1, f, subroutine);
                    this.newControlFlowEdge(insn, insn + 1);
                }
                else {
                    current.init((Frame<? extends V>)f).execute(insnNode, this.interpreter);
                    subroutine = ((subroutine == null) ? null : subroutine.copy());
                    if (insnNode instanceof JumpInsnNode) {
                        final JumpInsnNode j2 = (JumpInsnNode)insnNode;
                        if (insnOpcode != 167 && insnOpcode != 168) {
                            this.merge(insn + 1, current, subroutine);
                            this.newControlFlowEdge(insn, insn + 1);
                        }
                        final int jump = this.insns.indexOf(j2.label);
                        if (insnOpcode == 168) {
                            this.merge(jump, current, new Subroutine(j2.label, m.maxLocals, j2));
                        }
                        else {
                            this.merge(jump, current, subroutine);
                        }
                        this.newControlFlowEdge(insn, jump);
                    }
                    else if (insnNode instanceof LookupSwitchInsnNode) {
                        final LookupSwitchInsnNode lsi = (LookupSwitchInsnNode)insnNode;
                        int jump = this.insns.indexOf(lsi.dflt);
                        this.merge(jump, current, subroutine);
                        this.newControlFlowEdge(insn, jump);
                        for (int j3 = 0; j3 < lsi.labels.size(); ++j3) {
                            final LabelNode label = lsi.labels.get(j3);
                            jump = this.insns.indexOf(label);
                            this.merge(jump, current, subroutine);
                            this.newControlFlowEdge(insn, jump);
                        }
                    }
                    else if (insnNode instanceof TableSwitchInsnNode) {
                        final TableSwitchInsnNode tsi = (TableSwitchInsnNode)insnNode;
                        int jump = this.insns.indexOf(tsi.dflt);
                        this.merge(jump, current, subroutine);
                        this.newControlFlowEdge(insn, jump);
                        for (int j3 = 0; j3 < tsi.labels.size(); ++j3) {
                            final LabelNode label = tsi.labels.get(j3);
                            jump = this.insns.indexOf(label);
                            this.merge(jump, current, subroutine);
                            this.newControlFlowEdge(insn, jump);
                        }
                    }
                    else if (insnOpcode == 169) {
                        if (subroutine == null) {
                            throw new AnalyzerException(insnNode, "RET instruction outside of a sub routine");
                        }
                        for (int i2 = 0; i2 < subroutine.callers.size(); ++i2) {
                            final JumpInsnNode caller = subroutine.callers.get(i2);
                            final int call = this.insns.indexOf(caller);
                            if (this.frames[call] != null) {
                                this.merge(call + 1, this.frames[call], current, this.subroutines[call], subroutine.access);
                                this.newControlFlowEdge(insn, call + 1);
                            }
                        }
                    }
                    else if (insnOpcode != 191 && (insnOpcode < 172 || insnOpcode > 177)) {
                        if (subroutine != null) {
                            if (insnNode instanceof VarInsnNode) {
                                final int var = ((VarInsnNode)insnNode).var;
                                subroutine.access[var] = true;
                                if (insnOpcode == 22 || insnOpcode == 24 || insnOpcode == 55 || insnOpcode == 57) {
                                    subroutine.access[var + 1] = true;
                                }
                            }
                            else if (insnNode instanceof IincInsnNode) {
                                final int var = ((IincInsnNode)insnNode).var;
                                subroutine.access[var] = true;
                            }
                        }
                        this.merge(insn + 1, current, subroutine);
                        this.newControlFlowEdge(insn, insn + 1);
                    }
                }
                final List<TryCatchBlockNode> insnHandlers2 = this.handlers[insn];
                if (insnHandlers2 == null) {
                    continue;
                }
                for (int i3 = 0; i3 < insnHandlers2.size(); ++i3) {
                    final TryCatchBlockNode tcb2 = insnHandlers2.get(i3);
                    Type type;
                    if (tcb2.type == null) {
                        type = Type.getObjectType("java/lang/Throwable");
                    }
                    else {
                        type = Type.getObjectType(tcb2.type);
                    }
                    final int jump2 = this.insns.indexOf(tcb2.handler);
                    if (this.newControlFlowExceptionEdge(insn, tcb2)) {
                        handler.init((Frame<? extends V>)f);
                        handler.clearStack();
                        handler.push(this.interpreter.newValue(type));
                        this.merge(jump2, handler, subroutine);
                    }
                }
            }
            catch (AnalyzerException e) {
                throw new AnalyzerException(e.node, "Error at instruction " + insn + ": " + e.getMessage(), e);
            }
            catch (Exception e2) {
                throw new AnalyzerException(insnNode, "Error at instruction " + insn + ": " + e2.getMessage(), e2);
            }
        }
        return this.frames;
    }
    
    private void findSubroutine(int insn, final Subroutine sub, final List<AbstractInsnNode> calls) throws AnalyzerException {
        while (insn >= 0 && insn < this.n) {
            if (this.subroutines[insn] != null) {
                return;
            }
            this.subroutines[insn] = sub.copy();
            final AbstractInsnNode node = this.insns.get(insn);
            if (node instanceof JumpInsnNode) {
                if (node.getOpcode() == 168) {
                    calls.add(node);
                }
                else {
                    final JumpInsnNode jnode = (JumpInsnNode)node;
                    this.findSubroutine(this.insns.indexOf(jnode.label), sub, calls);
                }
            }
            else if (node instanceof TableSwitchInsnNode) {
                final TableSwitchInsnNode tsnode = (TableSwitchInsnNode)node;
                this.findSubroutine(this.insns.indexOf(tsnode.dflt), sub, calls);
                for (int i = tsnode.labels.size() - 1; i >= 0; --i) {
                    final LabelNode l = tsnode.labels.get(i);
                    this.findSubroutine(this.insns.indexOf(l), sub, calls);
                }
            }
            else if (node instanceof LookupSwitchInsnNode) {
                final LookupSwitchInsnNode lsnode = (LookupSwitchInsnNode)node;
                this.findSubroutine(this.insns.indexOf(lsnode.dflt), sub, calls);
                for (int i = lsnode.labels.size() - 1; i >= 0; --i) {
                    final LabelNode l = lsnode.labels.get(i);
                    this.findSubroutine(this.insns.indexOf(l), sub, calls);
                }
            }
            final List<TryCatchBlockNode> insnHandlers = this.handlers[insn];
            if (insnHandlers != null) {
                for (int i = 0; i < insnHandlers.size(); ++i) {
                    final TryCatchBlockNode tcb = insnHandlers.get(i);
                    this.findSubroutine(this.insns.indexOf(tcb.handler), sub, calls);
                }
            }
            switch (node.getOpcode()) {
                case 167:
                case 169:
                case 170:
                case 171:
                case 172:
                case 173:
                case 174:
                case 175:
                case 176:
                case 177:
                case 191: {
                    return;
                }
                default: {
                    ++insn;
                    continue;
                }
            }
        }
        throw new AnalyzerException(null, "Execution can fall off end of the code");
    }
    
    public Frame<V>[] getFrames() {
        return this.frames;
    }
    
    public List<TryCatchBlockNode> getHandlers(final int insn) {
        return this.handlers[insn];
    }
    
    protected void init(final String owner, final MethodNode m) throws AnalyzerException {
    }
    
    protected Frame<V> newFrame(final int nLocals, final int nStack) {
        return new Frame<V>(nLocals, nStack);
    }
    
    protected Frame<V> newFrame(final Frame<? extends V> src) {
        return new Frame<V>(src);
    }
    
    protected void newControlFlowEdge(final int insn, final int successor) {
    }
    
    protected boolean newControlFlowExceptionEdge(final int insn, final int successor) {
        return true;
    }
    
    protected boolean newControlFlowExceptionEdge(final int insn, final TryCatchBlockNode tcb) {
        return this.newControlFlowExceptionEdge(insn, this.insns.indexOf(tcb.handler));
    }
    
    private void merge(final int insn, final Frame<V> frame, final Subroutine subroutine) throws AnalyzerException {
        final Frame<V> oldFrame = this.frames[insn];
        final Subroutine oldSubroutine = this.subroutines[insn];
        boolean changes;
        if (oldFrame == null) {
            this.frames[insn] = this.newFrame((Frame<? extends V>)frame);
            changes = true;
        }
        else {
            changes = oldFrame.merge((Frame<? extends V>)frame, this.interpreter);
        }
        if (oldSubroutine == null) {
            if (subroutine != null) {
                this.subroutines[insn] = subroutine.copy();
                changes = true;
            }
        }
        else if (subroutine != null) {
            changes |= oldSubroutine.merge(subroutine);
        }
        if (changes && !this.queued[insn]) {
            this.queued[insn] = true;
            this.queue[this.top++] = insn;
        }
    }
    
    private void merge(final int insn, final Frame<V> beforeJSR, final Frame<V> afterRET, final Subroutine subroutineBeforeJSR, final boolean[] access) throws AnalyzerException {
        final Frame<V> oldFrame = this.frames[insn];
        final Subroutine oldSubroutine = this.subroutines[insn];
        afterRET.merge((Frame<? extends V>)beforeJSR, access);
        boolean changes;
        if (oldFrame == null) {
            this.frames[insn] = this.newFrame((Frame<? extends V>)afterRET);
            changes = true;
        }
        else {
            changes = oldFrame.merge((Frame<? extends V>)afterRET, this.interpreter);
        }
        if (oldSubroutine != null && subroutineBeforeJSR != null) {
            changes |= oldSubroutine.merge(subroutineBeforeJSR);
        }
        if (changes && !this.queued[insn]) {
            this.queued[insn] = true;
            this.queue[this.top++] = insn;
        }
    }
}
