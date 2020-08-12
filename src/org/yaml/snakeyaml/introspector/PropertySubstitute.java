// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.introspector;

import java.util.logging.Level;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.lang.annotation.Annotation;
import java.util.List;
import org.yaml.snakeyaml.error.YAMLException;
import java.util.Iterator;
import java.lang.reflect.Array;
import java.util.Map;
import java.util.Collection;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class PropertySubstitute extends Property
{
    private static final Logger log;
    protected Class<?> targetType;
    private final String readMethod;
    private final String writeMethod;
    private transient Method read;
    private transient Method write;
    private Field field;
    protected Class<?>[] parameters;
    private Property delegate;
    private boolean filler;
    
    public PropertySubstitute(final String name, final Class<?> type, final String readMethod, final String writeMethod, final Class<?>... params) {
        super(name, type);
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;
        this.setActualTypeArguments(params);
        this.filler = false;
    }
    
    public PropertySubstitute(final String name, final Class<?> type, final Class<?>... params) {
        this(name, type, null, (String)null, params);
    }
    
    @Override
    public Class<?>[] getActualTypeArguments() {
        if (this.parameters == null && this.delegate != null) {
            return this.delegate.getActualTypeArguments();
        }
        return this.parameters;
    }
    
    public void setActualTypeArguments(final Class<?>... args) {
        if (args != null && args.length > 0) {
            this.parameters = args;
        }
        else {
            this.parameters = null;
        }
    }
    
    @Override
    public void set(final Object object, final Object value) throws Exception {
        if (this.write != null) {
            if (!this.filler) {
                this.write.invoke(object, value);
            }
            else if (value != null) {
                if (value instanceof Collection) {
                    final Collection<?> collection = (Collection<?>)value;
                    for (final Object val : collection) {
                        this.write.invoke(object, val);
                    }
                }
                else if (value instanceof Map) {
                    final Map<?, ?> map = (Map<?, ?>)value;
                    for (final Map.Entry<?, ?> entry : map.entrySet()) {
                        this.write.invoke(object, entry.getKey(), entry.getValue());
                    }
                }
                else if (value.getClass().isArray()) {
                    for (int len = Array.getLength(value), i = 0; i < len; ++i) {
                        this.write.invoke(object, Array.get(value, i));
                    }
                }
            }
        }
        else if (this.field != null) {
            this.field.set(object, value);
        }
        else if (this.delegate != null) {
            this.delegate.set(object, value);
        }
        else {
            PropertySubstitute.log.warning("No setter/delegate for '" + this.getName() + "' on object " + object);
        }
    }
    
    @Override
    public Object get(final Object object) {
        try {
            if (this.read != null) {
                return this.read.invoke(object, new Object[0]);
            }
            if (this.field != null) {
                return this.field.get(object);
            }
        }
        catch (Exception e) {
            throw new YAMLException("Unable to find getter for property '" + this.getName() + "' on object " + object + ":" + e);
        }
        if (this.delegate != null) {
            return this.delegate.get(object);
        }
        throw new YAMLException("No getter or delegate for property '" + this.getName() + "' on object " + object);
    }
    
    @Override
    public List<Annotation> getAnnotations() {
        Annotation[] annotations = null;
        if (this.read != null) {
            annotations = this.read.getAnnotations();
        }
        else if (this.field != null) {
            annotations = this.field.getAnnotations();
        }
        return (annotations != null) ? Arrays.asList(annotations) : this.delegate.getAnnotations();
    }
    
    @Override
    public <A extends Annotation> A getAnnotation(final Class<A> annotationType) {
        A annotation;
        if (this.read != null) {
            annotation = this.read.getAnnotation(annotationType);
        }
        else if (this.field != null) {
            annotation = this.field.getAnnotation(annotationType);
        }
        else {
            annotation = this.delegate.getAnnotation(annotationType);
        }
        return annotation;
    }
    
    public void setTargetType(final Class<?> targetType) {
        if (this.targetType != targetType) {
            this.targetType = targetType;
            final String name = this.getName();
            for (Class<?> c = targetType; c != null; c = c.getSuperclass()) {
                final Field[] declaredFields = c.getDeclaredFields();
                final int length = declaredFields.length;
                int i = 0;
                while (i < length) {
                    final Field f = declaredFields[i];
                    if (f.getName().equals(name)) {
                        final int modifiers = f.getModifiers();
                        if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                            f.setAccessible(true);
                            this.field = f;
                            break;
                        }
                        break;
                    }
                    else {
                        ++i;
                    }
                }
            }
            if (this.field == null && PropertySubstitute.log.isLoggable(Level.FINE)) {
                PropertySubstitute.log.fine(String.format("Failed to find field for %s.%s", targetType.getName(), this.getName()));
            }
            if (this.readMethod != null) {
                this.read = this.discoverMethod(targetType, this.readMethod, (Class<?>[])new Class[0]);
            }
            if (this.writeMethod != null) {
                this.filler = false;
                this.write = this.discoverMethod(targetType, this.writeMethod, this.getType());
                if (this.write == null && this.parameters != null) {
                    this.filler = true;
                    this.write = this.discoverMethod(targetType, this.writeMethod, this.parameters);
                }
            }
        }
    }
    
    private Method discoverMethod(final Class<?> type, final String name, final Class<?>... params) {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            for (final Method method : c.getDeclaredMethods()) {
                if (name.equals(method.getName())) {
                    final Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == params.length) {
                        boolean found = true;
                        for (int i = 0; i < parameterTypes.length; ++i) {
                            if (!parameterTypes[i].isAssignableFrom(params[i])) {
                                found = false;
                            }
                        }
                        if (found) {
                            method.setAccessible(true);
                            return method;
                        }
                    }
                }
            }
        }
        if (PropertySubstitute.log.isLoggable(Level.FINE)) {
            PropertySubstitute.log.fine(String.format("Failed to find [%s(%d args)] for %s.%s", name, params.length, this.targetType.getName(), this.getName()));
        }
        return null;
    }
    
    @Override
    public String getName() {
        final String n = super.getName();
        if (n != null) {
            return n;
        }
        return (this.delegate != null) ? this.delegate.getName() : null;
    }
    
    @Override
    public Class<?> getType() {
        final Class<?> t = super.getType();
        if (t != null) {
            return t;
        }
        return (this.delegate != null) ? this.delegate.getType() : null;
    }
    
    @Override
    public boolean isReadable() {
        return this.read != null || this.field != null || (this.delegate != null && this.delegate.isReadable());
    }
    
    @Override
    public boolean isWritable() {
        return this.write != null || this.field != null || (this.delegate != null && this.delegate.isWritable());
    }
    
    public void setDelegate(final Property delegate) {
        this.delegate = delegate;
        if (this.writeMethod != null && this.write == null && !this.filler) {
            this.filler = true;
            this.write = this.discoverMethod(this.targetType, this.writeMethod, this.getActualTypeArguments());
        }
    }
    
    static {
        log = Logger.getLogger(PropertySubstitute.class.getPackage().getName());
    }
}
