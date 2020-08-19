package me.zeroeightsix.kami.manager

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.manager.mangers.FileInstanceManager
import me.zeroeightsix.kami.util.ClassFinder.findClasses

/**
 * @author Xiaro
 *
 * Created by Xiaro on 08/18/20
 */
object ManagerLoader {
    @JvmStatic
    fun loadManagers() {
        KamiMod.log.info("Registering managers...")
        for (clazz in findClasses(FileInstanceManager::class.java.getPackage().name, Manager::class.java)) {
            val manager = clazz.kotlin.objectInstance!!
            manager.new()
            KamiMod.EVENT_BUS.subscribe(manager)
        }
        KamiMod.log.info("Managers registered")
    }
}