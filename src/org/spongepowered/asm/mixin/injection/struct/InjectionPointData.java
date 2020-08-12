// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.struct;

import com.google.common.base.Joiner;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionPointException;
import org.spongepowered.asm.mixin.injection.modify.LocalVariableDiscriminator;
import org.spongepowered.asm.lib.Type;
import java.util.Iterator;
import java.util.regex.Matcher;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.List;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.refmap.IMixinContext;
import java.util.Map;
import java.util.regex.Pattern;

public class InjectionPointData
{
    private static final Pattern AT_PATTERN;
    private final Map<String, String> args;
    private final IMixinContext context;
    private final MethodNode method;
    private final AnnotationNode parent;
    private final String at;
    private final String type;
    private final InjectionPoint.Selector selector;
    private final String target;
    private final String slice;
    private final int ordinal;
    private final int opcode;
    private final String id;
    
    public InjectionPointData(final IMixinContext context, final MethodNode method, final AnnotationNode parent, final String at, final List<String> args, final String target, final String slice, final int ordinal, final int opcode, final String id) {
        this.args = new HashMap<String, String>();
        this.context = context;
        this.method = method;
        this.parent = parent;
        this.at = at;
        this.target = target;
        this.slice = Strings.nullToEmpty(slice);
        this.ordinal = Math.max(-1, ordinal);
        this.opcode = opcode;
        this.id = id;
        this.parseArgs(args);
        this.args.put("target", target);
        this.args.put("ordinal", String.valueOf(ordinal));
        this.args.put("opcode", String.valueOf(opcode));
        final Matcher matcher = InjectionPointData.AT_PATTERN.matcher(at);
        this.type = parseType(matcher, at);
        this.selector = parseSelector(matcher);
    }
    
    private void parseArgs(final List<String> args) {
        if (args == null) {
            return;
        }
        for (final String arg : args) {
            if (arg != null) {
                final int eqPos = arg.indexOf(61);
                if (eqPos > -1) {
                    this.args.put(arg.substring(0, eqPos), arg.substring(eqPos + 1));
                }
                else {
                    this.args.put(arg, "");
                }
            }
        }
    }
    
    public String getAt() {
        return this.at;
    }
    
    public String getType() {
        return this.type;
    }
    
    public InjectionPoint.Selector getSelector() {
        return this.selector;
    }
    
    public IMixinContext getContext() {
        return this.context;
    }
    
    public MethodNode getMethod() {
        return this.method;
    }
    
    public Type getMethodReturnType() {
        return Type.getReturnType(this.method.desc);
    }
    
    public AnnotationNode getParent() {
        return this.parent;
    }
    
    public String getSlice() {
        return this.slice;
    }
    
    public LocalVariableDiscriminator getLocalVariableDiscriminator() {
        return LocalVariableDiscriminator.parse(this.parent);
    }
    
    public String get(final String key, final String defaultValue) {
        final String value = this.args.get(key);
        return (value != null) ? value : defaultValue;
    }
    
    public int get(final String key, final int defaultValue) {
        return parseInt(this.get(key, String.valueOf(defaultValue)), defaultValue);
    }
    
    public boolean get(final String key, final boolean defaultValue) {
        return parseBoolean(this.get(key, String.valueOf(defaultValue)), defaultValue);
    }
    
    public MemberInfo get(final String key) {
        try {
            return MemberInfo.parseAndValidate(this.get(key, ""), this.context);
        }
        catch (InvalidMemberDescriptorException ex) {
            throw new InvalidInjectionPointException(this.context, "Failed parsing @At(\"%s\").%s descriptor \"%s\" on %s", new Object[] { this.at, key, this.target, InjectionInfo.describeInjector(this.context, this.parent, this.method) });
        }
    }
    
    public MemberInfo getTarget() {
        try {
            return MemberInfo.parseAndValidate(this.target, this.context);
        }
        catch (InvalidMemberDescriptorException ex) {
            throw new InvalidInjectionPointException(this.context, "Failed parsing @At(\"%s\") descriptor \"%s\" on %s", new Object[] { this.at, this.target, InjectionInfo.describeInjector(this.context, this.parent, this.method) });
        }
    }
    
    public int getOrdinal() {
        return this.ordinal;
    }
    
    public int getOpcode() {
        return this.opcode;
    }
    
    public int getOpcode(final int defaultOpcode) {
        return (this.opcode > 0) ? this.opcode : defaultOpcode;
    }
    
    public int getOpcode(final int defaultOpcode, final int... validOpcodes) {
        for (final int validOpcode : validOpcodes) {
            if (this.opcode == validOpcode) {
                return this.opcode;
            }
        }
        return defaultOpcode;
    }
    
    public String getId() {
        return this.id;
    }
    
    @Override
    public String toString() {
        return this.type;
    }
    
    private static Pattern createPattern() {
        return Pattern.compile(String.format("^([^:]+):?(%s)?$", Joiner.on('|').join((Object[])InjectionPoint.Selector.values())));
    }
    
    public static String parseType(final String at) {
        final Matcher matcher = InjectionPointData.AT_PATTERN.matcher(at);
        return parseType(matcher, at);
    }
    
    private static String parseType(final Matcher matcher, final String at) {
        return matcher.matches() ? matcher.group(1) : at;
    }
    
    private static InjectionPoint.Selector parseSelector(final Matcher matcher) {
        return (matcher.matches() && matcher.group(2) != null) ? InjectionPoint.Selector.valueOf(matcher.group(2)) : InjectionPoint.Selector.DEFAULT;
    }
    
    private static int parseInt(final String string, final int defaultValue) {
        try {
            return Integer.parseInt(string);
        }
        catch (Exception ex) {
            return defaultValue;
        }
    }
    
    private static boolean parseBoolean(final String string, final boolean defaultValue) {
        try {
            return Boolean.parseBoolean(string);
        }
        catch (Exception ex) {
            return defaultValue;
        }
    }
    
    static {
        AT_PATTERN = createPattern();
    }
}
