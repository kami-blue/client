package me.zeroeightsix.kami.gui.hudgui

import me.zeroeightsix.kami.KamiGui
import me.zeroeightsix.kami.event.KamiEventBus

object KamiHudGui : KamiGui<HudElement>() {

    init {

    }

    init {
        KamiEventBus.subscribe(this)
    }
}