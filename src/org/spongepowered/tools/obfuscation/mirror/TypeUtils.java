// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation.mirror;

import javax.lang.model.element.Modifier;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.ArrayType;
import org.spongepowered.asm.util.SignaturePrinter;
import java.util.Iterator;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeMirror;

public abstract class TypeUtils
{
    private static final int MAX_GENERIC_RECURSION_DEPTH = 5;
    private static final String OBJECT_SIG = "java.lang.Object";
    private static final String OBJECT_REF = "java/lang/Object";
    
    private TypeUtils() {
    }
    
    public static PackageElement getPackage(final TypeMirror type) {
        if (!(type instanceof DeclaredType)) {
            return null;
        }
        return getPackage((TypeElement)((DeclaredType)type).asElement());
    }
    
    public static PackageElement getPackage(final TypeElement type) {
        Element parent;
        for (parent = type.getEnclosingElement(); parent != null && !(parent instanceof PackageElement); parent = parent.getEnclosingElement()) {}
        return (PackageElement)parent;
    }
    
    public static String getElementType(final Element element) {
        if (element instanceof TypeElement) {
            return "TypeElement";
        }
        if (element instanceof ExecutableElement) {
            return "ExecutableElement";
        }
        if (element instanceof VariableElement) {
            return "VariableElement";
        }
        if (element instanceof PackageElement) {
            return "PackageElement";
        }
        if (element instanceof TypeParameterElement) {
            return "TypeParameterElement";
        }
        return element.getClass().getSimpleName();
    }
    
    public static String stripGenerics(final String type) {
        final StringBuilder sb = new StringBuilder();
        int pos = 0;
        int depth = 0;
        while (pos < type.length()) {
            final char c = type.charAt(pos);
            if (c == '<') {
                ++depth;
            }
            if (depth == 0) {
                sb.append(c);
            }
            else if (c == '>') {
                --depth;
            }
            ++pos;
        }
        return sb.toString();
    }
    
    public static String getName(final VariableElement field) {
        return (field != null) ? field.getSimpleName().toString() : null;
    }
    
    public static String getName(final ExecutableElement method) {
        return (method != null) ? method.getSimpleName().toString() : null;
    }
    
    public static String getJavaSignature(final Element element) {
        if (element instanceof ExecutableElement) {
            final ExecutableElement method = (ExecutableElement)element;
            final StringBuilder desc = new StringBuilder().append("(");
            boolean extra = false;
            for (final VariableElement arg : method.getParameters()) {
                if (extra) {
                    desc.append(',');
                }
                desc.append(getTypeName(arg.asType()));
                extra = true;
            }
            desc.append(')').append(getTypeName(method.getReturnType()));
            return desc.toString();
        }
        return getTypeName(element.asType());
    }
    
    public static String getJavaSignature(final String descriptor) {
        return new SignaturePrinter("", descriptor).setFullyQualified(true).toDescriptor();
    }
    
    public static String getTypeName(final TypeMirror type) {
        switch (type.getKind()) {
            case ARRAY: {
                return getTypeName(((ArrayType)type).getComponentType()) + "[]";
            }
            case DECLARED: {
                return getTypeName((DeclaredType)type);
            }
            case TYPEVAR: {
                return getTypeName(getUpperBound(type));
            }
            case ERROR: {
                return "java.lang.Object";
            }
            default: {
                return type.toString();
            }
        }
    }
    
    public static String getTypeName(final DeclaredType type) {
        if (type == null) {
            return "java.lang.Object";
        }
        return getInternalName((TypeElement)type.asElement()).replace('/', '.');
    }
    
    public static String getDescriptor(final Element element) {
        if (element instanceof ExecutableElement) {
            return getDescriptor((ExecutableElement)element);
        }
        if (element instanceof VariableElement) {
            return getInternalName((VariableElement)element);
        }
        return getInternalName(element.asType());
    }
    
    public static String getDescriptor(final ExecutableElement method) {
        if (method == null) {
            return null;
        }
        final StringBuilder signature = new StringBuilder();
        for (final VariableElement var : method.getParameters()) {
            signature.append(getInternalName(var));
        }
        final String returnType = getInternalName(method.getReturnType());
        return String.format("(%s)%s", signature, returnType);
    }
    
    public static String getInternalName(final VariableElement field) {
        if (field == null) {
            return null;
        }
        return getInternalName(field.asType());
    }
    
