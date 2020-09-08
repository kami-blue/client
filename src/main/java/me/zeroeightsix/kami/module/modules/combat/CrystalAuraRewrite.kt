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
import me.zeroeightsix.kami.util.CombatUtils.CrystalUtils
import me.zeroeightsix.kami.util.CombatUtils.equipBestWeapon
import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.LagCompensator
import me.zeroeightsix.kami.util.math.RotationUtils
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
import kotlin.collections.HashSet
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Created by Xiaro on 19/07/20
 */
@Module.Info(
        name = "CrystalAuraRewrite",
        description = "A reborn of the CrystalAura",
        category = Module.Category.COMBAT,
        modulePriority = 80
)
// TODO: AutoOffhand
// TODO: HoleBreaker

class CrystalAuraRewrite : Module() {
    /* Settings */
    private val page = register(Settings.e<Page>("Page", Page.GENERAL))

    /* General */
    private val tpsSync = register(Settings.booleanBuilder("TpsSync").withValue(false).withVisibility { page.value == Page.GENERAL }.build())
    private val facePlaceThreshold = register(Settings.floatBuilder("FacePlace").withValue(5.0f).withRange(0.0f, 20.0f).withVisibility { page.value == Page.GENERAL }.build())
    private val noSuicideThreshold = register(Settings.floatBuilder("NoSuicide").withValue(5.0f).withRange(0.0f, 20.0f).withVisibility { page.value == Page.GENERAL }.build())

    /* Place page one */
    private val doPlace = register(Settings.booleanBuilder("Place").withValue(true).withVisibility { page.value == Page.PLACE_ONE }.build())
    private val forcePlace = register(Settings.booleanBuilder("RightClickForcePlace").withValue(false).withVisibility { page.value == Page.PLACE_ONE }.build())
    private val autoSwap = register(Settings.booleanBuilder("AutoSwap").withValue(true).withVisibility { page.value == Page.PLACE_ONE }.build())

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
    private val hitAttempts = register(Settings.integerBuilder("HitAttempts").withValue(4).withRange(1, 10).withVisibility { page.value == Page.EXPLODE_TWO }.build())
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
    private val crystalList = TreeSet<EntityEnderCrystal>(compareBy { it.getDistance(mc.player) })
    private val ignoredList = HashSet<EntityEnderCrystal>()
    private var lastCrystal: EntityEnderCrystal? = null
    private var state = State.NONE
    private var hitCount = 0
    private var hitTimer = 0
    private var inactiveTicks = 0

    override fun isActive(): Boolean {
        return isEnabled && (canPlace() || canExplode())
    }

    override fun onEnable() {
        if (mc.player == null) this.disable()
    }

