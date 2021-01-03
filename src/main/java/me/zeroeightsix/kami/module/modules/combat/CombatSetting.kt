package me.zeroeightsix.kami.module.modules.combat

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.event.events.RenderOverlayEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.combat.CombatUtils
import me.zeroeightsix.kami.util.combat.CrystalUtils
import me.zeroeightsix.kami.util.graphics.*
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.math.Vec2d
import me.zeroeightsix.kami.util.math.VectorUtils.distanceTo
import me.zeroeightsix.kami.util.math.VectorUtils.toVec3dCenter
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.isActiveOrFalse
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.passive.AbstractHorse
import net.minecraft.entity.passive.EntityTameable
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemPickaxe
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.kamiblue.commons.extension.ceilToInt
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.GL11.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap

@Module.Info(
    name = "CombatSetting",
    description = "Settings for combat module targeting",
    category = Module.Category.COMBAT,
    showOnArray = Module.ShowOnArray.OFF,
    alwaysEnabled = true
)
object CombatSetting : Module() {
    private val page = register(Settings.e<Page>("Page", Page.TARGETING))

    /* Targeting */
    private val filter = register(Settings.enumBuilder(TargetFilter::class.java, "Filter").withValue(TargetFilter.ALL).withVisibility { page.value == Page.TARGETING })
    private val fov = register(Settings.floatBuilder("FOV").withValue(90f).withRange(0f, 180f).withVisibility { page.value == Page.TARGETING && filter.value == TargetFilter.FOV })
    private val priority = register(Settings.enumBuilder(TargetPriority::class.java, "Priority").withValue(TargetPriority.DISTANCE).withVisibility { page.value == Page.TARGETING })
    private val players = register(Settings.booleanBuilder("Players").withValue(true).withVisibility { page.value == Page.TARGETING })
    private val friends = register(Settings.booleanBuilder("Friends").withValue(false).withVisibility { page.value == Page.TARGETING && players.value })
    private val teammates = register(Settings.booleanBuilder("Teammates").withValue(false).withVisibility { page.value == Page.TARGETING && players.value })
    private val sleeping = register(Settings.booleanBuilder("Sleeping").withValue(false).withVisibility { page.value == Page.TARGETING && players.value })
    private val mobs = register(Settings.booleanBuilder("Mobs").withValue(true).withVisibility { page.value == Page.TARGETING })
    private val passive = register(Settings.booleanBuilder("PassiveMobs").withValue(false).withVisibility { page.value == Page.TARGETING && mobs.value })
    private val neutral = register(Settings.booleanBuilder("NeutralMobs").withValue(false).withVisibility { page.value == Page.TARGETING && mobs.value })
    private val hostile = register(Settings.booleanBuilder("HostileMobs").withValue(true).withVisibility { page.value == Page.TARGETING && mobs.value })
    private val tamed = register(Settings.booleanBuilder("TamedMobs").withValue(false).withVisibility { page.value == Page.TARGETING && mobs.value })
    private val invisible = register(Settings.booleanBuilder("Invisible").withValue(true).withVisibility { page.value == Page.TARGETING })
    private val ignoreWalls = register(Settings.booleanBuilder("IgnoreWalls").withValue(false).withVisibility { page.value == Page.TARGETING })
    private val range = register(Settings.floatBuilder("TargetRange").withValue(16.0f).withRange(2.0f, 64.0f).withStep(2.0f).withVisibility { page.value == Page.TARGETING })

