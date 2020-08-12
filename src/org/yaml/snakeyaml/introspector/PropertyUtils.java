// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.introspector;

import java.util.Iterator;
import java.util.Collection;
import java.util.TreeSet;
import java.lang.reflect.InvocationTargetException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.beans.IntrospectionException;
import org.yaml.snakeyaml.error.YAMLException;
import java.beans.FeatureDescriptor;
import java.beans.Introspector;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.lang.reflect.Method;
import org.yaml.snakeyaml.util.PlatformFeatureDetector;
import java.util.Set;
import java.util.Map;
import java.util.logging.Logger;

public class PropertyUtils
{
    private static final Logger log;
    private final Map<Class<?>, Map<String, Property>> propertiesCache;
    private final Map<Class<?>, Set<Property>> readableProperties;
    private BeanAccess beanAccess;
    private boolean allowReadOnlyProperties;
    private boolean skipMissingProperties;
    private PlatformFeatureDetector platformFeatureDetector;
    private boolean transientMethodChecked;
    private Method isTransientMethod;
    
    public PropertyUtils() {
        this(new PlatformFeatureDetector());
    }
    
    PropertyUtils(final PlatformFeatureDetector platformFeatureDetector) {
        this.propertiesCache = new HashMap<Class<?>, Map<String, Property>>();
        this.readableProperties = new HashMap<Class<?>, Set<Property>>();
        this.beanAccess = BeanAccess.DEFAULT;
        this.allowReadOnlyProperties = false;
        this.skipMissingProperties = false;
        this.platformFeatureDetector = platformFeatureDetector;
        if (platformFeatureDetector.isRunningOnAndroid()) {
            this.beanAccess = BeanAccess.FIELD;
        }
    }
    
    protected Map<String, Property> getPropertiesMap(final Class<?> type, final BeanAccess bAccess) {
        if (this.propertiesCache.containsKey(type)) {
            return this.propertiesCache.get(type);
        }
        final Map<String, Property> properties = new LinkedHashMap<String, Property>();
        boolean inaccessableFieldsExist = false;
        switch (bAccess) {
            case FIELD: {
                for (Class<?> c = type; c != null; c = c.getSuperclass()) {
                    for (final Field field : c.getDeclaredFields()) {
                        final int modifiers = field.getModifiers();
                        if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers) && !properties.containsKey(field.getName())) {
                            properties.put(field.getName(), new FieldProperty(field));
                        }
                    }
                }
                break;
            }
            default: {
                try {
                    for (final PropertyDescriptor property : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
                        final Method readMethod = property.getReadMethod();
                        if ((readMethod == null || !readMethod.getName().equals("getClass")) && !this.isTransient(property)) {
                            properties.put(property.getName(), new MethodProperty(property));
                        }
                    }
                }
                catch (IntrospectionException e) {
                    throw new YAMLException(e);
                }
                for (Class<?> c = type; c != null; c = c.getSuperclass()) {
                    for (final Field field : c.getDeclaredFields()) {
                        final int modifiers = field.getModifiers();
                        if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                            if (Modifier.isPublic(modifiers)) {
                                properties.put(field.getName(), new FieldProperty(field));
                            }
                            else {
                                inaccessableFieldsExist = true;
                            }
                        }
                    }
                }
                break;
            }
        }
        if (properties.isEmpty() && inaccessableFieldsExist) {
            throw new YAMLException("No JavaBean properties found in " + type.getName());
        }
        this.propertiesCache.put(type, properties);
        return properties;
    }
    
    private boolean isTransient(final FeatureDescriptor fd) {
        if (!this.transientMethodChecked) {
            this.transientMethodChecked = true;
            try {
                (this.isTransientMethod = FeatureDescriptor.class.getDeclaredMethod("isTransient", (Class<?>[])new Class[0])).setAccessible(true);
            }
            catch (NoSuchMethodException e5) {
                PropertyUtils.log.fine("NoSuchMethod: FeatureDescriptor.isTransient(). Don't check it anymore.");
            }
            catch (SecurityException e) {
                e.printStackTrace();
                this.isTransientMethod = null;
            }
        }
        if (this.isTransientMethod != null) {
            try {
                return Boolean.TRUE.equals(this.isTransientMethod.invoke(fd, new Object[0]));
            }
            catch (IllegalAccessException e2) {
                e2.printStackTrace();
            }
            catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            }
            catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
            this.isTransientMethod = null;
        }
        return false;
    }
    
    public Set<Property> getProperties(final Class<?> type) {
        return this.getProperties(type, this.beanAccess);
    }
    
    public Set<Property> getProperties(final Class<?> type, final BeanAccess bAccess) {
        if (this.readableProperties.containsKey(type)) {
            return this.readableProperties.get(type);
        }
        final Set<Property> properties = this.createPropertySet(type, bAccess);
        this.readableProperties.put(type, properties);
        return properties;
    }
    
    protected Set<Property> createPropertySet(final Class<?> type, final BeanAccess bAccess) {
        final Set<Property> properties = new TreeSet<Property>();
        final Collection<Property> props = this.getPropertiesMap(type, bAccess).values();
        for (final Property property : props) {
            if (property.isReadable() && (this.allowReadOnlyProperties || property.isWritable())) {
                properties.add(property);
            }
        }
        return properties;
    }
    
    public Property getProperty(final Class<?> type, final String name) {
        return this.getProperty(type, name, this.beanAccess);
    }
    
    public Property getProperty(final Class<?> type, final String name, final BeanAccess bAccess) {
        final Map<String, Property> properties = this.getPropertiesMap(type, bAccess);
        Property property = properties.get(name);
        if (property == null && this.skipMissingProperties) {
            property = new MissingProperty(name);
        }
        if (property == null) {
            throw new YAMLException("Unable to find property '" + name + "' on class: " + type.getName());
        }
        return property;
    }
    
    public void setBeanAccess(final BeanAccess beanAccess) {
        if (this.platformFeatureDetector.isRunningOnAndroid() && beanAccess != BeanAccess.FIELD) {
            throw new IllegalArgumentException("JVM is Android - only BeanAccess.FIELD is available");
        }
        if (this.beanAccess != beanAccess) {
            this.beanAccess = beanAccess;
            this.propertiesCache.clear();
            this.readableProperties.clear();
        }
    }
    
    public void setAllowReadOnlyProperties(final boolean allowReadOnlyProperties) {
        if (this.allowReadOnlyProperties != allowReadOnlyProperties) {
            this.allowReadOnlyProperties = allowReadOnlyProperties;
            this.readableProperties.clear();
        }
    }
    
    public boolean isAllowReadOnlyProperties() {
        return this.allowReadOnlyProperties;
    }
    
    public void setSkipMissingProperties(final boolean skipMissingProperties) {
        if (this.skipMissingProperties != skipMissingProperties) {
            this.skipMissingProperties = skipMissingProperties;
            this.readableProperties.clear();
        }
    }
    
    public boolean isSkipMissingProperties() {
        return this.skipMissingProperties;
    }
    
    static {
        log = Logger.getLogger(PropertyUtils.class.getPackage().getName());
    }
}
