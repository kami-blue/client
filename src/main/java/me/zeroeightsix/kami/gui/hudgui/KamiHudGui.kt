package me.zeroeightsix.kami.gui.hudgui

import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.gui.AbstractKamiGui
import me.zeroeightsix.kami.gui.GuiManager
import me.zeroeightsix.kami.gui.clickgui.KamiClickGui
import me.zeroeightsix.kami.gui.hudgui.component.HudButton
import me.zeroeightsix.kami.gui.hudgui.window.HudSettingWindow
import me.zeroeightsix.kami.gui.rgui.Component
import me.zeroeightsix.kami.gui.rgui.windows.ListWindow
import me.zeroeightsix.kami.util.math.Vec2f

object KamiHudGui : AbstractKamiGui<HudSettingWindow, HudElement>() {

    init {
        val allButtons = GuiManager.hudElementsMap.values.map { HudButton(it) }
        var posX = 10.0f

        for (category in HudElement.Category.values()) {
            val buttons = allButtons.filter { it.hudElement.category == category }.toTypedArray()
            if (buttons.isNullOrEmpty()) continue
            KamiClickGui.windowList.add(ListWindow(category.displayName, posX, 10.0f, 100.0f, 256.0f, Component.SettingGroup.HUD_GUI, *buttons))
            posX += 110.0f
        }
    }

    override fun newSettingWindow(element: HudElement, mousePos: Vec2f): HudSettingWindow {
        return HudSettingWindow(element, mousePos.x, mousePos.y)
    }

    init {
        KamiEventBus.subscribe(this)
    }

}