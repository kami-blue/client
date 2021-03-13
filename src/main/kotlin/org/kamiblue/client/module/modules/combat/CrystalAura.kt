package org.kamiblue.client.module.modules.combat

import net.minecraft.client.entity.EntityPlayerSP
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
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.Phase
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.OnUpdateWalkingPlayerEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.event.events.RunGameLoopEvent
import org.kamiblue.client.manager.managers.CombatManager
import org.kamiblue.client.manager.managers.HotbarManager
import org.kamiblue.client.manager.managers.HotbarManager.resetHotbar
import org.kamiblue.client.manager.managers.HotbarManager.serverSideItem
import org.kamiblue.client.manager.managers.HotbarManager.spoofHotbar
import org.kamiblue.client.manager.managers.PlayerPacketManager
import org.kamiblue.client.manager.managers.PlayerPacketManager.sendPlayerPacket
import org.kamiblue.client.mixin.extension.id
import org.kamiblue.client.mixin.extension.packetAction
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.*
import org.kamiblue.client.util.combat.CombatUtils.equipBestWeapon
import org.kamiblue.client.util.combat.CombatUtils.scaledHealth
import org.kamiblue.client.util.combat.CrystalUtils.canPlaceCollide
import org.kamiblue.client.util.combat.CrystalUtils.getCrystalBB
import org.kamiblue.client.util.combat.CrystalUtils.getCrystalList
import org.kamiblue.client.util.items.*
import org.kamiblue.client.util.math.RotationUtils
import org.kamiblue.client.util.math.RotationUtils.getRotationTo
import org.kamiblue.client.util.math.RotationUtils.getRotationToEntity
import org.kamiblue.client.util.math.VectorUtils.distanceTo
import org.kamiblue.client.util.math.VectorUtils.toBlockPos
import org.kamiblue.client.util.math.VectorUtils.toVec3d
import org.kamiblue.client.util.math.VectorUtils.toVec3dCenter
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.runSafeR
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.client.util.world.getClosestVisibleSide
import org.kamiblue.commons.extension.synchronized
import org.kamiblue.commons.interfaces.DisplayEnum
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Keyboard
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@CombatManager.CombatModule
internal object CrystalAura : Module(
    name = "CrystalAura",
    alias = arrayOf("CA", "AC", "AutoCrystal"),
    description = "Places End Crystals to kill enemies",
    category = Category.COMBAT,
    modulePriority = 80
) {
    /* Settings */
    private val page = setting("Page", Page.GENERAL)

    /* General */
    private val noSuicideThreshold by setting("No Suicide", 8.0f, 0.0f..20.0f, 0.5f, page.atValue(Page.GENERAL))
    private val rotationTolerance by setting("Rotation Tolerance", 20, 5..50, 5, page.atValue(Page.GENERAL))
    private val maxYawSpeed by setting("Max YawSpeed", 80, 10..100, 5, page.atValue(Page.GENERAL))
    private val swingMode by setting("Swing Mode", SwingMode.CLIENT, page.atValue(Page.GENERAL))

    /* Force place */
    private val bindForcePlace by setting("Bind Force Place", Bind(), page.atValue(Page.FORCE_PLACE))
    private val forcePlaceHealth by setting("Force Place Health", 5.0f, 0.0f..20.0f, 0.5f, page.atValue(Page.FORCE_PLACE))
    private val forcePlaceArmorDura by setting("Force Place Armor Dura", 3, 0..50, 1, page.atValue(Page.FORCE_PLACE))
    private val minDamageForcePlace by setting("Min Damage Force Place", 1.5f, 0.0f..10.0f, 0.25f, page.atValue(Page.FORCE_PLACE))

    /* Place page one */
    private val doPlace by setting("Place", true, page.atValue(Page.PLACE_ONE))
    private val autoSwap by setting("Auto Swap", true, page.atValue(Page.PLACE_ONE))
    private val spoofHotbar by setting("Spoof Hotbar", false, page.atValue(Page.PLACE_ONE))
    private val placeSwing by setting("Place Swing", true, page.atValue(Page.PLACE_ONE))
    private val placeSync by setting("Place Sync", false, page.atValue(Page.PLACE_ONE))
    private val extraPlacePacket by setting("Extra Place Packet", false, page.atValue(Page.PLACE_ONE))

    /* Place page two */
    private val minDamageP by setting("Min Damage Place", 4.75f, 0.0f..10.0f, 0.25f, page.atValue(Page.PLACE_TWO))
    private val maxSelfDamageP by setting("Max Self Damage Place", 3.5f, 0.0f..10.0f, 0.25f, page.atValue(Page.PLACE_TWO))
    private val placeOffset by setting("Place Offset", 1.0f, 0f..1f, 0.05f, page.atValue(Page.PLACE_TWO))
    private val maxCrystal by setting("Max Crystal", 2, 1..5, 1, page.atValue(Page.PLACE_TWO))
    private val placeDelayMode = setting("Place Delay Mode", PlaceDelayMode.TICKS, page.atValue(Page.PLACE_TWO))
    private val placeDelayTick by setting("Place Delay Ticks", 1, 1..10, 1, page.atValue(Page.PLACE_TWO, placeDelayMode.atValue(PlaceDelayMode.TICKS)))
    private val placeDelayMs by setting("Place Delay ms", 30, 10..500, 1, page.atValue(Page.PLACE_TWO, placeDelayMode.atValue(PlaceDelayMode.MS)))
    private val placeRange by setting("Place Range", 4.25f, 0.0f..5.0f, 0.25f, page.atValue(Page.PLACE_TWO))
    private val wallPlaceRange by setting("Wall Place Range", 3.5f, 0.0f..5.0f, 0.25f, page.atValue(Page.PLACE_TWO))

    /* Explode page one */
    private val doExplode by setting("Explode", true, page.atValue(Page.EXPLODE_ONE))
    private val autoForceExplode by setting("Auto Force Explode", true, page.atValue(Page.EXPLODE_ONE))
    private val antiWeakness by setting("Anti Weakness", true, page.atValue(Page.EXPLODE_ONE))
    private val packetExplode by setting("Packet Explode", true, page.atValue(Page.EXPLODE_ONE))

    /* Explode page two */
    private val minDamageE by setting("Min Damage Explode", 5.0f, 0.0f..10.0f, 0.25f, page.atValue(Page.EXPLODE_TWO))
    private val maxSelfDamageE by setting("Max Self Damage Explode", 3.5f, 0.0f..10.0f, 0.25f, page.atValue(Page.EXPLODE_TWO))
    private val swapDelay by setting("Swap Delay", 10, 1..50, 1, page.atValue(Page.EXPLODE_TWO))
    private val hitDelay by setting("Hit Delay", 1, 1..10, 1, page.atValue(Page.EXPLODE_TWO))
    private val hitAttempts by setting("Hit Attempts", 4, 0..8, 1, page.atValue(Page.EXPLODE_TWO))
    private val explodeRange by setting("Explode Range", 4.25f, 0.0f..5.0f, 0.25f, page.atValue(Page.EXPLODE_TWO))
    private val wallExplodeRange by setting("Wall Explode Range", 3.5f, 0.0f..5.0f, 0.25f, page.atValue(Page.EXPLODE_TWO))
    /* End of settings */

    private enum class Page {
        GENERAL, FORCE_PLACE, PLACE_ONE, PLACE_TWO, EXPLODE_ONE, EXPLODE_TWO
    }

    @Suppress("UNUSED")
    private enum class SwingMode {
        CLIENT, PACKET
    }

    @Suppress("UNUSED")
    private enum class PlaceDelayMode(override val displayName: String) : DisplayEnum {
        TICKS("Ticks"),
        MS("ms")
    }

    /* Variables */
    private val placedBBMap = HashMap<BlockPos, Pair<AxisAlignedBB, Long>>().synchronized() // <CrystalBoundingBox, Added Time>
    private val ignoredList = Collections.newSetFromMap<EntityEnderCrystal>(WeakHashMap())
    private val packetList = ArrayList<Packet<*>>(3)
    private val yawDiffList = FloatArray(20)
    private val placeTimerMs = TickTimer()

    private var placeMap = emptyMap<BlockPos, CombatManager.CrystalDamage>()
    private var crystalMap = emptyMap<EntityEnderCrystal, CombatManager.CrystalDamage>()
    private var lastCrystal: EntityEnderCrystal? = null
    private var lastLookAt = Vec3d.ZERO
    private var forcePlacing = false
    private var placeTimerTicks = 0
    private var hitTimer = 0
    private var hitCount = 0
    private var yawDiffIndex = 0

    var inactiveTicks = 20; private set
    val minDamage get() = max(minDamageP, minDamageE)
    val maxSelfDamage get() = min(maxSelfDamageP, maxSelfDamageE)

    override fun isActive() = isEnabled && inactiveTicks <= 20

    init {
        onEnable {
            runSafeR {
                resetRotation()
            } ?: disable()
        }

        onDisable {
            placeTimerMs.reset(-69420L)

            lastCrystal = null
            forcePlacing = false

            hitTimer = 0
            hitCount = 0
            inactiveTicks = 10

            placedBBMap.clear()
            synchronized(packetList) {
                packetList.clear()
            }
            resetHotbar()
        }

        listener<InputEvent.KeyInputEvent> {
            if (bindForcePlace.isDown(Keyboard.getEventKey())) {
                forcePlacing = !forcePlacing
                MessageSendHelper.sendChatMessage("$chatName Force placing" + if (forcePlacing) " &aenabled" else " &cdisabled")
            }
        }

        safeListener<PacketEvent.Receive> { event ->
            when (event.packet) {
                is SPacketSpawnObject -> {
                    if (event.packet.type == 51) {
                        val vec3d = Vec3d(event.packet.x, event.packet.y, event.packet.z)
                        val pos = vec3d.toBlockPos()

                        placedBBMap.remove(pos)?.let {
                            if (packetExplode) {
                                packetExplode(event.packet.entityID, pos.down(), vec3d)
                            }
                        }
                    }
                }
                is SPacketSoundEffect -> {
                    // Minecraft sends sounds packets a tick before removing the crystal lol
                    if (event.packet.category == SoundCategory.BLOCKS && event.packet.sound == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                        val crystalList = getCrystalList(Vec3d(event.packet.x, event.packet.y, event.packet.z), 6.0f)

                        for (crystal in crystalList) {
                            crystal.setDead()
                        }

                        ignoredList.clear()
                        hitCount = 0
                    }
                }
            }
        }

        safeListener<RunGameLoopEvent.Tick> {
            if (placeDelayMode.value == PlaceDelayMode.MS
                && CombatManager.isOnTopPriority(CrystalAura)
                && !CombatSetting.pause
                && packetList.size == 0
                && canPlace()) {
                place()
            }
        }

        safeListener<OnUpdateWalkingPlayerEvent> {
            if (!CombatManager.isOnTopPriority(CrystalAura) || CombatSetting.pause) return@safeListener

            if (it.phase == Phase.PRE && inactiveTicks <= 20 && lastLookAt != Vec3d.ZERO) {
                sendPlayerPacket {
                    rotate(getLastRotation())
                }
            }

            if (it.phase == Phase.POST) {
                synchronized(packetList) {
                    for (packet in packetList) sendPacketDirect(packet)
                    packetList.clear()
                }
            }
        }

        safeListener<TickEvent.ClientTickEvent>(2000) {
            if (it.phase == TickEvent.Phase.START) {
                inactiveTicks++
                placeTimerTicks++
                hitTimer++
                updateYawSpeed()
            }

            if (CombatManager.isOnTopPriority(CrystalAura) && !CombatSetting.pause && packetList.size == 0) {
                updateMap()
                if (canExplode()) explode()
                if (canPlace()) place()
            }

            if (it.phase == TickEvent.Phase.END) {
                if (getHand() == EnumHand.OFF_HAND) {
                    resetHotbar()
                }
                if (inactiveTicks > 20) {
                    resetHotbar()
                    resetRotation()
                }
            }
        }
    }

    private fun updateYawSpeed() {
        val yawDiff = abs(RotationUtils.normalizeAngle(PlayerPacketManager.prevServerSideRotation.x - PlayerPacketManager.serverSideRotation.x))
        yawDiffList[yawDiffIndex] = yawDiff
        yawDiffIndex = (yawDiffIndex + 1) % 20
    }

    private fun SafeClientEvent.updateMap() {
        placeMap = CombatManager.placeMap
        crystalMap = CombatManager.crystalMap

        synchronized(placedBBMap) {
            placedBBMap.values.removeIf { System.currentTimeMillis() - it.second > max(InfoCalculator.ping(), 100) }
        }

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

    private fun SafeClientEvent.place() {
        getPlacingPos()?.let { pos ->
            swapToCrystal()

            val hand = getHand()
            inactiveTicks = 0
            lastLookAt = pos.toVec3d(0.5, placeOffset.toDouble(), 0.5)

            if (hand == null) return
            placeTimerMs.reset()
            placeTimerTicks = 0

            sendOrQueuePacket(getPlacePacket(pos, hand))
            if (extraPlacePacket) sendOrQueuePacket(getPlacePacket(pos, hand))
            if (placeSwing) sendOrQueuePacket(CPacketAnimation(hand))

            val crystalPos = pos.up()
            placedBBMap[crystalPos] = getCrystalBB(crystalPos) to System.currentTimeMillis()
        }
    }

    private fun SafeClientEvent.swapToCrystal() {
        if (autoSwap && player.heldItemOffhand.item != Items.END_CRYSTAL) {
            if (spoofHotbar) {
                val slot = if (player.serverSideItem.item == Items.END_CRYSTAL) HotbarManager.serverSideHotbar
                else player.getCrystalSlot()?.hotbarSlot

                if (slot != null) {
                    spoofHotbar(slot, 1000L)
                }
            } else {
                if (player.serverSideItem.item != Items.END_CRYSTAL) {
                    player.getCrystalSlot()?.let {
                        swapToSlot(it)
                    }
                }
            }
        }
    }

    private fun EntityPlayerSP.getCrystalSlot() =
        this.hotbarSlots.firstItem(Items.END_CRYSTAL)

    private fun SafeClientEvent.getPlacePacket(pos: BlockPos, hand: EnumHand): CPacketPlayerTryUseItemOnBlock {
        val side = getClosestVisibleSide(pos) ?: EnumFacing.UP
        return CPacketPlayerTryUseItemOnBlock(pos, side, hand, 0.5f, placeOffset, 0.5f)
    }

    private fun SafeClientEvent.packetExplode(entityID: Int, pos: BlockPos, vec3d: Vec3d) {
        if (!preExplode()) return

        val calculation = placeMap[pos] ?: return

        if (!noSuicideCheck(calculation.selfDamage)) return
        if (!checkDamageExplode(calculation.targetDamage, calculation.selfDamage)) return
        if (calculation.distance > explodeRange) return

        val attackPacket = CPacketUseEntity().apply {
            id = entityID
            packetAction = CPacketUseEntity.Action.ATTACK
        }

        synchronized(packetList) {
            explodeDirect(attackPacket, vec3d)
        }
    }

    private fun SafeClientEvent.explode() {
        getExplodingCrystal()?.let {
            if (!preExplode()) return

            if (hitAttempts != 0 && it == lastCrystal) {
                hitCount++
                if (hitCount >= hitAttempts) ignoredList.add(it)
            } else {
                hitCount = 0
            }

            CombatManager.target?.let { target -> player.setLastAttackedEntity(target) }
            lastCrystal = it

            explodeDirect(CPacketUseEntity(it), it.positionVector)
        }
    }

    private fun SafeClientEvent.preExplode(): Boolean {
        if (antiWeakness && player.isPotionActive(MobEffects.WEAKNESS) && !isHoldingTool()) {
            equipBestWeapon(allowTool = true)
            resetHotbar()
            return false
        }

        // Anticheat doesn't allow you attack right after changing item
        if (System.currentTimeMillis() - HotbarManager.swapTime < swapDelay * 50L) {
            return false
        }

        return true
    }

    private fun SafeClientEvent.explodeDirect(packet: CPacketUseEntity, pos: Vec3d) {
        hitTimer = 0
        inactiveTicks = 0
        lastLookAt = pos

        sendOrQueuePacket(packet)
        sendOrQueuePacket(CPacketAnimation(getHand() ?: EnumHand.OFF_HAND))
    }

    private fun SafeClientEvent.sendOrQueuePacket(packet: Packet<*>) {
        val yawDiff = abs(RotationUtils.normalizeAngle(PlayerPacketManager.serverSideRotation.x - getLastRotation().x))
        if (yawDiff < rotationTolerance) {
            sendPacketDirect(packet)
        } else {
            synchronized(packetList) {
                packetList.add(packet)
            }
        }
    }

    private fun SafeClientEvent.sendPacketDirect(packet: Packet<*>) {
        if (packet is CPacketAnimation && swingMode == SwingMode.CLIENT) player.swingArm(packet.hand)
        else connection.sendPacket(packet)
    }
    /* End of main functions */

    /* Placing */
    private fun SafeClientEvent.canPlace() =
        doPlace
            && checkTimer()
            && player.allSlots.countItem(Items.END_CRYSTAL) > 0
            && countValidCrystal() < maxCrystal

    private fun checkTimer() =
        if (placeDelayMode.value == PlaceDelayMode.TICKS) placeTimerTicks > placeDelayTick
        else placeTimerMs.tick(placeDelayMs, false)

    @Suppress("UnconditionalJumpStatementInLoop") // The linter is wrong here, it will continue until it's supposed to return
    private fun SafeClientEvent.getPlacingPos(): BlockPos? {
        if (placeMap.isEmpty()) return null

        val eyePos = player.getPositionEyes(1f)

        for ((pos, crystalDamage) in placeMap) {
            // Damage check
            if (!noSuicideCheck(crystalDamage.selfDamage)) continue
            if (!checkDamagePlace(crystalDamage)) continue

            // Distance check
            if (crystalDamage.distance > placeRange) continue

            // Wall distance check
            val rayTraceResult = world.rayTraceBlocks(eyePos, pos.toVec3dCenter())
            val hitBlockPos = rayTraceResult?.blockPos ?: pos
            if (hitBlockPos.distanceTo(pos) > 1.0 && crystalDamage.distance > wallPlaceRange) continue

            // Collide check
            if (!canPlaceCollide(pos)) continue

            // Place sync
            if (placeSync) {
                val bb = getCrystalBB(pos.up())
                val intercepted = synchronized(placedBBMap) {
                    placedBBMap.values.any { it.first.intersects(bb) }
                }

                if (intercepted) continue
            }

            // Yaw speed check
            val hitVec = pos.toVec3d(0.5, placeOffset.toDouble(), 0.5)
            if (!checkYawSpeed(getRotationTo(hitVec).x)) continue

            return pos
        }
        return null
    }

    /**
     * @return True if passed placing damage check
     */
    private fun checkDamagePlace(crystalDamage: CombatManager.CrystalDamage) =
        (crystalDamage.selfDamage <= maxSelfDamageP)
            && (shouldFacePlace(crystalDamage.targetDamage) || crystalDamage.targetDamage >= minDamageP)
    /* End of placing */

    /* Exploding */
    private fun canExplode() = doExplode && hitTimer > hitDelay

    private fun SafeClientEvent.getExplodingCrystal(): EntityEnderCrystal? {
        val filteredCrystal = crystalMap.entries.filter { (crystal, triple) ->
            !ignoredList.contains(crystal)
                && !crystal.isDead
                && checkDamageExplode(triple.targetDamage, triple.selfDamage)
                && checkYawSpeed(getRotationToEntity(crystal).x)
        }

        return (filteredCrystal.firstOrNull { (crystal, calculation) ->
            calculation.distance <= explodeRange
                && (player.canEntityBeSeen(crystal) || EntityUtils.canEntityFeetBeSeen(crystal))
        } ?: filteredCrystal.firstOrNull { (_, calculation) ->
            calculation.distance <= wallExplodeRange
        })?.key
    }


    private fun checkDamageExplode(damage: Float, selfDamage: Float) =
        (shouldFacePlace(damage) || shouldForceExplode() || damage >= minDamageE) && selfDamage <= maxSelfDamageE

    private fun shouldForceExplode() = autoForceExplode
        && placeMap.values.any {
        it.targetDamage > minDamage && it.selfDamage <= maxSelfDamage && it.distance <= placeRange
    }
    /* End of exploding */

    /* General */
    private fun SafeClientEvent.getHand(): EnumHand? {
        return when (Items.END_CRYSTAL) {
            player.heldItemOffhand.item -> EnumHand.OFF_HAND
            player.serverSideItem.item -> EnumHand.MAIN_HAND
            else -> null
        }
    }

    private fun SafeClientEvent.noSuicideCheck(selfDamage: Float) = player.scaledHealth - selfDamage > noSuicideThreshold

    private fun SafeClientEvent.isHoldingTool(): Boolean {
        val item = player.heldItemMainhand.item
        return item is ItemTool || item is ItemSword
    }

    private fun shouldFacePlace(damage: Float) =
        damage >= minDamageForcePlace
            && (forcePlacing
            || forcePlaceHealth > 0.0f && CombatManager.target?.let { it.scaledHealth <= forcePlaceHealth } ?: false
            || forcePlaceArmorDura > 0.0f && getMinArmorDura() <= forcePlaceArmorDura)

    private fun getMinArmorDura() =
        (CombatManager.target?.let { target ->
            target.armorInventoryList
                .filter { !it.isEmpty && it.isItemStackDamageable }
                .maxByOrNull { it.itemDamage }
                ?.let {
                    (it.maxDamage - it.itemDamage) * 100 / it.maxDamage
                }
        }) ?: 100

    private fun SafeClientEvent.countValidCrystal(): Int {
        var count = 0
        CombatManager.target?.let {
            if (placeSync) {
                synchronized(placedBBMap) {
                    for ((pos, _) in placedBBMap) {
                        val crystalDamage = placeMap[pos] ?: continue

                        if (crystalDamage.distance > placeRange) continue
                        if (!checkDamagePlace(crystalDamage)) continue

                        count++
                    }
                }
            }

            for ((crystal, crystalDamage) in crystalMap) {
                if (crystalDamage.distance > placeRange) continue
                if (ignoredList.contains(crystal)) continue
                if (!checkDamagePlace(crystalDamage)) continue
                if (!checkYawSpeed(getRotationToEntity(crystal).x)) continue

                count++
            }
        }
        return count
    }
    /* End of general */

    /* Rotation */
    private fun checkYawSpeed(yaw: Float): Boolean {
        val yawDiff = abs(RotationUtils.normalizeAngle(yaw - PlayerPacketManager.serverSideRotation.x))
        return yawDiffList.sum() + yawDiff <= maxYawSpeed
    }

    private fun SafeClientEvent.getLastRotation() =
        getRotationTo(lastLookAt)

    private fun resetRotation() {
        lastLookAt = CombatManager.target?.positionVector ?: Vec3d.ZERO
    }
    /* End of rotation */
}
