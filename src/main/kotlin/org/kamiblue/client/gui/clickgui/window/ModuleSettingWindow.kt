package org.kamiblue.client.gui.clickgui.window

import org.kamiblue.client.gui.rgui.windows.SettingWindow
import org.kamiblue.client.module.AbstractModule
import org.kamiblue.client.setting.settings.AbstractSetting

class ModuleSettingWindow(
    module: AbstractModule,
    posX: Float,
    posY: Float
) : SettingWindow<AbstractModule>(module.name, module, posX, posY, SettingGroup.NONE) {

    override fun getSettingList(): List<AbstractSetting<*>> {
        return element.fullSettingList.filter { it.name != "Enabled" && it.name != "Clicks" }
    }

}