package org.kamiblue.client.module.modules.player

import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.mixin.extension.blockHitDelay
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.threads.safeListener
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*
import kotlin.collections.HashMap

internal object FastBreak : Module(
    name = "FastBreak",
    category = Category.PLAYER,
    description = "Breaks block faster and nullifies the break delay"
) {
    private val breakDelay by setting("Break Delay", 0, 0..5, 1)
    private val packetMine by setting("Packet Mine", true)
    private val sneakTrigger by setting("Sneak Trigger", true, { packetMine })
    private val morePackets by setting("More Packets", false, { packetMine })
    private val spamDelay by setting("Spam Delay", 4, 1..20, 1, { packetMine })

    private val spamTimer = TickTimer(TimeUnit.TICKS)
    private val miningBlocks = HashMap<BlockPos, Pair<Long, EnumFacing>>() // <Position, <StartTime, Facing>>

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@safeListener

            if (breakDelay != 5 && playerController.blockHitDelay == 5) {
                playerController.blockHitDelay = breakDelay
            }

            if (spamTimer.tick(spamDelay.toLong())) {
                miningBlocks.entries.removeIf { (pos, pair) ->
                    val (startTime, facing) = pair

                    if (world.isAirBlock(pos) || System.currentTimeMillis() - startTime > 10000L) {
                        true
                    } else {
                        if (morePackets) {
                            connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, facing))
                        }
                        connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, facing))
                        false
                    }
                }
            }
        }

        safeListener<PacketEvent.Send> {
            if (it.packet !is CPacketPlayerDigging || !packetMine || sneakTrigger && !player.isSneaking) return@safeListener

            when (it.packet.action) {
                CPacketPlayerDigging.Action.START_DESTROY_BLOCK -> {
                    if (!miningBlocks.containsKey(it.packet.position)) {
                        miningBlocks[it.packet.position] = System.currentTimeMillis() to it.packet.facing
                    }
                }
                CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK -> {
                    it.cancel()
                }
                else -> {
                    // Ignored
                }
            }
        }
    }
}