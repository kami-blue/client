// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation.mcp;

import com.google.common.collect.ImmutableList;
import org.spongepowered.tools.obfuscation.ObfuscationEnvironment;
import org.spongepowered.tools.obfuscation.service.ObfuscationTypeDescriptor;
import java.util.Collection;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.spongepowered.tools.obfuscation.service.IObfuscationService;

public class ObfuscationServiceMCP implements IObfuscationService
{
    public static final String SEARGE = "searge";
    public static final String NOTCH = "notch";
    public static final String REOBF_SRG_FILE = "reobfSrgFile";
    public static final String REOBF_EXTRA_SRG_FILES = "reobfSrgFiles";
    public static final String REOBF_NOTCH_FILE = "reobfNotchSrgFile";
    public static final String REOBF_EXTRA_NOTCH_FILES = "reobfNotchSrgFiles";
    public static final String OUT_SRG_SRG_FILE = "outSrgFile";
    public static final String OUT_NOTCH_SRG_FILE = "outNotchSrgFile";
    public static final String OUT_REFMAP_FILE = "outRefMapFile";
    
    @Override
    public Set<String> getSupportedOptions() {
        return (Set<String>)ImmutableSet.of((Object)"reobfSrgFile", (Object)"reobfSrgFiles", (Object)"reobfNotchSrgFile", (Object)"reobfNotchSrgFiles", (Object)"outSrgFile", (Object)"outNotchSrgFile", (Object[])new String[] { "outRefMapFile" });
    }
    
    @Override
    public Collection<ObfuscationTypeDescriptor> getObfuscationTypes() {
        return (Collection<ObfuscationTypeDescriptor>)ImmutableList.of((Object)new ObfuscationTypeDescriptor("searge", "reobfSrgFile", "reobfSrgFiles", "outSrgFile", ObfuscationEnvironmentMCP.class), (Object)new ObfuscationTypeDescriptor("notch", "reobfNotchSrgFile", "reobfNotchSrgFiles", "outNotchSrgFile", ObfuscationEnvironmentMCP.class));
    }
}
