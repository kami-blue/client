package me.zeroeightsix.kami.gui.hudgui.window

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.rgui.windows.SettingWindow
import me.zeroeightsix.kami.setting.settings.AbstractSetting

class HudSettingWindow(
    hudElement: HudElement,
    posX: Float,
    posY: Float
) : SettingWindow<HudElement>(hudElement.originalName, hudElement, posX, posY, SettingGroup.NONE) {

    override fun getSettingList(): List<AbstractSetting<*>> {
        return element.settingList
    }

}