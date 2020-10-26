package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.KamiEvent
import me.zeroeightsix.kami.event.events.OnUpdateWalkingPlayerEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.BlockUtils
import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.combat.CombatUtils
import me.zeroeightsix.kami.util.combat.CrystalUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.math.Vec2f
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Items
import net.minecraft.init.MobEffects
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.network.play.server.SPacketSpawnObject
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@CombatManager.CombatModule
@Module.Info(
        name = "CrystalAura",
        description = "Places End Crystals to kill enemies",
        category = Module.Category.COMBAT,
        modulePriority = 80
)
object CrystalAura : Module() {
    /* Settings */
    private val page = setting("Page", Page.GENERAL)

    /* General */
    private val noSuicideThreshold = setting("NoSuicide", 8.0f, 0.0f..20.0f, 0.5f, { page.value == Page.GENERAL })
    private val rotationTolerance = setting("RotationTolerance", 15, 5..50, 5, { page.value == Page.GENERAL })
    private val maxYawSpeed = setting("MaxYawSpeed", 50, 10..100, 5, { page.value == Page.GENERAL })
    private val swingMode = setting("SwingMode", SwingMode.CLIENT, { page.value == Page.GENERAL })

    /* Force place */
    private val bindForcePlace = setting("BindForcePlace", { page.value == Page.FORCE_PLACE })
    private val forcePlaceHealth = setting("ForcePlaceHealth", 6.0f, 0.0f..20.0f, 0.5f, { page.value == Page.FORCE_PLACE })
    private val forcePlaceArmorDura = setting("ForcePlaceArmorDura", 10, 0..50, 1, { page.value == Page.FORCE_PLACE })
    private val minDamageForcePlace = setting("MinDamageForcePlace", 1.5f, 0.0f..10.0f, 0.25f, { page.value == Page.FORCE_PLACE })

    /* Place page one */
    private val doPlace = setting("Place", true, { page.value == Page.PLACE_ONE })
    private val autoSwap = setting("AutoSwap", true, { page.value == Page.PLACE_ONE })
    private val spoofHotbar = setting("SpoofHotbar", true, { page.value == Page.PLACE_ONE && autoSwap.value })
    private val placeSwing = setting("PlaceSwing", false, { page.value == Page.PLACE_ONE })
    private val placeSync = setting("PlaceSync", false, { page.value == Page.PLACE_ONE })
    private val extraPlacePacket = setting("ExtraPlacePacket", false, { page.value == Page.PLACE_ONE })

    /* Place page two */
    private val minDamageP = setting("MinDamagePlace", 2.0f, 0.0f..10.0f, 0.25f, { page.value == Page.PLACE_TWO })
    private val maxSelfDamageP = setting("MaxSelfDamagePlace", 2.0f, 0.0f..10.0f, 0.25f, { page.value == Page.PLACE_TWO })
    private val placeOffset = setting("PlaceOffset", 1.0f, 0f..1f, 0.05f, { page.value == Page.PLACE_TWO })
    private val maxCrystal = setting("MaxCrystal", 2, 1..5, 1, { page.value == Page.PLACE_TWO })
    private val placeDelay = setting("PlaceDelay", 1, 1..10, 1, { page.value == Page.PLACE_TWO })
    private val placeRange = setting("PlaceRange", 4.0f, 0.0f..5.0f, 0.25f, { page.value == Page.PLACE_TWO })
    private val wallPlaceRange = setting("WallPlaceRange", 2.0f, 0.0f..5.0f, 0.25f, { page.value == Page.PLACE_TWO })

    /* Explode page one */
    private val doExplode = setting("Explode", true, { page.value == Page.EXPLODE_ONE })
    private val autoForceExplode = setting("AutoForceExplode", true, { page.value == Page.EXPLODE_ONE })
    private val antiWeakness = setting("AntiWeakness", true, { page.value == Page.EXPLODE_ONE })

    /* Explode page two */
    private val checkDamage = setting("CheckDamage", true, { page.value == Page.EXPLODE_TWO })
    private val minDamageE = setting("MinDamageExplode", 6.0f, 0.0f..10.0f, 0.25f, { page.value == Page.EXPLODE_TWO && checkDamage.value })
    private val maxSelfDamageE = setting("MaxSelfDamageExplode", 3.0f, 0.0f..10.0f, 0.25f, { page.value == Page.EXPLODE_TWO && checkDamage.value })
    private val swapDelay = setting("SwapDelay", 10, 1..50, 2, { page.value == Page.EXPLODE_TWO })
    private val hitDelay = setting("HitDelay", 1, 1..10, 1, { page.value == Page.EXPLODE_TWO })
    private val hitAttempts = setting("HitAttempts", 4, 0..8, 1, { page.value == Page.EXPLODE_TWO })
    private val explodeRange = setting("ExplodeRange", 4.0f, 0.0f..5.0f, 0.25f, { page.value == Page.EXPLODE_TWO })
    private val wallExplodeRange = setting("WallExplodeRange", 2.0f, 0.0f..5.0f, 0.25f, { page.value == Page.EXPLODE_TWO })
    /* End of settings */

