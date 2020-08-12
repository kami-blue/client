// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.callback;

import org.spongepowered.asm.mixin.injection.Coerce;
import java.util.ArrayList;
import org.spongepowered.asm.lib.tree.JumpInsnNode;
import org.spongepowered.asm.lib.tree.LabelNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.asm.util.SignaturePrinter;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.LdcInsnNode;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.throwables.InjectionError;
import java.lang.annotation.Annotation;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.lib.tree.LocalVariableNode;
import org.spongepowered.asm.util.Locals;
import org.spongepowered.asm.util.Bytecode;
import com.google.common.base.Strings;
import java.util.Set;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import java.util.Iterator;
import org.spongepowered.asm.mixin.injection.points.BeforeReturn;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import java.util.List;
import java.util.HashMap;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.Target;
import java.util.Map;
import org.spongepowered.asm.mixin.injection.code.Injector;

public class CallbackInjector extends Injector
{
    private final boolean cancellable;
    private final LocalCapture localCapture;
    private final String identifier;
    private final Map<Integer, String> ids;
    private int totalInjections;
    private int callbackInfoVar;
    private String lastId;
    private String lastDesc;
    private Target lastTarget;
    private String callbackInfoClass;
    
    public CallbackInjector(final InjectionInfo info, final boolean cancellable, final LocalCapture localCapture, final String identifier) {
        super(info);
        this.ids = new HashMap<Integer, String>();
        this.totalInjections = 0;
        this.callbackInfoVar = -1;
        this.cancellable = cancellable;
        this.localCapture = localCapture;
        this.identifier = identifier;
    }
    
    @Override
    protected void sanityCheck(final Target target, final List<InjectionPoint> injectionPoints) {
        super.sanityCheck(target, injectionPoints);
        if (target.isStatic != this.isStatic) {
            throw new InvalidInjectionException(this.info, "'static' modifier of callback method does not match target in " + this);
        }
        if ("<init>".equals(target.method.name)) {
            for (final InjectionPoint injectionPoint : injectionPoints) {
                if (!injectionPoint.getClass().equals(BeforeReturn.class)) {
                    throw new InvalidInjectionException(this.info, "Found injection point type " + injectionPoint.getClass().getSimpleName() + " targetting a ctor in " + this + ". Only RETURN allowed for a ctor target");
                }
            }
        }
    }
    
    @Override
    protected void addTargetNode(final Target target, final List<InjectionNodes.InjectionNode> myNodes, final AbstractInsnNode node, final Set<InjectionPoint> nominators) {
        final InjectionNodes.InjectionNode injectionNode = target.addInjectionNode(node);
        for (final InjectionPoint ip : nominators) {
            final String id = ip.getId();
            if (Strings.isNullOrEmpty(id)) {
                continue;
            }
            final String existingId = this.ids.get(injectionNode.getId());
            if (existingId != null && !existingId.equals(id)) {
                Injector.logger.warn("Conflicting id for {} insn in {}, found id {} on {}, previously defined as {}", new Object[] { Bytecode.getOpcodeName(node), target.toString(), id, this.info, existingId });
                break;
            }
            this.ids.put(injectionNode.getId(), id);
        }
        myNodes.add(injectionNode);
        ++this.totalInjections;
    }
    
    @Override
    protected void inject(final Target target, final InjectionNodes.InjectionNode node) {
        LocalVariableNode[] locals = null;
        if (this.localCapture.isCaptureLocals() || this.localCapture.isPrintLocals()) {
            locals = Locals.getLocalsAt(this.classNode, target.method, node.getCurrentTarget());
        }
        this.inject(new Callback(this.methodNode, target, node, locals, this.localCapture.isCaptureLocals()));
    }
    
