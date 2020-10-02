package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.math.Vec2d

class ModuleButton(val module: Module) : CheckButton (module.name.value, module.isEnabled) {
    override fun onTick() {
        super.onTick()
        name = module.name.value
        value = if (module.isEnabled) 1.0 else 0.0
    }

    override fun onRelease(mousePos: Vec2d, buttonId: Int) {
        if (buttonId == 0) module.toggle()
    }
}