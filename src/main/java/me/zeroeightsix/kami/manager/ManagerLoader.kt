package me.zeroeightsix.kami.manager

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.util.TimerUtils
import org.kamiblue.commons.utils.ClassUtils

object ManagerLoader {

    /** Thread for scanning managers during Forge pre-init */
    private var preLoadingThread: Thread? = null

    /** List for manager classes found during pre-loading */
    private var managerClassList: List<Class<out Manager>>? = null

    @JvmStatic
    fun preLoad() {
        preLoadingThread = Thread {
            val stopTimer = TimerUtils.StopTimer()
            managerClassList = ClassUtils.findClasses("me.zeroeightsix.kami.manager.managers", Manager::class.java)
            val time = stopTimer.stop()
            KamiMod.LOG.info("${managerClassList!!.size} manager(s) found, took ${time}ms")
        }
        preLoadingThread!!.name = "Managers Pre-Loading"
        preLoadingThread!!.start()
    }

    @JvmStatic
    fun load() {
        preLoadingThread!!.join()
        val stopTimer = TimerUtils.StopTimer()
        for (clazz in managerClassList!!) {
            ClassUtils.getInstance(clazz).also { KamiEventBus.subscribe(it) }
        }
        val time = stopTimer.stop()
        KamiMod.LOG.info("${managerClassList!!.size} managers loaded, took ${time}ms")
        preLoadingThread = null
        managerClassList = null
    }
}