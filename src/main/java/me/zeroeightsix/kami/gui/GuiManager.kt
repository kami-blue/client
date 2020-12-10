package me.zeroeightsix.kami.gui

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.clickgui.KamiClickGui
import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.KamiHudGui
import me.zeroeightsix.kami.util.TimerUtils
import org.kamiblue.commons.utils.ClassUtils
import java.lang.reflect.Modifier

object GuiManager {

    private var preLoadingThread: Thread? = null
    private var hudElementsClassList: List<Class<out HudElement>>? = null
    val hudElementsMap = LinkedHashMap<Class<out HudElement>, HudElement>()

    @JvmStatic
    fun preLoad() {
        preLoadingThread = Thread {
            hudElementsClassList = ClassUtils.findClasses("me.zeroeightsix.kami.gui.hudgui.elements", HudElement::class.java)
                .filter { Modifier.isFinal(it.modifiers) }
            KamiMod.LOG.info("${hudElementsClassList!!.size} hud elements found")
        }
        preLoadingThread!!.name = "Gui Pre-Loading"
        preLoadingThread!!.start()
    }

    @JvmStatic
    fun load() {
        preLoadingThread!!.join()
        val stopTimer = TimerUtils.StopTimer()
        for (clazz in hudElementsClassList!!) {
            hudElementsMap[clazz] = ClassUtils.getInstance(clazz)
        }
        val time = stopTimer.stop()
        KamiMod.LOG.info("${hudElementsClassList!!.size} hud elements loaded, took ${time}ms")

        preLoadingThread = null
        hudElementsClassList = null

        KamiClickGui.onGuiClosed()
        KamiHudGui.onGuiClosed()
    }

}