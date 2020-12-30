package me.zeroeightsix.kami.manager

import kotlinx.coroutines.Deferred
import me.zeroeightsix.kami.AsyncLoader
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.util.TimerUtils
import org.kamiblue.commons.utils.ClassUtils

internal object ManagerLoader : AsyncLoader<List<Class<out Manager>>> {
    override var deferred: Deferred<List<Class<out Manager>>>? = null

    override fun preLoad0(): List<Class<out Manager>> {
        val stopTimer = TimerUtils.StopTimer()

        val list = ClassUtils.findClasses("me.zeroeightsix.kami.manager.managers", Manager::class.java)
        val time = stopTimer.stop()

        KamiMod.LOG.info("${list.size} managers found, took ${time}ms")
        return list
    }

    override fun load0(input: List<Class<out Manager>>) {
        val stopTimer = TimerUtils.StopTimer()

        for (clazz in input) {
            ClassUtils.getInstance(clazz).also { KamiEventBus.subscribe(it) }
        }

        val time = stopTimer.stop()
        KamiMod.LOG.info("${input.size} managers loaded, took ${time}ms")
    }
}