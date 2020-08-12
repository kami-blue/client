// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.serializers;

import java.lang.reflect.Method;
import org.reflections.ReflectionUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import org.reflections.ReflectionsException;
import java.util.LinkedList;
import com.google.common.base.Joiner;
import com.google.common.collect.Multimap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import java.util.Set;
import com.google.common.base.Supplier;
import java.util.HashMap;
import java.util.Collections;
import com.google.common.collect.Lists;
import org.reflections.scanners.TypeElementsScanner;
import java.io.IOException;
import com.google.common.io.Files;
import java.nio.charset.Charset;
import java.util.Date;
import org.reflections.util.Utils;
import java.io.File;
import org.reflections.Reflections;
import java.io.InputStream;

public class JavaCodeSerializer implements Serializer
{
    private static final String pathSeparator = "_";
    private static final String doubleSeparator = "__";
    private static final String dotSeparator = ".";
    private static final String arrayDescriptor = "$$";
    private static final String tokenSeparator = "_";
    
    @Override
    public Reflections read(final InputStream inputStream) {
        throw new UnsupportedOperationException("read is not implemented on JavaCodeSerializer");
    }
    
    @Override
    public File save(final Reflections reflections, String name) {
        if (name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }
        final String filename = name.replace('.', '/').concat(".java");
        final File file = Utils.prepareFile(filename);
        final int lastDot = name.lastIndexOf(46);
        String packageName;
        String className;
        if (lastDot == -1) {
            packageName = "";
            className = name.substring(name.lastIndexOf(47) + 1);
        }
        else {
            packageName = name.substring(name.lastIndexOf(47) + 1, lastDot);
            className = name.substring(lastDot + 1);
        }
        try {
            final StringBuilder sb = new StringBuilder();
            sb.append("//generated using Reflections JavaCodeSerializer").append(" [").append(new Date()).append("]").append("\n");
            if (packageName.length() != 0) {
                sb.append("package ").append(packageName).append(";\n");
                sb.append("\n");
            }
            sb.append("public interface ").append(className).append(" {\n\n");
            sb.append(this.toString(reflections));
            sb.append("}\n");
            Files.write((CharSequence)sb.toString(), new File(filename), Charset.defaultCharset());
        }
        catch (IOException e) {
            throw new RuntimeException();
        }
        return file;
    }
    
    @Override
    public String toString(final Reflections reflections) {
        if (reflections.getStore().get(TypeElementsScanner.class.getSimpleName()).isEmpty() && Reflections.log != null) {
            Reflections.log.warn("JavaCodeSerializer needs TypeElementsScanner configured");
        }
        final StringBuilder sb = new StringBuilder();
        List<String> prevPaths = (List<String>)Lists.newArrayList();
        int indent = 1;
        final List<String> keys = (List<String>)Lists.newArrayList((Iterable)reflections.getStore().get(TypeElementsScanner.class.getSimpleName()).keySet());
        Collections.sort(keys);
        for (final String fqn : keys) {
            List<String> typePaths;
            int i;
            for (typePaths = (List<String>)Lists.newArrayList((Object[])fqn.split("\\.")), i = 0; i < Math.min(typePaths.size(), prevPaths.size()) && typePaths.get(i).equals(prevPaths.get(i)); ++i) {}
            for (int j = prevPaths.size(); j > i; --j) {
                sb.append(Utils.repeat("\t", --indent)).append("}\n");
            }
            for (int j = i; j < typePaths.size() - 1; ++j) {
                sb.append(Utils.repeat("\t", indent++)).append("public interface ").append(this.getNonDuplicateName(typePaths.get(j), typePaths, j)).append(" {\n");
            }
            final String className = typePaths.get(typePaths.size() - 1);
            final List<String> annotations = (List<String>)Lists.newArrayList();
            final List<String> fields = (List<String>)Lists.newArrayList();
            final Multimap<String, String> methods = (Multimap<String, String>)Multimaps.newSetMultimap((Map)new HashMap(), (Supplier)new Supplier<Set<String>>() {
                public Set<String> get() {
                    return (Set<String>)Sets.newHashSet();
                }
            });
            for (final String element : reflections.getStore().get(TypeElementsScanner.class.getSimpleName(), fqn)) {
                if (element.startsWith("@")) {
                    annotations.add(element.substring(1));
                }
                else if (element.contains("(")) {
                    if (element.startsWith("<")) {
                        continue;
                    }
                    final int i2 = element.indexOf(40);
                    final String name = element.substring(0, i2);
                    final String params = element.substring(i2 + 1, element.indexOf(")"));
                    String paramsDescriptor = "";
                    if (params.length() != 0) {
                        paramsDescriptor = "_" + params.replace(".", "_").replace(", ", "__").replace("[]", "$$");
                    }
                    final String normalized = name + paramsDescriptor;
                    methods.put((Object)name, (Object)normalized);
                }
                else {
                    if (Utils.isEmpty(element)) {
                        continue;
                    }
                    fields.add(element);
                }
            }
            sb.append(Utils.repeat("\t", indent++)).append("public interface ").append(this.getNonDuplicateName(className, typePaths, typePaths.size() - 1)).append(" {\n");
            if (!fields.isEmpty()) {
                sb.append(Utils.repeat("\t", indent++)).append("public interface fields {\n");
                for (final String field : fields) {
                    sb.append(Utils.repeat("\t", indent)).append("public interface ").append(this.getNonDuplicateName(field, typePaths)).append(" {}\n");
                }
                sb.append(Utils.repeat("\t", --indent)).append("}\n");
            }
            if (!methods.isEmpty()) {
                sb.append(Utils.repeat("\t", indent++)).append("public interface methods {\n");
                for (final Map.Entry<String, String> entry : methods.entries()) {
                    final String simpleName = entry.getKey();
                    final String normalized2 = entry.getValue();
                    String methodName = (methods.get((Object)simpleName).size() == 1) ? simpleName : normalized2;
                    methodName = this.getNonDuplicateName(methodName, fields);
                    sb.append(Utils.repeat("\t", indent)).append("public interface ").append(this.getNonDuplicateName(methodName, typePaths)).append(" {}\n");
                }
                sb.append(Utils.repeat("\t", --indent)).append("}\n");
            }
            if (!annotations.isEmpty()) {
                sb.append(Utils.repeat("\t", indent++)).append("public interface annotations {\n");
                for (String nonDuplicateName : annotations) {
                    final String annotation = nonDuplicateName;
                    nonDuplicateName = this.getNonDuplicateName(nonDuplicateName, typePaths);
                    sb.append(Utils.repeat("\t", indent)).append("public interface ").append(nonDuplicateName).append(" {}\n");
                }
                sb.append(Utils.repeat("\t", --indent)).append("}\n");
            }
            prevPaths = typePaths;
        }
        for (int k = prevPaths.size(); k >= 1; --k) {
            sb.append(Utils.repeat("\t", k)).append("}\n");
        }
        return sb.toString();
    }
    
