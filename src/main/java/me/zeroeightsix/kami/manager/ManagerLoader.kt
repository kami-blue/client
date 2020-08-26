package me.zeroeightsix.kami.manager

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.manager.mangers.FileInstanceManager
import me.zeroeightsix.kami.util.ClassFinder
import me.zeroeightsix.kami.util.TimerUtils

/**
 * @author Xiaro
 *
 * Created by Xiaro on 08/18/20
 */
object ManagerLoader {

    /** Thread for scanning managers during Forge pre-init */
    private var preLoadingThread: Thread? = null

    /** List for manager classes found during pre-loading */
    private var managerClassList: Array<Class<out Manager>>? = null

    @JvmStatic
    fun preLoad() {
        preLoadingThread = Thread {
            managerClassList = ClassFinder.findClasses(FileInstanceManager::class.java.getPackage().name, Manager::class.java)
            KamiMod.log.info("${managerClassList!!.size} managers found")
        }
        preLoadingThread!!.name = "Managers Pre-Loading"
        preLoadingThread!!.start()
    }

    @JvmStatic
    fun load() {
        Thread {
            preLoadingThread!!.join()
            val stopTimer = TimerUtils.StopTimer()
            for (clazz in managerClassList!!) {
                clazz.kotlin.objectInstance!!.new()
            }
            val time = stopTimer.stop()
            KamiMod.log.info("${managerClassList!!.size} managers loaded, took ${time}ms")
        }.start()
    }
}