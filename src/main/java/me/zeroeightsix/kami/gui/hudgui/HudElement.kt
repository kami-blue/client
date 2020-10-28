package me.zeroeightsix.kami.gui.hudgui

import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.gui.rgui.windows.BasicWindow

open class HudElement(
        name: String,
        val description: String
) : BasicWindow(name, 20.0f, 20.0f, 100.0f, 50.0f, true) {

    init {
        visible.valueListeners.add { _, it ->
            if (it) KamiEventBus.subscribe(this)
            else KamiEventBus.unsubscribe(this)
        }
    }

}