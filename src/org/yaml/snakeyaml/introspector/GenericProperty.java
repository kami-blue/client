// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.introspector;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class GenericProperty extends Property
{
    private Type genType;
    private boolean actualClassesChecked;
    private Class<?>[] actualClasses;
    
    public GenericProperty(final String name, final Class<?> aClass, final Type aType) {
        super(name, aClass);
        this.genType = aType;
        this.actualClassesChecked = (aType == null);
    }
    
    @Override
    public Class<?>[] getActualTypeArguments() {
        if (!this.actualClassesChecked) {
            if (this.genType instanceof ParameterizedType) {
                final ParameterizedType parameterizedType = (ParameterizedType)this.genType;
                final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length > 0) {
                    this.actualClasses = (Class<?>[])new Class[actualTypeArguments.length];
                    for (int i = 0; i < actualTypeArguments.length; ++i) {
                        if (actualTypeArguments[i] instanceof Class) {
                            this.actualClasses[i] = (Class<?>)actualTypeArguments[i];
                        }
                        else if (actualTypeArguments[i] instanceof ParameterizedType) {
                            this.actualClasses[i] = (Class<?>)((ParameterizedType)actualTypeArguments[i]).getRawType();
                        }
                        else {
                            if (!(actualTypeArguments[i] instanceof GenericArrayType)) {
                                this.actualClasses = null;
                                break;
                            }
                            final Type componentType = ((GenericArrayType)actualTypeArguments[i]).getGenericComponentType();
                            if (!(componentType instanceof Class)) {
                                this.actualClasses = null;
                                break;
                            }
                            this.actualClasses[i] = Array.newInstance((Class<?>)componentType, 0).getClass();
                        }
                    }
                }
            }
            else if (this.genType instanceof GenericArrayType) {
                final Type componentType2 = ((GenericArrayType)this.genType).getGenericComponentType();
                if (componentType2 instanceof Class) {
                    this.actualClasses = (Class<?>[])new Class[] { (Class)componentType2 };
                }
            }
            else if (this.genType instanceof Class) {
                final Class<?> classType = (Class<?>)this.genType;
                if (classType.isArray()) {
                    (this.actualClasses = (Class<?>[])new Class[1])[0] = this.getType().getComponentType();
                }
            }
            this.actualClassesChecked = true;
        }
        return this.actualClasses;
    }
}
