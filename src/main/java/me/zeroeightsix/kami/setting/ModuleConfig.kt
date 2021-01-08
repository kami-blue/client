package me.zeroeightsix.kami.setting

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.AbstractModule
import me.zeroeightsix.kami.setting.configs.NameableConfig
import java.io.File

internal object ModuleConfig : NameableConfig<AbstractModule>(
        "modules",
        "${KamiMod.DIRECTORY}modules",
) {
    private val pathSetting = GenericConfig.run { this@ModuleConfig.setting("CurrentPath", "default") }
    var currentPath by pathSetting

    override val file: File get() = File("$filePath/$currentPath.json")
    override val backup get() =  File("$filePath/$currentPath.bak")
}