    /* In Combat */
    private val pauseForDigging = register(Settings.booleanBuilder("PauseForDigging").withValue(true).withVisibility { page.value == Page.IN_COMBAT })
    private val pauseForEating = register(Settings.booleanBuilder("PauseForEating").withValue(true).withVisibility { page.value == Page.IN_COMBAT })
    private val ignoreOffhandEating = register(Settings.booleanBuilder("IgnoreOffhandEating").withValue(true).withVisibility { page.value == Page.IN_COMBAT && pauseForEating.value })
    private val pauseBaritone = register(Settings.booleanBuilder("PauseBaritone").withValue(true).withVisibility { page.value == Page.IN_COMBAT })
    private val resumeDelay = register(Settings.integerBuilder("ResumeDelay").withRange(1, 10).withValue(3).withVisibility { page.value == Page.IN_COMBAT && pauseBaritone.value })
    private val motionPrediction = register(Settings.booleanBuilder("MotionPrediction").withValue(true).withVisibility { page.value == Page.IN_COMBAT })
    private val pingSync = register(Settings.booleanBuilder("PingSync").withValue(true).withVisibility { page.value == Page.IN_COMBAT && motionPrediction.value })
    private val ticksAhead = register(Settings.integerBuilder("TicksAhead").withValue(5).withRange(0, 20).withVisibility { page.value == Page.IN_COMBAT && motionPrediction.value && !pingSync.value })

    /* Render */
    private val renderPredictedPos = register(Settings.booleanBuilder("RenderPredictedPosition").withValue(false).withVisibility { page.value == Page.RENDER })

    private enum class Page {
        TARGETING, IN_COMBAT, RENDER
    }

    private enum class TargetFilter {
        ALL, FOV, MANUAL
    }

    private enum class TargetPriority {
        DAMAGE, HEALTH, CROSS_HAIR, DISTANCE
    }

    private var overrideRange = range.value
    private var paused = false
    private val resumeTimer = TickTimer(TimeUnit.SECONDS)
    private val jobMap = hashMapOf<() -> Unit, Job?>(
        { updateTarget() } to null,
        { updatePlacingList() } to null,
        { updateCrystalList() } to null
    )

    val pause
        get() = mc.player.ticksExisted < 10
            || pauseForDigging.value && mc.player.heldItemMainhand.item is ItemPickaxe && mc.playerController.isHittingBlock
            || pauseForEating.value && mc.player.isHandActive && mc.player.activeItemStack.item is ItemFood && (mc.player.activeHand != EnumHand.OFF_HAND || !ignoreOffhandEating.value)


    override fun isActive() = KillAura.isActive() || BedAura.isActive() || CrystalAura.isActive() || Surround.isActive()

    init {
        listener<RenderOverlayEvent> {
            if (!renderPredictedPos.value) return@listener
            CombatManager.target?.let {
                val ticks = if (pingSync.value) (InfoCalculator.ping() / 25f).ceilToInt() else ticksAhead.value
                val posCurrent = EntityUtils.getInterpolatedPos(it, KamiTessellator.pTicks())
                val posAhead = CombatManager.motionTracker.calcPositionAhead(ticks, true) ?: return@listener
                val posAheadEye = posAhead.add(0.0, it.eyeHeight.toDouble(), 0.0)
                val posCurrentScreen = Vec2d(ProjectionUtils.toScaledScreenPos(posCurrent))
                val posAheadScreen = Vec2d(ProjectionUtils.toScaledScreenPos(posAhead))
                val posAheadEyeScreen = Vec2d(ProjectionUtils.toScaledScreenPos(posAheadEye))
                val vertexHelper = VertexHelper(GlStateUtils.useVbo())
                val vertices = arrayOf(posCurrentScreen, posAheadScreen, posAheadEyeScreen)
                glDisable(GL_TEXTURE_2D)
                RenderUtils2D.drawLineStrip(vertexHelper, vertices, 2f, ColorHolder(80, 255, 80))
                glEnable(GL_TEXTURE_2D)
            }
        }

        listener<SafeTickEvent>(5000) {
            for ((function, future) in jobMap) {
                if (future.isActiveOrFalse) continue // Skip if the previous thread isn't done
                jobMap[function] = defaultScope.launch { function.invoke() }
            }

            if (pauseBaritone.value && !paused && isActive()) {
                BaritoneUtils.pause()
                paused = true
            } else if (paused && resumeTimer.tick(resumeDelay.value.toLong())) {
                BaritoneUtils.unpause()
                paused = false
            }
        }
    }

