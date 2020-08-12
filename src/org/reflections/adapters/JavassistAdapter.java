// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.adapters;

import javassist.bytecode.Descriptor;
import java.util.Arrays;
import com.google.common.base.Joiner;
import javassist.bytecode.AccessFlag;
import org.reflections.util.Utils;
import java.io.IOException;
import org.reflections.ReflectionsException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import org.reflections.vfs.Vfs;
import javassist.bytecode.annotation.Annotation;
import java.util.Iterator;
import java.util.Collection;
import javassist.bytecode.ParameterAnnotationsAttribute;
import com.google.common.collect.Lists;
import javassist.bytecode.AnnotationsAttribute;
import java.util.List;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.ClassFile;

public class JavassistAdapter implements MetadataAdapter<ClassFile, FieldInfo, MethodInfo>
{
    public static boolean includeInvisibleTag;
    
    @Override
    public List<FieldInfo> getFields(final ClassFile cls) {
        return (List<FieldInfo>)cls.getFields();
    }
    
    @Override
    public List<MethodInfo> getMethods(final ClassFile cls) {
        return (List<MethodInfo>)cls.getMethods();
    }
    
    @Override
    public String getMethodName(final MethodInfo method) {
        return method.getName();
    }
    
    @Override
    public List<String> getParameterNames(final MethodInfo method) {
        String descriptor = method.getDescriptor();
        descriptor = descriptor.substring(descriptor.indexOf("(") + 1, descriptor.lastIndexOf(")"));
        return this.splitDescriptorToTypeNames(descriptor);
    }
    
    @Override
    public List<String> getClassAnnotationNames(final ClassFile aClass) {
        return this.getAnnotationNames((AnnotationsAttribute)aClass.getAttribute("RuntimeVisibleAnnotations"), JavassistAdapter.includeInvisibleTag ? ((AnnotationsAttribute)aClass.getAttribute("RuntimeInvisibleAnnotations")) : null);
    }
    
    @Override
    public List<String> getFieldAnnotationNames(final FieldInfo field) {
        return this.getAnnotationNames((AnnotationsAttribute)field.getAttribute("RuntimeVisibleAnnotations"), JavassistAdapter.includeInvisibleTag ? ((AnnotationsAttribute)field.getAttribute("RuntimeInvisibleAnnotations")) : null);
    }
    
    @Override
    public List<String> getMethodAnnotationNames(final MethodInfo method) {
        return this.getAnnotationNames((AnnotationsAttribute)method.getAttribute("RuntimeVisibleAnnotations"), JavassistAdapter.includeInvisibleTag ? ((AnnotationsAttribute)method.getAttribute("RuntimeInvisibleAnnotations")) : null);
    }
    
    @Override
    public List<String> getParameterAnnotationNames(final MethodInfo method, final int parameterIndex) {
        final List<String> result = (List<String>)Lists.newArrayList();
        final List<ParameterAnnotationsAttribute> parameterAnnotationsAttributes = (List<ParameterAnnotationsAttribute>)Lists.newArrayList((Object[])new ParameterAnnotationsAttribute[] { (ParameterAnnotationsAttribute)method.getAttribute("RuntimeVisibleParameterAnnotations"), (ParameterAnnotationsAttribute)method.getAttribute("RuntimeInvisibleParameterAnnotations") });
        if (parameterAnnotationsAttributes != null) {
            for (final ParameterAnnotationsAttribute parameterAnnotationsAttribute : parameterAnnotationsAttributes) {
                if (parameterAnnotationsAttribute != null) {
                    final Annotation[][] annotations = parameterAnnotationsAttribute.getAnnotations();
                    if (parameterIndex >= annotations.length) {
                        continue;
                    }
                    final Annotation[] annotation = annotations[parameterIndex];
                    result.addAll(this.getAnnotationNames(annotation));
                }
            }
        }
        return result;
    }
    
