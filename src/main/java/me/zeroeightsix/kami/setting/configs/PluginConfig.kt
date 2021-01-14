package me.zeroeightsix.kami.setting.configs

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.plugin.IPluginClass
import java.io.File

open class PluginConfig(
    pluginName: String
) : NameableConfig<IPluginClass>(pluginName, "${KamiMod.DIRECTORY}config/plugins/") {
    final override val name: String get() = super.name
    final override val file: File get() = super.file
    final override val backup: File get() = super.backup
}