    private void inject(final Callback callback) {
        if (this.localCapture.isPrintLocals()) {
            this.printLocals(callback);
            this.info.addCallbackInvocation(this.methodNode);
            return;
        }
        MethodNode callbackMethod = this.methodNode;
        if (!callback.checkDescriptor(this.methodNode.desc)) {
            if (this.info.getTargets().size() > 1) {
                return;
            }
            if (callback.canCaptureLocals) {
                final MethodNode surrogateHandler = Bytecode.findMethod(this.classNode, this.methodNode.name, callback.getDescriptor());
                if (surrogateHandler != null && Annotations.getVisible(surrogateHandler, Surrogate.class) != null) {
                    callbackMethod = surrogateHandler;
                }
                else {
                    final String message = this.generateBadLVTMessage(callback);
                    switch (this.localCapture) {
                        case CAPTURE_FAILEXCEPTION: {
                            Injector.logger.error("Injection error: {}", new Object[] { message });
                            callbackMethod = this.generateErrorMethod(callback, "org/spongepowered/asm/mixin/injection/throwables/InjectionError", message);
                            break;
                        }
                        case CAPTURE_FAILSOFT: {
                            Injector.logger.warn("Injection warning: {}", new Object[] { message });
                            return;
                        }
                        default: {
                            Injector.logger.error("Critical injection failure: {}", new Object[] { message });
                            throw new InjectionError(message);
                        }
                    }
                }
            }
            else {
                final String returnableSig = this.methodNode.desc.replace("Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;", "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;");
                if (callback.checkDescriptor(returnableSig)) {
                    throw new InvalidInjectionException(this.info, "Invalid descriptor on " + this.info + "! CallbackInfoReturnable is required!");
                }
                final MethodNode surrogateHandler2 = Bytecode.findMethod(this.classNode, this.methodNode.name, callback.getDescriptor());
                if (surrogateHandler2 == null || Annotations.getVisible(surrogateHandler2, Surrogate.class) == null) {
                    throw new InvalidInjectionException(this.info, "Invalid descriptor on " + this.info + "! Expected " + callback.getDescriptor() + " but found " + this.methodNode.desc);
                }
                callbackMethod = surrogateHandler2;
            }
        }
        this.dupReturnValue(callback);
        if (this.cancellable || this.totalInjections > 1) {
            this.createCallbackInfo(callback, true);
        }
        this.invokeCallback(callback, callbackMethod);
        this.injectCancellationCode(callback);
        callback.inject();
        this.info.notifyInjected(callback.target);
    }
    
    private String generateBadLVTMessage(final Callback callback) {
        final int position = callback.target.indexOf(callback.node);
        final List<String> expected = summariseLocals(this.methodNode.desc, callback.target.arguments.length + 1);
        final List<String> found = summariseLocals(callback.getDescriptorWithAllLocals(), callback.frameSize);
        return String.format("LVT in %s has incompatible changes at opcode %d in callback %s.\nExpected: %s\n   Found: %s", callback.target, position, this, expected, found);
    }
    
    private MethodNode generateErrorMethod(final Callback callback, final String errorClass, final String message) {
        final MethodNode method = this.info.addMethod(this.methodNode.access, this.methodNode.name + "$missing", callback.getDescriptor());
        method.maxLocals = Bytecode.getFirstNonArgLocalIndex(Type.getArgumentTypes(callback.getDescriptor()), !this.isStatic);
        method.maxStack = 3;
        final InsnList insns = method.instructions;
        insns.add(new TypeInsnNode(187, errorClass));
        insns.add(new InsnNode(89));
        insns.add(new LdcInsnNode(message));
        insns.add(new MethodInsnNode(183, errorClass, "<init>", "(Ljava/lang/String;)V", false));
        insns.add(new InsnNode(191));
        return method;
    }
    
