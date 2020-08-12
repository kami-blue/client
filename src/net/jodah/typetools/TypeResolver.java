// 
// Decompiled by Procyon v0.5.36
// 

package net.jodah.typetools;

import java.lang.reflect.AccessibleObject;
import java.security.AccessController;
import java.lang.reflect.Field;
import java.security.PrivilegedExceptionAction;
import sun.misc.Unsafe;
import java.util.Collections;
import java.util.WeakHashMap;
import java.lang.reflect.Member;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.lang.reflect.Modifier;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.ref.Reference;
import java.util.Map;

public final class TypeResolver
{
    private static final Map<Class<?>, Reference<Map<TypeVariable<?>, Type>>> TYPE_VARIABLE_CACHE;
    private static volatile boolean CACHE_ENABLED;
    private static boolean RESOLVES_LAMBDAS;
    private static Method GET_CONSTANT_POOL;
    private static Method GET_CONSTANT_POOL_SIZE;
    private static Method GET_CONSTANT_POOL_METHOD_AT;
    private static final Map<String, Method> OBJECT_METHODS;
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS;
    private static final Double JAVA_VERSION;
    
    private TypeResolver() {
    }
    
    public static void enableCache() {
        TypeResolver.CACHE_ENABLED = true;
    }
    
    public static void disableCache() {
        TypeResolver.TYPE_VARIABLE_CACHE.clear();
        TypeResolver.CACHE_ENABLED = false;
    }
    
    public static <T, S extends T> Class<?> resolveRawArgument(final Class<T> type, final Class<S> subType) {
        return resolveRawArgument(resolveGenericType(type, subType), subType);
    }
    
    public static Class<?> resolveRawArgument(final Type genericType, final Class<?> subType) {
        final Class<?>[] arguments = resolveRawArguments(genericType, subType);
        if (arguments == null) {
            return Unknown.class;
        }
        if (arguments.length != 1) {
            throw new IllegalArgumentException("Expected 1 argument for generic type " + genericType + " but found " + arguments.length);
        }
        return arguments[0];
    }
    
    public static <T, S extends T> Class<?>[] resolveRawArguments(final Class<T> type, final Class<S> subType) {
        return resolveRawArguments(resolveGenericType(type, subType), subType);
    }
    
    public static Class<?>[] resolveRawArguments(final Type genericType, final Class<?> subType) {
        Class<?>[] result = null;
        Class<?> functionalInterface = null;
        if (TypeResolver.RESOLVES_LAMBDAS && subType.isSynthetic()) {
            final Class<?> fi = (Class<?>)((genericType instanceof ParameterizedType && ((ParameterizedType)genericType).getRawType() instanceof Class) ? ((ParameterizedType)genericType).getRawType() : ((genericType instanceof Class) ? ((Class)genericType) : null));
            if (fi != null && fi.isInterface()) {
                functionalInterface = fi;
            }
        }
        if (genericType instanceof ParameterizedType) {
            final ParameterizedType paramType = (ParameterizedType)genericType;
            final Type[] arguments = paramType.getActualTypeArguments();
            result = (Class<?>[])new Class[arguments.length];
            for (int i = 0; i < arguments.length; ++i) {
                result[i] = resolveRawClass(arguments[i], subType, functionalInterface);
            }
        }
        else if (genericType instanceof TypeVariable) {
            result = (Class<?>[])new Class[] { resolveRawClass(genericType, subType, functionalInterface) };
        }
        else if (genericType instanceof Class) {
            final TypeVariable<?>[] typeParams = (TypeVariable<?>[])((Class)genericType).getTypeParameters();
            result = (Class<?>[])new Class[typeParams.length];
            for (int j = 0; j < typeParams.length; ++j) {
                result[j] = resolveRawClass(typeParams[j], subType, functionalInterface);
            }
        }
        return result;
    }
    
    public static Type resolveGenericType(final Class<?> type, final Type subType) {
        Class<?> rawType;
        if (subType instanceof ParameterizedType) {
            rawType = (Class<?>)((ParameterizedType)subType).getRawType();
        }
        else {
            rawType = (Class<?>)subType;
        }
        if (type.equals(rawType)) {
            return subType;
        }
        if (type.isInterface()) {
            for (final Type superInterface : rawType.getGenericInterfaces()) {
                final Type result;
                if (superInterface != null && !superInterface.equals(Object.class) && (result = resolveGenericType(type, superInterface)) != null) {
                    return result;
                }
            }
        }
        final Type superClass = rawType.getGenericSuperclass();
        Type result;
        if (superClass != null && !superClass.equals(Object.class) && (result = resolveGenericType(type, superClass)) != null) {
            return result;
        }
        return null;
    }
    
