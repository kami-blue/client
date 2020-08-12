// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.analysis;

import java.util.Iterator;
import java.util.HashMap;
import javassist.CtClass;
import java.util.Map;

public class MultiType extends Type
{
    private Map interfaces;
    private Type resolved;
    private Type potentialClass;
    private MultiType mergeSource;
    private boolean changed;
    
    public MultiType(final Map interfaces) {
        this(interfaces, null);
    }
    
    public MultiType(final Map interfaces, final Type potentialClass) {
        super(null);
        this.changed = false;
        this.interfaces = interfaces;
        this.potentialClass = potentialClass;
    }
    
    @Override
    public CtClass getCtClass() {
        if (this.resolved != null) {
            return this.resolved.getCtClass();
        }
        return Type.OBJECT.getCtClass();
    }
    
    @Override
    public Type getComponent() {
        return null;
    }
    
    @Override
    public int getSize() {
        return 1;
    }
    
    @Override
    public boolean isArray() {
        return false;
    }
    
    @Override
    boolean popChanged() {
        final boolean changed = this.changed;
        this.changed = false;
        return changed;
    }
    
    @Override
    public boolean isAssignableFrom(final Type type) {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    public boolean isAssignableTo(final Type type) {
        if (this.resolved != null) {
            return type.isAssignableFrom(this.resolved);
        }
        if (Type.OBJECT.equals(type)) {
            return true;
        }
        if (this.potentialClass != null && !type.isAssignableFrom(this.potentialClass)) {
            this.potentialClass = null;
        }
        final Map map = this.mergeMultiAndSingle(this, type);
        if (map.size() == 1 && this.potentialClass == null) {
            this.resolved = Type.get(map.values().iterator().next());
            this.propogateResolved();
            return true;
        }
        if (map.size() >= 1) {
            this.interfaces = map;
            this.propogateState();
            return true;
        }
        if (this.potentialClass != null) {
            this.resolved = this.potentialClass;
            this.propogateResolved();
            return true;
        }
        return false;
    }
    
    private void propogateState() {
        for (MultiType source = this.mergeSource; source != null; source = source.mergeSource) {
            source.interfaces = this.interfaces;
            source.potentialClass = this.potentialClass;
        }
    }
    
    private void propogateResolved() {
        for (MultiType source = this.mergeSource; source != null; source = source.mergeSource) {
            source.resolved = this.resolved;
        }
    }
    
    @Override
    public boolean isReference() {
        return true;
    }
    
    private Map getAllMultiInterfaces(final MultiType type) {
        final Map map = new HashMap();
        for (final CtClass intf : type.interfaces.values()) {
            map.put(intf.getName(), intf);
            this.getAllInterfaces(intf, map);
        }
        return map;
    }
    
    private Map mergeMultiInterfaces(final MultiType type1, final MultiType type2) {
        final Map map1 = this.getAllMultiInterfaces(type1);
        final Map map2 = this.getAllMultiInterfaces(type2);
        return this.findCommonInterfaces(map1, map2);
    }
    
    private Map mergeMultiAndSingle(final MultiType multi, final Type single) {
        final Map map1 = this.getAllMultiInterfaces(multi);
        final Map map2 = this.getAllInterfaces(single.getCtClass(), null);
        return this.findCommonInterfaces(map1, map2);
    }
    
    private boolean inMergeSource(MultiType source) {
        while (source != null) {
            if (source == this) {
                return true;
            }
            source = source.mergeSource;
        }
        return false;
    }
    
    @Override
    public Type merge(final Type type) {
        if (this == type) {
            return this;
        }
        if (type == MultiType.UNINIT) {
            return this;
        }
        if (type == MultiType.BOGUS) {
            return MultiType.BOGUS;
        }
        if (type == null) {
            return this;
        }
        if (this.resolved != null) {
            return this.resolved.merge(type);
        }
        if (this.potentialClass != null) {
            final Type mergePotential = this.potentialClass.merge(type);
            if (!mergePotential.equals(this.potentialClass) || mergePotential.popChanged()) {
                this.potentialClass = (Type.OBJECT.equals(mergePotential) ? null : mergePotential);
                this.changed = true;
            }
        }
        Map merged;
        if (type instanceof MultiType) {
            final MultiType multi = (MultiType)type;
            if (multi.resolved != null) {
                merged = this.mergeMultiAndSingle(this, multi.resolved);
            }
            else {
                merged = this.mergeMultiInterfaces(multi, this);
                if (!this.inMergeSource(multi)) {
                    this.mergeSource = multi;
                }
            }
        }
        else {
            merged = this.mergeMultiAndSingle(this, type);
        }
        if (merged.size() > 1 || (merged.size() == 1 && this.potentialClass != null)) {
            if (merged.size() != this.interfaces.size()) {
                this.changed = true;
            }
            else if (!this.changed) {
                final Iterator iter = merged.keySet().iterator();
                while (iter.hasNext()) {
                    if (!this.interfaces.containsKey(iter.next())) {
                        this.changed = true;
                    }
                }
            }
            this.interfaces = merged;
            this.propogateState();
            return this;
        }
        if (merged.size() == 1) {
            this.resolved = Type.get(merged.values().iterator().next());
        }
        else if (this.potentialClass != null) {
            this.resolved = this.potentialClass;
        }
        else {
            this.resolved = MultiType.OBJECT;
        }
        this.propogateResolved();
        return this.resolved;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof MultiType)) {
            return false;
        }
        final MultiType multi = (MultiType)o;
        if (this.resolved != null) {
            return this.resolved.equals(multi.resolved);
        }
        return multi.resolved == null && this.interfaces.keySet().equals(multi.interfaces.keySet());
    }
    
    @Override
    public String toString() {
        if (this.resolved != null) {
            return this.resolved.toString();
        }
        final StringBuffer buffer = new StringBuffer("{");
        final Iterator iter = this.interfaces.keySet().iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            buffer.append(", ");
        }
        buffer.setLength(buffer.length() - 2);
        if (this.potentialClass != null) {
            buffer.append(", *").append(this.potentialClass.toString());
        }
        buffer.append("}");
        return buffer.toString();
    }
}
