// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.util;

import com.google.common.base.Joiner;
import java.util.Arrays;
import javax.annotation.Nullable;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.io.IOException;
import org.reflections.Reflections;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import com.google.common.collect.Sets;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.List;
import org.reflections.ReflectionsException;
import org.reflections.ReflectionUtils;
import java.util.ArrayList;
import java.lang.reflect.Member;
import java.io.File;

public abstract class Utils
{
    public static String repeat(final String string, final int times) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; ++i) {
            sb.append(string);
        }
        return sb.toString();
    }
    
    public static boolean isEmpty(final String s) {
        return s == null || s.length() == 0;
    }
    
    public static boolean isEmpty(final Object[] objects) {
        return objects == null || objects.length == 0;
    }
    
    public static File prepareFile(final String filename) {
        final File file = new File(filename);
        final File parent = file.getAbsoluteFile().getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        return file;
    }
    
    public static Member getMemberFromDescriptor(final String descriptor, final ClassLoader... classLoaders) throws ReflectionsException {
        final int p0 = descriptor.lastIndexOf(40);
        final String memberKey = (p0 != -1) ? descriptor.substring(0, p0) : descriptor;
        final String methodParameters = (p0 != -1) ? descriptor.substring(p0 + 1, descriptor.lastIndexOf(41)) : "";
        final int p2 = Math.max(memberKey.lastIndexOf(46), memberKey.lastIndexOf("$"));
        final String className = memberKey.substring(memberKey.lastIndexOf(32) + 1, p2);
        final String memberName = memberKey.substring(p2 + 1);
        Class<?>[] parameterTypes = null;
        if (!isEmpty(methodParameters)) {
            final String[] parameterNames = methodParameters.split(",");
            final List<Class<?>> result = new ArrayList<Class<?>>(parameterNames.length);
            for (final String name : parameterNames) {
                result.add(ReflectionUtils.forName(name.trim(), classLoaders));
            }
            parameterTypes = result.toArray(new Class[result.size()]);
        }
        Class<?> aClass = ReflectionUtils.forName(className, classLoaders);
        while (aClass != null) {
            try {
                if (!descriptor.contains("(")) {
                    return aClass.isInterface() ? aClass.getField(memberName) : aClass.getDeclaredField(memberName);
                }
                if (isConstructor(descriptor)) {
                    return aClass.isInterface() ? aClass.getConstructor(parameterTypes) : aClass.getDeclaredConstructor(parameterTypes);
                }
                return aClass.isInterface() ? aClass.getMethod(memberName, parameterTypes) : aClass.getDeclaredMethod(memberName, parameterTypes);
            }
            catch (Exception e) {
                aClass = aClass.getSuperclass();
                continue;
            }
            break;
        }
        throw new ReflectionsException("Can't resolve member named " + memberName + " for class " + className);
    }
    
    public static Set<Method> getMethodsFromDescriptors(final Iterable<String> annotatedWith, final ClassLoader... classLoaders) {
        final Set<Method> result = (Set<Method>)Sets.newHashSet();
        for (final String annotated : annotatedWith) {
            if (!isConstructor(annotated)) {
                final Method member = (Method)getMemberFromDescriptor(annotated, classLoaders);
                if (member == null) {
                    continue;
                }
                result.add(member);
            }
        }
        return result;
    }
    
    public static Set<Constructor> getConstructorsFromDescriptors(final Iterable<String> annotatedWith, final ClassLoader... classLoaders) {
        final Set<Constructor> result = (Set<Constructor>)Sets.newHashSet();
        for (final String annotated : annotatedWith) {
            if (isConstructor(annotated)) {
                final Constructor member = (Constructor)getMemberFromDescriptor(annotated, classLoaders);
                if (member == null) {
                    continue;
                }
                result.add(member);
            }
        }
        return result;
    }
    
    public static Set<Member> getMembersFromDescriptors(final Iterable<String> values, final ClassLoader... classLoaders) {
        final Set<Member> result = (Set<Member>)Sets.newHashSet();
        for (final String value : values) {
            try {
                result.add(getMemberFromDescriptor(value, classLoaders));
            }
            catch (ReflectionsException e) {
                throw new ReflectionsException("Can't resolve member named " + value, e);
            }
        }
        return result;
    }
    
    public static Field getFieldFromString(final String field, final ClassLoader... classLoaders) {
        final String className = field.substring(0, field.lastIndexOf(46));
        final String fieldName = field.substring(field.lastIndexOf(46) + 1);
        try {
            return ReflectionUtils.forName(className, classLoaders).getDeclaredField(fieldName);
        }
        catch (NoSuchFieldException e) {
            throw new ReflectionsException("Can't resolve field named " + fieldName, e);
        }
    }
    
    public static void close(final InputStream closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        }
        catch (IOException e) {
            if (Reflections.log != null) {
                Reflections.log.warn("Could not close InputStream", (Throwable)e);
            }
        }
    }
    
    @Nullable
    public static Logger findLogger(final Class<?> aClass) {
        try {
            Class.forName("org.slf4j.impl.StaticLoggerBinder");
            return LoggerFactory.getLogger((Class)aClass);
        }
        catch (Throwable e) {
            return null;
        }
    }
    
    public static boolean isConstructor(final String fqn) {
        return fqn.contains("init>");
    }
    
    public static String name(Class type) {
        if (!type.isArray()) {
            return type.getName();
        }
        int dim = 0;
        while (type.isArray()) {
            ++dim;
            type = type.getComponentType();
        }
        return type.getName() + repeat("[]", dim);
    }
    
    public static List<String> names(final Iterable<Class<?>> types) {
        final List<String> result = new ArrayList<String>();
        for (final Class<?> type : types) {
            result.add(name(type));
        }
        return result;
    }
    
    public static List<String> names(final Class<?>... types) {
        return names(Arrays.asList(types));
    }
    
    public static String name(final Constructor constructor) {
        return constructor.getName() + ".<init>(" + Joiner.on(", ").join((Iterable)names((Class<?>[])constructor.getParameterTypes())) + ")";
    }
    
    public static String name(final Method method) {
        return method.getDeclaringClass().getName() + "." + method.getName() + "(" + Joiner.on(", ").join((Iterable)names(method.getParameterTypes())) + ")";
    }
    
    public static String name(final Field field) {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }
}
