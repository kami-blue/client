package me.zeroeightsix.kami.gui.hudgui

import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.gui.GuiManager
import me.zeroeightsix.kami.gui.KamiGui
import me.zeroeightsix.kami.gui.clickgui.KamiClickGui
import me.zeroeightsix.kami.gui.hudgui.component.HudButton
import me.zeroeightsix.kami.gui.rgui.Component
import me.zeroeightsix.kami.gui.rgui.windows.ListWindow

object KamiHudGui : KamiGui<HudElement>() {

    init {

    }

    init {
        KamiEventBus.subscribe(this)
    }
}