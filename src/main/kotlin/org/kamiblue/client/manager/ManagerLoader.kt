package org.kamiblue.client.manager

import kotlinx.coroutines.Deferred
import org.kamiblue.client.AsyncLoader
import org.kamiblue.client.KamiMod
import org.kamiblue.client.event.KamiEventBus
import org.kamiblue.client.util.StopTimer
import org.kamiblue.commons.utils.ClassUtils
import org.kamiblue.commons.utils.ClassUtils.instance

internal object ManagerLoader : AsyncLoader<List<Class<out Manager>>> {
    override var deferred: Deferred<List<Class<out Manager>>>? = null

    override fun preLoad0(): List<Class<out Manager>> {
        val stopTimer = StopTimer()

        val list = ClassUtils.findClasses<Manager>("org.kamiblue.client.manager.managers")

        val time = stopTimer.stop()

        KamiMod.LOG.info("${list.size} managers found, took ${time}ms")
        return list
    }

    override fun load0(input: List<Class<out Manager>>) {
        val stopTimer = StopTimer()

        for (clazz in input) {
            KamiEventBus.subscribe(clazz.instance)
        }

        val time = stopTimer.stop()
        KamiMod.LOG.info("${input.size} managers loaded, took ${time}ms")
    }
}