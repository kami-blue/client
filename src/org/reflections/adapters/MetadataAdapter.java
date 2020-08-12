// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.adapters;

import org.reflections.vfs.Vfs;
import java.util.List;

public interface MetadataAdapter<C, F, M>
{
    String getClassName(final C p0);
    
    String getSuperclassName(final C p0);
    
    List<String> getInterfacesNames(final C p0);
    
    List<F> getFields(final C p0);
    
    List<M> getMethods(final C p0);
    
    String getMethodName(final M p0);
    
    List<String> getParameterNames(final M p0);
    
    List<String> getClassAnnotationNames(final C p0);
    
    List<String> getFieldAnnotationNames(final F p0);
    
    List<String> getMethodAnnotationNames(final M p0);
    
    List<String> getParameterAnnotationNames(final M p0, final int p1);
    
    String getReturnTypeName(final M p0);
    
    String getFieldName(final F p0);
    
    C getOfCreateClassObject(final Vfs.File p0) throws Exception;
    
    String getMethodModifier(final M p0);
    
    String getMethodKey(final C p0, final M p1);
    
    String getMethodFullKey(final C p0, final M p1);
    
    boolean isPublic(final Object p0);
    
    boolean acceptsInput(final String p0);
}
