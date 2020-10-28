package me.zeroeightsix.kami.gui.clickgui.window

import me.zeroeightsix.kami.gui.rgui.component.*
import me.zeroeightsix.kami.gui.rgui.windows.SettingWindow
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.impl.number.NumberSetting
import me.zeroeightsix.kami.setting.impl.other.BindSetting
import me.zeroeightsix.kami.setting.impl.primitive.BooleanSetting
import me.zeroeightsix.kami.setting.impl.primitive.EnumSetting
import me.zeroeightsix.kami.setting.impl.primitive.StringSetting

class ModuleSettingWindow(
        module: Module,
        posX: Float,
        posY: Float
) : SettingWindow<Module>(module.name, module, posX, posY, SettingGroup.NONE) {

    init {
        for (setting in module.fullSettingList) {
            if (setting.name == "Enabled") continue
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