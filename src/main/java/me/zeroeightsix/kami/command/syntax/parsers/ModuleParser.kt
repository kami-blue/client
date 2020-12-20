package me.zeroeightsix.kami.command.syntax.parsers

import me.zeroeightsix.kami.command.syntax.SyntaxChunk
import me.zeroeightsix.kami.module.ModuleManager.getModules

class ModuleParser : AbstractParser() {
    override fun getChunk(chunks: Array<SyntaxChunk>, thisChunk: SyntaxChunk, values: Array<String>, chunkValue: String?): String? {
        if (chunkValue == null) return getDefaultChunk(thisChunk)

        for (module in getModules()) {
            if (!module.isProduction) continue
            if (module.name.value.startsWith(chunkValue, true)) {
                return module.name.value.subStringSafe(chunkValue.length)
            }
            module.alias.firstOrNull { it.startsWith(chunkValue, true) }?.let {
                return it.subStringSafe(chunkValue.length)
            }
        }

        return null
    }

    private fun String.subStringSafe(startIndex: Int) =
            if (startIndex == this.length) ""
            else this.substring(startIndex.coerceIn(this.indices))
}