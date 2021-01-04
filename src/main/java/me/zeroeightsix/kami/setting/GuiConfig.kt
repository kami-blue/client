package me.zeroeightsix.kami.setting

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.rgui.Component
import me.zeroeightsix.kami.setting.config.AbstractMultiConfig
import me.zeroeightsix.kami.setting.settings.AbstractSetting
import java.io.File

internal object GuiConfig : AbstractMultiConfig<Component>(
        "gui",
        KamiMod.DIRECTORY,
        "click_gui", "hud_gui"
) {
    override val file: File get() = File("$directoryPath$name")

    override fun <S : AbstractSetting<*>> Component.setting(setting: S): S {
        val groupName = settingGroup.groupName
        if (groupName.isNotEmpty()) {
            getGroupOrPut(groupName).getGroupOrPut(originalName).addSetting(setting)
        }
        return setting
    }

}