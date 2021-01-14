package me.zeroeightsix.kami.setting

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.rgui.Component
import me.zeroeightsix.kami.module.modules.client.Configurations
import me.zeroeightsix.kami.plugin.api.IPluginClass
import me.zeroeightsix.kami.setting.configs.AbstractConfig
import me.zeroeightsix.kami.setting.settings.AbstractSetting
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