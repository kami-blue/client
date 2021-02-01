package me.zeroeightsix.kami.module.modules.player

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.mixin.extension.blockHitDelay
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraftforge.fml.common.gameevent.TickEvent

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

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (breakDelay != 5 && playerController.blockHitDelay == 5) playerController.blockHitDelay = breakDelay
        }

        safeListener<PacketEvent.Send> {
            if (it.packet !is CPacketPlayerDigging || !packetMine || sneakTrigger && !player.isSneaking) return@safeListener

            when (it.packet.action) {
                CPacketPlayerDigging.Action.START_DESTROY_BLOCK -> {
                    spamPackets(it.packet)
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

    private fun SafeClientEvent.spamPackets(packet: CPacketPlayerDigging) {
        /* Spams stop digging packets so the blocks will actually be mined after the server side breaking animation */
        defaultScope.launch {
            val startTime = System.currentTimeMillis()
            /* Stops running if the block is mined or it took too long */
            while (!world.isAirBlock(packet.position) && System.currentTimeMillis() - startTime < 10000L) {
                if (morePackets) {
                    connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, packet.position, packet.facing))
                }
                connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, packet.position, packet.facing))
                delay(spamDelay * 50L)
            }
        }
    }
}