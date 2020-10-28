package me.zeroeightsix.kami.setting

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.clickgui.window.ModuleSettingWindow
import me.zeroeightsix.kami.gui.rgui.Component
import me.zeroeightsix.kami.gui.rgui.WindowComponent
import me.zeroeightsix.kami.setting.config.AbstractConfig

object GuiConfig : AbstractConfig<Component>(
        "Gui",
        KamiMod.DIRECTORY
) {

    override fun <S : Setting<*>> Component.setting(setting: S): S {
        if (this is WindowComponent && this !is ModuleSettingWindow) {
            getGroupOrPut(this.originalName).addSetting(setting)
        }
        return setting
    }

}