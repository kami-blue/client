package me.zeroeightsix.kami.module.modules.player

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.event.Phase
import me.zeroeightsix.kami.event.events.OnUpdateWalkingPlayerEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.mixin.client.entity.MixinEntity
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.EntityUtils.prevPosVector
import me.zeroeightsix.kami.util.WorldUtils.placeBlock
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.math.Vec2f
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.onMainThreadSafe
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
@Module.Info(
    name = "Scaffold",
    category = Module.Category.PLAYER,
    description = "Places blocks under you",
    modulePriority = 500
)
object Scaffold : Module() {
    private val tower = register(Settings.b("Tower", true))
    private val spoofHotbar = register(Settings.b("SpoofHotbar", true))
    val safeWalk = register(Settings.b("SafeWalk", true))
    private val sneak = register(Settings.b("Sneak", true))
    private val delay = register(Settings.integerBuilder("Delay").withValue(2).withRange(1, 10).withStep(1))
    private val maxRange = register(Settings.integerBuilder("MaxRange").withValue(1).withRange(0, 3).withStep(1))

    private var lastRotation = Vec2f.ZERO
    private var placeInfo: Pair<EnumFacing, BlockPos>? = null
    private var inactiveTicks = 69

    private val placeTimer = TickTimer(TimeUnit.TICKS)
    private val rubberBandTimer = TickTimer(TimeUnit.TICKS)

    override fun isActive(): Boolean {
        return isEnabled && inactiveTicks <= 5
    }

    override fun onDisable() {
        placeInfo = null
        inactiveTicks = 69
    }

    init {
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketPlayerPosLook) return@listener
            rubberBandTimer.reset()
        }

        listener<PlayerTravelEvent> {
            if (mc.player == null || !tower.value || !mc.gameSettings.keyBindJump.isKeyDown || inactiveTicks > 5 || !isHoldingBlock) return@listener
            if (rubberBandTimer.tick(10, false)) {
                if (shouldTower) mc.player.motionY = 0.41999998688697815
            } else if (mc.player.fallDistance <= 2.0f) {
                mc.player.motionY = -0.169
            }
        }
    }

    private val isHoldingBlock: Boolean
        get() = PlayerPacketManager.getHoldingItemStack().item is ItemBlock

    private val shouldTower: Boolean
        get() = !mc.player.onGround
            && mc.player.posY - floor(mc.player.posY) <= 0.1
    init {
        listener<OnUpdateWalkingPlayerEvent> { event ->
            if (mc.world == null || mc.player == null || event.phase != Phase.PRE) return@listener
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
                PlayerPacketManager.addPacket(this, packet)
            }
        }
    }

    private fun calcNextPos(): BlockPos? {
        val posVec = mc.player.positionVector
        val blockPos = posVec.toBlockPos()
        return checkPos(blockPos)
            ?: run {
                val realMotion = posVec.subtract(mc.player.prevPosVector)
                val nextPos = blockPos.add(roundToRange(realMotion.x), 0, roundToRange(realMotion.z))
                checkPos(nextPos)
            }
    }

    private fun checkPos(blockPos: BlockPos): BlockPos? {
        val center = Vec3d(blockPos.x + 0.5, blockPos.y.toDouble(), blockPos.z + 0.5)
        val rayTraceResult = mc.world.rayTraceBlocks(
            center,
            center.subtract(0.0, 0.5, 0.0),
            false,
            true,
            false
        )
        return blockPos.down().takeIf { rayTraceResult?.typeOfHit != RayTraceResult.Type.BLOCK }
    }

    private fun roundToRange(value: Double) =
        (value * 2.5 * maxRange.value).roundToInt().coerceAtMost(maxRange.value)

    private fun swapAndPlace(pos: BlockPos, side: EnumFacing) {
        getBlockSlot()?.let { slot ->
            if (spoofHotbar.value) PlayerPacketManager.spoofHotbar(slot)
            else InventoryUtils.swapSlot(slot)

            inactiveTicks = 0

            if (placeTimer.tick(delay.value.toLong())) {
                val shouldSneak = sneak.value && !mc.player.isSneaking
                defaultScope.launch {
                    if (shouldSneak) {
                        mc.player?.let {
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

    private fun getBlockSlot(): Int? {
        mc.playerController.updateController()
        for (i in 0..8) {
            val itemStack = mc.player.inventory.mainInventory[i]
            if (itemStack.isEmpty || itemStack.item !is ItemBlock) continue
            return i
        }
        return null
    }

}