    public static Class<?> resolveRawClass(final Type genericType, final Class<?> subType) {
        return resolveRawClass(genericType, subType, null);
    }
    
    private static Class<?> resolveRawClass(Type genericType, final Class<?> subType, final Class<?> functionalInterface) {
        if (genericType instanceof Class) {
            return (Class<?>)genericType;
        }
        if (genericType instanceof ParameterizedType) {
            return resolveRawClass(((ParameterizedType)genericType).getRawType(), subType, functionalInterface);
        }
        if (genericType instanceof GenericArrayType) {
            final GenericArrayType arrayType = (GenericArrayType)genericType;
            final Class<?> component = resolveRawClass(arrayType.getGenericComponentType(), subType, functionalInterface);
            return Array.newInstance(component, 0).getClass();
        }
        if (genericType instanceof TypeVariable) {
            final TypeVariable<?> variable = (TypeVariable<?>)genericType;
            genericType = getTypeVariableMap(subType, functionalInterface).get(variable);
            genericType = ((genericType == null) ? resolveBound(variable) : resolveRawClass(genericType, subType, functionalInterface));
        }
        return (Class<?>)((genericType instanceof Class) ? ((Class)genericType) : Unknown.class);
    }
    
    private static Map<TypeVariable<?>, Type> getTypeVariableMap(final Class<?> targetType, final Class<?> functionalInterface) {
        final Reference<Map<TypeVariable<?>, Type>> ref = TypeResolver.TYPE_VARIABLE_CACHE.get(targetType);
        Map<TypeVariable<?>, Type> map = (ref != null) ? ref.get() : null;
        if (map == null) {
            map = new HashMap<TypeVariable<?>, Type>();
            if (functionalInterface != null) {
                populateLambdaArgs(functionalInterface, targetType, map);
            }
            populateSuperTypeArgs(targetType.getGenericInterfaces(), map, functionalInterface != null);
            Type genericType = targetType.getGenericSuperclass();
            for (Class<?> type = targetType.getSuperclass(); type != null && !Object.class.equals(type); type = type.getSuperclass()) {
                if (genericType instanceof ParameterizedType) {
                    populateTypeArgs((ParameterizedType)genericType, map, false);
                }
                populateSuperTypeArgs(type.getGenericInterfaces(), map, false);
                genericType = type.getGenericSuperclass();
            }
            for (Class<?> type = targetType; type.isMemberClass(); type = type.getEnclosingClass()) {
                genericType = type.getGenericSuperclass();
                if (genericType instanceof ParameterizedType) {
                    populateTypeArgs((ParameterizedType)genericType, map, functionalInterface != null);
                }
            }
            if (TypeResolver.CACHE_ENABLED) {
                TypeResolver.TYPE_VARIABLE_CACHE.put(targetType, new WeakReference<Map<TypeVariable<?>, Type>>(map));
            }
        }
        return map;
    }
    
    private static void populateSuperTypeArgs(final Type[] types, final Map<TypeVariable<?>, Type> map, final boolean depthFirst) {
        for (final Type type : types) {
            if (type instanceof ParameterizedType) {
                final ParameterizedType parameterizedType = (ParameterizedType)type;
                if (!depthFirst) {
                    populateTypeArgs(parameterizedType, map, depthFirst);
                }
                final Type rawType = parameterizedType.getRawType();
                if (rawType instanceof Class) {
                    populateSuperTypeArgs(((Class)rawType).getGenericInterfaces(), map, depthFirst);
                }
                if (depthFirst) {
                    populateTypeArgs(parameterizedType, map, depthFirst);
                }
            }
            else if (type instanceof Class) {
                populateSuperTypeArgs(((Class)type).getGenericInterfaces(), map, depthFirst);
            }
        }
    }
    
