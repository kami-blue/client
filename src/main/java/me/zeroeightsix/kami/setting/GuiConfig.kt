package me.zeroeightsix.kami.setting

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.rgui.Component
import me.zeroeightsix.kami.setting.config.AbstractConfig

object GuiConfig : AbstractConfig<Component>(
        "Gui",
        KamiMod.DIRECTORY
) {

    override fun <S : Setting<*>> Component.setting(setting: S): S {
        getGroupOrPut(this.name).addSetting(setting)
        return setting
    }

}