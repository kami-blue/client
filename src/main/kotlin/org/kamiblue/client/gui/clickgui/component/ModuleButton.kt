package org.kamiblue.client.gui.clickgui.component

import org.kamiblue.client.gui.clickgui.KamiClickGui
import org.kamiblue.client.gui.rgui.component.BooleanSlider
import org.kamiblue.client.module.AbstractModule
import org.kamiblue.client.util.math.Vec2f

class ModuleButton(val module: AbstractModule) : BooleanSlider(module.name, 0.0, module.description) {
    init {
        if (module.isEnabled) value = 1.0
    }

    override fun onTick() {
        super.onTick()
        value = if (module.isEnabled) 1.0 else 0.0
    }

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        if (buttonId == 0) module.toggle()
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (buttonId == 1) KamiClickGui.displaySettingWindow(module)
    }
}