    private static void populateTypeArgs(final ParameterizedType type, final Map<TypeVariable<?>, Type> map, final boolean depthFirst) {
        if (type.getRawType() instanceof Class) {
            final TypeVariable<?>[] typeVariables = (TypeVariable<?>[])((Class)type.getRawType()).getTypeParameters();
            final Type[] typeArguments = type.getActualTypeArguments();
            if (type.getOwnerType() != null) {
                final Type owner = type.getOwnerType();
                if (owner instanceof ParameterizedType) {
                    populateTypeArgs((ParameterizedType)owner, map, depthFirst);
                }
            }
            for (int i = 0; i < typeArguments.length; ++i) {
                final TypeVariable<?> variable = typeVariables[i];
                final Type typeArgument = typeArguments[i];
                if (typeArgument instanceof Class) {
                    map.put(variable, typeArgument);
                }
                else if (typeArgument instanceof GenericArrayType) {
                    map.put(variable, typeArgument);
                }
                else if (typeArgument instanceof ParameterizedType) {
                    map.put(variable, typeArgument);
                }
                else if (typeArgument instanceof TypeVariable) {
                    final TypeVariable<?> typeVariableArgument = (TypeVariable<?>)typeArgument;
                    if (depthFirst) {
                        final Type existingType = map.get(variable);
                        if (existingType != null) {
                            map.put(typeVariableArgument, existingType);
                            continue;
                        }
                    }
                    Type resolvedType = map.get(typeVariableArgument);
                    if (resolvedType == null) {
                        resolvedType = resolveBound(typeVariableArgument);
                    }
                    map.put(variable, resolvedType);
                }
            }
        }
    }
    
    public static Type resolveBound(final TypeVariable<?> typeVariable) {
        final Type[] bounds = typeVariable.getBounds();
        if (bounds.length == 0) {
            return Unknown.class;
        }
        Type bound = bounds[0];
        if (bound instanceof TypeVariable) {
            bound = resolveBound((TypeVariable<?>)bound);
        }
        return (bound == Object.class) ? Unknown.class : bound;
    }
    
    private static void populateLambdaArgs(final Class<?> functionalInterface, final Class<?> lambdaType, final Map<TypeVariable<?>, Type> map) {
        if (TypeResolver.RESOLVES_LAMBDAS) {
            for (final Method m : functionalInterface.getMethods()) {
                if (!isDefaultMethod(m) && !Modifier.isStatic(m.getModifiers()) && !m.isBridge()) {
                    final Method objectMethod = TypeResolver.OBJECT_METHODS.get(m.getName());
                    if (objectMethod == null || !Arrays.equals(m.getTypeParameters(), objectMethod.getTypeParameters())) {
                        final Type returnTypeVar = m.getGenericReturnType();
                        final Type[] paramTypeVars = m.getGenericParameterTypes();
                        final Member member = getMemberRef(lambdaType);
                        if (member == null) {
                            return;
                        }
                        if (returnTypeVar instanceof TypeVariable) {
                            Class<?> returnType = (member instanceof Method) ? ((Method)member).getReturnType() : ((Constructor)member).getDeclaringClass();
                            returnType = wrapPrimitives(returnType);
                            if (!returnType.equals(Void.class)) {
                                map.put((TypeVariable<?>)returnTypeVar, returnType);
                            }
                        }
                        final Class<?>[] arguments = (member instanceof Method) ? ((Method)member).getParameterTypes() : ((Constructor)member).getParameterTypes();
                        int paramOffset = 0;
                        if (paramTypeVars.length > 0 && paramTypeVars[0] instanceof TypeVariable && paramTypeVars.length == arguments.length + 1) {
                            final Class<?> instanceType = member.getDeclaringClass();
                            map.put((TypeVariable<?>)paramTypeVars[0], instanceType);
                            paramOffset = 1;
                        }
                        int argOffset = 0;
                        if (paramTypeVars.length < arguments.length) {
                            argOffset = arguments.length - paramTypeVars.length;
                        }
                        for (int i = 0; i + argOffset < arguments.length; ++i) {
                            if (paramTypeVars[i] instanceof TypeVariable) {
                                map.put((TypeVariable<?>)paramTypeVars[i + paramOffset], wrapPrimitives(arguments[i + argOffset]));
                            }
                        }
                        return;
                    }
                }
            }
        }
    }
    
    private static boolean isDefaultMethod(final Method m) {
        return TypeResolver.JAVA_VERSION >= 1.8 && m.isDefault();
    }
    
    private static Member getMemberRef(final Class<?> type) {
        Object constantPool;
        try {
            constantPool = TypeResolver.GET_CONSTANT_POOL.invoke(type, new Object[0]);
        }
        catch (Exception ignore) {
            return null;
        }
        Member result = null;
        for (int i = getConstantPoolSize(constantPool) - 1; i >= 0; --i) {
            final Member member = getConstantPoolMethodAt(constantPool, i);
            if (member != null && (!(member instanceof Constructor) || !member.getDeclaringClass().getName().equals("java.lang.invoke.SerializedLambda"))) {
                if (!member.getDeclaringClass().isAssignableFrom(type)) {
                    result = member;
                    if (!(member instanceof Method)) {
                        break;
                    }
                    if (!isAutoBoxingMethod((Method)member)) {
                        break;
                    }
                }
            }
        }
        return result;
    }
    
