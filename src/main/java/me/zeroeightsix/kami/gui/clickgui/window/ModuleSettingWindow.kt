package me.zeroeightsix.kami.gui.clickgui.window

import me.zeroeightsix.kami.gui.rgui.windows.SettingWindow
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.settings.AbstractSetting

class ModuleSettingWindow(
    module: Module,
    posX: Float,
    posY: Float
) : SettingWindow<Module>(module.name, module, posX, posY, SettingGroup.NONE) {

    override fun getSettingList(): List<AbstractSetting<*>> {
        return element.fullSettingList.filter { it.name != "Enabled" }
    }

}