package org.kamiblue.client.manager

import kotlinx.coroutines.Deferred
import org.kamiblue.client.AsyncLoader
import org.kamiblue.client.KamiMod
import org.kamiblue.client.event.KamiEventBus
import org.kamiblue.client.util.StopTimer
import org.kamiblue.commons.utils.ClassUtils.instance
import java.lang.reflect.Modifier
import kotlin.system.measureTimeMillis

internal object ManagerLoader : AsyncLoader<List<Class<out Manager>>> {
    override var deferred: Deferred<List<Class<out Manager>>>? = null

    override suspend fun preLoad0(): List<Class<out Manager>> {
        val classes = AsyncLoader.classes.await()
        val list: List<Class<*>>

        val time = measureTimeMillis {
            val clazz = Manager::class.java

            list = classes.asSequence()
                .filter { Modifier.isFinal(it.modifiers) }
                .filter { it.name.startsWith("org.kamiblue.client.manager.managers") }
                .filter { clazz.isAssignableFrom(it) }
                .sortedBy { it.simpleName }
                .toList()
        }

        KamiMod.LOG.info("${list.size} managers found, took ${time}ms")

        @Suppress("UNCHECKED_CAST")
        return list as List<Class<out Manager>>
    }

    override suspend fun load0(input: List<Class<out Manager>>) {
        val stopTimer = StopTimer()

        for (clazz in input) {
            KamiEventBus.subscribe(clazz.instance)
        }

        val time = stopTimer.stop()
        KamiMod.LOG.info("${input.size} managers loaded, took ${time}ms")
    }
}