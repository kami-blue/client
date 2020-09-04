package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.manager.mangers.CombatManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.CombatUtils
import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import me.zeroeightsix.kami.util.math.RotationUtils
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
    private val filter = register(Settings.enumBuilder(TargetFilter::class.java).withName("Filter").withValue(TargetFilter.ALL).build())
    private val fov = register(Settings.floatBuilder("FOV").withValue(90f).withRange(0f, 180f).withVisibility { filter.value == TargetFilter.FOV })
    private val priority = register(Settings.enumBuilder(TargetPriority::class.java).withName("Priority").withValue(TargetPriority.DISTANCE).build())
    private val players = register(Settings.b("Players", true))
    private val friends = register(Settings.booleanBuilder("Friends").withValue(false).withVisibility { players.value }.build())
    private val teammates = register(Settings.booleanBuilder("Teammates").withValue(false).withVisibility { players.value }.build())
    private val sleeping = register(Settings.booleanBuilder("Sleeping").withValue(false).withVisibility { players.value }.build())
    private val mobs = register(Settings.b("Mobs", false))
    private val passive = register(Settings.booleanBuilder("PassiveMobs").withValue(false).withVisibility { mobs.value }.build())
    private val neutral = register(Settings.booleanBuilder("NeutralMobs").withValue(false).withVisibility { mobs.value }.build())
    private val hostile = register(Settings.booleanBuilder("HostileMobs").withValue(false).withVisibility { mobs.value }.build())
    private val invisible = register(Settings.b("Invisible", false))
    private val ignoreWalls = register(Settings.booleanBuilder("IgnoreWalls").withValue(false).build())
    private val range = register(Settings.floatBuilder("TargetRange").withValue(16.0f).withRange(2.0f, 64.0f).build())

    private enum class TargetFilter {
        ALL, FOV, MANUAL
    }

    private enum class TargetPriority {
        DAMAGE, HEALTH, CROSS_HAIR, DISTANCE
    }

    private var overrideRange = range.value

    override fun onDisable() {
        enable()
    }

    override fun onUpdate() {
        if (isDisabled) enable()
        updateRange()
        val targetList = getTargetList()
        CombatManager.targetList = targetList
        CombatManager.target = getTarget(targetList)
    }

    private fun updateRange() {
        val topModule = CombatManager.getTopModule()
        overrideRange = if (topModule is Aura) topModule.getRange() else range.value
    }

    private fun getTargetList(): ArrayList<EntityLivingBase> {
        val player = arrayOf(players.value, friends.value, sleeping.value)
        val mob = arrayOf(mobs.value, passive.value, neutral.value, hostile.value)
        val targetList = EntityUtils.getTargetList(player, mob, invisible.value, overrideRange)
        if ((targetList.isEmpty() || getTarget(targetList) == null) && overrideRange != range.value) {
            targetList.addAll(EntityUtils.getTargetList(player, mob, invisible.value, range.value))
        }
        if (!shouldIgnoreWall()) targetList.removeIf {
            !mc.player.canEntityBeSeen(it) && EntityUtils.canEntityFeetBeSeen(it) && EntityUtils.canEntityHitboxBeSeen(it) == null
        }
        if (!teammates.value) targetList.removeIf {
            mc.player.isOnSameTeam(it)
        }
        return targetList
    }

    private fun shouldIgnoreWall(): Boolean {
        val module = CombatManager.getTopModule()
        return if (module is Aura || module is AimBot) ignoreWalls.value
        else true
    }

    private fun getTarget(listIn: ArrayList<EntityLivingBase>): EntityLivingBase? {
        val copiedList = ArrayList(listIn)
        return filterTargetList(copiedList) ?: CombatManager.target?.let { entity ->
            if (!entity.isDead && listIn.contains(entity)) entity else null
        }
    }

    private fun filterTargetList(listIn: ArrayList<EntityLivingBase>): EntityLivingBase? {
        if (listIn.isEmpty()) return null
        return filterByPriority(filterByFilter(listIn))
    }

    private fun filterByFilter(listIn: ArrayList<EntityLivingBase>): ArrayList<EntityLivingBase> {
        when (filter.value) {
            TargetFilter.FOV -> {
                listIn.removeIf { RotationUtils.getRelativeRotation(it) > fov.value }
            }

            TargetFilter.MANUAL -> {
                if (!mc.gameSettings.keyBindAttack.isKeyDown && !mc.gameSettings.keyBindUseItem.isKeyDown) {
                    return ArrayList()
                }
                val eyePos = mc.player.getPositionEyes(KamiTessellator.pTicks())
                val lookVec = mc.player.lookVec.scale(range.value.toDouble())
                val sightEndPos = eyePos.add(lookVec)
                listIn.removeIf { it.boundingBox.calculateIntercept(eyePos, sightEndPos) == null }
            }
        }
        return listIn
    }

    private fun filterByPriority(listIn: ArrayList<EntityLivingBase>): EntityLivingBase? {
        if (listIn.isEmpty()) return null

        if (priority.value == TargetPriority.DAMAGE) filterByDamage(listIn)

        if (priority.value == TargetPriority.HEALTH) filterByHealth(listIn)

        return if (priority.value == TargetPriority.CROSS_HAIR) filterByCrossHair(listIn) else filterByDistance(listIn)
    }

    private fun filterByDamage(listIn: ArrayList<EntityLivingBase>) {
        if (listIn.isEmpty()) return
        var damage = Float.MIN_VALUE
        val toKeep = HashSet<Entity>()
        for (entity in listIn) {
            val currentDamage = CombatUtils.calcDamage(entity, true)
            if (currentDamage >= damage) {
                if (currentDamage > damage) {
                    damage = currentDamage
                    toKeep.clear()
                }
                toKeep.add(entity)
            }
        }
        listIn.removeIf { !toKeep.contains(it) }
    }

    private fun filterByHealth(listIn: ArrayList<EntityLivingBase>) {
        if (listIn.isEmpty()) return
        var health = Float.MAX_VALUE
        val toKeep = HashSet<Entity>()
        for (e in listIn) {
            val currentHealth = e.health
            if (currentHealth <= health) {
                if (currentHealth < health) {
                    health = currentHealth
                    toKeep.clear()
                }
                toKeep.add(e)
            }
        }
        listIn.removeIf { !toKeep.contains(it) }
    }

    private fun filterByCrossHair(listIn: ArrayList<EntityLivingBase>): EntityLivingBase? {
        if (listIn.isEmpty()) return null
        return listIn.sortedBy { RotationUtils.getRelativeRotation(it) }[0]
    }

    private fun filterByDistance(listIn: ArrayList<EntityLivingBase>): EntityLivingBase? {
        if (listIn.isEmpty()) return null
        return listIn.sortedBy { it.getDistance(mc.player) }[0]
    }
}