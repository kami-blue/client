package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.module.AbstractModule
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.setting.GenericConfig

internal object Configurations : AbstractModule(
    name = "Configurations",
    description = "Setting up configurations of the client",
    category = Category.CLIENT,
    alwaysEnabled = true,
    config = GenericConfig
) {
    val guiPresetSetting = GenericConfig.run { this@Configurations.setting("GuiPreset", "default") }
    var guiPreset by guiPresetSetting

    val modulePresetSetting = GenericConfig.run { this@Configurations.setting("ModulePreset", "default") }
    var modulePreset by modulePresetSetting
}