    private enum class Page {
        GENERAL, FORCE_PLACE, PLACE_ONE, PLACE_TWO, EXPLODE_ONE, EXPLODE_TWO
    }

    @Suppress("UNUSED")
    private enum class SwingMode {
        CLIENT, PACKET
    }

    /* Variables */
    private val placedBBMap = HashMap<AxisAlignedBB, Long>() // <CrystalBoundingBox, Added Time>
    private val ignoredList = HashSet<EntityEnderCrystal>()
    private val packetList = ArrayList<Packet<*>>(3)
    private val lockObject = Any()

    private var placeList = emptyList<Triple<BlockPos, Float, Float>>() // <BlockPos, Target Damage, Self Damage>
    private var crystalMap = emptyMap<EntityEnderCrystal, Triple<Float, Float, Double>>() // <Crystal, <Target Damage, Self Damage>>
    private var lastCrystal: EntityEnderCrystal? = null
    private var lastLookAt = Vec3d.ZERO
    private var forcePlacing = false
    private var placeTimer = 0
    private var hitTimer = 0
    private var hitCount = 0

    var inactiveTicks = 20; private set
    val minDamage get() = max(minDamageP.value, minDamageE.value)
    val maxSelfDamage get() = min(maxSelfDamageP.value, maxSelfDamageE.value)

    override fun isActive() = isEnabled && InventoryUtils.countItemAll(426) > 0 && inactiveTicks <= 20

    override fun onEnable() {
        if (mc.player == null) disable()
        else resetRotation()
    }

    override fun onDisable() {
        placedBBMap.clear()
        ignoredList.clear()
        packetList.clear()

        lastCrystal = null
        forcePlacing = false
        placeTimer = 0
        hitTimer = 0
        hitCount = 0
        inactiveTicks = 10
        PlayerPacketManager.resetHotbar()
    }

    init {
        listener<InputEvent.KeyInputEvent> {
            if (bindForcePlace.value.isDown(Keyboard.getEventKey())){
                forcePlacing = !forcePlacing
                MessageSendHelper.sendChatMessage("$chatName Force placing" + if (forcePlacing) " &aenabled" else " &cdisabled")
            }
        }

        listener<PacketEvent.Receive> {
            if (mc.player == null) return@listener

            if (it.packet is SPacketSpawnObject && it.packet.type == 51) {
                val pos = Vec3d(it.packet.x, it.packet.y + 1.0, it.packet.z)
                synchronized(lockObject) {
                    placedBBMap.keys.removeIf { bb -> bb.contains(pos) }
                }
            }

            // Minecraft sends sounds packets a tick before removing the crystal lol
            if (it.packet is SPacketSoundEffect
                    && it.packet.getCategory() == SoundCategory.BLOCKS
                    && it.packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                val crystalList = CrystalUtils.getCrystalList(Vec3d(it.packet.x, it.packet.y, it.packet.z), 5f)

                for (crystal in crystalList) {
                    crystal.setDead()
                    mc.world.removeEntityFromWorld(crystal.entityId)
                }

                ignoredList.clear()
                hitCount = 0
            }
        }

        listener<OnUpdateWalkingPlayerEvent> {
            if (!CombatManager.isOnTopPriority(this) || CombatSetting.pause) return@listener

            if (it.era == KamiEvent.Era.PRE && inactiveTicks <= 20 && lastLookAt != Vec3d.ZERO) {
                val packet = PlayerPacketManager.PlayerPacket(rotating = true, rotation = Vec2f(getLastRotation()))
                PlayerPacketManager.addPacket(this, packet)
            }

            if (it.era == KamiEvent.Era.POST) {
                for (packet in packetList) sendPacketDirect(packet)
                packetList.clear()
            }
        }

        listener<SafeTickEvent>(2000) {
            if (it.phase == TickEvent.Phase.START) {
                inactiveTicks++
                hitTimer++
                placeTimer++
            }

            runTick()

            if (it.phase == TickEvent.Phase.END) {
                if (inactiveTicks > 5 || getHand() == EnumHand.OFF_HAND) PlayerPacketManager.resetHotbar()
                if (inactiveTicks > 20) resetRotation()
            }
        }
    }

