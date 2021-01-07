package me.zeroeightsix.kami.module

import kotlinx.coroutines.Deferred
import me.zeroeightsix.kami.AsyncLoader
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.util.AsyncCachedValue
import me.zeroeightsix.kami.util.StopTimer
import me.zeroeightsix.kami.util.TimeUnit
import org.kamiblue.commons.collections.AliasSet
import org.kamiblue.commons.utils.ClassUtils
import org.lwjgl.input.Keyboard

object ModuleManager : AsyncLoader<List<Class<out Module>>> {
    override var deferred: Deferred<List<Class<out Module>>>? = null

    private val moduleSet = AliasSet<Module>()
    val modules by AsyncCachedValue(5L, TimeUnit.SECONDS) {
        moduleSet.distinct()
    }

    override fun preLoad0(): List<Class<out Module>> {
        val stopTimer = StopTimer()

        val list = ClassUtils.findClasses("me.zeroeightsix.kami.module.modules", Module::class.java)
        val time = stopTimer.stop()

        KamiMod.LOG.info("${list.size} modules found, took ${time}ms")
        return list
    }

    override fun load0(input: List<Class<out Module>>) {
        val stopTimer = StopTimer()

        for (clazz in input) {
            moduleSet.add(ClassUtils.getInstance(clazz))
        }

        val time = stopTimer.stop()
        KamiMod.LOG.info("${input.size} modules loaded, took ${time}ms")
    }

    fun onBind(eventKey: Int) {
        if (eventKey == 0 || Keyboard.isKeyDown(Keyboard.KEY_F3)) return  // if key is the 'none' key (stuff like mod key in i3 might return 0)
        for (module in modules) {
            if (module.bind.value.isDown(eventKey)) module.toggle()
        }
    }

    fun getModuleOrNull(moduleName: String?) = moduleName?.let{ moduleSet[it] }
}