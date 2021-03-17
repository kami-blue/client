package org.kamiblue.client.module.modules.combat

import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemBed
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.tileentity.TileEntityBed
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.manager.managers.CombatManager
import org.kamiblue.client.manager.managers.PlayerPacketManager
import org.kamiblue.client.manager.managers.PlayerPacketManager.sendPlayerPacket
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.*
import org.kamiblue.client.util.combat.CrystalUtils.calcCrystalDamage
import org.kamiblue.client.util.items.*
import org.kamiblue.client.util.math.RotationUtils
import org.kamiblue.client.util.math.RotationUtils.getRotationTo
import org.kamiblue.client.util.math.Vec2f
import org.kamiblue.client.util.math.VectorUtils
import org.kamiblue.client.util.math.VectorUtils.distanceTo
import org.kamiblue.client.util.math.VectorUtils.toVec3d
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.client.util.world.*
import java.util.*
import kotlin.collections.HashMap

@CombatManager.CombatModule
internal object BedAura : Module(
    name = "BedAura",
    description = "Place bed and kills enemies",
    category = Category.COMBAT,
    modulePriority = 70
) {
    private val ignoreSecondBaseBlock = setting("Ignore Second Base Block", false)
    private val suicideMode = setting("Suicide Mode", false)
    private val hitDelay = setting("Hit Delay", 5, 1..10, 1)
    private val refillDelay = setting("Refill Delay", 2, 1..5, 1)
    private val minDamage = setting("Min Damage", 10f, 1f..20f, 0.25f)
    private val maxSelfDamage = setting("Max Self Damage", 4f, 1f..10f, 0.25f, { !suicideMode.value })
    private val range = setting("Range", 5f, 1f..5f, 0.25f)
    private val wallRange = setting("Wall Range", 2.5f, 1f..5f, 0.25f)

    private val placeMap = TreeMap<Pair<Float, Float>, BlockPos>(compareByDescending { it.first }) // <<TargetDamage, SelfDamage>, BlockPos>
    private val bedMap = TreeMap<Float, BlockPos>(compareBy { it }) // <SquaredDistance, BlockPos>
    private val refillTimer = TickTimer(TimeUnit.TICKS)
    private var state = State.NONE
    private var clickPos = BlockPos(0, -6969, 0)
    private var lastRotation = Vec2f.ZERO
    private var hitTickCount = 0
    private var inactiveTicks = 0

    private enum class State {
        NONE, PLACE, EXPLODE
    }

    override fun getHudInfo(): String {
        return (mc.player?.inventorySlots?.countItem<ItemBed>() ?: 0).toString()
    }

    override fun isActive(): Boolean {
        return isEnabled && inactiveTicks <= 5
    }

    init {
        onDisable {
            state = State.NONE
            inactiveTicks = 6
        }

        safeListener<PacketEvent.PostSend> {
            if (!CombatManager.isOnTopPriority(this@BedAura) || it.packet !is CPacketPlayer || state == State.NONE || CombatSetting.pause) return@safeListener

            val hand = getBedHand() ?: EnumHand.MAIN_HAND
            val facing = if (state == State.PLACE) EnumFacing.UP else getClosestVisibleSide(clickPos) ?: EnumFacing.UP
            val hitVecOffset = getHitVecOffset(facing)
            val packet = CPacketPlayerTryUseItemOnBlock(clickPos, facing, hand, hitVecOffset.x.toFloat(), hitVecOffset.y.toFloat(), hitVecOffset.z.toFloat())

            connection.sendPacket(packet)
            player.swingArm(hand)
            state = State.NONE
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (player.dimension == 0 || !CombatManager.isOnTopPriority(BedAura) || CombatSetting.pause) {
                state = State.NONE
                resetRotation()
                inactiveTicks = 6
                return@safeListener
            }

            inactiveTicks++
            if (canRefill() && refillTimer.tick(refillDelay.value.toLong())) {
                player.storageSlots.firstItem<ItemBed, Slot>()?.let {
                    quickMoveSlot(it)
                }
            }

            updatePlaceMap()
            updateBedMap()

            if (hitTickCount >= hitDelay.value) {
                bedMap.values.firstOrNull()?.let { preExplode(it) } ?: getPlacePos()?.let { prePlace(it) }
            } else {
                hitTickCount++
                getPlacePos()?.let { prePlace(it) }
            }
            if (inactiveTicks <= 5) sendRotation()
            else resetRotation()
        }
    }

    private fun SafeClientEvent.canRefill(): Boolean {
        return player.hotbarSlots.firstEmpty() != null
            && player.storageSlots.firstItem<ItemBed, Slot>() != null
    }

    private fun SafeClientEvent.updatePlaceMap() {
        val cacheMap = CombatManager.target?.let {
            val posList = VectorUtils.getBlockPosInSphere(player.getPositionEyes(1f), range.value)
            val damagePosMap = HashMap<Pair<Float, Float>, BlockPos>()
            val eyePos = player.getPositionEyes(1.0f)

            for (pos in posList) {
                val dist = player.distanceTo(pos)

                if (!world.isVisible(pos) && dist > wallRange.value) continue

                val topSideVec = pos.toVec3d(0.5, 1.0, 0.5)
                val rotation = getRotationTo(topSideVec)
                val facing = EnumFacing.fromAngle(rotation.x.toDouble())
                if (!canPlaceBed(pos)) continue

                val targetDamage = calcCrystalDamage(pos.offset(facing), it)
                val selfDamage = calcCrystalDamage(pos.offset(facing), player)
                if (targetDamage < minDamage.value && (suicideMode.value || selfDamage > maxSelfDamage.value)) {
                    damagePosMap[Pair(targetDamage, selfDamage)] = pos
                }
            }

            damagePosMap
        }
        placeMap.clear()
        if (cacheMap != null) placeMap.putAll(cacheMap)
    }

    private fun SafeClientEvent.canPlaceBed(pos: BlockPos): Boolean {
        if (!world.getBlockState(pos).isSideSolid(world, pos, EnumFacing.UP)) return false
        val bedPos1 = pos.up()
        val bedPos2 = getSecondBedPos(bedPos1)
        return (!ignoreSecondBaseBlock.value || world.getBlockState(bedPos2.down()).isSideSolid(world, bedPos2.down(), EnumFacing.UP))
            && !isFire(bedPos1)
            && !isFire(bedPos2)
            && world.getBlockState(bedPos1).isReplaceable
            && (!ignoreSecondBaseBlock.value || world.getBlockState(bedPos2).isReplaceable)
    }

    private fun SafeClientEvent.isFire(pos: BlockPos): Boolean {
        return world.getBlockState(pos).block == Blocks.FIRE
    }

    private fun SafeClientEvent.updateBedMap() {
        val cacheMap = CombatManager.target?.let {
            val damagePosMap = HashMap<Float, BlockPos>()
            for (tileEntity in world.loadedTileEntityList) {
                if (tileEntity !is TileEntityBed) continue
                if (!tileEntity.isHeadPiece) continue

                val dist = player.distanceTo(tileEntity.pos).toFloat()
                if (dist > range.value) continue
                if (!world.isVisible(tileEntity.pos) && dist > wallRange.value) continue

                damagePosMap[dist] = tileEntity.pos
            }
            damagePosMap
        }
        bedMap.clear()
        if (cacheMap != null) bedMap.putAll(cacheMap)
    }

    private fun getPlacePos() = placeMap.values.firstOrNull()

    private fun SafeClientEvent.prePlace(pos: BlockPos) {
        if (getExplodePos() != null || player.allSlots.countItem<ItemBed>() == 0) return
        if (getBedHand() == null) swapToItem<ItemBed>()

        preClick(pos, Vec3d(0.5, 1.0, 0.5))
        state = State.PLACE
    }

    private fun getExplodePos() = bedMap.values.firstOrNull()

    private fun SafeClientEvent.preExplode(pos: BlockPos) {
        hitTickCount = 0
        preClick(pos, Vec3d(0.5, 0.0, 0.5))
        state = State.EXPLODE
    }

    private fun SafeClientEvent.preClick(pos: BlockPos, hitOffset: Vec3d) {
        inactiveTicks = 0
        clickPos = pos
        lastRotation = getRotationTo(Vec3d(pos).add(hitOffset))
    }

    private fun SafeClientEvent.getSecondBedPos(pos: BlockPos): BlockPos {
        val vec3d = Vec3d(pos).add(0.5, 0.0, 0.5)
        val rotation = getRotationTo(vec3d)
        val facing = EnumFacing.fromAngle(rotation.x.toDouble())
        return pos.offset(facing)
    }

    private fun SafeClientEvent.getBedHand(): EnumHand? {
        return when (Items.BED) {
            player.heldItemMainhand.item -> EnumHand.MAIN_HAND
            player.heldItemOffhand.item -> EnumHand.OFF_HAND
            else -> null
        }
    }

    private fun sendRotation() {
        sendPlayerPacket {
            rotate(lastRotation)
        }
    }

    private fun SafeClientEvent.resetRotation() {
        lastRotation = Vec2f(RotationUtils.normalizeAngle(player.rotationYaw), player.rotationPitch)
    }
}