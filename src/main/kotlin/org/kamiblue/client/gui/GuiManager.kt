package org.kamiblue.client.gui

import kotlinx.coroutines.Deferred
import org.kamiblue.client.AsyncLoader
import org.kamiblue.client.KamiMod
import org.kamiblue.client.event.KamiEventBus
import org.kamiblue.client.gui.clickgui.KamiClickGui
import org.kamiblue.client.gui.hudgui.HudElement
import org.kamiblue.client.gui.hudgui.KamiHudGui
import org.kamiblue.client.util.StopTimer
import org.kamiblue.commons.utils.ClassUtils
import org.kamiblue.commons.utils.ClassUtils.instance
import org.lwjgl.input.Keyboard
import java.lang.reflect.Modifier

object GuiManager : AsyncLoader<List<Class<out HudElement>>> {
    override var deferred: Deferred<List<Class<out HudElement>>>? = null
    val hudElementsMap = LinkedHashMap<Class<out HudElement>, HudElement>()

    override fun preLoad0(): List<Class<out HudElement>> {
        val stopTimer = StopTimer()

        val list = ClassUtils.findClasses<HudElement>("org.kamiblue.client.gui.hudgui.elements") {
            filter { Modifier.isFinal(it.modifiers) }
        }

        val time = stopTimer.stop()

        KamiMod.LOG.info("${list.size} hud elements found, took ${time}ms")
        return list
    }

    override fun load0(input: List<Class<out HudElement>>) {
        val stopTimer = StopTimer()

        for (clazz in input) {
            hudElementsMap[clazz] = clazz.instance
        }

        val time = stopTimer.stop()
        KamiMod.LOG.info("${input.size} hud elements loaded, took ${time}ms")

        KamiClickGui.onGuiClosed()
        KamiHudGui.onGuiClosed()

        KamiEventBus.subscribe(KamiClickGui)
        KamiEventBus.subscribe(KamiHudGui)
    }

    internal fun onBind(eventKey: Int) {
        if (eventKey == 0 || Keyboard.isKeyDown(Keyboard.KEY_F3)) return  // if key is the 'none' key (stuff like mod key in i3 might return 0)
        for (hudElement in hudElementsMap) {
            if (hudElement.value.bind.isDown(eventKey)) hudElement.value.visible = !hudElement.value.visible
        }
    }
}