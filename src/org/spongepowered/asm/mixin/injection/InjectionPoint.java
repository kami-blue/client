// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.lang.reflect.Array;
import java.util.ArrayList;
import com.google.common.base.Joiner;
import org.spongepowered.asm.mixin.injection.points.BeforeConstant;
import org.spongepowered.asm.mixin.injection.points.BeforeFinalReturn;
import org.spongepowered.asm.mixin.injection.modify.AfterStoreLocal;
import org.spongepowered.asm.mixin.injection.modify.BeforeLoadLocal;
import org.spongepowered.asm.mixin.injection.points.AfterInvoke;
import org.spongepowered.asm.mixin.injection.points.MethodHead;
import org.spongepowered.asm.mixin.injection.points.JumpInsnPoint;
import org.spongepowered.asm.mixin.injection.points.BeforeStringInvoke;
import org.spongepowered.asm.mixin.injection.points.BeforeReturn;
import org.spongepowered.asm.mixin.injection.points.BeforeNew;
import org.spongepowered.asm.mixin.injection.points.BeforeInvoke;
import org.spongepowered.asm.mixin.injection.points.BeforeFieldAccess;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.util.Bytecode;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;
import org.spongepowered.asm.mixin.MixinEnvironment;
import java.lang.reflect.Constructor;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.spongepowered.asm.util.Annotations;
import java.util.Arrays;
import java.util.Iterator;
import com.google.common.collect.ImmutableList;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.refmap.IMixinContext;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import java.util.List;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import java.util.Collection;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData;
import java.util.Map;

public abstract class InjectionPoint
{
    public static final int DEFAULT_ALLOWED_SHIFT_BY = 0;
    public static final int MAX_ALLOWED_SHIFT_BY = 0;
    private static Map<String, Class<? extends InjectionPoint>> types;
    private final String slice;
    private final Selector selector;
    private final String id;
    
    protected InjectionPoint() {
        this("", Selector.DEFAULT, null);
    }
    
    protected InjectionPoint(final InjectionPointData data) {
        this(data.getSlice(), data.getSelector(), data.getId());
    }
    
    public InjectionPoint(final String slice, final Selector selector, final String id) {
        this.slice = slice;
        this.selector = selector;
        this.id = id;
    }
    
    public String getSlice() {
        return this.slice;
    }
    
    public Selector getSelector() {
        return this.selector;
    }
    
    public String getId() {
        return this.id;
    }
    
    public abstract boolean find(final String p0, final InsnList p1, final Collection<AbstractInsnNode> p2);
    
    @Override
    public String toString() {
        return String.format("@At(\"%s\")", this.getAtCode());
    }
    
    protected static AbstractInsnNode nextNode(final InsnList insns, final AbstractInsnNode insn) {
        final int index = insns.indexOf(insn) + 1;
        if (index > 0 && index < insns.size()) {
            return insns.get(index);
        }
        return insn;
    }
    
    public static InjectionPoint and(final InjectionPoint... operands) {
        return new Intersection(operands);
    }
    
    public static InjectionPoint or(final InjectionPoint... operands) {
        return new Union(operands);
    }
    
    public static InjectionPoint after(final InjectionPoint point) {
        return new Shift(point, 1);
    }
    
    public static InjectionPoint before(final InjectionPoint point) {
        return new Shift(point, -1);
    }
    
    public static InjectionPoint shift(final InjectionPoint point, final int count) {
        return new Shift(point, count);
    }
    
    public static List<InjectionPoint> parse(final IInjectionPointContext owner, final List<AnnotationNode> ats) {
        return parse(owner.getContext(), owner.getMethod(), owner.getAnnotation(), ats);
    }
    
    public static List<InjectionPoint> parse(final IMixinContext context, final MethodNode method, final AnnotationNode parent, final List<AnnotationNode> ats) {
        final ImmutableList.Builder<InjectionPoint> injectionPoints = (ImmutableList.Builder<InjectionPoint>)ImmutableList.builder();
        for (final AnnotationNode at : ats) {
            final InjectionPoint injectionPoint = parse(context, method, parent, at);
            if (injectionPoint != null) {
                injectionPoints.add((Object)injectionPoint);
            }
        }
        return (List<InjectionPoint>)injectionPoints.build();
    }
    
    public static InjectionPoint parse(final IInjectionPointContext owner, final At at) {
        return parse(owner.getContext(), owner.getMethod(), owner.getAnnotation(), at.value(), at.shift(), at.by(), Arrays.asList(at.args()), at.target(), at.slice(), at.ordinal(), at.opcode(), at.id());
    }
    
    public static InjectionPoint parse(final IMixinContext context, final MethodNode method, final AnnotationNode parent, final At at) {
        return parse(context, method, parent, at.value(), at.shift(), at.by(), Arrays.asList(at.args()), at.target(), at.slice(), at.ordinal(), at.opcode(), at.id());
    }
    
