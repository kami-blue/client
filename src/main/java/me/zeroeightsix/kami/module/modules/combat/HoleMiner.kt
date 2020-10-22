package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.mangers.CombatManager
import me.zeroeightsix.kami.manager.mangers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.combat.CrystalUtils
import me.zeroeightsix.kami.util.combat.SurroundUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.math.Vec2f
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.math.VectorUtils.toVec3d
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import kotlin.math.pow

@CombatManager.CombatModule
@Module.Info(
        name = "HoleMiner",
        category = Module.Category.COMBAT,
        description = "Mines your opponent's hole",
        modulePriority = 100
)
object HoleMiner : Module() {
    private val range = register(Settings.floatBuilder("Range").withValue(5.0f).withRange(0.0f, 10.0f))

    private var miningPos: BlockPos? = null
    private var start = true

    override fun getHudInfo() = "${CombatManager.target?.name}"

    override fun onDisable() {
        miningPos = null
        start = true
    }

    override fun onEnable() {
        if (mc.player == null) {
            disable()
            return
        }
        val target = CombatManager.target
        if (target != null) {
            if (SurroundUtils.checkHole(target) != SurroundUtils.HoleType.OBBY) {
                MessageSendHelper.sendChatMessage("$chatName Target is not in a valid hole, disabling")
                disable()
            } else {
                miningPos = findHoleBlock(target)
            }
        } else {
            MessageSendHelper.sendChatMessage("$chatName No target found, disabling")
            disable()
        }
    }

    init {
        listener<SafeTickEvent> {
            if (!CombatManager.isOnTopPriority(this)) return@listener
            if (mc.player.heldItemMainhand.getItem() != Items.DIAMOND_PICKAXE) {
                val slot = InventoryUtils.getSlotsHotbar(278)?.get(0)
                if (slot == null) {
                    MessageSendHelper.sendChatMessage("$chatName No pickaxe found, disabling")
                    disable()
                    return@listener
                } else {
                    InventoryUtils.swapSlot(slot)
                }
            }
            val pos = miningPos
            if (pos == null) {
                MessageSendHelper.sendChatMessage("$chatName No hole block to mine, disabling")
                disable()
            } else if (mc.player.ticksExisted % 2 == 0) {
                if (mc.world.isAirBlock(pos)) {
                    MessageSendHelper.sendChatMessage("$chatName Done mining")
                    disable()
                    return@listener
                }
                val action = if (start) CPacketPlayerDigging.Action.START_DESTROY_BLOCK else CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK
                val rotation = Vec2f(RotationUtils.getRotationTo(pos.toVec3d(), true))
                val diff = mc.player.getPositionEyes(1f).subtract(pos.toVec3d())
                val normalizedVec = diff.scale(1.0 / diff.length())
                val facing = EnumFacing.getFacingFromVector(normalizedVec.x.toFloat(), normalizedVec.y.toFloat(), normalizedVec.z.toFloat())
                PlayerPacketManager.addPacket(this, PlayerPacketManager.PlayerPacket(rotating = true, rotation = rotation))
                mc.connection!!.sendPacket(CPacketPlayerDigging(action, pos, facing))
                mc.player.swingArm(EnumHand.MAIN_HAND)
                start = false
            }
        }
    }

    private fun findHoleBlock(entity: Entity): BlockPos? {
        val pos = entity.positionVector.toBlockPos()
        var closestPos = 114.514 to BlockPos.ORIGIN
        for (facing in EnumFacing.HORIZONTALS) {
            val offsetPos = pos.offset(facing)
            val dist = mc.player.getDistanceSqToCenter(offsetPos)
            if (dist > range.value.pow(2) || dist > closestPos.first) continue
            if (mc.world.getBlockState(offsetPos).block == Blocks.BEDROCK) continue
            if (!checkPos(offsetPos, facing)) continue
            closestPos = dist to offsetPos
        }
        return if (closestPos.second != BlockPos.ORIGIN) closestPos.second else null
    }

    private fun checkPos(pos: BlockPos, facingIn: EnumFacing): Boolean {
        if (CrystalUtils.canPlaceOn(pos.down()) && mc.world.isAirBlock(pos.up())) return true
        for (facing in EnumFacing.HORIZONTALS) {
            if (facing == facingIn.opposite) continue
            if (!CrystalUtils.canPlace(pos.offset(facing))) continue
            return true
        }
        return false
    }
}