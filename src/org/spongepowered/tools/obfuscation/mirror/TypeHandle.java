// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation.mirror;

import javax.lang.model.element.VariableElement;
import javax.lang.model.element.ExecutableElement;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import org.spongepowered.tools.obfuscation.mirror.mapping.ResolvableMappingMethod;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import javax.lang.model.element.Modifier;
import java.util.Iterator;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.element.ElementKind;
import java.util.List;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.PackageElement;

public class TypeHandle
{
    private final String name;
    private final PackageElement pkg;
    private final TypeElement element;
    private TypeReference reference;
    
    public TypeHandle(final PackageElement pkg, final String name) {
        this.name = name.replace('.', '/');
        this.pkg = pkg;
        this.element = null;
    }
    
    public TypeHandle(final TypeElement element) {
        this.pkg = TypeUtils.getPackage(element);
        this.name = TypeUtils.getInternalName(element);
        this.element = element;
    }
    
    public TypeHandle(final DeclaredType type) {
        this((TypeElement)type.asElement());
    }
    
    @Override
    public final String toString() {
        return this.name.replace('/', '.');
    }
    
    public final String getName() {
        return this.name;
    }
    
    public final PackageElement getPackage() {
        return this.pkg;
    }
    
    public final TypeElement getElement() {
        return this.element;
    }
    
    protected TypeElement getTargetElement() {
        return this.element;
    }
    
    public AnnotationHandle getAnnotation(final Class<? extends Annotation> annotationClass) {
        return AnnotationHandle.of(this.getTargetElement(), annotationClass);
    }
    
    public final List<? extends Element> getEnclosedElements() {
        return getEnclosedElements(this.getTargetElement());
    }
    
    public <T extends Element> List<T> getEnclosedElements(final ElementKind... kind) {
        return getEnclosedElements(this.getTargetElement(), kind);
    }
    
    public TypeMirror getType() {
        return (this.getTargetElement() != null) ? this.getTargetElement().asType() : null;
    }
    
    public TypeHandle getSuperclass() {
        final TypeElement targetElement = this.getTargetElement();
        if (targetElement == null) {
            return null;
        }
        final TypeMirror superClass = targetElement.getSuperclass();
        if (superClass == null || superClass.getKind() == TypeKind.NONE) {
            return null;
        }
        return new TypeHandle((DeclaredType)superClass);
    }
    
    public List<TypeHandle> getInterfaces() {
        if (this.getTargetElement() == null) {
            return Collections.emptyList();
        }
        final ImmutableList.Builder<TypeHandle> list = (ImmutableList.Builder<TypeHandle>)ImmutableList.builder();
        for (final TypeMirror iface : this.getTargetElement().getInterfaces()) {
            list.add((Object)new TypeHandle((DeclaredType)iface));
        }
        return (List<TypeHandle>)list.build();
    }
    
    public boolean isPublic() {
        return this.getTargetElement() != null && this.getTargetElement().getModifiers().contains(Modifier.PUBLIC);
    }
    
    public boolean isImaginary() {
        return this.getTargetElement() == null;
    }
    
    public boolean isSimulated() {
        return false;
    }
    
    public final TypeReference getReference() {
        if (this.reference == null) {
            this.reference = new TypeReference(this);
        }
        return this.reference;
    }
    
    public MappingMethod getMappingMethod(final String name, final String desc) {
        return new ResolvableMappingMethod(this, name, desc);
    }
    
    public String findDescriptor(final MemberInfo memberInfo) {
        String desc = memberInfo.desc;
        if (desc == null) {
            for (final ExecutableElement method : this.getEnclosedElements(ElementKind.METHOD)) {
                if (method.getSimpleName().toString().equals(memberInfo.name)) {
                    desc = TypeUtils.getDescriptor(method);
                    break;
                }
            }
        }
        return desc;
    }
    
    public final FieldHandle findField(final VariableElement element) {
        return this.findField(element, true);
    }
    
    public final FieldHandle findField(final VariableElement element, final boolean caseSensitive) {
        return this.findField(element.getSimpleName().toString(), TypeUtils.getTypeName(element.asType()), caseSensitive);
    }
    
    public final FieldHandle findField(final String name, final String type) {
        return this.findField(name, type, true);
    }
    
    public FieldHandle findField(final String name, final String type, final boolean caseSensitive) {
        final String rawType = TypeUtils.stripGenerics(type);
        for (final VariableElement field : this.getEnclosedElements(ElementKind.FIELD)) {
            if (compareElement(field, name, type, caseSensitive)) {
                return new FieldHandle(this.getTargetElement(), field);
            }
            if (compareElement(field, name, rawType, caseSensitive)) {
                return new FieldHandle(this.getTargetElement(), field, true);
            }
        }
        return null;
    }
    
    public final MethodHandle findMethod(final ExecutableElement element) {
        return this.findMethod(element, true);
    }
    
    public final MethodHandle findMethod(final ExecutableElement element, final boolean caseSensitive) {
        return this.findMethod(element.getSimpleName().toString(), TypeUtils.getJavaSignature(element), caseSensitive);
    }
    
    public final MethodHandle findMethod(final String name, final String signature) {
        return this.findMethod(name, signature, true);
    }
    
    public MethodHandle findMethod(final String name, final String signature, final boolean matchCase) {
        final String rawSignature = TypeUtils.stripGenerics(signature);
        return findMethod(this, name, signature, rawSignature, matchCase);
    }
    
    protected static MethodHandle findMethod(final TypeHandle target, final String name, final String signature, final String rawSignature, final boolean matchCase) {
        for (final ExecutableElement method : getEnclosedElements(target.getTargetElement(), ElementKind.CONSTRUCTOR, ElementKind.METHOD)) {
            if (compareElement(method, name, signature, matchCase) || compareElement(method, name, rawSignature, matchCase)) {
                return new MethodHandle(target, method);
            }
        }
        return null;
    }
    
    protected static boolean compareElement(final Element elem, final String name, final String type, final boolean matchCase) {
        try {
            final String elementName = elem.getSimpleName().toString();
            final String elementType = TypeUtils.getJavaSignature(elem);
            final String rawElementType = TypeUtils.stripGenerics(elementType);
            final boolean compared = matchCase ? name.equals(elementName) : name.equalsIgnoreCase(elementName);
            return compared && (type.length() == 0 || type.equals(elementType) || type.equals(rawElementType));
        }
        catch (NullPointerException ex) {
            return false;
        }
    }
    
    protected static <T extends Element> List<T> getEnclosedElements(final TypeElement targetElement, final ElementKind... kind) {
        if (kind == null || kind.length < 1) {
            return (List<T>)getEnclosedElements(targetElement);
        }
        if (targetElement == null) {
            return Collections.emptyList();
        }
        final ImmutableList.Builder<T> list = (ImmutableList.Builder<T>)ImmutableList.builder();
        for (final Element elem : targetElement.getEnclosedElements()) {
            for (final ElementKind ek : kind) {
                if (elem.getKind() == ek) {
                    list.add((Object)elem);
                    break;
                }
            }
        }
        return (List<T>)list.build();
    }
    
    protected static List<? extends Element> getEnclosedElements(final TypeElement targetElement) {
        return (targetElement != null) ? targetElement.getEnclosedElements() : Collections.emptyList();
    }
}
