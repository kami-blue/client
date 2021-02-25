package org.kamiblue.client.gui.hudgui.window

import org.kamiblue.client.gui.hudgui.HudElement
import org.kamiblue.client.gui.rgui.windows.SettingWindow
import org.kamiblue.client.setting.settings.AbstractSetting

class HudSettingWindow(
    hudElement: HudElement,
    posX: Float,
    posY: Float
) : SettingWindow<HudElement>(hudElement.originalName, hudElement, posX, posY, SettingGroup.NONE) {

    override fun getSettingList(): List<AbstractSetting<*>> {
        return element.settingList
    }

}