    public static InjectionPoint parse(final IInjectionPointContext owner, final AnnotationNode node) {
        return parse(owner.getContext(), owner.getMethod(), owner.getAnnotation(), node);
    }
    
    public static InjectionPoint parse(final IMixinContext context, final MethodNode method, final AnnotationNode parent, final AnnotationNode node) {
        final String at = Annotations.getValue(node, "value");
        List<String> args = Annotations.getValue(node, "args");
        final String target = Annotations.getValue(node, "target", "");
        final String slice = Annotations.getValue(node, "slice", "");
        final At.Shift shift = Annotations.getValue(node, "shift", At.Shift.class, At.Shift.NONE);
        final int by = Annotations.getValue(node, "by", 0);
        final int ordinal = Annotations.getValue(node, "ordinal", -1);
        final int opcode = Annotations.getValue(node, "opcode", 0);
        final String id = Annotations.getValue(node, "id");
        if (args == null) {
            args = (List<String>)ImmutableList.of();
        }
        return parse(context, method, parent, at, shift, by, args, target, slice, ordinal, opcode, id);
    }
    
    public static InjectionPoint parse(final IMixinContext context, final MethodNode method, final AnnotationNode parent, final String at, final At.Shift shift, final int by, final List<String> args, final String target, final String slice, final int ordinal, final int opcode, final String id) {
        final InjectionPointData data = new InjectionPointData(context, method, parent, at, args, target, slice, ordinal, opcode, id);
        final Class<? extends InjectionPoint> ipClass = findClass(context, data);
        final InjectionPoint point = create(context, data, ipClass);
        return shift(context, method, parent, point, shift, by);
    }
    
    private static Class<? extends InjectionPoint> findClass(final IMixinContext context, final InjectionPointData data) {
        final String type = data.getType();
        Class<? extends InjectionPoint> ipClass = InjectionPoint.types.get(type);
        if (ipClass == null) {
            if (type.matches("^([A-Za-z_][A-Za-z0-9_]*\\.)+[A-Za-z_][A-Za-z0-9_]*$")) {
                try {
                    ipClass = (Class<? extends InjectionPoint>)Class.forName(type);
                    InjectionPoint.types.put(type, ipClass);
                    return ipClass;
                }
                catch (Exception ex) {
                    throw new InvalidInjectionException(context, data + " could not be loaded or is not a valid InjectionPoint", ex);
                }
            }
            throw new InvalidInjectionException(context, data + " is not a valid injection point specifier");
        }
        return ipClass;
    }
    
    private static InjectionPoint create(final IMixinContext context, final InjectionPointData data, final Class<? extends InjectionPoint> ipClass) {
        Constructor<? extends InjectionPoint> ipCtor = null;
        try {
            ipCtor = ipClass.getDeclaredConstructor(InjectionPointData.class);
            ipCtor.setAccessible(true);
        }
        catch (NoSuchMethodException ex) {
            throw new InvalidInjectionException(context, ipClass.getName() + " must contain a constructor which accepts an InjectionPointData", ex);
        }
        InjectionPoint point = null;
        try {
            point = (InjectionPoint)ipCtor.newInstance(data);
        }
        catch (Exception ex2) {
            throw new InvalidInjectionException(context, "Error whilst instancing injection point " + ipClass.getName() + " for " + data.getAt(), ex2);
        }
        return point;
    }
    
    private static InjectionPoint shift(final IMixinContext context, final MethodNode method, final AnnotationNode parent, final InjectionPoint point, final At.Shift shift, final int by) {
        if (point != null) {
            if (shift == At.Shift.BEFORE) {
                return before(point);
            }
            if (shift == At.Shift.AFTER) {
                return after(point);
            }
            if (shift == At.Shift.BY) {
                validateByValue(context, method, parent, point, by);
                return shift(point, by);
            }
        }
        return point;
    }
    
    private static void validateByValue(final IMixinContext context, final MethodNode method, final AnnotationNode parent, final InjectionPoint point, final int by) {
        final MixinEnvironment env = context.getMixin().getConfig().getEnvironment();
        final ShiftByViolationBehaviour err = env.getOption(MixinEnvironment.Option.SHIFT_BY_VIOLATION_BEHAVIOUR, ShiftByViolationBehaviour.WARN);
        if (err == ShiftByViolationBehaviour.IGNORE) {
            return;
        }
        int allowed = 0;
        if (context instanceof MixinTargetContext) {
            allowed = ((MixinTargetContext)context).getMaxShiftByValue();
        }
        if (by <= allowed) {
            return;
        }
        final String message = String.format("@%s(%s) Shift.BY=%d on %s::%s exceeds the maximum allowed value %d.", Bytecode.getSimpleName(parent), point, by, context, method.name, allowed);
        if (err == ShiftByViolationBehaviour.WARN) {
            LogManager.getLogger("mixin").warn("{} Increase the value of maxShiftBy to suppress this warning.", new Object[] { message });
            return;
        }
        throw new InvalidInjectionException(context, message);
    }
    
