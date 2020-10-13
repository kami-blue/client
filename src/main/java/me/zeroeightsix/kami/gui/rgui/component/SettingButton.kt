package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.util.math.Vec2f

class SettingButton(val setting: Setting<Boolean>) : CheckButton(setting.name, setting.value) {
    override fun onTick() {
        super.onTick()
        value = if (setting.value) 0.0f else 1.0f
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        setting.value = !setting.value
    }
}