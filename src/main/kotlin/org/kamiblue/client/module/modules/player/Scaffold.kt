package org.kamiblue.client.module.modules.player

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import org.kamiblue.client.event.Phase
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.OnUpdateWalkingPlayerEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.event.events.PlayerTravelEvent
import org.kamiblue.client.manager.managers.HotbarManager.resetHotbar
import org.kamiblue.client.manager.managers.HotbarManager.serverSideItem
import org.kamiblue.client.manager.managers.HotbarManager.spoofHotbar
import org.kamiblue.client.manager.managers.PlayerPacketManager
import org.kamiblue.client.manager.managers.PlayerPacketManager.sendPlayerPacket
import org.kamiblue.client.mixin.client.entity.MixinEntity
import org.kamiblue.client.mixin.extension.syncCurrentPlayItem
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.*
import org.kamiblue.client.util.EntityUtils.prevPosVector
import org.kamiblue.client.util.items.HotbarSlot
import org.kamiblue.client.util.items.firstItem
import org.kamiblue.client.util.items.hotbarSlots
import org.kamiblue.client.util.items.swapToSlot
import org.kamiblue.client.util.math.RotationUtils.getRotationTo
import org.kamiblue.client.util.math.Vec2f
import org.kamiblue.client.util.math.VectorUtils.toBlockPos
import org.kamiblue.client.util.threads.defaultScope
import org.kamiblue.client.util.threads.onMainThreadSafe
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.client.util.world.PlaceInfo
import org.kamiblue.client.util.world.getNeighbour
import org.kamiblue.client.util.world.placeBlock
import org.kamiblue.event.listener.listener
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * @see MixinEntity.moveInvokeIsSneakingPre
 * @see MixinEntity.moveInvokeIsSneakingPost
 */
internal object Scaffold : Module(
    name = "Scaffold",
    category = Category.PLAYER,
    description = "Places blocks under you",
    modulePriority = 500
) {
    private val tower by setting("Tower", true)
    private val spoofHotbar by setting("Spoof Hotbar", true)
    val safeWalk by setting("Safe Walk", true)
    private val sneak by setting("Sneak", true)
    private val strictDirection by setting("Strict Direction", false)
    private val delay by setting("Delay", 2, 1..10, 1)
    private val maxRange by setting("Max Range", 1, 0..3, 1)

    private var lastHitVec: Vec3d? = null
    private var placeInfo: PlaceInfo? = null
    private var inactiveTicks = 69

    private val placeTimer = TickTimer(TimeUnit.TICKS)
    private val rubberBandTimer = TickTimer(TimeUnit.TICKS)

    override fun isActive(): Boolean {
        return isEnabled && inactiveTicks <= 5
    }

    init {
        onDisable {
            placeInfo = null
            inactiveTicks = 69
        }

        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketPlayerPosLook) return@listener
            rubberBandTimer.reset()
        }

        safeListener<PlayerTravelEvent> {
            if (!tower || !mc.gameSettings.keyBindJump.isKeyDown || inactiveTicks > 5 || !isHoldingBlock) return@safeListener
            if (rubberBandTimer.tick(10, false)) {
                if (shouldTower) player.motionY = 0.41999998688697815
            } else if (player.fallDistance <= 2.0f) {
                player.motionY = -0.169
            }
        }
    }

    private val SafeClientEvent.isHoldingBlock: Boolean
        get() = player.serverSideItem.item is ItemBlock

    private val SafeClientEvent.shouldTower: Boolean
        get() = !player.onGround
            && player.posY - floor(player.posY) <= 0.1

    init {
        safeListener<OnUpdateWalkingPlayerEvent> { event ->
            if (event.phase != Phase.PRE) return@safeListener

            inactiveTicks++
            placeInfo = calcNextPos()?.let {
                getNeighbour(it, 1, visibleSideCheck = strictDirection, sides = arrayOf(EnumFacing.DOWN))
                    ?: getNeighbour(it, 3, visibleSideCheck = strictDirection, sides = EnumFacing.HORIZONTALS)
            }

            placeInfo?.let {
                lastHitVec = it.hitVec
                swapAndPlace(it)
            }

            if (inactiveTicks > 5) {
                resetHotbar()
            } else if (isHoldingBlock) {
                lastHitVec?.let {
                    sendPlayerPacket {
                        rotate(getRotationTo(it))
                    }
                }
            }
        }
    }

    private fun SafeClientEvent.calcNextPos(): BlockPos? {
        val posVec = player.positionVector
        val blockPos = posVec.toBlockPos()
        return checkPos(blockPos)
            ?: run {
                val realMotion = posVec.subtract(player.prevPosVector)
                val nextPos = blockPos.add(roundToRange(realMotion.x), 0, roundToRange(realMotion.z))
                checkPos(nextPos)
            }
    }

    private fun SafeClientEvent.checkPos(blockPos: BlockPos): BlockPos? {
        val center = Vec3d(blockPos.x + 0.5, blockPos.y.toDouble(), blockPos.z + 0.5)
        val rayTraceResult = world.rayTraceBlocks(
            center,
            center.subtract(0.0, 0.5, 0.0),
            false,
            true,
            false
        )
        return blockPos.down().takeIf { rayTraceResult?.typeOfHit != RayTraceResult.Type.BLOCK }
    }

    private fun roundToRange(value: Double) =
        (value * 2.5 * maxRange).roundToInt().coerceAtMost(maxRange)

    private fun SafeClientEvent.swapAndPlace(placeInfo: PlaceInfo) {
        getBlockSlot()?.let { slot ->
            if (spoofHotbar) spoofHotbar(slot)
            else swapToSlot(slot)

            inactiveTicks = 0

            if (placeTimer.tick(delay.toLong())) {
                val shouldSneak = sneak && !player.isSneaking
                defaultScope.launch {
                    if (shouldSneak) {
                        player.let {
                            it.connection.sendPacket(CPacketEntityAction(it, CPacketEntityAction.Action.START_SNEAKING))
                        }
                    }
                    delay(5)
                    onMainThreadSafe {
                        placeBlock(placeInfo)
                        if (shouldSneak) {
                            connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))
                        }
                    }
                }
            }
        }
    }

    private fun SafeClientEvent.getBlockSlot(): HotbarSlot? {
        playerController.syncCurrentPlayItem()
        return player.hotbarSlots.firstItem<ItemBlock, HotbarSlot>()
    }

}