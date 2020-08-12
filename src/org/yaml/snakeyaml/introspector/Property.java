// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.introspector;

import java.lang.annotation.Annotation;
import java.util.List;

public abstract class Property implements Comparable<Property>
{
    private final String name;
    private final Class<?> type;
    
    public Property(final String name, final Class<?> type) {
        this.name = name;
        this.type = type;
    }
    
    public Class<?> getType() {
        return this.type;
    }
    
    public abstract Class<?>[] getActualTypeArguments();
    
    public String getName() {
        return this.name;
    }
    
    @Override
    public String toString() {
        return this.getName() + " of " + this.getType();
    }
    
    @Override
    public int compareTo(final Property o) {
        return this.getName().compareTo(o.getName());
    }
    
    public boolean isWritable() {
        return true;
    }
    
    public boolean isReadable() {
        return true;
    }
    
    public abstract void set(final Object p0, final Object p1) throws Exception;
    
    public abstract Object get(final Object p0);
    
    public abstract List<Annotation> getAnnotations();
    
    public abstract <A extends Annotation> A getAnnotation(final Class<A> p0);
    
    @Override
    public int hashCode() {
        return this.getName().hashCode() + this.getType().hashCode();
    }
    
    @Override
    public boolean equals(final Object other) {
        if (other instanceof Property) {
            final Property p = (Property)other;
            return this.getName().equals(p.getName()) && this.getType().equals(p.getType());
        }
        return false;
    }
}