    private fun runTick() {
        if (!CombatManager.isOnTopPriority(this) || CombatSetting.pause || packetList.size > 0) return
        updateMap()
        if (canExplode()) explode() else if (canPlace()) place()
    }

    private fun updateMap() {
        placeList = CombatManager.crystalPlaceList
        crystalMap = CombatManager.crystalMap

        placedBBMap.values.removeIf { System.currentTimeMillis() - it > max(InfoCalculator.ping(), 100) }

        if (inactiveTicks > 20) {
            if (getPlacingPos() == null && placedBBMap.isNotEmpty()) {
                placedBBMap.clear()
            }

            if (getExplodingCrystal() == null && ignoredList.isNotEmpty()) {
                ignoredList.clear()
                hitCount = 0
            }
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
                sendOrQueuePacket(getPlacePacket(pos, hand))
                if (extraPlacePacket.value) sendOrQueuePacket(getPlacePacket(pos, hand))
                if (placeSwing.value) sendOrQueuePacket(CPacketAnimation(hand))
                placedBBMap[CrystalUtils.getCrystalBB(pos.up())] = System.currentTimeMillis()
            }
        }
    }

    private fun explode() {
        if (antiWeakness.value && mc.player.isPotionActive(MobEffects.WEAKNESS) && !isHoldingTool()) {
            CombatUtils.equipBestWeapon()
            PlayerPacketManager.resetHotbar()
            return
        }

        // Anticheat doesn't allow you attack right after changing item
        if (System.currentTimeMillis() - PlayerPacketManager.lastSwapTime < swapDelay.value * 50) {
            return
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
            sendOrQueuePacket(CPacketUseEntity(it))
            sendOrQueuePacket(CPacketAnimation(getHand() ?: EnumHand.OFF_HAND))
            CombatManager.target?.let { target -> mc.player.setLastAttackedEntity(target) }
            lastCrystal = it
        }
    }

    private fun getPlacePacket(pos: BlockPos, hand: EnumHand) =
            CPacketPlayerTryUseItemOnBlock(pos, BlockUtils.getHitSide(pos), hand, 0.5f, placeOffset.value, 0.5f)

    private fun sendOrQueuePacket(packet: Packet<*>) {
        val yawDiff = abs(RotationUtils.normalizeAngle(PlayerPacketManager.serverSideRotation.x - getLastRotation().x))
        if (yawDiff < rotationTolerance.value) sendPacketDirect(packet)
        else packetList.add(packet)
    }

    private fun sendPacketDirect(packet: Packet<*>) {
        if (packet is CPacketAnimation && swingMode.value == SwingMode.CLIENT) mc.player?.swingArm(packet.hand)
        else mc.connection?.sendPacket(packet)
    }
    /* End of main functions */

    /* Placing */
    private fun canPlace(): Boolean {
        return doPlace.value
                && placeTimer > placeDelay.value
                && InventoryUtils.countItemAll(426) > 0
                && getPlacingPos() != null
                && countValidCrystal() < maxCrystal.value
    }

    @Suppress("UnconditionalJumpStatementInLoop") // The linter is wrong here, it will continue until it's supposed to return
    private fun getPlacingPos(): BlockPos? {
        if (CombatManager.crystalPlaceList.isEmpty()) return null
        val eyePos = mc.player.getPositionEyes(1f)
        for ((pos, damage, selfDamage) in CombatManager.crystalPlaceList) {
            // Damage check
            if (!noSuicideCheck(selfDamage)) continue
            if (!checkDamagePlace(damage, selfDamage)) continue

            // Distance check
            val hitVec = Vec3d(pos).add(0.5, placeOffset.value.toDouble(), 0.5)
            val dist = eyePos.distanceTo(hitVec)
            if (dist > placeRange.value) continue

            // Wall distance check
            val rayTraceResult = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1f), Vec3d(pos).add(0.5, 0.5, 0.5))
            val hitBlockPos = rayTraceResult?.blockPos ?: pos
            if (hitBlockPos.distanceSq(pos) > 2.0 && dist > wallPlaceRange.value) continue

            // Collide check
            if (!CrystalUtils.canPlaceCollide(pos)) continue

            // Place sync
            if (placeSync.value) {
                val bb = CrystalUtils.getCrystalBB(pos.up())
                if (placedBBMap.keys.firstOrNull { it.intersects(bb) } != null) continue
            }

            // Yaw rate check
            if (!checkYawSpeed(RotationUtils.getRotationTo(hitVec, true).x)) continue

            return pos
        }
        return null
    }

    /**
     * @return True if passed placing damage check
     */
    private fun checkDamagePlace(damage: Float, selfDamage: Float) =
            (shouldFacePlace(damage) || damage >= minDamageP.value) && (selfDamage <= maxSelfDamageP.value)
    /* End of placing */

    /* Exploding */
    private fun canExplode() =
            doExplode.value
                    && hitTimer > hitDelay.value
                    && getExplodingCrystal() != null
                    && CombatManager.target?.let {
                if (checkDamage.value) {
                    val maxDamage = crystalMap.values.maxBy { it.first }?.first ?: 0.0f
                    val maxSelfDamage = crystalMap.values.maxBy { it.second }?.second ?: 0.0f
                    if (!noSuicideCheck(maxSelfDamage)) return false
                    if (!checkDamageExplode(maxDamage, maxSelfDamage)) return false
                }
                return true
            } ?: false

    private fun getExplodingCrystal(): EntityEnderCrystal? {
        val eyePos = mc.player.getPositionEyes(1f)
        return crystalMap.keys.firstOrNull {
            !ignoredList.contains(it)
                    && !it.isDead
                    && (mc.player.canEntityBeSeen(it) || EntityUtils.canEntityFeetBeSeen(it))
                    && eyePos.distanceTo(it.positionVector) <= explodeRange.value
                    && checkYawSpeed(RotationUtils.getRotationToEntity(it).x)
        } ?: crystalMap.keys.firstOrNull {
            !ignoredList.contains(it)
                    && !it.isDead
                    && EntityUtils.canEntityHitboxBeSeen(it) != null
                    && eyePos.distanceTo(it.positionVector) <= wallExplodeRange.value
                    && checkYawSpeed(RotationUtils.getRotationToEntity(it).x)
        }
    }


    private fun checkDamageExplode(damage: Float, selfDamage: Float) = (shouldFacePlace(damage) || shouldForceExplode() || damage >= minDamageE.value) && selfDamage <= maxSelfDamageE.value

    private fun shouldForceExplode() = autoForceExplode.value && CombatManager.crystalPlaceList.isNotEmpty() && CombatManager.crystalPlaceList.first().second > minDamageE.value
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

    private fun noSuicideCheck(selfDamage: Float) = CombatUtils.getHealthSmart(mc.player) - selfDamage > noSuicideThreshold.value

    private fun isHoldingTool(): Boolean {
        val item = mc.player.heldItemMainhand.getItem()
        return item is ItemTool || item is ItemSword
    }

    private fun shouldFacePlace(damage: Float) =
            damage >= minDamageForcePlace.value
                    && (forcePlacing
                    || forcePlaceHealth.value > 0.0f && CombatManager.target?.let { CombatUtils.getHealthSmart(it) <= forcePlaceHealth.value } ?: false
                    || forcePlaceArmorDura.value > 0.0f && getMinArmorDura() <= forcePlaceArmorDura.value)

    private fun getMinArmorDura() =
            (CombatManager.target?.let { target ->
                target.armorInventoryList
                        .filter { !it.isEmpty() && it.isItemStackDamageable }
                        .maxBy { it.itemDamage }
                        ?.let {
                            (it.maxDamage - it.itemDamage) * 100 / it.maxDamage
                        }
            }) ?: 100

    private fun countValidCrystal(): Int {
        var count = 0
        CombatManager.target?.let {
            val eyePos = mc.player.getPositionEyes(1f)

            if (placeSync.value) {
                // For some reasons it causes ConcurrentModificationException here, so we have to make a copy of it
                for (bb in ArrayList(placedBBMap.keys)) {
                    val pos = bb.center.subtract(0.0, 1.0, 0.0)
                    if (pos.distanceTo(eyePos) > placeRange.value) continue
                    val damage = CrystalUtils.calcDamage(pos, it)
                    val selfDamage = CrystalUtils.calcDamage(pos, mc.player)
                    if (!checkDamagePlace(damage, selfDamage)) continue
                    count++
                }
            }

            for ((crystal, pair) in crystalMap) {
                if (!checkDamagePlace(pair.first, pair.second)) continue
                if (ignoredList.contains(crystal)) continue
                if (crystal.positionVector.distanceTo(eyePos) > placeRange.value) continue
                if (!checkYawSpeed(RotationUtils.getRotationToEntity(crystal).x)) continue
                count++
            }
        }
        return count
    }
    /* End of general */

    /* Rotation */
    private fun checkYawSpeed(yaw: Double) =
            abs(RotationUtils.normalizeAngle(yaw - getLastRotation().x)) <= maxYawSpeed.value + (inactiveTicks * 8f)

    private fun getLastRotation() =
            RotationUtils.getRotationTo(lastLookAt, true)

    private fun resetRotation() {
        lastLookAt = CombatManager.target?.positionVector ?: Vec3d.ZERO
    }
    /* End of rotation */
}