    private void printLocals(final Callback callback) {
        final Type[] args = Type.getArgumentTypes(callback.getDescriptorWithAllLocals());
        final SignaturePrinter methodSig = new SignaturePrinter(callback.target.method, callback.argNames);
        final SignaturePrinter handlerSig = new SignaturePrinter(this.methodNode.name, callback.target.returnType, args, callback.argNames);
        handlerSig.setModifiers(this.methodNode);
        final PrettyPrinter printer = new PrettyPrinter();
        printer.kv("Target Class", (Object)this.classNode.name.replace('/', '.'));
        printer.kv("Target Method", methodSig);
        printer.kv("Target Max LOCALS", callback.target.getMaxLocals());
        printer.kv("Initial Frame Size", callback.frameSize);
        printer.kv("Callback Name", (Object)this.methodNode.name);
        printer.kv("Instruction", "%s %s", callback.node.getClass().getSimpleName(), Bytecode.getOpcodeName(callback.node.getCurrentTarget().getOpcode()));
        printer.hr();
        if (callback.locals.length > callback.frameSize) {
            printer.add("  %s  %20s  %s", "LOCAL", "TYPE", "NAME");
            for (int l = 0; l < callback.locals.length; ++l) {
                final String marker = (l == callback.frameSize) ? ">" : " ";
                if (callback.locals[l] != null) {
                    printer.add("%s [%3d]  %20s  %-50s %s", marker, l, SignaturePrinter.getTypeName(callback.localTypes[l], false), meltSnowman(l, callback.locals[l].name), (l >= callback.frameSize) ? "<capture>" : "");
                }
                else {
                    final boolean isTop = l > 0 && callback.localTypes[l - 1] != null && callback.localTypes[l - 1].getSize() > 1;
                    printer.add("%s [%3d]  %20s", marker, l, isTop ? "<top>" : "-");
                }
            }
            printer.hr();
        }
        printer.add().add("/**").add(" * Expected callback signature").add(" * /");
        printer.add("%s {", handlerSig);
        printer.add("    // Method body").add("}").add().print(System.err);
    }
    
    private void createCallbackInfo(final Callback callback, final boolean store) {
        if (callback.target != this.lastTarget) {
            this.lastId = null;
            this.lastDesc = null;
        }
        this.lastTarget = callback.target;
        final String id = this.getIdentifier(callback);
        final String desc = callback.getCallbackInfoConstructorDescriptor();
        if (id.equals(this.lastId) && desc.equals(this.lastDesc) && !callback.isAtReturn && !this.cancellable) {
            return;
        }
        this.instanceCallbackInfo(callback, id, desc, store);
    }
    
    private void loadOrCreateCallbackInfo(final Callback callback) {
        if (this.cancellable || this.totalInjections > 1) {
            callback.add(new VarInsnNode(25, this.callbackInfoVar), false, true);
        }
        else {
            this.createCallbackInfo(callback, false);
        }
    }
    
    private void dupReturnValue(final Callback callback) {
        if (!callback.isAtReturn) {
            return;
        }
        callback.add(new InsnNode(89));
        callback.add(new VarInsnNode(callback.target.returnType.getOpcode(54), callback.marshalVar()));
    }
    
    protected void instanceCallbackInfo(final Callback callback, final String id, final String desc, final boolean store) {
        this.lastId = id;
        this.lastDesc = desc;
        this.callbackInfoVar = callback.marshalVar();
        this.callbackInfoClass = callback.target.getCallbackInfoClass();
        final boolean head = store && this.totalInjections > 1 && !callback.isAtReturn && !this.cancellable;
        callback.add(new TypeInsnNode(187, this.callbackInfoClass), true, !store, head);
        callback.add(new InsnNode(89), true, true, head);
        callback.add(new LdcInsnNode(id), true, !store, head);
        callback.add(new InsnNode(this.cancellable ? 4 : 3), true, !store, head);
        if (callback.isAtReturn) {
            callback.add(new VarInsnNode(callback.target.returnType.getOpcode(21), callback.marshalVar()), true, !store);
            callback.add(new MethodInsnNode(183, this.callbackInfoClass, "<init>", desc, false));
        }
        else {
            callback.add(new MethodInsnNode(183, this.callbackInfoClass, "<init>", desc, false), false, false, head);
        }
        if (store) {
            callback.target.addLocalVariable(this.callbackInfoVar, "callbackInfo" + this.callbackInfoVar, "L" + this.callbackInfoClass + ";");
            callback.add(new VarInsnNode(58, this.callbackInfoVar), false, false, head);
        }
    }
    
