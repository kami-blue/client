package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.math.Vec2f

class ModuleButton(val module: Module) : AbstractSlider (module.name.value, 0.0) {
    init {
        if (module.isEnabled) value = 1.0
    }

    override fun onTick() {
        super.onTick()
        name = module.name.value
        value = if (module.isEnabled) 1.0 else 0.0
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (prevState != MouseState.DRAG) {
            if (buttonId == 0) module.toggle()
        }
    }
}