// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation;

import java.util.Collection;
import java.util.Iterator;
import org.spongepowered.tools.obfuscation.service.ObfuscationServices;
import java.util.ArrayList;
import org.spongepowered.tools.obfuscation.mapping.IMappingConsumer;
import org.spongepowered.tools.obfuscation.interfaces.IReferenceManager;
import org.spongepowered.tools.obfuscation.interfaces.IObfuscationDataProvider;
import java.util.List;
import org.spongepowered.tools.obfuscation.interfaces.IMixinAnnotationProcessor;
import org.spongepowered.tools.obfuscation.interfaces.IObfuscationManager;

public class ObfuscationManager implements IObfuscationManager
{
    private final IMixinAnnotationProcessor ap;
    private final List<ObfuscationEnvironment> environments;
    private final IObfuscationDataProvider obfs;
    private final IReferenceManager refs;
    private final List<IMappingConsumer> consumers;
    private boolean initDone;
    
    public ObfuscationManager(final IMixinAnnotationProcessor ap) {
        this.environments = new ArrayList<ObfuscationEnvironment>();
        this.consumers = new ArrayList<IMappingConsumer>();
        this.ap = ap;
        this.obfs = new ObfuscationDataProvider(ap, this.environments);
        this.refs = new ReferenceManager(ap, this.environments);
    }
    
    @Override
    public void init() {
        if (this.initDone) {
            return;
        }
        this.initDone = true;
        ObfuscationServices.getInstance().initProviders(this.ap);
        for (final ObfuscationType obfType : ObfuscationType.types()) {
            if (obfType.isSupported()) {
                this.environments.add(obfType.createEnvironment());
            }
        }
    }
    
    @Override
    public IObfuscationDataProvider getDataProvider() {
        return this.obfs;
    }
    
    @Override
    public IReferenceManager getReferenceManager() {
        return this.refs;
    }
    
    @Override
    public IMappingConsumer createMappingConsumer() {
        final Mappings mappings = new Mappings();
        this.consumers.add(mappings);
        return mappings;
    }
    
    @Override
    public List<ObfuscationEnvironment> getEnvironments() {
        return this.environments;
    }
    
    @Override
    public void writeMappings() {
        for (final ObfuscationEnvironment env : this.environments) {
            env.writeMappings(this.consumers);
        }
    }
    
    @Override
    public void writeReferences() {
        this.refs.write();
    }
}
