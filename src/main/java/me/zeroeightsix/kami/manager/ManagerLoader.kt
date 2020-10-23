package me.zeroeightsix.kami.manager

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.manager.managers.FileInstanceManager
import me.zeroeightsix.kami.util.ClassUtils
import me.zeroeightsix.kami.util.TimerUtils

/**
 * @author Xiaro
 *
 * Created by Xiaro on 08/18/20
 * Updated by Xiaro on 06/09/20
 */
object ManagerLoader {

    /** Thread for scanning managers during Forge pre-init */
    private var preLoadingThread: Thread? = null

    /** List for manager classes found during pre-loading */
    private var managerClassList: List<Class<out Manager>>? = null

    @JvmStatic
    fun preLoad() {
        preLoadingThread = Thread {
            managerClassList = ClassUtils.findClasses(FileInstanceManager::class.java.getPackage().name, Manager::class.java)
            KamiMod.log.info("${managerClassList!!.size} managers found")
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
        KamiMod.log.info("${managerClassList!!.size} managers loaded, took ${time}ms")
        preLoadingThread = null
        managerClassList = null
    }
}