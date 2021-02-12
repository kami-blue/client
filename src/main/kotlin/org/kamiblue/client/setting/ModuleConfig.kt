package org.kamiblue.client.setting

import org.kamiblue.client.KamiMod
import org.kamiblue.client.module.AbstractModule
import org.kamiblue.client.module.modules.client.Configurations
import org.kamiblue.client.setting.configs.NameableConfig
import java.io.File

internal object ModuleConfig : NameableConfig<AbstractModule>(
    "modules",
    "${KamiMod.DIRECTORY}config/modules",
) {
    override val file: File get() = File("$filePath/${Configurations.modulePreset}.json")
    override val backup get() = File("$filePath/${Configurations.modulePreset}.bak")
}