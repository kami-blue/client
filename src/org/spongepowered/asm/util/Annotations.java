// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.util;

import java.util.ListIterator;
import java.util.Collections;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.base.Function;
import java.util.Iterator;
import org.spongepowered.asm.lib.tree.ClassNode;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.Type;
import java.lang.annotation.Annotation;
import org.spongepowered.asm.lib.tree.FieldNode;

public final class Annotations
{
    private Annotations() {
    }
    
    public static void setVisible(final FieldNode field, final Class<? extends Annotation> annotationClass, final Object... value) {
        final AnnotationNode node = createNode(Type.getDescriptor(annotationClass), value);
        field.visibleAnnotations = add(field.visibleAnnotations, node);
    }
    
    public static void setInvisible(final FieldNode field, final Class<? extends Annotation> annotationClass, final Object... value) {
        final AnnotationNode node = createNode(Type.getDescriptor(annotationClass), value);
        field.invisibleAnnotations = add(field.invisibleAnnotations, node);
    }
    
    public static void setVisible(final MethodNode method, final Class<? extends Annotation> annotationClass, final Object... value) {
        final AnnotationNode node = createNode(Type.getDescriptor(annotationClass), value);
        method.visibleAnnotations = add(method.visibleAnnotations, node);
    }
    
    public static void setInvisible(final MethodNode method, final Class<? extends Annotation> annotationClass, final Object... value) {
        final AnnotationNode node = createNode(Type.getDescriptor(annotationClass), value);
        method.invisibleAnnotations = add(method.invisibleAnnotations, node);
    }
    
    private static AnnotationNode createNode(final String annotationType, final Object... value) {
        final AnnotationNode node = new AnnotationNode(annotationType);
        for (int pos = 0; pos < value.length - 1; pos += 2) {
            if (!(value[pos] instanceof String)) {
                throw new IllegalArgumentException("Annotation keys must be strings, found " + value[pos].getClass().getSimpleName() + " with " + value[pos].toString() + " at index " + pos + " creating " + annotationType);
            }
            node.visit((String)value[pos], value[pos + 1]);
        }
        return node;
    }
    
    private static List<AnnotationNode> add(List<AnnotationNode> annotations, final AnnotationNode node) {
        if (annotations == null) {
            annotations = new ArrayList<AnnotationNode>(1);
        }
        else {
            annotations.remove(get(annotations, node.desc));
        }
        annotations.add(node);
        return annotations;
    }
    
    public static AnnotationNode getVisible(final FieldNode field, final Class<? extends Annotation> annotationClass) {
        return get(field.visibleAnnotations, Type.getDescriptor(annotationClass));
    }
    
    public static AnnotationNode getInvisible(final FieldNode field, final Class<? extends Annotation> annotationClass) {
        return get(field.invisibleAnnotations, Type.getDescriptor(annotationClass));
    }
    
    public static AnnotationNode getVisible(final MethodNode method, final Class<? extends Annotation> annotationClass) {
        return get(method.visibleAnnotations, Type.getDescriptor(annotationClass));
    }
    
    public static AnnotationNode getInvisible(final MethodNode method, final Class<? extends Annotation> annotationClass) {
        return get(method.invisibleAnnotations, Type.getDescriptor(annotationClass));
    }
    
    public static AnnotationNode getSingleVisible(final MethodNode method, final Class<? extends Annotation>... annotationClasses) {
        return getSingle(method.visibleAnnotations, annotationClasses);
    }
    
    public static AnnotationNode getSingleInvisible(final MethodNode method, final Class<? extends Annotation>... annotationClasses) {
        return getSingle(method.invisibleAnnotations, annotationClasses);
    }
    
    public static AnnotationNode getVisible(final ClassNode classNode, final Class<? extends Annotation> annotationClass) {
        return get(classNode.visibleAnnotations, Type.getDescriptor(annotationClass));
    }
    
    public static AnnotationNode getInvisible(final ClassNode classNode, final Class<? extends Annotation> annotationClass) {
        return get(classNode.invisibleAnnotations, Type.getDescriptor(annotationClass));
    }
    
    public static AnnotationNode getVisibleParameter(final MethodNode method, final Class<? extends Annotation> annotationClass, final int paramIndex) {
        return getParameter(method.visibleParameterAnnotations, Type.getDescriptor(annotationClass), paramIndex);
    }
    
