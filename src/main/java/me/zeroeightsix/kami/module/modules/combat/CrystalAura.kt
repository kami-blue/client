package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.KamiEvent
import me.zeroeightsix.kami.event.events.OnUpdateWalkingPlayerEvent
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
import me.zeroeightsix.kami.util.math.Vec2f
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Items
import net.minecraft.init.MobEffects
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

@Module.Info(
        name = "CrystalAura",
        description = "Places End Crystals to kill enemies",
        category = Module.Category.COMBAT,
        modulePriority = 80
)
object CrystalAura : Module() {
    /* Settings */
    private val page = register(Settings.e<Page>("Page", Page.GENERAL))

    /* General */
    private val facePlaceThreshold = register(Settings.floatBuilder("FacePlace").withValue(5.0f).withRange(0.0f, 20.0f).withVisibility { page.value == Page.GENERAL })
    private val noSuicideThreshold = register(Settings.floatBuilder("NoSuicide").withValue(5.0f).withRange(0.0f, 20.0f).withVisibility { page.value == Page.GENERAL })
    private val maxYawRate = register(Settings.integerBuilder("MaxYawRate").withValue(50).withRange(10, 100).withVisibility { page.value == Page.GENERAL })
    private val motionPrediction = register(Settings.booleanBuilder("MotionPrediction").withValue(true).withVisibility { page.value == Page.GENERAL })
    private val pingSync = register(Settings.booleanBuilder("PingSync").withValue(true).withVisibility { page.value == Page.GENERAL && motionPrediction.value })
    private val ticksAhead = register(Settings.integerBuilder("TicksAhead").withValue(5).withRange(0, 20).withVisibility { page.value == Page.GENERAL && motionPrediction.value && !pingSync.value })

    /* Place page one */
    private val doPlace = register(Settings.booleanBuilder("Place").withValue(true).withVisibility { page.value == Page.PLACE_ONE })
    private val forcePlace = register(Settings.booleanBuilder("RightClickForcePlace").withValue(false).withVisibility { page.value == Page.PLACE_ONE })
    private val autoSwap = register(Settings.booleanBuilder("AutoSwap").withValue(true).withVisibility { page.value == Page.PLACE_ONE })
    private val spoofHotbar = register(Settings.booleanBuilder("SpoofHotbar").withValue(true).withVisibility { page.value == Page.PLACE_ONE && autoSwap.value })
    private val placeSwing = register(Settings.booleanBuilder("PlaceSwing").withValue(false).withVisibility { page.value == Page.PLACE_ONE })

    /* Place page two */
    private val minDamageP = register(Settings.integerBuilder("MinDamagePlace").withValue(4).withRange(0, 20).withVisibility { page.value == Page.PLACE_TWO })
    private val maxSelfDamageP = register(Settings.integerBuilder("MaxSelfDamagePlace").withValue(4).withRange(0, 20).withVisibility { page.value == Page.PLACE_TWO })
    private val placeOffset = register(Settings.floatBuilder("PlaceOffset").withValue(1.0f).withRange(0f, 1f).withStep(0.05f).withVisibility { page.value == Page.PLACE_TWO })
    private val maxCrystal = register(Settings.integerBuilder("MaxCrystal").withValue(2).withRange(1, 5).withVisibility { page.value == Page.PLACE_TWO })
    private val placeDelay = register(Settings.integerBuilder("PlaceDelay").withValue(1).withRange(1, 10).withVisibility { page.value == Page.PLACE_TWO })
    private val placeRange = register(Settings.floatBuilder("PlaceRange").withValue(4.0f).withRange(0.0f, 5.0f).withVisibility { page.value == Page.PLACE_TWO })
    private val wallPlaceRange = register(Settings.floatBuilder("WallPlaceRange").withValue(2.0f).withRange(0.0f, 5.0f).withVisibility { page.value == Page.PLACE_TWO })

    /* Explode page one */
    private val doExplode = register(Settings.booleanBuilder("Explode").withValue(true).withVisibility { page.value == Page.EXPLODE_ONE })
    private val forceExplode = register(Settings.booleanBuilder("LeftClickForceExplode").withValue(false).withVisibility { page.value == Page.EXPLODE_ONE })
    private val autoForceExplode = register(Settings.booleanBuilder("AutoForceExplode").withValue(true).withVisibility { page.value == Page.EXPLODE_ONE })
    private val antiWeakness = register(Settings.booleanBuilder("AntiWeakness").withValue(true).withVisibility { page.value == Page.EXPLODE_ONE })