    private static boolean isAutoBoxingMethod(final Method method) {
        final Class<?>[] parameters = method.getParameterTypes();
        return method.getName().equals("valueOf") && parameters.length == 1 && parameters[0].isPrimitive() && wrapPrimitives(parameters[0]).equals(method.getDeclaringClass());
    }
    
    private static Class<?> wrapPrimitives(final Class<?> clazz) {
        return clazz.isPrimitive() ? TypeResolver.PRIMITIVE_WRAPPERS.get(clazz) : clazz;
    }
    
    private static int getConstantPoolSize(final Object constantPool) {
        try {
            return (int)TypeResolver.GET_CONSTANT_POOL_SIZE.invoke(constantPool, new Object[0]);
        }
        catch (Exception ignore) {
            return 0;
        }
    }
    
    private static Member getConstantPoolMethodAt(final Object constantPool, final int i) {
        try {
            return (Member)TypeResolver.GET_CONSTANT_POOL_METHOD_AT.invoke(constantPool, i);
        }
        catch (Exception ignore) {
            return null;
        }
    }
    
    static {
        TYPE_VARIABLE_CACHE = Collections.synchronizedMap(new WeakHashMap<Class<?>, Reference<Map<TypeVariable<?>, Type>>>());
        TypeResolver.CACHE_ENABLED = true;
        OBJECT_METHODS = new HashMap<String, Method>();
        JAVA_VERSION = Double.parseDouble(System.getProperty("java.specification.version", "0"));
        try {
            final Unsafe unsafe = AccessController.doPrivileged((PrivilegedExceptionAction<Unsafe>)new PrivilegedExceptionAction<Unsafe>() {
                @Override
                public Unsafe run() throws Exception {
                    final Field f = Unsafe.class.getDeclaredField("theUnsafe");
                    f.setAccessible(true);
                    return (Unsafe)f.get(null);
                }
            });
            TypeResolver.GET_CONSTANT_POOL = Class.class.getDeclaredMethod("getConstantPool", (Class<?>[])new Class[0]);
            final String constantPoolName = (TypeResolver.JAVA_VERSION < 9.0) ? "sun.reflect.ConstantPool" : "jdk.internal.reflect.ConstantPool";
            final Class<?> constantPoolClass = Class.forName(constantPoolName);
            TypeResolver.GET_CONSTANT_POOL_SIZE = constantPoolClass.getDeclaredMethod("getSize", (Class<?>[])new Class[0]);
            TypeResolver.GET_CONSTANT_POOL_METHOD_AT = constantPoolClass.getDeclaredMethod("getMethodAt", Integer.TYPE);
            final Field overrideField = AccessibleObject.class.getDeclaredField("override");
            final long overrideFieldOffset = unsafe.objectFieldOffset(overrideField);
            unsafe.putBoolean(TypeResolver.GET_CONSTANT_POOL, overrideFieldOffset, true);
            unsafe.putBoolean(TypeResolver.GET_CONSTANT_POOL_SIZE, overrideFieldOffset, true);
            unsafe.putBoolean(TypeResolver.GET_CONSTANT_POOL_METHOD_AT, overrideFieldOffset, true);
            final Object constantPool = TypeResolver.GET_CONSTANT_POOL.invoke(Object.class, new Object[0]);
            TypeResolver.GET_CONSTANT_POOL_SIZE.invoke(constantPool, new Object[0]);
            for (final Method method : Object.class.getDeclaredMethods()) {
                TypeResolver.OBJECT_METHODS.put(method.getName(), method);
            }
            TypeResolver.RESOLVES_LAMBDAS = true;
        }
        catch (Exception ex) {}
        final Map<Class<?>, Class<?>> types = new HashMap<Class<?>, Class<?>>();
        types.put(Boolean.TYPE, Boolean.class);
        types.put(Byte.TYPE, Byte.class);
        types.put(Character.TYPE, Character.class);
        types.put(Double.TYPE, Double.class);
        types.put(Float.TYPE, Float.class);
        types.put(Integer.TYPE, Integer.class);
        types.put(Long.TYPE, Long.class);
        types.put(Short.TYPE, Short.class);
        types.put(Void.TYPE, Void.class);
        PRIMITIVE_WRAPPERS = Collections.unmodifiableMap((Map<? extends Class<?>, ? extends Class<?>>)types);
    }
    
    public static final class Unknown
    {
        private Unknown() {
        }
    }
}
