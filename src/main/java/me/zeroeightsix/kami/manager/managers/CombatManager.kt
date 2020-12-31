package me.zeroeightsix.kami.manager.managers

import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.util.MotionTracker
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.util.math.BlockPos

object CombatManager : Manager {
    private val combatModules: List<Module>

    var target: EntityLivingBase? = null
        set(value) {
            motionTracker.target = value
            field = value
        }
    var placeMap = emptyMap<BlockPos, Triple<Float, Float, Double>>() // <BlockPos, <Target Damage, Self Damage, Distance>>
    var crystalMap = emptyMap<EntityEnderCrystal, Triple<Float, Float, Double>>() // <Crystal, <Target Damage, Self Damage, Distance>>
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

    /** Use to mark a module that should be added to [combatModules] */
    annotation class CombatModule

    init {
        val cacheList = ArrayList<Module>()
        val annotationClass = CombatModule::class.java
        for (module in ModuleManager.getModules()) {
            if (module.category != Module.Category.COMBAT) continue
            if (!module.javaClass.isAnnotationPresent(annotationClass)) continue
            cacheList.add(module)
        }
        combatModules = cacheList
    }
}