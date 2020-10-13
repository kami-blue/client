package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.math.Vec2f

class ModuleButton(val module: Module) : CheckButton (module.name.value, module.isEnabled) {
    override fun onTick() {
        super.onTick()
        name = module.name.value
        value = if (module.isEnabled) 1.0f else 0.0f
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        if (buttonId == 0) module.toggle()
    }
}