    @Override
    public String getReturnTypeName(final MethodInfo method) {
        String descriptor = method.getDescriptor();
        descriptor = descriptor.substring(descriptor.lastIndexOf(")") + 1);
        return this.splitDescriptorToTypeNames(descriptor).get(0);
    }
    
    @Override
    public String getFieldName(final FieldInfo field) {
        return field.getName();
    }
    
    @Override
    public ClassFile getOfCreateClassObject(final Vfs.File file) {
        InputStream inputStream = null;
        try {
            inputStream = file.openInputStream();
            final DataInputStream dis = new DataInputStream(new BufferedInputStream(inputStream));
            return new ClassFile(dis);
        }
        catch (IOException e) {
            throw new ReflectionsException("could not create class file from " + file.getName(), e);
        }
        finally {
            Utils.close(inputStream);
        }
    }
    
    @Override
    public String getMethodModifier(final MethodInfo method) {
        final int accessFlags = method.getAccessFlags();
        return AccessFlag.isPrivate(accessFlags) ? "private" : (AccessFlag.isProtected(accessFlags) ? "protected" : (this.isPublic(accessFlags) ? "public" : ""));
    }
    
    @Override
    public String getMethodKey(final ClassFile cls, final MethodInfo method) {
        return this.getMethodName(method) + "(" + Joiner.on(", ").join((Iterable)this.getParameterNames(method)) + ")";
    }
    
    @Override
    public String getMethodFullKey(final ClassFile cls, final MethodInfo method) {
        return this.getClassName(cls) + "." + this.getMethodKey(cls, method);
    }
    
    @Override
    public boolean isPublic(final Object o) {
        final Integer accessFlags = (o instanceof ClassFile) ? ((ClassFile)o).getAccessFlags() : ((o instanceof FieldInfo) ? ((FieldInfo)o).getAccessFlags() : ((o instanceof MethodInfo) ? Integer.valueOf(((MethodInfo)o).getAccessFlags()) : null));
        return accessFlags != null && AccessFlag.isPublic(accessFlags);
    }
    
    @Override
    public String getClassName(final ClassFile cls) {
        return cls.getName();
    }
    
    @Override
    public String getSuperclassName(final ClassFile cls) {
        return cls.getSuperclass();
    }
    
    @Override
    public List<String> getInterfacesNames(final ClassFile cls) {
        return Arrays.asList(cls.getInterfaces());
    }
    
    @Override
    public boolean acceptsInput(final String file) {
        return file.endsWith(".class");
    }
    
    private List<String> getAnnotationNames(final AnnotationsAttribute... annotationsAttributes) {
        final List<String> result = (List<String>)Lists.newArrayList();
        if (annotationsAttributes != null) {
            for (final AnnotationsAttribute annotationsAttribute : annotationsAttributes) {
                if (annotationsAttribute != null) {
                    for (final Annotation annotation : annotationsAttribute.getAnnotations()) {
                        result.add(annotation.getTypeName());
                    }
                }
            }
        }
        return result;
    }
    
    private List<String> getAnnotationNames(final Annotation[] annotations) {
        final List<String> result = (List<String>)Lists.newArrayList();
        for (final Annotation annotation : annotations) {
            result.add(annotation.getTypeName());
        }
        return result;
    }
    
    private List<String> splitDescriptorToTypeNames(final String descriptors) {
        final List<String> result = (List<String>)Lists.newArrayList();
        if (descriptors != null && descriptors.length() != 0) {
            final List<Integer> indices = (List<Integer>)Lists.newArrayList();
            final Descriptor.Iterator iterator = new Descriptor.Iterator(descriptors);
            while (iterator.hasNext()) {
                indices.add(iterator.next());
            }
            indices.add(descriptors.length());
            for (int i = 0; i < indices.size() - 1; ++i) {
                final String s1 = Descriptor.toString(descriptors.substring(indices.get(i), indices.get(i + 1)));
                result.add(s1);
            }
        }
        return result;
    }
    
    static {
        JavassistAdapter.includeInvisibleTag = true;
    }
}
