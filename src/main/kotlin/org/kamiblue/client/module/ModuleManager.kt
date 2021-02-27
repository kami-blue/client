package org.kamiblue.client.module

import kotlinx.coroutines.Deferred
import org.kamiblue.client.AsyncLoader
import org.kamiblue.client.KamiMod
import org.kamiblue.client.util.AsyncCachedValue
import org.kamiblue.client.util.StopTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.commons.collections.AliasSet
import org.kamiblue.commons.utils.ClassUtils.instance
import org.lwjgl.input.Keyboard
import java.lang.reflect.Modifier
import kotlin.system.measureTimeMillis

object ModuleManager : AsyncLoader<List<Class<out AbstractModule>>> {
    override var deferred: Deferred<List<Class<out AbstractModule>>>? = null

    private val moduleSet = AliasSet<AbstractModule>()
    val modules by AsyncCachedValue(5L, TimeUnit.SECONDS) {
        moduleSet.distinct().sortedBy { it.name }
    }

    override suspend fun preLoad0(): List<Class<out AbstractModule>> {
        val classes = AsyncLoader.classes.await()
        val list: List<Class<*>>

        val time = measureTimeMillis {
            val clazz = AbstractModule::class.java

            list = classes.asSequence()
                .filter { Modifier.isFinal(it.modifiers) }
                .filter { it.name.startsWith("org.kamiblue.client.module.modules") }
                .filter { clazz.isAssignableFrom(it) }
                .sortedBy { it.simpleName }
                .toList()
        }

        KamiMod.LOG.info("${list.size} modules found, took ${time}ms")

        @Suppress("UNCHECKED_CAST")
        return list as List<Class<out AbstractModule>>
    }

    override suspend fun load0(input: List<Class<out AbstractModule>>) {
        val stopTimer = StopTimer()

        for (clazz in input) {
            moduleSet.add(clazz.instance.apply { postInit() })
        }

        val time = stopTimer.stop()
        KamiMod.LOG.info("${input.size} modules loaded, took ${time}ms")
    }

    internal fun onBind(eventKey: Int) {
        if (eventKey == 0 || Keyboard.isKeyDown(Keyboard.KEY_F3)) return  // if key is the 'none' key (stuff like mod key in i3 might return 0)
        for (module in modules) {
            if (module.bind.value.isDown(eventKey)) module.toggle()
        }
    }

    fun getModuleOrNull(moduleName: String?) = moduleName?.let { moduleSet[it] }
}