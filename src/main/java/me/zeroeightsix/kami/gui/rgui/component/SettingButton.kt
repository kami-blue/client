package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.util.math.Vec2f

class SettingButton(val setting: Setting<Boolean>) : AbstractSlider(setting.name, 0.0) {
    init {
        if (setting.value) value = 1.0
    }

    override fun onTick() {
        super.onTick()
        value = if (setting.value) 0.0 else 1.0
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (prevState != MouseState.DRAG) {
            setting.value = !setting.value
        }
    }
}