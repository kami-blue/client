package me.zeroeightsix.kami.gui.rgui.windows

import me.zeroeightsix.kami.gui.rgui.component.EnumSlider
import me.zeroeightsix.kami.gui.rgui.component.SettingButton
import me.zeroeightsix.kami.gui.rgui.component.SettingSlider
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.impl.number.NumberSetting
import me.zeroeightsix.kami.setting.impl.primitive.BooleanSetting
import me.zeroeightsix.kami.setting.impl.primitive.EnumSetting

class SettingWindow(val module: Module, posX: Float, posY: Float) : ListWindow("", posX, posY, 100.0f, 200.0f, false) {
    override val minWidth: Float get() = 100.0f
    override val minHeight: Float get() = draggableHeight

    override val minimizable get() = false

    init {
        for (setting in module.settingList) {
            when (setting) {
                is BooleanSetting -> SettingButton(setting)
                is NumberSetting -> SettingSlider(setting)
                is EnumSetting -> EnumSlider(setting)
                else -> null
            }?.also {
                children.add(it)
            }
        }
    }

    override fun onDisplayed() {
        super.onDisplayed()
        lastActiveTime = System.currentTimeMillis() + 1000L
        name.value = module.name.value
    }

}