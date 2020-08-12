// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation;

import org.spongepowered.asm.obfuscation.mapping.IMapping;
import java.util.Iterator;
import java.util.HashMap;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import java.util.Map;
import org.spongepowered.tools.obfuscation.mapping.IMappingConsumer;

class Mappings implements IMappingConsumer
{
    private final Map<ObfuscationType, MappingSet<MappingField>> fieldMappings;
    private final Map<ObfuscationType, MappingSet<MappingMethod>> methodMappings;
    private UniqueMappings unique;
    
    public Mappings() {
        this.fieldMappings = new HashMap<ObfuscationType, MappingSet<MappingField>>();
        this.methodMappings = new HashMap<ObfuscationType, MappingSet<MappingMethod>>();
        this.init();
    }
    
    private void init() {
        for (final ObfuscationType obfType : ObfuscationType.types()) {
            this.fieldMappings.put(obfType, new MappingSet<MappingField>());
            this.methodMappings.put(obfType, new MappingSet<MappingMethod>());
        }
    }
    
    public IMappingConsumer asUnique() {
        if (this.unique == null) {
            this.unique = new UniqueMappings(this);
        }
        return this.unique;
    }
    
    @Override
    public MappingSet<MappingField> getFieldMappings(final ObfuscationType type) {
        final MappingSet<MappingField> mappings = this.fieldMappings.get(type);
        return (mappings != null) ? mappings : new MappingSet<MappingField>();
    }
    
    @Override
    public MappingSet<MappingMethod> getMethodMappings(final ObfuscationType type) {
        final MappingSet<MappingMethod> mappings = this.methodMappings.get(type);
        return (mappings != null) ? mappings : new MappingSet<MappingMethod>();
    }
    
    @Override
    public void clear() {
        this.fieldMappings.clear();
        this.methodMappings.clear();
        if (this.unique != null) {
            this.unique.clearMaps();
        }
        this.init();
    }
    
    @Override
    public void addFieldMapping(final ObfuscationType type, final MappingField from, final MappingField to) {
        MappingSet<MappingField> mappings = this.fieldMappings.get(type);
        if (mappings == null) {
            mappings = new MappingSet<MappingField>();
            this.fieldMappings.put(type, mappings);
        }
        mappings.add(new MappingSet.Pair<MappingField>(from, to));
    }
    
    @Override
    public void addMethodMapping(final ObfuscationType type, final MappingMethod from, final MappingMethod to) {
        MappingSet<MappingMethod> mappings = this.methodMappings.get(type);
        if (mappings == null) {
            mappings = new MappingSet<MappingMethod>();
            this.methodMappings.put(type, mappings);
        }
        mappings.add(new MappingSet.Pair<MappingMethod>(from, to));
    }
    
    public static class MappingConflictException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
        private final IMapping<?> oldMapping;
        private final IMapping<?> newMapping;
        
        public MappingConflictException(final IMapping<?> oldMapping, final IMapping<?> newMapping) {
            this.oldMapping = oldMapping;
            this.newMapping = newMapping;
        }
        
        public IMapping<?> getOld() {
            return this.oldMapping;
        }
        
        public IMapping<?> getNew() {
            return this.newMapping;
        }
    }
    
    static class UniqueMappings implements IMappingConsumer
    {
        private final IMappingConsumer mappings;
        private final Map<ObfuscationType, Map<MappingField, MappingField>> fields;
        private final Map<ObfuscationType, Map<MappingMethod, MappingMethod>> methods;
        
        public UniqueMappings(final IMappingConsumer mappings) {
            this.fields = new HashMap<ObfuscationType, Map<MappingField, MappingField>>();
            this.methods = new HashMap<ObfuscationType, Map<MappingMethod, MappingMethod>>();
            this.mappings = mappings;
        }
        
        @Override
        public void clear() {
            this.clearMaps();
            this.mappings.clear();
        }
        
        protected void clearMaps() {
            this.fields.clear();
            this.methods.clear();
        }
        
        @Override
        public void addFieldMapping(final ObfuscationType type, final MappingField from, final MappingField to) {
            if (!this.checkForExistingMapping(type, from, to, this.fields)) {
                this.mappings.addFieldMapping(type, from, to);
            }
        }
        
        @Override
        public void addMethodMapping(final ObfuscationType type, final MappingMethod from, final MappingMethod to) {
            if (!this.checkForExistingMapping(type, from, to, this.methods)) {
                this.mappings.addMethodMapping(type, from, to);
            }
        }
        
        private <TMapping extends IMapping<TMapping>> boolean checkForExistingMapping(final ObfuscationType type, final TMapping from, final TMapping to, final Map<ObfuscationType, Map<TMapping, TMapping>> mappings) throws MappingConflictException {
            Map<TMapping, TMapping> existingMappings = mappings.get(type);
            if (existingMappings == null) {
                existingMappings = new HashMap<TMapping, TMapping>();
                mappings.put(type, existingMappings);
            }
            final TMapping existing = existingMappings.get(from);
            if (existing == null) {
                existingMappings.put(from, to);
                return false;
            }
            if (existing.equals(to)) {
                return true;
            }
            throw new MappingConflictException(existing, to);
        }
        
        @Override
        public MappingSet<MappingField> getFieldMappings(final ObfuscationType type) {
            return this.mappings.getFieldMappings(type);
        }
        
        @Override
        public MappingSet<MappingMethod> getMethodMappings(final ObfuscationType type) {
            return this.mappings.getMethodMappings(type);
        }
    }
}
