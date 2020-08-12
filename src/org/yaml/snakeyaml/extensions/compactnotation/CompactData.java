// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.extensions.compactnotation;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class CompactData
{
    private String prefix;
    private List<String> arguments;
    private Map<String, String> properties;
    
    public CompactData(final String prefix) {
        this.arguments = new ArrayList<String>();
        this.properties = new HashMap<String, String>();
        this.prefix = prefix;
    }
    
    public String getPrefix() {
        return this.prefix;
    }
    
    public Map<String, String> getProperties() {
        return this.properties;
    }
    
    public List<String> getArguments() {
        return this.arguments;
    }
    
    @Override
    public String toString() {
        return "CompactData: " + this.prefix + " " + this.properties;
    }
}