    private void invokeCallback(final Callback callback, final MethodNode callbackMethod) {
        if (!this.isStatic) {
            callback.add(new VarInsnNode(25, 0), false, true);
        }
        if (callback.captureArgs()) {
            Bytecode.loadArgs(callback.target.arguments, callback, this.isStatic ? 0 : 1, -1);
        }
        this.loadOrCreateCallbackInfo(callback);
        if (callback.canCaptureLocals) {
            Locals.loadLocals(callback.localTypes, callback, callback.frameSize, callback.extraArgs);
        }
        this.invokeHandler(callback, callbackMethod);
    }
    
    private String getIdentifier(final Callback callback) {
        final String baseId = Strings.isNullOrEmpty(this.identifier) ? callback.target.method.name : this.identifier;
        final String locationId = this.ids.get(callback.node.getId());
        return baseId + (Strings.isNullOrEmpty(locationId) ? "" : (":" + locationId));
    }
    
    protected void injectCancellationCode(final Callback callback) {
        if (!this.cancellable) {
            return;
        }
        callback.add(new VarInsnNode(25, this.callbackInfoVar));
        callback.add(new MethodInsnNode(182, this.callbackInfoClass, CallbackInfo.getIsCancelledMethodName(), CallbackInfo.getIsCancelledMethodSig(), false));
        final LabelNode notCancelled = new LabelNode();
        callback.add(new JumpInsnNode(153, notCancelled));
        this.injectReturnCode(callback);
        callback.add(notCancelled);
    }
    
    protected void injectReturnCode(final Callback callback) {
        if (callback.target.returnType.equals(Type.VOID_TYPE)) {
            callback.add(new InsnNode(177));
        }
        else {
            callback.add(new VarInsnNode(25, callback.marshalVar()));
            final String accessor = CallbackInfoReturnable.getReturnAccessor(callback.target.returnType);
            final String descriptor = CallbackInfoReturnable.getReturnDescriptor(callback.target.returnType);
            callback.add(new MethodInsnNode(182, this.callbackInfoClass, accessor, descriptor, false));
            if (callback.target.returnType.getSort() == 10) {
                callback.add(new TypeInsnNode(192, callback.target.returnType.getInternalName()));
            }
            callback.add(new InsnNode(callback.target.returnType.getOpcode(172)));
        }
    }
    
    protected boolean isStatic() {
        return this.isStatic;
    }
    
    private static List<String> summariseLocals(final String desc, final int pos) {
        return summariseLocals(Type.getArgumentTypes(desc), pos);
    }
    
    private static List<String> summariseLocals(final Type[] locals, int pos) {
        final List<String> list = new ArrayList<String>();
        if (locals != null) {
            while (pos < locals.length) {
                if (locals[pos] != null) {
                    list.add(locals[pos].toString());
                }
                ++pos;
            }
        }
        return list;
    }
    
    static String meltSnowman(final int index, final String varName) {
        return (varName != null && '\u2603' == varName.charAt(0)) ? ("var" + index) : varName;
    }
    
    private class Callback extends InsnList
    {
        private final MethodNode handler;
        private final AbstractInsnNode head;
        final Target target;
        final InjectionNodes.InjectionNode node;
        final LocalVariableNode[] locals;
        final Type[] localTypes;
        final int frameSize;
        final int extraArgs;
        final boolean canCaptureLocals;
        final boolean isAtReturn;
        final String desc;
        final String descl;
        final String[] argNames;
        int ctor;
        int invoke;
        private int marshalVar;
        private boolean captureArgs;
        
