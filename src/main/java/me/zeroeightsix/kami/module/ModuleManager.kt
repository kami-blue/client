package me.zeroeightsix.kami.module

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.util.ClassUtils
import me.zeroeightsix.kami.util.TimerUtils

object ModuleManager {

    /** Thread for scanning module during Forge pre-init */
    private var preLoadingThread: Thread? = null

    /** List for module classes found during pre-loading */
    private var moduleClassList: List<Class<out Module>>? = null

    /** HashMap for the registered Modules */
    private val moduleMap = LinkedHashMap<Class<out Module>, Module>()

    @JvmStatic
    fun preLoad() {
        preLoadingThread = Thread {
            moduleClassList = ClassUtils.findClasses("me.zeroeightsix.kami.module.modules", Module::class.java).sortedBy { it.simpleName }
            KamiMod.log.info("${moduleClassList!!.size} modules found")
        }
        preLoadingThread!!.name = "Modules Pre-Loading"
        preLoadingThread!!.start()
    }

    /**
     * Registers modules
     */
    @JvmStatic
    fun load() {
        preLoadingThread!!.join()
        val stopTimer = TimerUtils.StopTimer()
        for (clazz in moduleClassList!!) {
            try {
                moduleMap[clazz] = ClassUtils.getInstance(clazz)
            } catch (exception: Throwable) {
                System.err.println("Couldn't initiate module " + clazz.simpleName + "! Err: " + exception.javaClass.simpleName + ", message: " + exception.message)
                exception.printStackTrace()
            }
        }
        val time = stopTimer.stop()
        KamiMod.log.info("${moduleMap.size} modules loaded, took ${time}ms")

        /* Clean up variables used during pre-loading and registering */
        preLoadingThread = null
        moduleClassList = null
    }

    fun onBind(eventKey: Int) {
        if (eventKey == 0) return  // if key is the 'none' key (stuff like mod key in i3 might return 0)
        for (module in getModules()) {
            if (module.bind.value.isDown(eventKey)) module.toggle()
        }
    }

    @JvmStatic
    fun getModules() = moduleMap.values

    @JvmStatic
    fun getModule(moduleName: String?): Module? {
        return moduleName?.replace(" ", "").let { name ->
            getModules().firstOrNull { module ->
                module.name.value.replace(" ", "").equals(name, true)
                        || module.alias.any { it.replace(" ", "").equals(name, true) }
            }
        } ?: throw ModuleNotFoundException("Error: Module not found. Check the spelling of the module. (getModuleByName(String) failed)")
    }

    class ModuleNotFoundException(s: String?) : IllegalArgumentException(s)
}