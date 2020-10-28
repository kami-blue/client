package me.zeroeightsix.kami.gui.hudgui.window

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.rgui.component.*
import me.zeroeightsix.kami.gui.rgui.windows.SettingWindow
import me.zeroeightsix.kami.setting.impl.number.NumberSetting
import me.zeroeightsix.kami.setting.impl.other.BindSetting
import me.zeroeightsix.kami.setting.impl.primitive.BooleanSetting
import me.zeroeightsix.kami.setting.impl.primitive.EnumSetting
import me.zeroeightsix.kami.setting.impl.primitive.StringSetting

class HudSettingWindow(
        hudElement: HudElement,
        posX: Float,
        posY: Float
) : SettingWindow<HudElement>(hudElement.originalName, hudElement, posX, posY, SettingGroup.NONE) {

    init {
        for (setting in hudElement.settingList) {
            when (setting) {
                is BooleanSetting -> SettingButton(setting)
                is NumberSetting -> SettingSlider(setting)
                is EnumSetting -> EnumSlider(setting)
                is StringSetting -> StringButton(setting)
                is BindSetting -> BindButton(setting)
                else -> null
            }?.also {
                children.add(it)
            }
        }
    }

}