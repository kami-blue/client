// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer;

import java.util.HashMap;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import com.google.common.base.Strings;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.util.Counter;
import java.util.Map;
import java.util.List;
import org.apache.logging.log4j.Logger;

public class MethodMapper
{
    private static final Logger logger;
    private static final List<String> classes;
    private static final Map<String, Counter> methods;
    private final ClassInfo info;
    
    public MethodMapper(final MixinEnvironment env, final ClassInfo info) {
        this.info = info;
    }
    
    public ClassInfo getClassInfo() {
        return this.info;
    }
    
    public void remapHandlerMethod(final MixinInfo mixin, final MethodNode handler, final ClassInfo.Method method) {
        if (!(handler instanceof MixinInfo.MixinMethodNode) || !((MixinInfo.MixinMethodNode)handler).isInjector()) {
            return;
        }
        if (method.isUnique()) {
            MethodMapper.logger.warn("Redundant @Unique on injector method {} in {}. Injectors are implicitly unique", new Object[] { method, mixin });
        }
        if (method.isRenamed()) {
            handler.name = method.getName();
            return;
        }
        final String handlerName = this.getHandlerName((MixinInfo.MixinMethodNode)handler);
        handler.name = method.renameTo(handlerName);
    }
    
    public String getHandlerName(final MixinInfo.MixinMethodNode method) {
        final String prefix = InjectionInfo.getInjectorPrefix(method.getInjectorAnnotation());
        final String classUID = getClassUID(method.getOwner().getClassRef());
        final String methodUID = getMethodUID(method.name, method.desc, !method.isSurrogate());
        return String.format("%s$%s$%s%s", prefix, method.name, classUID, methodUID);
    }
    
    private static String getClassUID(final String classRef) {
        int index = MethodMapper.classes.indexOf(classRef);
        if (index < 0) {
            index = MethodMapper.classes.size();
            MethodMapper.classes.add(classRef);
        }
        return finagle(index);
    }
    
    private static String getMethodUID(final String name, final String desc, final boolean increment) {
        final String descriptor = String.format("%s%s", name, desc);
        Counter id = MethodMapper.methods.get(descriptor);
        if (id == null) {
            id = new Counter();
            MethodMapper.methods.put(descriptor, id);
        }
        else if (increment) {
            final Counter counter = id;
            ++counter.value;
        }
        return String.format("%03x", id.value);
    }
    
    private static String finagle(final int index) {
        final String hex = Integer.toHexString(index);
        final StringBuilder sb = new StringBuilder();
        for (int pos = 0; pos < hex.length(); ++pos) {
            char c = hex.charAt(pos);
            sb.append(c += ((c < ':') ? '1' : '\n'));
        }
        return Strings.padStart(sb.toString(), 3, 'z');
    }
    
    static {
        logger = LogManager.getLogger("mixin");
        classes = new ArrayList<String>();
        methods = new HashMap<String, Counter>();
    }
}
