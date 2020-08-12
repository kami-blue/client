// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.util;

import java.util.HashMap;
import org.spongepowered.asm.lib.tree.analysis.Frame;
import org.spongepowered.asm.lib.tree.analysis.AnalyzerException;
import org.spongepowered.asm.lib.tree.analysis.Interpreter;
import org.spongepowered.asm.lib.tree.analysis.BasicValue;
import org.spongepowered.asm.lib.tree.analysis.Analyzer;
import org.spongepowered.asm.util.asm.MixinVerifier;
import java.util.ArrayList;
import java.util.Iterator;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.lib.tree.FrameNode;
import org.spongepowered.asm.util.throwables.LVTGeneratorException;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.lib.tree.LineNumberNode;
import org.spongepowered.asm.lib.tree.LabelNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.LocalVariableNode;
import java.util.List;
import java.util.Map;

public final class Locals
{
    private static final Map<String, List<LocalVariableNode>> calculatedLocalVariables;
    
    private Locals() {
    }
    
    public static void loadLocals(final Type[] locals, final InsnList insns, int pos, int limit) {
        while (pos < locals.length && limit > 0) {
            if (locals[pos] != null) {
                insns.add(new VarInsnNode(locals[pos].getOpcode(21), pos));
                --limit;
            }
            ++pos;
        }
    }
    
    public static LocalVariableNode[] getLocalsAt(final ClassNode classNode, final MethodNode method, AbstractInsnNode node) {
        for (int i = 0; i < 3 && (node instanceof LabelNode || node instanceof LineNumberNode); node = nextNode(method.instructions, node), ++i) {}
        final ClassInfo classInfo = ClassInfo.forName(classNode.name);
        if (classInfo == null) {
            throw new LVTGeneratorException("Could not load class metadata for " + classNode.name + " generating LVT for " + method.name);
        }
        final ClassInfo.Method methodInfo = classInfo.findMethod(method);
        if (methodInfo == null) {
            throw new LVTGeneratorException("Could not locate method metadata for " + method.name + " generating LVT in " + classNode.name);
        }
        final List<ClassInfo.FrameData> frames = methodInfo.getFrames();
        final LocalVariableNode[] frame = new LocalVariableNode[method.maxLocals];
        int local = 0;
        int index = 0;
        if ((method.access & 0x8) == 0x0) {
            frame[local++] = new LocalVariableNode("this", classNode.name, null, null, null, 0);
        }
        for (final Type argType : Type.getArgumentTypes(method.desc)) {
            frame[local] = new LocalVariableNode("arg" + index++, argType.toString(), null, null, null, local);
            local += argType.getSize();
        }
        final int initialFrameSize = local;
        int frameIndex = -1;
        int locals = 0;
        for (final AbstractInsnNode insn : method.instructions) {
            if (insn instanceof FrameNode) {
                ++frameIndex;
                final FrameNode frameNode = (FrameNode)insn;
                final ClassInfo.FrameData frameData = (frameIndex < frames.size()) ? frames.get(frameIndex) : null;
                locals = ((frameData != null && frameData.type == 0) ? Math.min(locals, frameData.locals) : frameNode.local.size());
                for (int localPos = 0, framePos = 0; framePos < frame.length; ++framePos, ++localPos) {
                    final Object localType = (localPos < frameNode.local.size()) ? frameNode.local.get(localPos) : null;
                    if (localType instanceof String) {
                        frame[framePos] = getLocalVariableAt(classNode, method, node, framePos);
                    }
                    else if (localType instanceof Integer) {
                        final boolean isMarkerType = localType == Opcodes.UNINITIALIZED_THIS || localType == Opcodes.NULL;
                        final boolean is32bitValue = localType == Opcodes.INTEGER || localType == Opcodes.FLOAT;
                        final boolean is64bitValue = localType == Opcodes.DOUBLE || localType == Opcodes.LONG;
                        if (localType != Opcodes.TOP) {
                            if (isMarkerType) {
                                frame[framePos] = null;
                            }
                            else {
                                if (!is32bitValue && !is64bitValue) {
                                    throw new LVTGeneratorException("Unrecognised locals opcode " + localType + " in locals array at position " + localPos + " in " + classNode.name + "." + method.name + method.desc);
                                }
                                frame[framePos] = getLocalVariableAt(classNode, method, node, framePos);
                                if (is64bitValue) {
                                    ++framePos;
                                    frame[framePos] = null;
                                }
                            }
                        }
                    }
                    else {
                        if (localType != null) {
                            throw new LVTGeneratorException("Invalid value " + localType + " in locals array at position " + localPos + " in " + classNode.name + "." + method.name + method.desc);
                        }
                        if (framePos >= initialFrameSize && framePos >= locals && locals > 0) {
                            frame[framePos] = null;
                        }
                    }
                }
            }
            else if (insn instanceof VarInsnNode) {
                final VarInsnNode varNode = (VarInsnNode)insn;
                frame[varNode.var] = getLocalVariableAt(classNode, method, node, varNode.var);
            }
            if (insn == node) {
                break;
            }
        }
        for (int l = 0; l < frame.length; ++l) {
            if (frame[l] != null && frame[l].desc == null) {
                frame[l] = null;
            }
        }
        return frame;
    }
    
    public static LocalVariableNode getLocalVariableAt(final ClassNode classNode, final MethodNode method, final AbstractInsnNode node, final int var) {
        return getLocalVariableAt(classNode, method, method.instructions.indexOf(node), var);
    }
    
