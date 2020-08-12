// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.scanners;

import java.util.Iterator;
import java.lang.annotation.Inherited;

public class TypeAnnotationsScanner extends AbstractScanner
{
    @Override
    public void scan(final Object cls) {
        final String className = this.getMetadataAdapter().getClassName(cls);
        for (final String annotationType : this.getMetadataAdapter().getClassAnnotationNames(cls)) {
            if (this.acceptResult(annotationType) || annotationType.equals(Inherited.class.getName())) {
                this.getStore().put((Object)annotationType, (Object)className);
            }
        }
    }
}
