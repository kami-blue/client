// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.scanners;

import java.util.Iterator;

public class MethodAnnotationsScanner extends AbstractScanner
{
    @Override
    public void scan(final Object cls) {
        for (final Object method : this.getMetadataAdapter().getMethods(cls)) {
            for (final String methodAnnotation : this.getMetadataAdapter().getMethodAnnotationNames(method)) {
                if (this.acceptResult(methodAnnotation)) {
                    this.getStore().put((Object)methodAnnotation, (Object)this.getMetadataAdapter().getMethodFullKey(cls, method));
                }
            }
        }
    }
}
