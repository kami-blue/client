package org.kamiblue.client.gui.hudgui.component

import org.kamiblue.client.gui.hudgui.HudElement
import org.kamiblue.client.gui.hudgui.KamiHudGui
import org.kamiblue.client.gui.rgui.component.BooleanSlider
import org.kamiblue.client.util.math.Vec2f

class HudButton(val hudElement: HudElement) : BooleanSlider(hudElement.name, 0.0, hudElement.description) {
    init {
        if (hudElement.visible) value = 1.0
    }

    override fun onTick() {
        super.onTick()
        value = if (hudElement.visible) 1.0 else 0.0
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (prevState != MouseState.DRAG) {
            if (buttonId == 0) hudElement.visible = !hudElement.visible
            else if (buttonId == 1) KamiHudGui.displaySettingWindow(hudElement)
        }
    }
}