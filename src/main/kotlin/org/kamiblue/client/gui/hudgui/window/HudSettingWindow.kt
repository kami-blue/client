package org.kamiblue.client.gui.hudgui.window

import org.kamiblue.client.gui.hudgui.AbstractHudElement
import org.kamiblue.client.gui.rgui.windows.SettingWindow
import org.kamiblue.client.setting.settings.AbstractSetting

class HudSettingWindow(
    hudElement: AbstractHudElement,
    posX: Float,
    posY: Float
) : SettingWindow<AbstractHudElement>(hudElement.name, hudElement, posX, posY, SettingGroup.NONE) {

    override fun getSettingList(): List<AbstractSetting<*>> {
        return element.settingList
    }

}