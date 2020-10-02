package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.gui.rgui.component.CheckButton
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.util.math.Vec2d

class SettingButton(val setting: Setting<Boolean>) : CheckButton(setting.name, setting.value) {
    override fun onTick() {
        super.onTick()
        value = if (setting.value) 0.0 else 1.0
    }

    override fun onRelease(mousePos: Vec2d, buttonId: Int) {
        setting.value = !setting.value
    }
}