    /* Explode page two */
    private val checkDamage = register(Settings.booleanBuilder("CheckDamage").withValue(true).withVisibility { page.value == Page.EXPLODE_TWO })
    private val minDamageE = register(Settings.integerBuilder("MinDamageExplode").withValue(8).withRange(0, 20).withVisibility { page.value == Page.EXPLODE_TWO && checkDamage.value })
    private val maxSelfDamageE = register(Settings.integerBuilder("MaxSelfDamageExplode").withValue(6).withRange(0, 20).withVisibility { page.value == Page.EXPLODE_TWO && checkDamage.value })
    private val hitDelay = register(Settings.integerBuilder("HitDelay").withValue(2).withRange(1, 10).withVisibility { page.value == Page.EXPLODE_TWO })
    private val hitAttempts = register(Settings.integerBuilder("HitAttempts").withValue(2).withRange(0, 5).withVisibility { page.value == Page.EXPLODE_TWO })
    private val explodeRange = register(Settings.floatBuilder("ExplodeRange").withValue(4.0f).withRange(0.0f, 5.0f).withVisibility { page.value == Page.EXPLODE_TWO })
    private val wallExplodeRange = register(Settings.floatBuilder("WallExplodeRange").withValue(2.0f).withRange(0.0f, 5.0f).withVisibility { page.value == Page.EXPLODE_TWO })
    /* End of settings */

    private enum class Page {
        GENERAL, PLACE_ONE, PLACE_TWO, EXPLODE_ONE, EXPLODE_TWO
    }

    /* Variables */
    private val placeMap = TreeMap<Float, BlockPos>(Comparator.reverseOrder())
    private val crystalList = HashSet<EntityEnderCrystal>()
    private val ignoredList = HashSet<EntityEnderCrystal>()
    private var lastCrystal: EntityEnderCrystal? = null
    private var lastLookAt = Vec3d.ZERO
    private var targetPosition = Vec3d.ZERO
    private var placeTimer = 0
    private var hitTimer = 0
    private var hitCount = 0
    private var inactiveTicks = 30

    override fun isActive(): Boolean {
        return isEnabled && InventoryUtils.countItemAll(426) > 0 && inactiveTicks <= 20
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
        lastLookAt = Vec3d.ZERO
        targetPosition = Vec3d.ZERO
        placeTimer = 0
        hitTimer = 0
        hitCount = 0
        inactiveTicks = 30
        PlayerPacketManager.resetHotbar()
    }

