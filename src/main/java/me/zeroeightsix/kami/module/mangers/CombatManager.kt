package me.zeroeightsix.kami.module.mangers

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.combat.AimBot
import me.zeroeightsix.kami.module.modules.combat.AntiBot
import me.zeroeightsix.kami.module.modules.combat.Aura
import me.zeroeightsix.kami.module.modules.combat.CrystalAuraRewrite
import net.minecraft.entity.Entity

/**
 * @author Xiaro
 *
 * Created by Xiaro on 06/08/20
 */
object CombatManager {
    val moduleList = hashMapOf(
            Pair(AntiBot::class.java, true),
            Pair(CrystalAuraRewrite::class.java, false),
            Pair(Aura::class.java, false),
            Pair(AimBot::class.java, true)
    )
    val ka = KamiMod.MODULE_MANAGER.getModuleT(Aura::class.java)
    val ca = KamiMod.MODULE_MANAGER.getModuleT(CrystalAuraRewrite::class.java)

    var targetList = ArrayList<Entity>()
    var currentTarget: Entity? = null
    var yaw: Float? = null
    var pitch: Float? = null

    private fun spoofRotation(yawIn: Float, pitchIn: Float, priority: Int) {
        if (priority < getTopPriority(true)) return
        yaw = yawIn
        pitch = pitchIn
    }

    private fun resetRotation() {
        yaw = null
        pitch = null
    }

    fun getTopPriority(ignoreAntiBot: Boolean): Int {
        return getPriority(getTopModule(ignoreAntiBot))
    }

    fun getTopModule(ignoreAntiBot: Boolean): Module? {
        var module: Module? = null
        for ((clazz, active) in moduleList) {
            if (!active) continue
            val currentModule = KamiMod.MODULE_MANAGER.getModuleT(clazz) ?: continue
            if (currentModule.isDisabled) continue
            if (ignoreAntiBot && currentModule is AntiBot) continue
            if (getPriority(currentModule) < getPriority(module)) continue
            module = currentModule
        }
        return module
    }

    private fun getPriority(module: Module?): Int {
        return when (module) {
            is AntiBot -> 4
            is CrystalAuraRewrite -> 3
            is Aura -> 2
            is AimBot -> 1
            else -> 0
        }
    }
}