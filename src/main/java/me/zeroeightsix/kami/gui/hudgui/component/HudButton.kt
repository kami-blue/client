package me.zeroeightsix.kami.gui.hudgui.component

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.KamiHudGui
import me.zeroeightsix.kami.gui.rgui.component.AbstractBooleanSlider
import me.zeroeightsix.kami.util.math.Vec2f

class HudButton(val hudElement: HudElement) : AbstractBooleanSlider(hudElement.name.value, 0.0, hudElement.description) {
    init {
        if (hudElement.visible.value) value = 1.0
    }

    override fun onTick() {
        super.onTick()
        value = if (hudElement.visible.value) 1.0 else 0.0
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (prevState != MouseState.DRAG) {
            if (buttonId == 0) hudElement.visible.value = !hudElement.visible.value
            else if (buttonId == 1) KamiHudGui.displaySettingWindow(module)
        }
    }
}