    // Minecraft sends sounds packets a tick before removing the crystal lol
    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (!CombatManager.isOnTopPriority(this) || mc.player == null || event.packet !is SPacketSoundEffect) return@EventHook
        if (event.packet.getCategory() == SoundCategory.BLOCKS && event.packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
            val crystalList = CrystalUtils.getCrystalList(Vec3d(event.packet.x, event.packet.y, event.packet.z), 5f)
            for (crystal in crystalList) {
                crystal.setDead()
                mc.world.removeEntityFromWorld(crystal.entityId)
            }
            ignoredList.clear()
            hitCount = 0
        }
    })

    @EventHandler
    private val onUpdateWalkingPlayerListener = Listener(EventHook { event: OnUpdateWalkingPlayerEvent ->
        if (inactiveTicks > 20 || event.era != KamiEvent.Era.PRE || lastLookAt == Vec3d.ZERO || CombatSetting.pause) return@EventHook
        val packet = PlayerPacketManager.PlayerPacket(rotating = true, rotation = Vec2f(getLastRotation()))
        PlayerPacketManager.addPacket(this, packet)
    })

    override fun onUpdate() {
        if (!CombatManager.isOnTopPriority(this) || CombatSetting.pause) return
        inactiveTicks++
        hitTimer++
        placeTimer++

        updateMap()
        if (canExplode()) explode() else if (canPlace()) place()
        if (inactiveTicks > 5 || getHand() == EnumHand.OFF_HAND) PlayerPacketManager.resetHotbar()
        if (inactiveTicks > 20) resetRotation()
    }

    private fun updateMap() {
        setPosition()
        placeMap.clear()
        placeMap.putAll(CrystalUtils.getPlacePos(CombatManager.target, mc.player, placeRange.value + 1f))
        resetPosition()

        crystalList.clear()
        crystalList.addAll(CrystalUtils.getCrystalList(max(placeRange.value, explodeRange.value)))

        if (inactiveTicks > 10 && getExplodingCrystal() == null && ignoredList.isNotEmpty()) {
            ignoredList.clear()
            hitCount = 0
        }
    }

    private fun place() {
        if (autoSwap.value && getHand() == null) {
            InventoryUtils.getSlotsHotbar(426)?.get(0)?.let {
                if (spoofHotbar.value) PlayerPacketManager.spoofHotbar(it)
                else InventoryUtils.swapSlot(it)
            }
        }
        getPlacingPos()?.let { pos ->
            getHand()?.let { hand ->
                placeTimer = 0
                inactiveTicks = 0
                lastLookAt = Vec3d(pos).add(0.5, placeOffset.value.toDouble(), 0.5)
                mc.player.connection.sendPacket(CPacketPlayerTryUseItemOnBlock(pos, BlockUtils.getHitSide(pos), hand, 0.5f, placeOffset.value, 0.5f))
                if (placeSwing.value) mc.player.swingArm(hand)
            }
        }
    }

    private fun explode() {
        if (antiWeakness.value && mc.player.isPotionActive(MobEffects.WEAKNESS) && !isHoldingTool()) {
            CombatUtils.equipBestWeapon()
            PlayerPacketManager.resetHotbar()
        }
        getExplodingCrystal()?.let {
            hitTimer = 0
            inactiveTicks = 0
            lastLookAt = it.positionVector

            if (hitAttempts.value != 0 && it == lastCrystal) {
                hitCount++
                if (hitCount >= hitAttempts.value) ignoredList.add(it)
            } else {
                hitCount = 0
            }
            mc.connection!!.sendPacket(CPacketUseEntity(it))
            mc.player.swingArm(getHand() ?: EnumHand.OFF_HAND)
            mc.player.setLastAttackedEntity(CombatManager.target!!)
            lastCrystal = it
        }
    }
    /* End of main functions */

    /* Placing */
    private fun canPlace(): Boolean {
        return doPlace.value
                && placeTimer >= placeDelay.value
                && InventoryUtils.countItemAll(426) > 0
                && getPlacingPos() != null
                && countValidCrystal() < maxCrystal.value
    }

    @Suppress("UnconditionalJumpStatementInLoop") // The linter is wrong here, it will continue until it's supposed to return
    private fun getPlacingPos(): BlockPos? {
        if (placeMap.isEmpty()) return null
        for ((damage, pos) in placeMap) {
            val hitVec = Vec3d(pos).add(0.5, 1.0, 0.5)
            val dist = mc.player.getDistance(hitVec.x, hitVec.y, hitVec.z)
            if (dist > placeRange.value) continue
            if (!CrystalUtils.canPlaceCollide(pos)) continue
            if (BlockUtils.rayTraceTo(pos) == null && dist > wallPlaceRange.value) continue
            if (!shouldForcePlace()) {
                val rotation = RotationUtils.getRotationTo(hitVec, true)
                if (abs(rotation.x - getLastRotation().x) > maxYawRate.value + (inactiveTicks * 10f)) continue
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
        return doExplode.value
                && hitTimer >= hitDelay.value
                && getExplodingCrystal() != null
                && CombatManager.target?.let { target ->
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
                    && abs(RotationUtils.getRotationToEntity(it).x - getLastRotation().x) <= maxYawRate.value + (inactiveTicks * 10f)
        } ?: crystalList.firstOrNull {
            !ignoredList.contains(it)
                    && EntityUtils.canEntityHitboxBeSeen(it) != null
                    && it.getDistance(mc.player) < wallExplodeRange.value
        }
    }

    private fun shouldForceExplode(): Boolean {
        return (autoForceExplode.value && placeMap.isNotEmpty() && placeMap.firstKey() > minDamageE.value)
                || (forceExplode.value && mc.gameSettings.keyBindAttack.isKeyDown
                && mc.player.heldItemMainhand.getItem() != Items.DIAMOND_PICKAXE
                && mc.player.heldItemMainhand.getItem() != Items.GOLDEN_APPLE)
    }

    private fun checkDamageExplode(damage: Float, selfDamage: Float): Boolean {
        return (damage >= minDamageE.value || shouldFacePlace()) && (selfDamage <= maxSelfDamageE.value)
    }
    /* End of exploding */

    /* General */
    private fun getHand(): EnumHand? {
        val serverSideItem = if (spoofHotbar.value) mc.player.inventory.getStackInSlot(PlayerPacketManager.serverSideHotbar).getItem() else null
        return when (Items.END_CRYSTAL) {
            mc.player.heldItemOffhand.getItem() -> EnumHand.OFF_HAND
            mc.player.heldItemMainhand.getItem() -> EnumHand.MAIN_HAND
            serverSideItem -> EnumHand.MAIN_HAND
            else -> null
        }
    }

    private fun noSuicideCheck(selfDamage: Float) = mc.player.health - selfDamage > noSuicideThreshold.value

    private fun isHoldingTool(): Boolean {
        val item = mc.player.heldItemMainhand.getItem()
        return item is ItemTool || item is ItemSword
    }

    private fun shouldFacePlace() = facePlaceThreshold.value > 0f && CombatManager.target?.let { it.health <= facePlaceThreshold.value } ?: false

    private fun countValidCrystal(): Int {
        var count = 0
        CombatManager.target?.let { target ->
            setPosition()
            for (crystal in crystalList) {
                if (ignoredList.contains(crystal)) continue
                if (crystal.getDistance(mc.player) > placeRange.value) continue
                val rotation = RotationUtils.getRotationToEntity(crystal)
                if (abs(rotation.x - getLastRotation().x) > maxYawRate.value) continue
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
        if (targetPosition == Vec3d.ZERO) return
        CombatManager.target?.setPosition(targetPosition.x, targetPosition.y, targetPosition.z)
    }
    /* End of Motion prediction */

    /* Rotation spoofing */
    private fun getLastRotation() = RotationUtils.getRotationTo(lastLookAt, true)

    private fun resetRotation() {
        lastLookAt = CombatManager.target?.positionVector ?: Vec3d.ZERO
    }
    /* End of rotation spoofing */
}