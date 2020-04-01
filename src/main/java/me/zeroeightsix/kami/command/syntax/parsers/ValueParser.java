package me.zeroeightsix.kami.command.syntax.parsers;

import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Setting;

import java.util.HashMap;
import java.util.TreeMap;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

public class ValueParser extends AbstractParser {

    private int moduleIndex;

    public ValueParser(int moduleIndex) {
        this.moduleIndex = moduleIndex;
    }

    public String getChunk(SyntaxChunk[] chunks, SyntaxChunk thisChunk, String[] values, String chunkValue) {
        if (moduleIndex > values.length - 1 || chunkValue == null) return getDefaultChunk(thisChunk);
        String module = values[moduleIndex];
        try {
            Module m = MODULE_MANAGER.getModule(module);
            HashMap<String, Setting<?>> possibilities = new HashMap<>();

            for (Setting<?> v : m.settingList) {
                if (v.getName().toLowerCase().startsWith(chunkValue.toLowerCase()))
                    possibilities.put(v.getName(), v);
            }

            if (possibilities.isEmpty()) return "";

            TreeMap<String, Setting<?>> p = new TreeMap<>(possibilities);
            Setting<?> aV = p.firstEntry().getValue();
            return aV.getName().substring(chunkValue.length());
        } catch (ModuleManager.ModuleNotFoundException x) {
            return "";
        }

    }
}