    public static String getInternalName(final TypeMirror type) {
        switch (type.getKind()) {
            case ARRAY: {
                return "[" + getInternalName(((ArrayType)type).getComponentType());
            }
            case DECLARED: {
                return "L" + getInternalName((DeclaredType)type) + ";";
            }
            case TYPEVAR: {
                return "L" + getInternalName(getUpperBound(type)) + ";";
            }
            case BOOLEAN: {
                return "Z";
            }
            case BYTE: {
                return "B";
            }
            case CHAR: {
                return "C";
            }
            case DOUBLE: {
                return "D";
            }
            case FLOAT: {
                return "F";
            }
            case INT: {
                return "I";
            }
            case LONG: {
                return "J";
            }
            case SHORT: {
                return "S";
            }
            case VOID: {
                return "V";
            }
            case ERROR: {
                return "Ljava/lang/Object;";
            }
            default: {
                throw new IllegalArgumentException("Unable to parse type symbol " + type + " with " + type.getKind() + " to equivalent bytecode type");
            }
        }
    }
    
    public static String getInternalName(final DeclaredType type) {
        if (type == null) {
            return "java/lang/Object";
        }
        return getInternalName((TypeElement)type.asElement());
    }
    
    public static String getInternalName(final TypeElement element) {
        if (element == null) {
            return null;
        }
        final StringBuilder reference = new StringBuilder();
        reference.append(element.getSimpleName());
        for (Element parent = element.getEnclosingElement(); parent != null; parent = parent.getEnclosingElement()) {
            if (parent instanceof TypeElement) {
                reference.insert(0, "$").insert(0, parent.getSimpleName());
            }
            else if (parent instanceof PackageElement) {
                reference.insert(0, "/").insert(0, ((PackageElement)parent).getQualifiedName().toString().replace('.', '/'));
            }
        }
        return reference.toString();
    }
    
    private static DeclaredType getUpperBound(final TypeMirror type) {
        try {
            return getUpperBound0(type, 5);
        }
        catch (IllegalStateException ex) {
            throw new IllegalArgumentException("Type symbol \"" + type + "\" is too complex", ex);
        }
        catch (IllegalArgumentException ex2) {
            throw new IllegalArgumentException("Unable to compute upper bound of type symbol " + type, ex2);
        }
    }
    
    private static DeclaredType getUpperBound0(final TypeMirror type, int depth) {
        if (depth == 0) {
            throw new IllegalStateException("Generic symbol \"" + type + "\" is too complex, exceeded " + 5 + " iterations attempting to determine upper bound");
        }
        if (type instanceof DeclaredType) {
            return (DeclaredType)type;
        }
        if (type instanceof TypeVariable) {
            try {
                final TypeMirror upper = ((TypeVariable)type).getUpperBound();
                return getUpperBound0(upper, --depth);
            }
            catch (IllegalStateException ex) {
                throw ex;
            }
            catch (IllegalArgumentException ex2) {
                throw ex2;
            }
            catch (Exception ex3) {
                throw new IllegalArgumentException("Unable to compute upper bound of type symbol " + type);
            }
        }
        return null;
    }
    
    public static boolean isAssignable(final ProcessingEnvironment processingEnv, final TypeMirror targetType, final TypeMirror superClass) {
        final boolean assignable = processingEnv.getTypeUtils().isAssignable(targetType, superClass);
        if (!assignable && targetType instanceof DeclaredType && superClass instanceof DeclaredType) {
            final TypeMirror rawTargetType = toRawType(processingEnv, (DeclaredType)targetType);
            final TypeMirror rawSuperType = toRawType(processingEnv, (DeclaredType)superClass);
            return processingEnv.getTypeUtils().isAssignable(rawTargetType, rawSuperType);
        }
        return assignable;
    }
    
    private static TypeMirror toRawType(final ProcessingEnvironment processingEnv, final DeclaredType targetType) {
        return processingEnv.getElementUtils().getTypeElement(((TypeElement)targetType.asElement()).getQualifiedName()).asType();
    }
    
    public static Visibility getVisibility(final Element element) {
        if (element == null) {
            return null;
        }
        for (final Modifier modifier : element.getModifiers()) {
            switch (modifier) {
                case PUBLIC: {
                    return Visibility.PUBLIC;
                }
                case PROTECTED: {
                    return Visibility.PROTECTED;
                }
                case PRIVATE: {
                    return Visibility.PRIVATE;
                }
                default: {
                    continue;
                }
            }
        }
        return Visibility.PACKAGE;
    }
}
