package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.manager.mangers.CombatManager
import me.zeroeightsix.kami.manager.mangers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BlockUtils
import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.combat.CombatUtils
import me.zeroeightsix.kami.util.combat.CrystalUtils
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.math.Vec2d
import me.zeroeightsix.kami.util.math.Vec2f
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Items
import net.minecraft.init.MobEffects
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Created by Xiaro on 19/07/20
 */
// TODO: AutoOffhand
// TODO: HoleBreaker
// TODO: HoleTP
@Module.Info(
        name = "CrystalAuraRewrite",
        description = "A reborn of the CrystalAura",
        category = Module.Category.COMBAT,
        modulePriority = 80
)
class CrystalAuraRewrite : Module() {
    /* Settings */
    private val page = register(Settings.e<Page>("Page", Page.GENERAL))

    /* General */
    private val facePlaceThreshold = register(Settings.floatBuilder("FacePlace").withValue(5.0f).withRange(0.0f, 20.0f).withVisibility { page.value == Page.GENERAL }.build())
    private val noSuicideThreshold = register(Settings.floatBuilder("NoSuicide").withValue(5.0f).withRange(0.0f, 20.0f).withVisibility { page.value == Page.GENERAL }.build())
    private val maxYawRate = register(Settings.integerBuilder("MaxYawRate").withValue(50).withRange(10, 100).withVisibility { page.value == Page.GENERAL }.build())
    private val motionPrediction = register(Settings.booleanBuilder("MotionPrediction").withValue(true).withVisibility { page.value == Page.GENERAL }.build())
    private val pingSync = register(Settings.booleanBuilder("PingSync").withValue(true).withVisibility { page.value == Page.GENERAL && motionPrediction.value }.build())
    private val ticksAhead = register(Settings.integerBuilder("TicksAhead").withValue(5).withRange(0, 20).withVisibility { page.value == Page.GENERAL && motionPrediction.value && !pingSync.value }.build())

    /* Place page one */
    private val doPlace = register(Settings.booleanBuilder("Place").withValue(true).withVisibility { page.value == Page.PLACE_ONE }.build())
    private val forcePlace = register(Settings.booleanBuilder("RightClickForcePlace").withValue(false).withVisibility { page.value == Page.PLACE_ONE }.build())
    private val autoSwap = register(Settings.booleanBuilder("AutoSwap").withValue(true).withVisibility { page.value == Page.PLACE_ONE }.build())
    private val placeSwing = register(Settings.booleanBuilder("PlaceSwing").withValue(false).withVisibility { page.value == Page.PLACE_ONE }.build())

    /* Place page two */
    private val minDamageP = register(Settings.integerBuilder("MinDamagePlace").withValue(4).withRange(0, 20).withVisibility { page.value == Page.PLACE_TWO }.build())
    private val maxSelfDamageP = register(Settings.integerBuilder("MaxSelfDamagePlace").withValue(8).withRange(0, 20).withVisibility { page.value == Page.PLACE_TWO }.build())
    private val maxCrystal = register(Settings.integerBuilder("MaxCrystal").withValue(2).withRange(1, 5).withVisibility { page.value == Page.PLACE_TWO }.build())
    private val placeRange = register(Settings.floatBuilder("PlaceRange").withValue(5.0f).withRange(0.0f, 10.0f).withVisibility { page.value == Page.PLACE_TWO }.build())
    private val wallPlaceRange = register(Settings.floatBuilder("WallPlaceRange").withValue(2.5f).withRange(0.0f, 10.0f).withVisibility { page.value == Page.PLACE_TWO }.build())

    /* Explode page one */
    private val doExplode = register(Settings.booleanBuilder("Explode").withValue(true).withVisibility { page.value == Page.EXPLODE_ONE }.build())
    private val forceExplode = register(Settings.booleanBuilder("LeftClickForceExplode").withValue(false).withVisibility { page.value == Page.EXPLODE_ONE }.build())
    private val autoForceExplode = register(Settings.booleanBuilder("AutoForceExplode").withValue(true).withVisibility { page.value == Page.EXPLODE_ONE }.build())
    private val antiWeakness = register(Settings.booleanBuilder("AntiWeakness").withValue(true).withVisibility { page.value == Page.EXPLODE_ONE }.build())
    private val checkImmune = register(Settings.booleanBuilder("CheckImmune").withValue(false).withVisibility { page.value == Page.EXPLODE_ONE }.build())

