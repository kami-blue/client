package me.zeroeightsix.kami.setting

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame
import me.zeroeightsix.kami.setting.config.AbstractConfig

object GuiConfig : AbstractConfig<Frame>(
        "Gui",
        KamiMod.DIRECTORY
) {

    override fun <S : Setting<*>> Frame.setting(setting: S): S {
        getGroupOrPut(this.title).addSetting(setting)
        return setting
    }

    fun clearSettings() {
        subSetting.clear()
        subGroup.clear()
    }

}