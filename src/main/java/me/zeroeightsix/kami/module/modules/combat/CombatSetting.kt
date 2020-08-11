package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.manager.CombatManager
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.CombatUtils
import me.zeroeightsix.kami.util.EntityUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase

/**
 * Created by Xiaro on 26/07/20
 */
@Module.Info(
        name = "CombatSetting",
        description = "Settings for combat module targeting",
        category = Module.Category.COMBAT,
        showOnArray = Module.ShowOnArray.OFF,
        alwaysListening = true
)
class CombatSetting : Module() {
    private val mainPriority = register(Settings.e<MainPriority>("MainPriority", MainPriority.ALL))
    private val subPriority = register(Settings.e<SubPriority>("SubPriority", SubPriority.DISTANCE))
    private val players = register(Settings.b("Players", true))
    private val friends = register(Settings.booleanBuilder("Friends").withValue(false).withVisibility { players.value }.build())
    private val sleeping = register(Settings.booleanBuilder("Sleeping").withValue(false).withVisibility { players.value }.build())
    private val mobs = register(Settings.b("Mobs", false))
    private val passive = register(Settings.booleanBuilder("PassiveMobs").withValue(false).withVisibility { mobs.value }.build())
    private val neutral = register(Settings.booleanBuilder("NeutralMobs").withValue(false).withVisibility { mobs.value }.build())
    private val hostile = register(Settings.booleanBuilder("HostileMobs").withValue(false).withVisibility { mobs.value }.build())
    private val invisible = register(Settings.b("Invisible", false))
    private val ignoreWalls = register(Settings.booleanBuilder("IgnoreWalls").withValue(false).build())
    private val range = register(Settings.floatBuilder("TargetRange").withValue(12.0f).withRange(0.0f, 48.0f).build())

    private enum class MainPriority {
        ALL, FOV, MANUAL
    }

    private enum class SubPriority {
        DISTANCE, HEALTH, DAMAGE
    }

    override fun onUpdate() {
        val player = arrayOf(players.value, friends.value, sleeping.value)
        val mob = arrayOf(mobs.value, passive.value, neutral.value, hostile.value)
        val targetList = EntityUtils.getTargetList(player, mob, invisible.value, range.value)
        val toRemove = ArrayList<Entity>()
        if (!shouldIgnoreWall()) for (entity in targetList) {
            if (mc.player.canEntityBeSeen(entity) || EntityUtils.canEntityFeetBeSeen(entity)) continue
            toRemove.add(entity)
        }
        targetList.removeAll(toRemove)
        CombatManager.targetList = targetList
        CombatManager.currentTarget = if (targetList.isEmpty()) {
            null
        } else {
            getTarget(targetList)
        }
    }

    override fun onDisable() {
        enable()
    }

    private fun getTarget(targetList: ArrayList<Entity>): Entity? {
        val mainPriorityList = getByMainPriority(targetList)

        return getBySubPriority(mainPriorityList)
    }

    private fun getByMainPriority(targetList: ArrayList<Entity>): ArrayList<Entity> {
        val toRemove = ArrayList<Entity>()

        when (mainPriority.value) {
            MainPriority.ALL -> {

            }

            MainPriority.FOV -> {
                //TODO
            }

            MainPriority.MANUAL -> {
                if (!mc.gameSettings.keyBindAttack.isKeyDown && !mc.gameSettings.keyBindUseItem.isKeyDown) {
                    return if (CombatManager.currentTarget != null && targetList.contains(CombatManager.currentTarget!!)) {
                        arrayListOf(CombatManager.currentTarget!!)
                    } else emptyList<Entity>() as ArrayList
                }
                val eyePos = mc.player.getPositionEyes(mc.renderPartialTicks)
                val lookVec = mc.player.lookVec.scale(range.value.toDouble())
                val sightEndPos = eyePos.add(lookVec)
                for (e in targetList) {
                    e.boundingBox.calculateIntercept(eyePos, sightEndPos)
                            ?: toRemove.add(e)
                }
            }

            else -> { }
        }

        targetList.removeAll(toRemove)
        return targetList
    }

    private fun getBySubPriority(targetList: ArrayList<Entity>): Entity? {
        var entity = targetList.getOrNull(0) ?: return null
        val toKeep = ArrayList<Entity>(targetList)

        if (subPriority.value == SubPriority.DAMAGE) {
            var damage = CombatUtils.calcDamage(targetList[0] as EntityLivingBase, true)
            for (e in targetList) {
                val currentDamage = CombatUtils.calcDamage(e as EntityLivingBase, true)
                if (currentDamage >= damage) {
                    if (currentDamage > damage) {
                        damage = currentDamage
                        toKeep.clear()
                    }
                    toKeep.add(e)
                }
            }
            targetList.clear()
            targetList.addAll(toKeep)
            toKeep.clear()
        }

        if (subPriority.value == SubPriority.DAMAGE || subPriority.value == SubPriority.HEALTH) {
            var health = (targetList[0] as EntityLivingBase).health
            for (e in targetList) {
                val currentHealth = (e as EntityLivingBase).health
                if (currentHealth <= health) {
                    if (currentHealth < health) {
                        health = currentHealth
                        toKeep.clear()
                    }
                    toKeep.add(e)
                }
            }
            targetList.clear()
            targetList.addAll(toKeep)
        }

        var distance = mc.player.getDistance(targetList[0])
        for (e in targetList) {
            val currentDistance = mc.player.getDistance(e)
            if (currentDistance < distance) {
                distance = currentDistance
                entity = e
            }
        }

        return entity
    }

    private fun shouldIgnoreWall(): Boolean {
        val module = CombatManager.getTopModule(true)
        return if (module is Aura || module is AimBot) ignoreWalls.value else true
    }

}