    private static LocalVariableNode getLocalVariableAt(final ClassNode classNode, final MethodNode method, final int pos, final int var) {
        LocalVariableNode localVariableNode = null;
        LocalVariableNode fallbackNode = null;
        for (final LocalVariableNode local : getLocalVariableTable(classNode, method)) {
            if (local.index != var) {
                continue;
            }
            if (isOpcodeInRange(method.instructions, local, pos)) {
                localVariableNode = local;
            }
            else {
                if (localVariableNode != null) {
                    continue;
                }
                fallbackNode = local;
            }
        }
        if (localVariableNode == null && !method.localVariables.isEmpty()) {
            for (final LocalVariableNode local : getGeneratedLocalVariableTable(classNode, method)) {
                if (local.index == var && isOpcodeInRange(method.instructions, local, pos)) {
                    localVariableNode = local;
                }
            }
        }
        return (localVariableNode != null) ? localVariableNode : fallbackNode;
    }
    
    private static boolean isOpcodeInRange(final InsnList insns, final LocalVariableNode local, final int pos) {
        return insns.indexOf(local.start) < pos && insns.indexOf(local.end) > pos;
    }
    
    public static List<LocalVariableNode> getLocalVariableTable(final ClassNode classNode, final MethodNode method) {
        if (method.localVariables.isEmpty()) {
            return getGeneratedLocalVariableTable(classNode, method);
        }
        return method.localVariables;
    }
    
    public static List<LocalVariableNode> getGeneratedLocalVariableTable(final ClassNode classNode, final MethodNode method) {
        final String methodId = String.format("%s.%s%s", classNode.name, method.name, method.desc);
        List<LocalVariableNode> localVars = Locals.calculatedLocalVariables.get(methodId);
        if (localVars != null) {
            return localVars;
        }
        localVars = generateLocalVariableTable(classNode, method);
        Locals.calculatedLocalVariables.put(methodId, localVars);
        return localVars;
    }
    
    public static List<LocalVariableNode> generateLocalVariableTable(final ClassNode classNode, final MethodNode method) {
        List<Type> interfaces = null;
        if (classNode.interfaces != null) {
            interfaces = new ArrayList<Type>();
            for (final String iface : classNode.interfaces) {
                interfaces.add(Type.getObjectType(iface));
            }
        }
        Type objectType = null;
        if (classNode.superName != null) {
            objectType = Type.getObjectType(classNode.superName);
        }
        final Analyzer<BasicValue> analyzer = new Analyzer<BasicValue>(new MixinVerifier(Type.getObjectType(classNode.name), objectType, interfaces, false));
        try {
            analyzer.analyze(classNode.name, method);
        }
        catch (AnalyzerException ex) {
            ex.printStackTrace();
        }
        final Frame<BasicValue>[] frames = analyzer.getFrames();
        final int methodSize = method.instructions.size();
        final List<LocalVariableNode> localVariables = new ArrayList<LocalVariableNode>();
        final LocalVariableNode[] localNodes = new LocalVariableNode[method.maxLocals];
        final BasicValue[] locals = new BasicValue[method.maxLocals];
        final LabelNode[] labels = new LabelNode[methodSize];
        final String[] lastKnownType = new String[method.maxLocals];
        for (int i = 0; i < methodSize; ++i) {
            final Frame<BasicValue> f = frames[i];
            if (f != null) {
                LabelNode label = null;
                for (int j = 0; j < f.getLocals(); ++j) {
                    final BasicValue local = f.getLocal(j);
                    if (local != null || locals[j] != null) {
                        if (local == null || !local.equals(locals[j])) {
                            if (label == null) {
                                final AbstractInsnNode existingLabel = method.instructions.get(i);
                                if (existingLabel instanceof LabelNode) {
                                    label = (LabelNode)existingLabel;
                                }
                                else {
                                    label = (labels[i] = new LabelNode());
                                }
                            }
                            if (local == null && locals[j] != null) {
                                localVariables.add(localNodes[j]);
                                localNodes[j].end = label;
                                localNodes[j] = null;
                            }
                            else if (local != null) {
                                if (locals[j] != null) {
                                    localVariables.add(localNodes[j]);
                                    localNodes[j].end = label;
                                    localNodes[j] = null;
                                }
                                final String desc = (local.getType() != null) ? local.getType().getDescriptor() : lastKnownType[j];
                                localNodes[j] = new LocalVariableNode("var" + j, desc, null, label, null, j);
                                if (desc != null) {
                                    lastKnownType[j] = desc;
                                }
                            }
                            locals[j] = local;
                        }
                    }
                }
            }
        }
        LabelNode label2 = null;
        for (int k = 0; k < localNodes.length; ++k) {
            if (localNodes[k] != null) {
                if (label2 == null) {
                    label2 = new LabelNode();
                    method.instructions.add(label2);
                }
                localNodes[k].end = label2;
                localVariables.add(localNodes[k]);
            }
        }
        for (int n = methodSize - 1; n >= 0; --n) {
            if (labels[n] != null) {
                method.instructions.insert(method.instructions.get(n), labels[n]);
            }
        }
        return localVariables;
    }
    
    private static AbstractInsnNode nextNode(final InsnList insns, final AbstractInsnNode insn) {
        final int index = insns.indexOf(insn) + 1;
        if (index > 0 && index < insns.size()) {
            return insns.get(index);
        }
        return insn;
    }
    
    static {
        calculatedLocalVariables = new HashMap<String, List<LocalVariableNode>>();
    }
}
