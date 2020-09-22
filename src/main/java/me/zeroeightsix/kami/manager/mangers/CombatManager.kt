package me.zeroeightsix.kami.manager.mangers

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.util.MotionTracker
import net.minecraft.entity.EntityLivingBase
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Xiaro
 *
 * Created by Xiaro on 06/08/20
 */
object CombatManager {
    private val combatModules = ArrayList<Module>()

    var targetList = LinkedList<EntityLivingBase>()
    var target: EntityLivingBase? = null
        set(value) {
            motionTracker.target = value
            field = value
        }
    val motionTracker = MotionTracker(null)

    fun isActiveAndTopPriority(module: Module) = module.isActive() && isOnTopPriority(module)

    fun isOnTopPriority(module: Module): Boolean {
        return getTopPriority() <= module.modulePriority
    }

    fun getTopPriority(): Int {
        return getTopModule()?.modulePriority ?: -1
    }

    fun getTopModule(): Module? {
        var topModule: Module? = null
        for (module in combatModules) {
            if (!module.isActive()) continue
            if (module.modulePriority < topModule?.modulePriority ?: 0) continue
            topModule = module
        }
        return topModule
    }

    init {
        for (module in ModuleManager.getModules()) {
            if (module.category != Module.Category.COMBAT) continue
            if (module.modulePriority == -1) continue
            combatModules.add(module)
        }
    }
}