    public static AnnotationNode getInvisibleParameter(final MethodNode method, final Class<? extends Annotation> annotationClass, final int paramIndex) {
        return getParameter(method.invisibleParameterAnnotations, Type.getDescriptor(annotationClass), paramIndex);
    }
    
    public static AnnotationNode getParameter(final List<AnnotationNode>[] parameterAnnotations, final String annotationType, final int paramIndex) {
        if (parameterAnnotations == null || paramIndex < 0 || paramIndex >= parameterAnnotations.length) {
            return null;
        }
        return get(parameterAnnotations[paramIndex], annotationType);
    }
    
    public static AnnotationNode get(final List<AnnotationNode> annotations, final String annotationType) {
        if (annotations == null) {
            return null;
        }
        for (final AnnotationNode annotation : annotations) {
            if (annotationType.equals(annotation.desc)) {
                return annotation;
            }
        }
        return null;
    }
    
    private static AnnotationNode getSingle(final List<AnnotationNode> annotations, final Class<? extends Annotation>[] annotationClasses) {
        final List<AnnotationNode> nodes = new ArrayList<AnnotationNode>();
        for (final Class<? extends Annotation> annotationClass : annotationClasses) {
            final AnnotationNode annotation = get(annotations, Type.getDescriptor(annotationClass));
            if (annotation != null) {
                nodes.add(annotation);
            }
        }
        final int foundNodes = nodes.size();
        if (foundNodes > 1) {
            throw new IllegalArgumentException("Conflicting annotations found: " + Lists.transform((List)nodes, (Function)new Function<AnnotationNode, String>() {
                public String apply(final AnnotationNode input) {
                    return input.desc;
                }
            }));
        }
        return (foundNodes == 0) ? null : nodes.get(0);
    }
    
    public static <T> T getValue(final AnnotationNode annotation) {
        return getValue(annotation, "value");
    }
    
    public static <T> T getValue(final AnnotationNode annotation, final String key, final T defaultValue) {
        final T returnValue = getValue(annotation, key);
        return (returnValue != null) ? returnValue : defaultValue;
    }
    
    public static <T> T getValue(final AnnotationNode annotation, final String key, final Class<?> annotationClass) {
        Preconditions.checkNotNull((Object)annotationClass, (Object)"annotationClass cannot be null");
        T value = getValue(annotation, key);
        if (value == null) {
            try {
                value = (T)annotationClass.getDeclaredMethod(key, (Class<?>[])new Class[0]).getDefaultValue();
            }
            catch (NoSuchMethodException ex) {}
        }
        return value;
    }
    
    public static <T> T getValue(final AnnotationNode annotation, final String key) {
        boolean getNextValue = false;
        if (annotation == null || annotation.values == null) {
            return null;
        }
        for (final Object value : annotation.values) {
            if (getNextValue) {
                return (T)value;
            }
            if (!value.equals(key)) {
                continue;
            }
            getNextValue = true;
        }
        return null;
    }
    
    public static <T extends Enum<T>> T getValue(final AnnotationNode annotation, final String key, final Class<T> enumClass, final T defaultValue) {
        final String[] value = getValue(annotation, key);
        if (value == null) {
            return defaultValue;
        }
        return toEnumValue(enumClass, value);
    }
    
    public static <T> List<T> getValue(final AnnotationNode annotation, final String key, final boolean notNull) {
        final Object value = getValue(annotation, key);
        if (value instanceof List) {
            return (List<T>)value;
        }
        if (value != null) {
            final List<T> list = new ArrayList<T>();
            list.add((T)value);
            return list;
        }
        return Collections.emptyList();
    }
    
    public static <T extends Enum<T>> List<T> getValue(final AnnotationNode annotation, final String key, final boolean notNull, final Class<T> enumClass) {
        final Object value = getValue(annotation, key);
        if (value instanceof List) {
            final ListIterator<Object> iter = ((List)value).listIterator();
            while (iter.hasNext()) {
                iter.set(toEnumValue(enumClass, iter.next()));
            }
            return (List<T>)value;
        }
        if (value instanceof String[]) {
            final List<T> list = new ArrayList<T>();
            list.add(toEnumValue(enumClass, (String[])value));
            return list;
        }
        return Collections.emptyList();
    }
    
    private static <T extends Enum<T>> T toEnumValue(final Class<T> enumClass, final String[] value) {
        if (!enumClass.getName().equals(Type.getType(value[0]).getClassName())) {
            throw new IllegalArgumentException("The supplied enum class does not match the stored enum value");
        }
        return Enum.valueOf(enumClass, value[1]);
    }
}
