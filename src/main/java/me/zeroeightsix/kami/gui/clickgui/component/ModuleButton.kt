package me.zeroeightsix.kami.gui.clickgui.component

import me.zeroeightsix.kami.gui.clickgui.KamiClickGui
import me.zeroeightsix.kami.gui.rgui.component.BooleanSlider
import me.zeroeightsix.kami.module.AbstractModule
import me.zeroeightsix.kami.util.math.Vec2f

class ModuleButton(val module: AbstractModule) : BooleanSlider(module.name, 0.0, module.description) {
    init {
        if (module.isEnabled) value = 1.0
    }

    override fun onTick() {
        super.onTick()
        value = if (module.isEnabled) 1.0 else 0.0
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (prevState != MouseState.DRAG) {
            if (buttonId == 0) module.toggle()
            else if (buttonId == 1) KamiClickGui.displaySettingWindow(module)
        }
    }
}