    private String getNonDuplicateName(final String candidate, final List<String> prev, final int offset) {
        final String normalized = this.normalize(candidate);
        for (int i = 0; i < offset; ++i) {
            if (normalized.equals(prev.get(i))) {
                return this.getNonDuplicateName(normalized + "_", prev, offset);
            }
        }
        return normalized;
    }
    
    private String normalize(final String candidate) {
        return candidate.replace(".", "_");
    }
    
    private String getNonDuplicateName(final String candidate, final List<String> prev) {
        return this.getNonDuplicateName(candidate, prev, prev.size());
    }
    
    public static Class<?> resolveClassOf(final Class element) throws ClassNotFoundException {
        Class<?> cursor = (Class<?>)element;
        final LinkedList<String> ognl = (LinkedList<String>)Lists.newLinkedList();
        while (cursor != null) {
            ognl.addFirst(cursor.getSimpleName());
            cursor = cursor.getDeclaringClass();
        }
        final String classOgnl = Joiner.on(".").join((Iterable)ognl.subList(1, ognl.size())).replace(".$", "$");
        return Class.forName(classOgnl);
    }
    
    public static Class<?> resolveClass(final Class aClass) {
        try {
            return resolveClassOf(aClass);
        }
        catch (Exception e) {
            throw new ReflectionsException("could not resolve to class " + aClass.getName(), e);
        }
    }
    
    public static Field resolveField(final Class aField) {
        try {
            final String name = aField.getSimpleName();
            final Class<?> declaringClass = (Class<?>)aField.getDeclaringClass().getDeclaringClass();
            return resolveClassOf(declaringClass).getDeclaredField(name);
        }
        catch (Exception e) {
            throw new ReflectionsException("could not resolve to field " + aField.getName(), e);
        }
    }
    
    public static Annotation resolveAnnotation(final Class annotation) {
        try {
            final String name = annotation.getSimpleName().replace("_", ".");
            final Class<?> declaringClass = (Class<?>)annotation.getDeclaringClass().getDeclaringClass();
            final Class<?> aClass = resolveClassOf(declaringClass);
            final Class<? extends Annotation> aClass2 = (Class<? extends Annotation>)ReflectionUtils.forName(name, new ClassLoader[0]);
            final Annotation annotation2 = aClass.getAnnotation(aClass2);
            return annotation2;
        }
        catch (Exception e) {
            throw new ReflectionsException("could not resolve to annotation " + annotation.getName(), e);
        }
    }
    
    public static Method resolveMethod(final Class aMethod) {
        final String methodOgnl = aMethod.getSimpleName();
        try {
            String methodName;
            Class<?>[] paramTypes;
            if (methodOgnl.contains("_")) {
                methodName = methodOgnl.substring(0, methodOgnl.indexOf("_"));
                final String[] params = methodOgnl.substring(methodOgnl.indexOf("_") + 1).split("__");
                paramTypes = (Class<?>[])new Class[params.length];
                for (int i = 0; i < params.length; ++i) {
                    final String typeName = params[i].replace("$$", "[]").replace("_", ".");
                    paramTypes[i] = ReflectionUtils.forName(typeName, new ClassLoader[0]);
                }
            }
            else {
                methodName = methodOgnl;
                paramTypes = null;
            }
            final Class<?> declaringClass = (Class<?>)aMethod.getDeclaringClass().getDeclaringClass();
            return resolveClassOf(declaringClass).getDeclaredMethod(methodName, paramTypes);
        }
        catch (Exception e) {
            throw new ReflectionsException("could not resolve to method " + aMethod.getName(), e);
        }
    }
}
