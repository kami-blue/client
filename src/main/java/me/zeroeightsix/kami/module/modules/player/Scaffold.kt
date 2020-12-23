package me.zeroeightsix.kami.module.modules.player

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.event.KamiEvent
import me.zeroeightsix.kami.event.events.OnUpdateWalkingPlayerEvent
import me.zeroeightsix.kami.event.events.PlayerTravelEvent
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.mixin.client.entity.MixinEntity
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.BlockUtils.placeBlock
import me.zeroeightsix.kami.util.EntityUtils.prevPosVector
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.math.Vec2f
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import net.minecraft.item.ItemBlock
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
    private val delay = register(Settings.integerBuilder("Delay").withValue(2).withRange(1, 10).withStep(1))
    private val maxRange = register(Settings.integerBuilder("MaxRange").withValue(1).withRange(0, 3).withStep(1))

    private var lastRotation = Vec2f.ZERO
    private var placeInfo: Pair<EnumFacing, BlockPos>? = null
    private val placeTimer = TimerUtils.TickTimer(TimerUtils.TimeUnit.TICKS)
    private var inactiveTicks = 69

    override fun isActive(): Boolean {
        return isEnabled && inactiveTicks <= 5
    }

    override fun onDisable() {
        placeInfo = null
        inactiveTicks = 69
    }

    init {
        listener<PlayerTravelEvent> {
            if (mc.player == null || !tower.value || !mc.gameSettings.keyBindJump.isKeyDown) return@listener
            val flooredY = floor(mc.player.posY)
            if (!mc.player.onGround && mc.player.posY - flooredY <= 0.002) {
                mc.player.motionY = 0.41955
            }
        }

        listener<OnUpdateWalkingPlayerEvent> { event ->
            if (mc.world == null || mc.player == null || event.era != KamiEvent.Era.PRE) return@listener
            inactiveTicks++
            placeInfo = calcNextPos()?.let {
                BlockUtils.getNeighbour(it, 1, sides = arrayOf(EnumFacing.DOWN))
                    ?: BlockUtils.getNeighbour(it, 3, sides = EnumFacing.HORIZONTALS)
            }

            placeInfo?.let {
                val hitVec = BlockUtils.getHitVec(it.second, it.first)
                lastRotation = Vec2f(RotationUtils.getRotationTo(hitVec, true))
                swapAndPlace(it.second, it.first)
            }

            if (inactiveTicks > 5) {
                PlayerPacketManager.resetHotbar()
            } else if (PlayerPacketManager.getHoldingItemStack().item is ItemBlock) {
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
                moduleScope.launch {
                    delay(40)
                    onMainThreadSafe {
                        placeBlock(pos, side)
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