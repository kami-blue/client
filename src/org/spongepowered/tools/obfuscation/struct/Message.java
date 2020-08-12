// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation.struct;

import javax.annotation.processing.Messager;
import org.spongepowered.tools.obfuscation.mirror.AnnotationHandle;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class Message
{
    private Diagnostic.Kind kind;
    private CharSequence msg;
    private final Element element;
    private final AnnotationMirror annotation;
    private final AnnotationValue value;
    
    public Message(final Diagnostic.Kind kind, final CharSequence msg) {
        this(kind, msg, null, (AnnotationMirror)null, null);
    }
    
    public Message(final Diagnostic.Kind kind, final CharSequence msg, final Element element) {
        this(kind, msg, element, (AnnotationMirror)null, null);
    }
    
    public Message(final Diagnostic.Kind kind, final CharSequence msg, final Element element, final AnnotationHandle annotation) {
        this(kind, msg, element, annotation.asMirror(), null);
    }
    
    public Message(final Diagnostic.Kind kind, final CharSequence msg, final Element element, final AnnotationMirror annotation) {
        this(kind, msg, element, annotation, null);
    }
    
    public Message(final Diagnostic.Kind kind, final CharSequence msg, final Element element, final AnnotationHandle annotation, final AnnotationValue value) {
        this(kind, msg, element, annotation.asMirror(), value);
    }
    
    public Message(final Diagnostic.Kind kind, final CharSequence msg, final Element element, final AnnotationMirror annotation, final AnnotationValue value) {
        this.kind = kind;
        this.msg = msg;
        this.element = element;
        this.annotation = annotation;
        this.value = value;
    }
    
    public Message sendTo(final Messager messager) {
        if (this.value != null) {
            messager.printMessage(this.kind, this.msg, this.element, this.annotation, this.value);
        }
        else if (this.annotation != null) {
            messager.printMessage(this.kind, this.msg, this.element, this.annotation);
        }
        else if (this.element != null) {
            messager.printMessage(this.kind, this.msg, this.element);
        }
        else {
            messager.printMessage(this.kind, this.msg);
        }
        return this;
    }
    
    public Diagnostic.Kind getKind() {
        return this.kind;
    }
    
    public Message setKind(final Diagnostic.Kind kind) {
        this.kind = kind;
        return this;
    }
    
    public CharSequence getMsg() {
        return this.msg;
    }
    
    public Message setMsg(final CharSequence msg) {
        this.msg = msg;
        return this;
    }
    
    public Element getElement() {
        return this.element;
    }
    
    public AnnotationMirror getAnnotation() {
        return this.annotation;
    }
    
    public AnnotationValue getValue() {
        return this.value;
    }
}
