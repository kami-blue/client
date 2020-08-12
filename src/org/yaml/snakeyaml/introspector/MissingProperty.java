// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.introspector;

import java.util.Collections;
import java.lang.annotation.Annotation;
import java.util.List;

public class MissingProperty extends Property
{
    public MissingProperty(final String name) {
        super(name, Object.class);
    }
    
    @Override
    public Class<?>[] getActualTypeArguments() {
        return (Class<?>[])new Class[0];
    }
    
    @Override
    public void set(final Object object, final Object value) throws Exception {
    }
    
    @Override
    public Object get(final Object object) {
        return object;
    }
    
    @Override
    public List<Annotation> getAnnotations() {
        return Collections.emptyList();
    }
    
    @Override
    public <A extends Annotation> A getAnnotation(final Class<A> annotationType) {
        return null;
    }
}
