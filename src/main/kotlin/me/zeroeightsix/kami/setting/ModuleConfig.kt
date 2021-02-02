package me.zeroeightsix.kami.setting

import org.kamiblue.client.KamiMod
import me.zeroeightsix.kami.module.AbstractModule
import me.zeroeightsix.kami.module.modules.client.Configurations
import me.zeroeightsix.kami.setting.configs.NameableConfig
import java.io.File

internal object ModuleConfig : NameableConfig<AbstractModule>(
    "modules",
    "${KamiMod.DIRECTORY}config/modules",
) {
    override val file: File get() = File("$filePath/${Configurations.modulePreset}.json")
    override val backup get() = File("$filePath/${Configurations.modulePreset}.bak")
}