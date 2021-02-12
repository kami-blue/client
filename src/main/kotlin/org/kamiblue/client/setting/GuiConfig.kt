package org.kamiblue.client.setting

import org.kamiblue.client.KamiMod
import org.kamiblue.client.gui.rgui.Component
import org.kamiblue.client.module.modules.client.Configurations
import org.kamiblue.client.plugin.api.IPluginClass
import org.kamiblue.client.setting.configs.AbstractConfig
import org.kamiblue.client.setting.settings.AbstractSetting
import java.io.File

internal object GuiConfig : AbstractConfig<Component>(
    "gui",
    "${KamiMod.DIRECTORY}config/gui"
) {
    override val file: File get() = File("$filePath/${Configurations.guiPreset}.json")
    override val backup get() = File("$filePath/${Configurations.guiPreset}.bak")

    override fun <S : AbstractSetting<*>> Component.setting(setting: S): S {
        if (this is IPluginClass) {
            return pluginMain.config.addSetting(setting)
        }

        val groupName = settingGroup.groupName
        if (groupName.isNotEmpty()) {
            getGroupOrPut(groupName).getGroupOrPut(name).addSetting(setting)
        }

        return setting
    }
}