    override fun onDisable() {
        placeMap.clear()
        crystalList.clear()
        ignoredList.clear()
        lastCrystal = null
        hitCount = 0
        hitTimer = 0
        inactiveTicks = 0
    }

    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (mc.player == null || event.packet !is SPacketSoundEffect) return@EventHook
        if (event.packet.getCategory() == SoundCategory.BLOCKS && event.packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
            val crystalList = CrystalUtils.getCrystalList(10f)
            for (crystal in crystalList) {
                if (crystal.getDistance(event.packet.x, event.packet.y, event.packet.z) > 5) continue
                crystal.setDead()
            }
        }
    })

    // CA is very order sensitive for some reasons so we have to make sure that we spoof the rotations before placing or exploding
    @EventHandler
    private val sendListener = Listener(EventHook { event: PacketEvent.PostSend ->
        if (event.packet !is CPacketPlayer) return@EventHook
        if (state == State.PLACE) place()
        else if (state == State.EXPLODE) explode()
    })

    override fun onUpdate() {
        if (CombatManager.getTopPriority() > modulePriority) return
        updateTickCounts()
        updateMap()

        if (hitTimer > getHitDelay()) {
            if (canExplode()) preExplode()
            else if (canPlace()) prePlace()
        } else {
            hitTimer++
            if (canPlace()) prePlace()
        }
        spoofRotation()
    }

    /* Main functions */
    private fun updateTickCounts() {
        if (isActive()) {
            inactiveTicks = 0
        } else {
            inactiveTicks++
        }
    }

    private fun updateMap() {
        placeMap.clear()
        placeMap.putAll(CrystalUtils.getPlacePos(CombatManager.target, mc.player, placeRange.value))

        crystalList.clear()
        crystalList.addAll(CrystalUtils.getCrystalList(max(placeRange.value, explodeRange.value)))

        ignoredList.removeIf { it.isDead }

        if (getExplodingCrystal() == null && ignoredList.isNotEmpty()) ignoredList.clear()
    }

    private fun prePlace() {
        if (autoSwap.value && getCrystalHand() == null) InventoryUtils.swapSlotToItem(426)
        getPlacingPos()?.let { pos ->
                lastRotation = Vec2f(RotationUtils.getRotationTo(Vec3d(pos).add(0.5, 0.5, 0.5), true))
                state = State.PLACE
        }
    }

    private fun place() {
        getPlacingPos()?.let { pos ->
            getCrystalHand()?.let { hand ->
                mc.player.connection.sendPacket(CPacketPlayerTryUseItemOnBlock(pos, BlockUtils.getHitSide(pos), hand, 0F, 0F, 0F))
            }
        }
        state = State.NONE
    }

    private fun preExplode() {
        if (antiWeakness.value && mc.player.isPotionActive(MobEffects.WEAKNESS) && !isHoldingTool()) equipBestWeapon()
        getExplodingCrystal()?.let {
            hitTimer = 0
            lastRotation = Vec2f(RotationUtils.getRotationTo(getExplodingHitPos(it), true))

            if (it == lastCrystal) {
                if (hitAttempts.value != 0) {
                    if (hitCount < hitAttempts.value) {
                        hitCount++
                    } else if (hitAttempts.value != 0) {
                        ignoredList.add(it)
                        hitCount = 0
                    }
                }
            } else {
                lastCrystal = it
                hitCount = 0
            }
            state = State.EXPLODE
        }
    }

    private fun explode() {
        getExplodingCrystal()?.let {
            mc.playerController.attackEntity(mc.player, it)
            mc.player.swingArm(EnumHand.MAIN_HAND)
            mc.player.setLastAttackedEntity(CombatManager.target!!)
        }
        state = State.NONE
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
                val selfDamage = CrystalUtils.calcExplosionDamage(pos, mc.player)
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
    private fun getHitDelay(): Int {
        return if (!tpsSync.value) {
            hitDelay.value
        } else {
            ceil(hitDelay.value * (20f / LagCompensator.tickRate)).toInt()
        }
    }

    private fun canExplode(): Boolean {
        return doExplode.value && getExplodingCrystal() != null && CombatManager.target?.let { target ->
            if (checkImmune.value && target.isInvulnerable) return false
            if (checkDamage.value && !shouldForceExplode()) {
                var maxDamage = 0f
                var maxSelfDamage = 0f
                for (crystal in crystalList) {
                    maxDamage = max(maxDamage, CrystalUtils.calcExplosionDamage(crystal, target))
                    maxSelfDamage = max(maxSelfDamage, CrystalUtils.calcExplosionDamage(crystal, mc.player))
                }
                if (!noSuicideCheck(maxSelfDamage)) return false
                if (!checkDamageExplode(maxDamage, maxSelfDamage)) return false
            }
            return true
        } ?: false
    }

    private fun getExplodingCrystal(): EntityEnderCrystal? {
        if (crystalList.isEmpty()) return null
        return crystalList.firstOrNull {
            !ignoredList.contains(it) && (mc.player.canEntityBeSeen(it) || EntityUtils.canEntityFeetBeSeen(it)) && it.getDistance(mc.player) < explodeRange.value
        } ?: crystalList.firstOrNull {
            !ignoredList.contains(it) && EntityUtils.canEntityHitboxBeSeen(it) != null && it.getDistance(mc.player) < wallExplodeRange.value
        }
    }

    private fun getExplodingHitPos(crystal: EntityEnderCrystal): Vec3d {
        return if (mc.player.canEntityBeSeen(crystal)) {
            crystal.getPositionEyes(1f) // If we can see the eyes then we look at the eye pos
        } else if (EntityUtils.canEntityFeetBeSeen(crystal)) {
            crystal.positionVector // If we can see the feet then we look at the feet pos
        } else {
            EntityUtils.canEntityHitboxBeSeen(crystal) // If we can it any vertex of the hit box then we look at it
        } ?: crystal.getPositionEyes(1f)  // If not then just look at the eye pos
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
            for (crystal in crystalList) {
                if (ignoredList.contains(crystal)) continue
                if (crystal.getDistance(mc.player) > placeRange.value) continue
                if (!shouldForcePlace()) {
                    val damage = CrystalUtils.calcExplosionDamage(crystal, target)
                    val selfDamage = CrystalUtils.calcExplosionDamage(crystal, mc.player)
                    if (!checkDamagePlace(damage, selfDamage)) continue
                }
                count++
            }
        }
        return count
    }
    /* End of general */

    /* Rotation spoof */
    private var lastRotation = Vec2f(0f, 0f)

    private fun spoofRotation() {
        if (inactiveTicks > 10) return
        PlayerPacketManager.addPacket(this, (PlayerPacketManager.PlayerPacket(rotating = true, rotation = lastRotation)))
    }
    /* End of rotation spoof */
}