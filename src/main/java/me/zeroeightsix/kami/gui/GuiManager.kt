package me.zeroeightsix.kami.gui

import kotlinx.coroutines.Deferred
import me.zeroeightsix.kami.AsyncLoader
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.gui.clickgui.KamiClickGui
import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.KamiHudGui
import me.zeroeightsix.kami.util.StopTimer
import org.kamiblue.commons.utils.ClassUtils
import java.lang.reflect.Modifier

object GuiManager : AsyncLoader<List<Class<out HudElement>>> {
    override var deferred: Deferred<List<Class<out HudElement>>>? = null
    val hudElementsMap = LinkedHashMap<Class<out HudElement>, HudElement>()

    override fun preLoad0(): List<Class<out HudElement>> {
        val stopTimer = StopTimer()

        val list = ClassUtils.findClasses("me.zeroeightsix.kami.gui.hudgui.elements", HudElement::class.java)
            .filter { Modifier.isFinal(it.modifiers) }
        val time = stopTimer.stop()

        KamiMod.LOG.info("${list.size} hud elements found, took ${time}ms")
        return list
    }

    override fun load0(input: List<Class<out HudElement>>) {
        val stopTimer = StopTimer()

        for (clazz in input) {
            hudElementsMap[clazz] = ClassUtils.getInstance(clazz)
        }

        val time = stopTimer.stop()
        KamiMod.LOG.info("${input.size} hud elements loaded, took ${time}ms")

        KamiClickGui.onGuiClosed()
        KamiHudGui.onGuiClosed()

        KamiEventBus.subscribe(KamiClickGui)
        KamiEventBus.subscribe(KamiHudGui)
    }
}