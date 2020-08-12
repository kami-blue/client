// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.code;

import java.util.Iterator;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.mixin.injection.throwables.InvalidSliceException;
import java.util.HashMap;
import java.util.Map;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;

public final class MethodSlices
{
    private final InjectionInfo info;
    private final Map<String, MethodSlice> slices;
    
    private MethodSlices(final InjectionInfo info) {
        this.slices = new HashMap<String, MethodSlice>(4);
        this.info = info;
    }
    
    private void add(final MethodSlice slice) {
        final String id = this.info.getSliceId(slice.getId());
        if (this.slices.containsKey(id)) {
            throw new InvalidSliceException((ISliceContext)this.info, slice + " has a duplicate id, '" + id + "' was already defined");
        }
        this.slices.put(id, slice);
    }
    
    public MethodSlice get(final String id) {
        return this.slices.get(id);
    }
    
    @Override
    public String toString() {
        return String.format("MethodSlices%s", this.slices.keySet());
    }
    
    public static MethodSlices parse(final InjectionInfo info) {
        final MethodSlices slices = new MethodSlices(info);
        final AnnotationNode annotation = info.getAnnotation();
        if (annotation != null) {
            for (final AnnotationNode node : Annotations.getValue(annotation, "slice", true)) {
                final MethodSlice slice = MethodSlice.parse(info, node);
                slices.add(slice);
            }
        }
        return slices;
    }
}