    /* Explode page two */
    private val checkDamage = register(Settings.booleanBuilder("CheckDamage").withValue(true).withVisibility { page.value == Page.EXPLODE_TWO }.build())
    private val minDamageE = register(Settings.integerBuilder("MinDamageExplode").withValue(8).withRange(0, 20).withVisibility { page.value == Page.EXPLODE_TWO && checkDamage.value }.build())
    private val maxSelfDamageE = register(Settings.integerBuilder("MaxSelfDamageExplode").withValue(6).withRange(0, 20).withVisibility { page.value == Page.EXPLODE_TWO && checkDamage.value }.build())
    private val hitDelay = register(Settings.integerBuilder("HitDelay").withValue(1).withRange(1, 10).withVisibility { page.value == Page.EXPLODE_TWO }.build())
    private val hitAttempts = register(Settings.integerBuilder("HitAttempts").withValue(2).withRange(0, 5).withVisibility { page.value == Page.EXPLODE_TWO }.build())
    private val explodeRange = register(Settings.floatBuilder("ExplodeRange").withValue(5.0f).withRange(0.0f, 10.0f).withVisibility { page.value == Page.EXPLODE_TWO }.build())
    private val wallExplodeRange = register(Settings.floatBuilder("WallExplodeRange").withValue(2.5f).withRange(0.0f, 10.0f).withVisibility { page.value == Page.EXPLODE_TWO }.build())
    /* End of settings */

    private enum class Page {
        GENERAL, PLACE_ONE, PLACE_TWO, EXPLODE_ONE, EXPLODE_TWO
    }

    private enum class State {
        NONE, PLACE, EXPLODE
    }

    /* Variables */
    private val placeMap = TreeMap<Float, BlockPos>(Comparator.reverseOrder())
    private val crystalList = ArrayList<EntityEnderCrystal>()
    private val ignoredList = HashSet<EntityEnderCrystal>()
    private var lastCrystal: EntityEnderCrystal? = null
    private var state = State.NONE
    private var hitCount = 0
    private var hitTimer = 0
    private var lastRotation = Vec2d(0.0, 0.0)
    private var inactiveTicks = 0
    private var targetPosition = Vec3d(0.0, -999.0, 0.0)

    override fun isActive(): Boolean {
        return isEnabled && inactiveTicks <= 10
    }

    override fun onEnable() {
        if (mc.player == null) disable()
        else resetRotation()
    }

