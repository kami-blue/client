package org.kamiblue.client.setting

import org.kamiblue.client.KamiMod
import org.kamiblue.client.gui.rgui.Component
import org.kamiblue.client.module.modules.client.Configurations
import org.kamiblue.client.plugin.api.IPluginClass
import org.kamiblue.client.setting.GuiConfig.setting
import org.kamiblue.client.setting.configs.AbstractConfig
import org.kamiblue.client.setting.configs.PluginConfig
import org.kamiblue.client.setting.settings.AbstractSetting
import java.io.File

internal object GuiConfig : AbstractConfig<Component>(
    "gui",
    "${KamiMod.DIRECTORY}config/gui"
) {
    override val file: File get() = File("$filePath/${Configurations.guiPreset}.json")
    override val backup get() = File("$filePath/${Configurations.guiPreset}.bak")

    override fun addSettingToConfig(owner: Component, setting: AbstractSetting<*>) {
        if (owner is IPluginClass) {
            (owner.config as PluginConfig).addSettingToConfig(owner, setting)
        } else {
            val groupName = owner.settingGroup.groupName
            if (groupName.isNotEmpty()) {
                getGroupOrPut(groupName).getGroupOrPut(owner.name).addSetting(setting)
            }
        }
    }
}