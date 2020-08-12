// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.scanners;

import org.reflections.adapters.MetadataAdapter;
import org.reflections.ReflectionsException;
import org.reflections.vfs.Vfs;
import com.google.common.base.Predicates;
import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;
import org.reflections.Configuration;

public abstract class AbstractScanner implements Scanner
{
    private Configuration configuration;
    private Multimap<String, String> store;
    private Predicate<String> resultFilter;
    
    public AbstractScanner() {
        this.resultFilter = (Predicate<String>)Predicates.alwaysTrue();
    }
    
    @Override
    public boolean acceptsInput(final String file) {
        return this.getMetadataAdapter().acceptsInput(file);
    }
    
    @Override
    public Object scan(final Vfs.File file, Object classObject) {
        if (classObject == null) {
            try {
                classObject = this.configuration.getMetadataAdapter().getOfCreateClassObject(file);
            }
            catch (Exception e) {
                throw new ReflectionsException("could not create class object from file " + file.getRelativePath(), e);
            }
        }
        this.scan(classObject);
        return classObject;
    }
    
    public abstract void scan(final Object p0);
    
    public Configuration getConfiguration() {
        return this.configuration;
    }
    
    @Override
    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }
    
    @Override
    public Multimap<String, String> getStore() {
        return this.store;
    }
    
    @Override
    public void setStore(final Multimap<String, String> store) {
        this.store = store;
    }
    
    public Predicate<String> getResultFilter() {
        return this.resultFilter;
    }
    
    public void setResultFilter(final Predicate<String> resultFilter) {
        this.resultFilter = resultFilter;
    }
    
    @Override
    public Scanner filterResultsBy(final Predicate<String> filter) {
        this.setResultFilter(filter);
        return this;
    }
    
    @Override
    public boolean acceptResult(final String fqn) {
        return fqn != null && this.resultFilter.apply((Object)fqn);
    }
    
    protected MetadataAdapter getMetadataAdapter() {
        return this.configuration.getMetadataAdapter();
    }
    
    @Override
    public boolean equals(final Object o) {
        return this == o || (o != null && this.getClass() == o.getClass());
    }
    
    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }
}