    override fun onDisable() {
        placeMap.clear()
        crystalList.clear()
        ignoredList.clear()
        lastCrystal = null
        state = State.NONE
        hitCount = 0
        hitTimer = 0
        inactiveTicks = 11
        targetPosition = Vec3d(0.0, -999.0, 0.0)
    }

    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (!CombatManager.isOnTopPriority(this) || mc.player == null || event.packet !is SPacketSoundEffect) return@EventHook
        if (event.packet.getCategory() == SoundCategory.BLOCKS && event.packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
            val crystalList = CrystalUtils.getCrystalList(Vec3d(event.packet.x, event.packet.y, event.packet.z), 5f)
            for (crystal in crystalList) {
                crystal.setDead()
            }
            ignoredList.clear()
            hitCount = 0
        }
    })

    // CA is very order sensitive for some reasons so we have to make sure that we spoof the rotations before placing or exploding
    @EventHandler
    private val postSendListener = Listener(EventHook { event: PacketEvent.PostSend ->
        if (!CombatManager.isOnTopPriority(this) || event.packet !is CPacketPlayer) return@EventHook
        if (state == State.PLACE) place()
        else if (state == State.EXPLODE) explode()
    })

    override fun onUpdate() {
        if (!CombatManager.isOnTopPriority(this)) return
        inactiveTicks++
        updateMap()

        if (hitTimer > hitDelay.value) {
            if (canExplode()) preExplode()
            else if (canPlace()) prePlace()
        } else {
            hitTimer++
            if (canPlace()) prePlace()
        }

        if (inactiveTicks > 10) resetRotation()
        else sendRotation()
    }

    private fun updateMap() {
        setPosition()
        placeMap.clear()
        placeMap.putAll(CrystalUtils.getPlacePos(CombatManager.target, mc.player, placeRange.value))
        resetPosition()

        crystalList.clear()
        crystalList.addAll(CrystalUtils.getCrystalList(max(placeRange.value, explodeRange.value)))

        if (getExplodingCrystal() == null && ignoredList.isNotEmpty()) {
            ignoredList.clear()
            hitCount = 0
        }
    }

    private fun prePlace() {
        if (autoSwap.value && getCrystalHand() == null) InventoryUtils.swapSlotToItem(426)
        getPlacingPos()?.let {
            state = State.PLACE
            inactiveTicks = 0
            sendRotation(RotationUtils.getRotationTo(Vec3d(it).add(0.5, 1.0, 0.5), true))
        }
    }

    private fun place() {
        getPlacingPos()?.let { pos ->
            getCrystalHand()?.let { hand ->
                state = State.NONE
                mc.player.connection.sendPacket(CPacketPlayerTryUseItemOnBlock(pos, BlockUtils.getHitSide(pos), hand, 0.5f, 1f, 0.5f))
                if (placeSwing.value) mc.player.swingArm(hand)
            }
        }
    }

    private fun preExplode() {
        if (antiWeakness.value && mc.player.isPotionActive(MobEffects.WEAKNESS) && !isHoldingTool()) CombatUtils.equipBestWeapon()
        getExplodingCrystal()?.let {
            state = State.EXPLODE
            hitTimer = 0
            inactiveTicks = 0
            sendRotation(getExplodingRotation(it))

            if (hitAttempts.value != 0) {
                if (it == lastCrystal) {
                    hitCount++
                    if (hitCount >= hitAttempts.value) {
                        ignoredList.add(it)
                        hitCount = 0
                    }
                }
            } else {
                lastCrystal = it
                hitCount = 0
            }
        }
    }

    private fun explode() {
        getExplodingCrystal()?.let {
            state = State.NONE
            mc.playerController.attackEntity(mc.player, it)
            mc.player.swingArm(getCrystalHand() ?: EnumHand.MAIN_HAND)
            mc.player.setLastAttackedEntity(CombatManager.target!!)
        }
    }
    /* End of main functions */

    /* Placing */
    private fun canPlace(): Boolean {
        return doPlace.value
                && getPlacingPos() != null
                && countValidCrystal() < maxCrystal.value
    }

    private fun getCrystalHand(): EnumHand? {
        return when (Items.END_CRYSTAL) {
            mc.player.heldItemMainhand.getItem() -> EnumHand.MAIN_HAND
            mc.player.heldItemOffhand.getItem() -> EnumHand.OFF_HAND
            else -> null
        }
    }

    private fun getPlacingPos(): BlockPos? {
        if (placeMap.isEmpty()) return null
        for ((damage, pos) in placeMap) {
            val dist = sqrt(mc.player.getDistanceSq(pos))
            if (dist > placeRange.value) continue
            if (!CrystalUtils.canPlaceCollide(pos)) continue
            if (BlockUtils.rayTraceTo(pos) == null && dist > wallPlaceRange.value) continue
            if (!shouldForcePlace()) {
                val rotation = RotationUtils.getRotationTo(Vec3d(pos).add(0.5, 1.0, 0.5), true)
                if (abs(rotation.x - lastRotation.x) > maxYawRate.value) continue
                val selfDamage = CrystalUtils.calcDamage(pos, mc.player)
                if (!noSuicideCheck(selfDamage)) continue
                if (!checkDamagePlace(damage, selfDamage)) continue
            }
            return pos
        }
        return null
    }

    private fun shouldForcePlace(): Boolean {
        return forcePlace.value && mc.gameSettings.keyBindUseItem.isKeyDown
                && mc.player.heldItemMainhand.getItem() == Items.END_CRYSTAL
    }

    /**
     * @return True if passed placing damage check
     */
    private fun checkDamagePlace(damage: Float, selfDamage: Float): Boolean {
        return (damage >= minDamageP.value || shouldFacePlace()) && (selfDamage <= maxSelfDamageP.value)
    }
    /* End of placing */

    /* Exploding */
    private fun canExplode(): Boolean {
        return doExplode.value && getExplodingCrystal() != null && CombatManager.target?.let { target ->
            if (checkImmune.value && target.isInvulnerable) return false
            if (checkDamage.value && !shouldForceExplode()) {
                var maxDamage = 0f
                var maxSelfDamage = 0f
                setPosition()
                for (crystal in crystalList) {
                    maxDamage = max(maxDamage, CrystalUtils.calcDamage(crystal, target))
                    maxSelfDamage = max(maxSelfDamage, CrystalUtils.calcDamage(crystal, mc.player))
                }
                resetPosition()
                if (!noSuicideCheck(maxSelfDamage)) return false
                if (!checkDamageExplode(maxDamage, maxSelfDamage)) return false
            }
            return true
        } ?: false
    }

    private fun getExplodingCrystal(): EntityEnderCrystal? {
        if (crystalList.isEmpty()) return null
        return crystalList.firstOrNull {
            !ignoredList.contains(it)
                    && (mc.player.canEntityBeSeen(it) || EntityUtils.canEntityFeetBeSeen(it))
                    && it.getDistance(mc.player) < explodeRange.value
                    && abs(getExplodingRotation(it).x - lastRotation.x) <= maxYawRate.value
        } ?: crystalList.firstOrNull {
            !ignoredList.contains(it)
                    && EntityUtils.canEntityHitboxBeSeen(it) != null
                    && it.getDistance(mc.player) < wallExplodeRange.value
        }
    }

    private fun getExplodingRotation(crystal: EntityEnderCrystal): Vec2d {
        val hitPos = if (mc.player.canEntityBeSeen(crystal)) {
            crystal.positionVector // If we can see the feet then we look at the feet pos
        } else if (EntityUtils.canEntityFeetBeSeen(crystal)) {
            crystal.getPositionEyes(1f) // If we can see the eyes then we look at the eye pos
        } else {
            EntityUtils.canEntityHitboxBeSeen(crystal) // If we can it any vertex of the hit box then we look at it
        } ?: crystal.positionVector  // If not then just look at the eye pos

        return RotationUtils.getRotationTo(hitPos, true)
    }

    private fun shouldForceExplode(): Boolean {
        return (autoForceExplode.value && placeMap.isNotEmpty() && placeMap.firstKey() > minDamageE.value)
                || (forceExplode.value && mc.gameSettings.keyBindAttack.isKeyDown
                && mc.player.heldItemMainhand.getItem() != Items.DIAMOND_PICKAXE
                && mc.player.heldItemMainhand.getItem() != Items.GOLDEN_APPLE)
    }

    /**
     * @return True if passed exploding damage check
     */
    private fun checkDamageExplode(damage: Float, selfDamage: Float): Boolean {
        return (damage >= minDamageE.value || shouldFacePlace()) && (selfDamage <= maxSelfDamageE.value)
    }
    /* End of exploding */

    /* General */
    private fun noSuicideCheck(selfDamage: Float): Boolean {
        return mc.player.health - selfDamage > noSuicideThreshold.value
    }

    private fun isHoldingTool(): Boolean {
        val item = mc.player.heldItemMainhand.getItem()
        return item is ItemTool || item is ItemSword
    }

    private fun shouldFacePlace(): Boolean {
        return facePlaceThreshold.value > 0f && CombatManager.target?.let {
            it.health <= facePlaceThreshold.value
        } ?: false
    }

    private fun countValidCrystal(): Int {
        var count = 0
        CombatManager.target?.let { target ->
            setPosition()
            for (crystal in crystalList) {
                if (ignoredList.contains(crystal)) continue
                if (crystal.getDistance(mc.player) > placeRange.value) continue
                if (abs(getExplodingRotation(crystal).x - lastRotation.x) > maxYawRate.value) continue
                val damage = CrystalUtils.calcDamage(crystal, target)
                val selfDamage = CrystalUtils.calcDamage(crystal, mc.player)
                if (!checkDamagePlace(damage, selfDamage)) continue
                count++
            }
            resetPosition()
        }
        return count
    }
    /* End of general */

    /* Motion prediction */
    private fun setPosition() {
        if (!motionPrediction.value) return
        val ticks = if (pingSync.value) ceil(InfoCalculator.ping() / 25f).toInt() else ticksAhead.value
        val posAhead = CombatManager.motionTracker.calcPositionAhead(ticks, true) ?: return
        CombatManager.target?.let {
            targetPosition = it.positionVector
            it.setPosition(posAhead.x, posAhead.y, posAhead.z)
        }
    }

    private fun resetPosition() {
        if (!motionPrediction.value) return
        if (targetPosition.y == -999.0) return
        CombatManager.target?.setPosition(targetPosition.x, targetPosition.y, targetPosition.z)
    }
    /* End of Motion prediction */

    /* Rotation spoofing */
    private fun sendRotation(rotation: Vec2d? = null) {
        if (rotation != null) lastRotation = rotation
        PlayerPacketManager.addPacket(this, PlayerPacketManager.PlayerPacket(rotating = true, rotation = Vec2f(lastRotation)))
    }

    private fun resetRotation() {
        lastRotation = Vec2d(RotationUtils.normalizeAngle(mc.player.rotationYaw.toDouble()), mc.player.rotationPitch.toDouble())
    }
    /* End of rotation spoofing */
}