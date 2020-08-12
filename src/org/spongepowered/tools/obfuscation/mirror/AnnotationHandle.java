// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation.mirror;

import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.Iterator;
import java.util.ArrayList;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import javax.lang.model.element.AnnotationMirror;

public final class AnnotationHandle
{
    public static final AnnotationHandle MISSING;
    private final AnnotationMirror annotation;
    
    private AnnotationHandle(final AnnotationMirror annotation) {
        this.annotation = annotation;
    }
    
    public AnnotationMirror asMirror() {
        return this.annotation;
    }
    
    public boolean exists() {
        return this.annotation != null;
    }
    
    @Override
    public String toString() {
        if (this.annotation == null) {
            return "@{UnknownAnnotation}";
        }
        return "@" + (Object)this.annotation.getAnnotationType().asElement().getSimpleName();
    }
    
    public <T> T getValue(final String key, final T defaultValue) {
        if (this.annotation == null) {
            return defaultValue;
        }
        final AnnotationValue value = this.getAnnotationValue(key);
        if (!(defaultValue instanceof Enum) || value == null) {
            return (T)((value != null) ? value.getValue() : defaultValue);
        }
        final VariableElement varValue = (VariableElement)value.getValue();
        if (varValue == null) {
            return defaultValue;
        }
        return Enum.valueOf(defaultValue.getClass(), varValue.getSimpleName().toString());
    }
    
    public <T> T getValue() {
        return this.getValue("value", (T)null);
    }
    
    public <T> T getValue(final String key) {
        return this.getValue(key, (T)null);
    }
    
    public boolean getBoolean(final String key, final boolean defaultValue) {
        return this.getValue(key, defaultValue);
    }
    
    public AnnotationHandle getAnnotation(final String key) {
        final Object value = this.getValue(key);
        if (value instanceof AnnotationMirror) {
            return of((AnnotationMirror)value);
        }
        if (value instanceof AnnotationValue) {
            final Object mirror = ((AnnotationValue)value).getValue();
            if (mirror instanceof AnnotationMirror) {
                return of((AnnotationMirror)mirror);
            }
        }
        return null;
    }
    
    public <T> List<T> getList() {
        return this.getList("value");
    }
    
    public <T> List<T> getList(final String key) {
        final List<AnnotationValue> list = this.getValue(key, Collections.emptyList());
        return unwrapAnnotationValueList(list);
    }
    
    public List<AnnotationHandle> getAnnotationList(final String key) {
        final Object val = this.getValue(key, (Object)null);
        if (val == null) {
            return Collections.emptyList();
        }
        if (val instanceof AnnotationMirror) {
            return (List<AnnotationHandle>)ImmutableList.of((Object)of((AnnotationMirror)val));
        }
        final List<AnnotationValue> list = (List<AnnotationValue>)val;
        final List<AnnotationHandle> annotations = new ArrayList<AnnotationHandle>(list.size());
        for (final AnnotationValue value : list) {
            annotations.add(new AnnotationHandle((AnnotationMirror)value.getValue()));
        }
        return Collections.unmodifiableList((List<? extends AnnotationHandle>)annotations);
    }
    
    protected AnnotationValue getAnnotationValue(final String key) {
        for (final ExecutableElement elem : this.annotation.getElementValues().keySet()) {
            if (elem.getSimpleName().contentEquals(key)) {
                return (AnnotationValue)this.annotation.getElementValues().get(elem);
            }
        }
        return null;
    }
    
    protected static <T> List<T> unwrapAnnotationValueList(final List<AnnotationValue> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        final List<T> unfolded = new ArrayList<T>(list.size());
        for (final AnnotationValue value : list) {
            unfolded.add((T)value.getValue());
        }
        return unfolded;
    }
    
    protected static AnnotationMirror getAnnotation(final Element elem, final Class<? extends Annotation> annotationClass) {
        if (elem == null) {
            return null;
        }
        final List<? extends AnnotationMirror> annotations = elem.getAnnotationMirrors();
        if (annotations == null) {
            return null;
        }
        for (final AnnotationMirror annotation : annotations) {
            final Element element = annotation.getAnnotationType().asElement();
            if (!(element instanceof TypeElement)) {
                continue;
            }
            final TypeElement annotationElement = (TypeElement)element;
            if (annotationElement.getQualifiedName().contentEquals(annotationClass.getName())) {
                return annotation;
            }
        }
        return null;
    }
    
    public static AnnotationHandle of(final AnnotationMirror annotation) {
        return new AnnotationHandle(annotation);
    }
    
    public static AnnotationHandle of(final Element elem, final Class<? extends Annotation> annotationClass) {
        return new AnnotationHandle(getAnnotation(elem, annotationClass));
    }
    
    static {
        MISSING = new AnnotationHandle(null);
    }
}
