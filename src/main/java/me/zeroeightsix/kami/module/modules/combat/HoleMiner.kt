package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.combat.CrystalUtils.canPlace
import me.zeroeightsix.kami.util.combat.CrystalUtils.canPlaceOn
import me.zeroeightsix.kami.util.combat.SurroundUtils
import me.zeroeightsix.kami.util.items.swapToItem
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.math.VectorUtils.distanceTo
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.math.VectorUtils.toVec3dCenter
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.runSafeR
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent

@CombatManager.CombatModule
object HoleMiner : Module(
    name = "HoleMiner",
    category = Category.COMBAT,
    description = "Mines your opponent's hole",
    modulePriority = 100
) {
    private val range by setting("Range", 5.0f, 1.0f..8.0f, 0.25f)

    private var miningPos: BlockPos? = null
    private var start = true

    override fun getHudInfo() = CombatManager.target?.name ?: ""

    init {
        onEnable {
            runSafeR {
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
            } ?: disable()
        }

        onDisable {
            miningPos = null
            start = true
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (!CombatManager.isOnTopPriority(HoleMiner)) return@safeListener

            if (player.heldItemMainhand.item != Items.DIAMOND_PICKAXE) {
                if (!swapToItem(Items.DIAMOND_PICKAXE)) {
                    MessageSendHelper.sendChatMessage("$chatName No pickaxe found, disabling")
                    disable()
                    return@safeListener
                }
            }

            val pos = miningPos

            if (pos == null) {
                MessageSendHelper.sendChatMessage("$chatName No hole block to mine, disabling")
                disable()
            } else if (player.ticksExisted % 2 == 0) {
                if (world.isAirBlock(pos)) {
                    MessageSendHelper.sendChatMessage("$chatName Done mining")
                    disable()
                    return@safeListener
                }

                val action = if (start) CPacketPlayerDigging.Action.START_DESTROY_BLOCK else CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK
                val rotation = RotationUtils.getRotationTo(pos.toVec3dCenter())
                val diff = player.getPositionEyes(1f).subtract(pos.toVec3dCenter())
                val normalizedVec = diff.scale(1.0 / diff.length())
                val facing = EnumFacing.getFacingFromVector(normalizedVec.x.toFloat(), normalizedVec.y.toFloat(), normalizedVec.z.toFloat())

                PlayerPacketManager.addPacket(HoleMiner, PlayerPacketManager.PlayerPacket(rotating = true, rotation = rotation))
                connection.sendPacket(CPacketPlayerDigging(action, pos, facing))
                player.swingArm(EnumHand.MAIN_HAND)
                start = false
            }
        }
    }

    private fun SafeClientEvent.findHoleBlock(entity: Entity): BlockPos? {
        val pos = entity.positionVector.toBlockPos()
        var closestPos = 114.514 to BlockPos.ORIGIN

        for (facing in EnumFacing.HORIZONTALS) {
            val offsetPos = pos.offset(facing)
            val dist = player.distanceTo(offsetPos)
            if (dist > range || dist > closestPos.first) continue
            if (world.getBlockState(offsetPos).block == Blocks.BEDROCK) continue
            if (!checkPos(offsetPos, facing)) continue
            closestPos = dist to offsetPos
        }

        return if (closestPos.second != BlockPos.ORIGIN) closestPos.second else null
    }

    private fun SafeClientEvent.checkPos(pos: BlockPos, facingIn: EnumFacing): Boolean {
        if (canPlaceOn(pos.down()) && world.isAirBlock(pos.up())) return true

        for (facing in EnumFacing.HORIZONTALS) {
            if (facing == facingIn.opposite) continue
            if (!canPlace(pos.offset(facing))) continue
            return true
        }

        return false
    }
}