        Callback(final MethodNode handler, final Target target, final InjectionNodes.InjectionNode node, final LocalVariableNode[] locals, final boolean captureLocals) {
            this.marshalVar = -1;
            this.captureArgs = true;
            this.handler = handler;
            this.target = target;
            this.head = target.insns.getFirst();
            this.node = node;
            this.locals = locals;
            this.localTypes = (Type[])((locals != null) ? new Type[locals.length] : null);
            this.frameSize = Bytecode.getFirstNonArgLocalIndex(target.arguments, !CallbackInjector.this.isStatic());
            List<String> argNames = null;
            if (locals != null) {
                final int baseArgIndex = CallbackInjector.this.isStatic() ? 0 : 1;
                argNames = new ArrayList<String>();
                for (int l = 0; l <= locals.length; ++l) {
                    if (l == this.frameSize) {
                        argNames.add((target.returnType == Type.VOID_TYPE) ? "ci" : "cir");
                    }
                    if (l < locals.length && locals[l] != null) {
                        this.localTypes[l] = Type.getType(locals[l].desc);
                        if (l >= baseArgIndex) {
                            argNames.add(CallbackInjector.meltSnowman(l, locals[l].name));
                        }
                    }
                }
            }
            this.extraArgs = Math.max(0, Bytecode.getFirstNonArgLocalIndex(this.handler) - (this.frameSize + 1));
            this.argNames = (String[])((argNames != null) ? ((String[])argNames.toArray(new String[argNames.size()])) : null);
            this.canCaptureLocals = (captureLocals && locals != null && locals.length > this.frameSize);
            this.isAtReturn = (this.node.getCurrentTarget() instanceof InsnNode && this.isValueReturnOpcode(this.node.getCurrentTarget().getOpcode()));
            this.desc = target.getCallbackDescriptor(this.localTypes, target.arguments);
            this.descl = target.getCallbackDescriptor(true, this.localTypes, target.arguments, this.frameSize, this.extraArgs);
            this.invoke = target.arguments.length + (this.canCaptureLocals ? (this.localTypes.length - this.frameSize) : 0);
        }
        
        private boolean isValueReturnOpcode(final int opcode) {
            return opcode >= 172 && opcode < 177;
        }
        
        String getDescriptor() {
            return this.canCaptureLocals ? this.descl : this.desc;
        }
        
        String getDescriptorWithAllLocals() {
            return this.target.getCallbackDescriptor(true, this.localTypes, this.target.arguments, this.frameSize, 32767);
        }
        
        String getCallbackInfoConstructorDescriptor() {
            return this.isAtReturn ? CallbackInfo.getConstructorDescriptor(this.target.returnType) : CallbackInfo.getConstructorDescriptor();
        }
        
        void add(final AbstractInsnNode insn, final boolean ctorStack, final boolean invokeStack) {
            this.add(insn, ctorStack, invokeStack, false);
        }
        
        void add(final AbstractInsnNode insn, final boolean ctorStack, final boolean invokeStack, final boolean head) {
            if (head) {
                this.target.insns.insertBefore(this.head, insn);
            }
            else {
                this.add(insn);
            }
            this.ctor += (ctorStack ? 1 : 0);
            this.invoke += (invokeStack ? 1 : 0);
        }
        
        void inject() {
            this.target.insertBefore(this.node, this);
            this.target.addToStack(Math.max(this.invoke, this.ctor));
        }
        
        boolean checkDescriptor(final String desc) {
            if (this.getDescriptor().equals(desc)) {
                return true;
            }
            if (this.target.getSimpleCallbackDescriptor().equals(desc) && !this.canCaptureLocals) {
                this.captureArgs = false;
                return true;
            }
            final Type[] inTypes = Type.getArgumentTypes(desc);
            final Type[] myTypes = Type.getArgumentTypes(this.descl);
            if (inTypes.length != myTypes.length) {
                return false;
            }
            for (int arg = 0; arg < myTypes.length; ++arg) {
                final Type type = inTypes[arg];
                if (!type.equals(myTypes[arg])) {
                    if (type.getSort() == 9) {
                        return false;
                    }
                    if (Annotations.getInvisibleParameter(this.handler, Coerce.class, arg) == null) {
                        return false;
                    }
                    if (!Injector.canCoerce(inTypes[arg], myTypes[arg])) {
                        return false;
                    }
                }
            }
            return true;
        }
        
        boolean captureArgs() {
            return this.captureArgs;
        }
        
        int marshalVar() {
            if (this.marshalVar < 0) {
                this.marshalVar = this.target.allocateLocal();
            }
            return this.marshalVar;
        }
    }
}
