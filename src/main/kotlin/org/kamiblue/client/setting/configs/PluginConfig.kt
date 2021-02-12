package me.zeroeightsix.kami.setting.configs

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.plugin.api.IPluginClass
import me.zeroeightsix.kami.plugin.api.PluginHudElement
import me.zeroeightsix.kami.plugin.api.PluginModule
import me.zeroeightsix.kami.setting.settings.AbstractSetting
import java.io.File

class PluginConfig(pluginName: String) : NameableConfig<IPluginClass>(
    pluginName, "${KamiMod.DIRECTORY}config/plugins/$pluginName"
) {
    override val file: File get() = File("$filePath/default.json")
    override val backup: File get() = File("$filePath/default.bak")

    override fun <S : AbstractSetting<*>> IPluginClass.setting(setting: S): S {
        when (this) {
            is PluginModule -> {
                getGroupOrPut("modules").getGroupOrPut(name).addSetting(setting)
            }
            is PluginHudElement -> {
                getGroupOrPut("hud").getGroupOrPut(name).addSetting(setting)
            }
            else -> {
                getGroupOrPut("misc").getGroupOrPut(name).addSetting(setting)
            }
        }

        return setting
    }
}