    private fun updateTarget() {
        with(CombatManager.getTopModule()) {
            overrideRange = if (this is KillAura) this.range.value else range.value
        }

        getTargetList().let {
            CombatManager.target = getTarget(it)
        }
    }

    private fun updatePlacingList() {
        if (CrystalAura.isDisabled && CrystalBasePlace.isDisabled && CrystalESP.isDisabled && mc.player.ticksExisted % 4 != 0) return

        val eyePos = mc.player?.getPositionEyes(1f) ?: Vec3d.ZERO
        val cacheList = ArrayList<Pair<BlockPos, Triple<Float, Float, Double>>>()
        val target = CombatManager.target
        val prediction = target?.let { getPrediction(it) }

        for (pos in CrystalUtils.getPlacePos(target, mc.player, 8f)) {
            val dist = eyePos.distanceTo(pos.toVec3dCenter(0.0, 0.5, 0.0))
            val damage = target?.let { CrystalUtils.calcDamage(pos, it, prediction?.first, prediction?.second) } ?: 0.0f
            val selfDamage = CrystalUtils.calcDamage(pos, mc.player)
            cacheList.add(Pair(pos, Triple(damage, selfDamage, dist)))
        }

        CombatManager.placeMap = linkedMapOf(*cacheList.sortedByDescending { triple -> triple.second.first }.toTypedArray())
    }

    /* Crystal damage calculation */
    private fun updateCrystalList() {
        if (CrystalAura.isDisabled && CrystalESP.isDisabled && mc.player.ticksExisted % 4 - 2 != 0) return

        val entityList = ArrayList(mc.world.loadedEntityList)
        val cacheList = ArrayList<Pair<EntityEnderCrystal, Triple<Float, Float, Double>>>()
        val cacheMap = LinkedHashMap<EntityEnderCrystal, Triple<Float, Float, Double>>()
        val eyePos = mc.player.getPositionEyes(1f)
        val target = CombatManager.target
        val prediction = target?.let { getPrediction(it) }

        for (entity in entityList) {
            if (entity.isDead) continue
            if (entity !is EntityEnderCrystal) continue
            val dist = entity.distanceTo(eyePos)
            if (dist > 16.0f) continue
            val damage = if (target != null && prediction != null) CrystalUtils.calcDamage(entity, target, prediction.first, prediction.second) else 0.0f
            val selfDamage = CrystalUtils.calcDamage(entity, mc.player)
            cacheList.add(entity to Triple(damage, selfDamage, dist))
        }

        cacheMap.putAll(cacheList.sortedByDescending { pair -> pair.second.first })
        CombatManager.crystalMap = cacheMap
    }

    fun getPrediction(entity: Entity) = CombatManager.target?.let {
        if (motionPrediction.value) {
            val ticks = if (pingSync.value) (InfoCalculator.ping() / 25f).ceilToInt() else ticksAhead.value
            CombatManager.motionTracker.getPositionAndBBAhead(ticks) ?: it.positionVector to it.entityBoundingBox
        } else {
            it.positionVector to it.entityBoundingBox
        }
    } ?: entity.positionVector to entity.entityBoundingBox
    /* End of crystal damage calculation */

    /* Targeting */
    private fun getTargetList(): LinkedList<EntityLivingBase> {
        val targetList = LinkedList<EntityLivingBase>()
        for (entity in getCacheList()) {
            if (AntiBot.isEnabled
                && AntiBot.botSet.contains((entity))) continue

            if (!tamed.value
                && (entity is EntityTameable && entity.isTamed
                    || entity is AbstractHorse && entity.isTame)) continue

            if (!teammates.value
                && mc.player.isOnSameTeam(entity)) continue

            if (!shouldIgnoreWall()
                && mc.player.canEntityBeSeen(entity)
                && !EntityUtils.canEntityFeetBeSeen(entity)
                && EntityUtils.canEntityHitboxBeSeen(entity) == null) continue

            targetList.add(entity)
        }

        return targetList
    }

