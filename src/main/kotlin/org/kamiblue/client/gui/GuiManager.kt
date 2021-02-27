package org.kamiblue.client.gui

import kotlinx.coroutines.Deferred
import org.kamiblue.client.AsyncLoader
import org.kamiblue.client.KamiMod
import org.kamiblue.client.event.KamiEventBus
import org.kamiblue.client.gui.clickgui.KamiClickGui
import org.kamiblue.client.gui.hudgui.HudElement
import org.kamiblue.client.gui.hudgui.KamiHudGui
import org.kamiblue.client.util.StopTimer
import org.kamiblue.commons.utils.ClassUtils.instance
import org.lwjgl.input.Keyboard
import java.lang.reflect.Modifier
import kotlin.system.measureTimeMillis

object GuiManager : AsyncLoader<List<Class<out HudElement>>> {
    override var deferred: Deferred<List<Class<out HudElement>>>? = null
    val hudElementsMap = LinkedHashMap<Class<out HudElement>, HudElement>()

    override suspend fun preLoad0(): List<Class<out HudElement>> {
        val classes = AsyncLoader.classes.await()
        val list: List<Class<*>>

        val time = measureTimeMillis {
            val clazz = HudElement::class.java

            list = classes.asSequence()
                .filter { Modifier.isFinal(it.modifiers) }
                .filter { it.name.startsWith("org.kamiblue.client.gui.hudgui.elements") }
                .filter { clazz.isAssignableFrom(it) }
                .sortedBy { it.simpleName }
                .toList()
        }

        KamiMod.LOG.info("${list.size} hud elements found, took ${time}ms")

        @Suppress("UNCHECKED_CAST")
        return list as List<Class<out HudElement>>
    }

    override suspend fun load0(input: List<Class<out HudElement>>) {
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