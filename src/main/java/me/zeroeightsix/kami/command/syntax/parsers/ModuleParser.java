package me.zeroeightsix.kami.command.syntax.parsers;

import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;

public class ModuleParser extends AbstractParser {

    @Override
    public String getChunk(SyntaxChunk[] chunks, SyntaxChunk thisChunk, String[] values, String chunkValue) {
        if (chunkValue == null)
            return getDefaultChunk(thisChunk);

        Module chosen = null;
        for (Module module : ModuleManager.getModules()) {
            if (!module.isProduction()) continue;
            if (!module.name.getValue().toLowerCase().startsWith(chunkValue.toLowerCase())) continue;
            chosen = module;
            break;
        }

        if (chosen == null) return null;
        return chosen.name.getValue().substring(chunkValue.length());
    }

}
