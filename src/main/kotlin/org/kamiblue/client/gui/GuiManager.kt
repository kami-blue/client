package org.kamiblue.client.gui

import kotlinx.coroutines.Deferred
import org.kamiblue.client.AsyncLoader
import org.kamiblue.client.KamiMod
import org.kamiblue.client.event.KamiEventBus
import org.kamiblue.client.gui.clickgui.KamiClickGui
import org.kamiblue.client.gui.hudgui.HudElement
import org.kamiblue.client.gui.hudgui.KamiHudGui
import org.kamiblue.client.util.AsyncCachedValue
import org.kamiblue.client.util.StopTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.commons.collections.AliasSet
import org.kamiblue.commons.utils.ClassUtils
import org.kamiblue.commons.utils.ClassUtils.instance
import org.lwjgl.input.Keyboard
import java.lang.reflect.Modifier

object GuiManager : AsyncLoader<List<Class<out HudElement>>> {
    override var deferred: Deferred<List<Class<out HudElement>>>? = null
    private val hudElementSet = AliasSet<HudElement>()

    val hudElements by AsyncCachedValue(5L, TimeUnit.SECONDS) {
        hudElementSet.distinct().sortedBy { it.name }
    }

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
            hudElementSet.add(clazz.instance)
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
        for (hudElement in hudElementSet) {
            if (hudElement.bind.isDown(eventKey)) hudElement.visible = !hudElement.visible
        }
    }

    fun getHudElementOrNull(name: String?) = name?.let { hudElementSet[it] }
}