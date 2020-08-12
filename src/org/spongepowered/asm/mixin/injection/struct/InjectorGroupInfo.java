// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.struct;

import org.spongepowered.asm.lib.tree.AnnotationNode;
import java.lang.annotation.Annotation;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.lib.tree.MethodNode;
import java.util.HashMap;
import java.util.Iterator;
import org.spongepowered.asm.mixin.injection.throwables.InjectionValidationException;
import org.apache.logging.log4j.LogManager;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class InjectorGroupInfo
{
    private final String name;
    private final List<InjectionInfo> members;
    private final boolean isDefault;
    private int minCallbackCount;
    private int maxCallbackCount;
    
    public InjectorGroupInfo(final String name) {
        this(name, false);
    }
    
    InjectorGroupInfo(final String name, final boolean flag) {
        this.members = new ArrayList<InjectionInfo>();
        this.minCallbackCount = -1;
        this.maxCallbackCount = Integer.MAX_VALUE;
        this.name = name;
        this.isDefault = flag;
    }
    
    @Override
    public String toString() {
        return String.format("@Group(name=%s, min=%d, max=%d)", this.getName(), this.getMinRequired(), this.getMaxAllowed());
    }
    
    public boolean isDefault() {
        return this.isDefault;
    }
    
    public String getName() {
        return this.name;
    }
    
    public int getMinRequired() {
        return Math.max(this.minCallbackCount, 1);
    }
    
    public int getMaxAllowed() {
        return Math.min(this.maxCallbackCount, Integer.MAX_VALUE);
    }
    
    public Collection<InjectionInfo> getMembers() {
        return Collections.unmodifiableCollection((Collection<? extends InjectionInfo>)this.members);
    }
    
    public void setMinRequired(final int min) {
        if (min < 1) {
            throw new IllegalArgumentException("Cannot set zero or negative value for injector group min count. Attempted to set min=" + min + " on " + this);
        }
        if (this.minCallbackCount > 0 && this.minCallbackCount != min) {
            LogManager.getLogger("mixin").warn("Conflicting min value '{}' on @Group({}), previously specified {}", new Object[] { min, this.name, this.minCallbackCount });
        }
        this.minCallbackCount = Math.max(this.minCallbackCount, min);
    }
    
    public void setMaxAllowed(final int max) {
        if (max < 1) {
            throw new IllegalArgumentException("Cannot set zero or negative value for injector group max count. Attempted to set max=" + max + " on " + this);
        }
        if (this.maxCallbackCount < Integer.MAX_VALUE && this.maxCallbackCount != max) {
            LogManager.getLogger("mixin").warn("Conflicting max value '{}' on @Group({}), previously specified {}", new Object[] { max, this.name, this.maxCallbackCount });
        }
        this.maxCallbackCount = Math.min(this.maxCallbackCount, max);
    }
    
    public InjectorGroupInfo add(final InjectionInfo member) {
        this.members.add(member);
        return this;
    }
    
    public InjectorGroupInfo validate() throws InjectionValidationException {
        if (this.members.size() == 0) {
            return this;
        }
        int total = 0;
        for (final InjectionInfo member : this.members) {
            total += member.getInjectedCallbackCount();
        }
        final int min = this.getMinRequired();
        final int max = this.getMaxAllowed();
        if (total < min) {
            throw new InjectionValidationException(this, String.format("expected %d invocation(s) but only %d succeeded", min, total));
        }
        if (total > max) {
            throw new InjectionValidationException(this, String.format("maximum of %d invocation(s) allowed but %d succeeded", max, total));
        }
        return this;
    }
    
    public static final class Map extends HashMap<String, InjectorGroupInfo>
    {
        private static final long serialVersionUID = 1L;
        private static final InjectorGroupInfo NO_GROUP;
        
        @Override
        public InjectorGroupInfo get(final Object key) {
            return this.forName(key.toString());
        }
        
        public InjectorGroupInfo forName(final String name) {
            InjectorGroupInfo value = super.get(name);
            if (value == null) {
                value = new InjectorGroupInfo(name);
                this.put(name, value);
            }
            return value;
        }
        
        public InjectorGroupInfo parseGroup(final MethodNode method, final String defaultGroup) {
            return this.parseGroup(Annotations.getInvisible(method, Group.class), defaultGroup);
        }
        
        public InjectorGroupInfo parseGroup(final AnnotationNode annotation, final String defaultGroup) {
            if (annotation == null) {
                return Map.NO_GROUP;
            }
            String name = Annotations.getValue(annotation, "name");
            if (name == null || name.isEmpty()) {
                name = defaultGroup;
            }
            final InjectorGroupInfo groupInfo = this.forName(name);
            final Integer min = Annotations.getValue(annotation, "min");
            if (min != null && min != -1) {
                groupInfo.setMinRequired(min);
            }
            final Integer max = Annotations.getValue(annotation, "max");
            if (max != null && max != -1) {
                groupInfo.setMaxAllowed(max);
            }
            return groupInfo;
        }
        
        public void validateAll() throws InjectionValidationException {
            for (final InjectorGroupInfo group : ((HashMap<K, InjectorGroupInfo>)this).values()) {
                group.validate();
            }
        }
        
        static {
            NO_GROUP = new InjectorGroupInfo("NONE", true);
        }
    }
}
