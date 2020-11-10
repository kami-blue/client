package me.zeroeightsix.kami.setting

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.rgui.Component
import me.zeroeightsix.kami.setting.config.AbstractMultiConfig
import java.io.File

object GuiConfig : AbstractMultiConfig<Component>(
        "Gui",
        KamiMod.DIRECTORY,
        "ClickGUI", "HudGUI"
) {
    override val file: File get() = File("$directoryPath$name")

    override fun <S : Setting<*>> Component.setting(setting: S): S {
        settingGroup.groupName?.let {
            getGroupOrPut(it).getGroupOrPut(originalName).addSetting(setting)
        }
        return setting
    }

}