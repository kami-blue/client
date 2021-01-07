package me.zeroeightsix.kami.module.modules.player

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.event.Phase
import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.OnUpdateWalkingPlayerEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.mixin.client.entity.MixinEntity
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.EntityUtils.prevPosVector
import me.zeroeightsix.kami.util.WorldUtils.placeBlock
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.math.Vec2f
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.onMainThreadSafe
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import org.kamiblue.event.listener.listener
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * @see MixinEntity.isSneaking
 */
object Scaffold : Module(
    name = "Scaffold",
    category = Category.PLAYER,
    description = "Places blocks under you",
    modulePriority = 500
) {
    private val tower by setting("Tower", true)
    private val spoofHotbar by setting("SpoofHotbar", true)
    val safeWalk by setting("SafeWalk", true)
    private val sneak by setting("Sneak", true)
    private val delay by setting("Delay", 2, 1..10, 1)
    private val maxRange by setting("MaxRange", 1, 0..3, 1)

    private var lastRotation = Vec2f.ZERO
    private var placeInfo: Pair<EnumFacing, BlockPos>? = null
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

    private val isHoldingBlock: Boolean
        get() = PlayerPacketManager.getHoldingItemStack().item is ItemBlock

    private val SafeClientEvent.shouldTower: Boolean
        get() = !player.onGround
            && player.posY - floor(player.posY) <= 0.1
    init {
        safeListener<OnUpdateWalkingPlayerEvent> { event ->
            if (event.phase != Phase.PRE) return@safeListener

            inactiveTicks++
            placeInfo = calcNextPos()?.let {
                WorldUtils.getNeighbour(it, 1, sides = arrayOf(EnumFacing.DOWN))
                    ?: WorldUtils.getNeighbour(it, 3, sides = EnumFacing.HORIZONTALS)
            }

            placeInfo?.let {
                val hitVec = WorldUtils.getHitVec(it.second, it.first)
                lastRotation = RotationUtils.getRotationTo(hitVec)
                swapAndPlace(it.second, it.first)
            }

            if (inactiveTicks > 5) {
                PlayerPacketManager.resetHotbar()
            } else if (isHoldingBlock) {
                val packet = PlayerPacketManager.PlayerPacket(rotating = true, rotation = lastRotation)
                PlayerPacketManager.addPacket(this@Scaffold, packet)
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

    private fun SafeClientEvent.swapAndPlace(pos: BlockPos, side: EnumFacing) {
        getBlockSlot()?.let { slot ->
            if (spoofHotbar) PlayerPacketManager.spoofHotbar(slot)
            else InventoryUtils.swapSlot(slot)

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
                        placeBlock(pos, side)
                        if (shouldSneak) {
                            connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))
                        }
                    }
                }
            }
        }
    }

    private fun SafeClientEvent.getBlockSlot(): Int? {
        playerController.updateController()
        for (i in 0..8) {
            val itemStack = player.inventory.mainInventory[i]
            if (itemStack.isEmpty || itemStack.item !is ItemBlock) continue
            return i
        }
        return null
    }

}