    protected String getAtCode() {
        final AtCode code = this.getClass().getAnnotation(AtCode.class);
        return (code == null) ? this.getClass().getName() : code.value();
    }
    
    public static void register(final Class<? extends InjectionPoint> type) {
        final AtCode code = type.getAnnotation(AtCode.class);
        if (code == null) {
            throw new IllegalArgumentException("Injection point class " + type + " is not annotated with @AtCode");
        }
        final Class<? extends InjectionPoint> existing = InjectionPoint.types.get(code.value());
        if (existing != null && !existing.equals(type)) {
            LogManager.getLogger("mixin").debug("Overriding InjectionPoint {} with {} (previously {})", new Object[] { code.value(), type.getName(), existing.getName() });
        }
        InjectionPoint.types.put(code.value(), type);
    }
    
    static {
        InjectionPoint.types = new HashMap<String, Class<? extends InjectionPoint>>();
        register(BeforeFieldAccess.class);
        register(BeforeInvoke.class);
        register(BeforeNew.class);
        register(BeforeReturn.class);
        register(BeforeStringInvoke.class);
        register(JumpInsnPoint.class);
        register(MethodHead.class);
        register(AfterInvoke.class);
        register(BeforeLoadLocal.class);
        register(AfterStoreLocal.class);
        register(BeforeFinalReturn.class);
        register(BeforeConstant.class);
    }
    
    public enum Selector
    {
        FIRST, 
        LAST, 
        ONE;
        
        public static final Selector DEFAULT;
        
        static {
            DEFAULT = Selector.FIRST;
        }
    }
    
    enum ShiftByViolationBehaviour
    {
        IGNORE, 
        WARN, 
        ERROR;
    }
    
    abstract static class CompositeInjectionPoint extends InjectionPoint
    {
        protected final InjectionPoint[] components;
        
        protected CompositeInjectionPoint(final InjectionPoint... components) {
            if (components == null || components.length < 2) {
                throw new IllegalArgumentException("Must supply two or more component injection points for composite point!");
            }
            this.components = components;
        }
        
        @Override
        public String toString() {
            return "CompositeInjectionPoint(" + this.getClass().getSimpleName() + ")[" + Joiner.on(',').join((Object[])this.components) + "]";
        }
    }
    
    static final class Intersection extends CompositeInjectionPoint
    {
        public Intersection(final InjectionPoint... points) {
            super(points);
        }
        
        @Override
        public boolean find(final String desc, final InsnList insns, final Collection<AbstractInsnNode> nodes) {
            boolean found = false;
            final ArrayList<AbstractInsnNode>[] allNodes = (ArrayList<AbstractInsnNode>[])Array.newInstance(ArrayList.class, this.components.length);
            for (int i = 0; i < this.components.length; ++i) {
                allNodes[i] = new ArrayList<AbstractInsnNode>();
                this.components[i].find(desc, insns, allNodes[i]);
            }
            final ArrayList<AbstractInsnNode> alpha = allNodes[0];
            for (int nodeIndex = 0; nodeIndex < alpha.size(); ++nodeIndex) {
                final AbstractInsnNode node = alpha.get(nodeIndex);
                final boolean in = true;
                for (int b = 1; b < allNodes.length && allNodes[b].contains(node); ++b) {}
                if (in) {
                    nodes.add(node);
                    found = true;
                }
            }
            return found;
        }
    }
    
    static final class Union extends CompositeInjectionPoint
    {
        public Union(final InjectionPoint... points) {
            super(points);
        }
        
        @Override
        public boolean find(final String desc, final InsnList insns, final Collection<AbstractInsnNode> nodes) {
            final LinkedHashSet<AbstractInsnNode> allNodes = new LinkedHashSet<AbstractInsnNode>();
            for (int i = 0; i < this.components.length; ++i) {
                this.components[i].find(desc, insns, allNodes);
            }
            nodes.addAll(allNodes);
            return allNodes.size() > 0;
        }
    }
    
    static final class Shift extends InjectionPoint
    {
        private final InjectionPoint input;
        private final int shift;
        
        public Shift(final InjectionPoint input, final int shift) {
            if (input == null) {
                throw new IllegalArgumentException("Must supply an input injection point for SHIFT");
            }
            this.input = input;
            this.shift = shift;
        }
        
        @Override
        public String toString() {
            return "InjectionPoint(" + this.getClass().getSimpleName() + ")[" + this.input + "]";
        }
        
        @Override
        public boolean find(final String desc, final InsnList insns, final Collection<AbstractInsnNode> nodes) {
            final List<AbstractInsnNode> list = (nodes instanceof List) ? ((List)nodes) : new ArrayList<AbstractInsnNode>(nodes);
            this.input.find(desc, insns, nodes);
            for (int i = 0; i < list.size(); ++i) {
                list.set(i, insns.get(insns.indexOf(list.get(i)) + this.shift));
            }
            if (nodes != list) {
                nodes.clear();
                nodes.addAll(list);
            }
            return nodes.size() > 0;
        }
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE })
    public @interface AtCode {
        String value();
    }
}
