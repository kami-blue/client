// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.command.syntax.parsers;

import java.util.Iterator;
import me.zeroeightsix.kami.module.Module;
import java.util.Map;
import java.util.TreeMap;
import me.zeroeightsix.kami.setting.Setting;
import java.util.HashMap;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.command.syntax.SyntaxChunk;

public class ValueParser extends AbstractParser
{
    int moduleIndex;
    
    public ValueParser(final int moduleIndex) {
        this.moduleIndex = moduleIndex;
    }
    
    @Override
    public String getChunk(final SyntaxChunk[] chunks, final SyntaxChunk thisChunk, final String[] values, final String chunkValue) {
        if (this.moduleIndex > values.length - 1 || chunkValue == null) {
            return this.getDefaultChunk(thisChunk);
        }
        final String module = values[this.moduleIndex];
        final Module m = ModuleManager.getModuleByName(module);
        if (m == null) {
            return "";
        }
        final HashMap<String, Setting> possibilities = new HashMap<String, Setting>();
        for (final Setting v : m.settingList) {
            if (v.getName().toLowerCase().startsWith(chunkValue.toLowerCase())) {
                possibilities.put(v.getName(), v);
            }
        }
        if (possibilities.isEmpty()) {
            return "";
        }
        final TreeMap<String, Setting> p = new TreeMap<String, Setting>(possibilities);
        final Setting aV = p.firstEntry().getValue();
        return aV.getName().substring(chunkValue.length());
    }
}