    private fun getCacheList(): LinkedList<EntityLivingBase> {
        val player = arrayOf(players.value, friends.value, sleeping.value)
        val mob = arrayOf(mobs.value, passive.value, neutral.value, hostile.value)
        val cacheList = LinkedList(EntityUtils.getTargetList(player, mob, invisible.value, overrideRange))
        if ((cacheList.isEmpty() || getTarget(cacheList) == null) && overrideRange != range.value) {
            cacheList.addAll(EntityUtils.getTargetList(player, mob, invisible.value, range.value))
        }
        return cacheList
    }

    private fun shouldIgnoreWall(): Boolean {
        val module = CombatManager.getTopModule()
        return if (module is KillAura || module is AimBot) ignoreWalls.value
        else true
    }

    private fun getTarget(listIn: LinkedList<EntityLivingBase>): EntityLivingBase? {
        val copiedList = LinkedList(listIn)
        return filterTargetList(copiedList) ?: CombatManager.target?.let { entity ->
            if (!entity.isDead && listIn.contains(entity)) entity else null
        }
    }

    private fun filterTargetList(listIn: LinkedList<EntityLivingBase>): EntityLivingBase? {
        if (listIn.isEmpty()) return null
        return filterByPriority(filterByFilter(listIn))
    }

    private fun filterByFilter(listIn: LinkedList<EntityLivingBase>): LinkedList<EntityLivingBase> {
        when (filter.value) {
            TargetFilter.FOV -> {
                listIn.removeIf { RotationUtils.getRelativeRotation(it) > fov.value }
            }

            TargetFilter.MANUAL -> {
                if (!mc.gameSettings.keyBindAttack.isKeyDown && !mc.gameSettings.keyBindUseItem.isKeyDown) {
                    return LinkedList()
                }
                val eyePos = mc.player.getPositionEyes(KamiTessellator.pTicks())
                val lookVec = mc.player.lookVec.scale(range.value.toDouble())
                val sightEndPos = eyePos.add(lookVec)
                listIn.removeIf { it.entityBoundingBox.calculateIntercept(eyePos, sightEndPos) == null }
            }

            else -> {
            }
        }
        return listIn
    }

    private fun filterByPriority(listIn: LinkedList<EntityLivingBase>): EntityLivingBase? {
        if (listIn.isEmpty()) return null

        if (priority.value == TargetPriority.DAMAGE) filterByDamage(listIn)

        if (priority.value == TargetPriority.HEALTH) filterByHealth(listIn)

        return if (priority.value == TargetPriority.CROSS_HAIR) filterByCrossHair(listIn) else filterByDistance(listIn)
    }

    private fun filterByDamage(listIn: LinkedList<EntityLivingBase>) {
        if (listIn.isEmpty()) return
        var damage = Float.MIN_VALUE
        val toKeep = HashSet<Entity>()
        for (entity in listIn) {
            val currentDamage = CombatUtils.calcDamage(entity, roundDamage = true)
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

    private fun filterByHealth(listIn: LinkedList<EntityLivingBase>) {
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

    private fun filterByCrossHair(listIn: LinkedList<EntityLivingBase>): EntityLivingBase? {
        if (listIn.isEmpty()) return null
        return listIn.sortedBy { RotationUtils.getRelativeRotation(it) }[0]
    }

    private fun filterByDistance(listIn: LinkedList<EntityLivingBase>): EntityLivingBase? {
        if (listIn.isEmpty()) return null
        return listIn.sortedBy { it.getDistance(mc.player) }[0]
    }
    /* End of targeting */
}
