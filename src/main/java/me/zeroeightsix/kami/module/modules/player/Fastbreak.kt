package me.zeroeightsix.kami.module.modules.player

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zero.alpine.listener.EventHook
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.network.play.client.CPacketPlayerDigging

/**
 * @author 086
 * Updated by Xiaro on 24/06/2018.
 */
@Module.Info(
        name = "Fastbreak",
        category = Module.Category.PLAYER,
        description = "Breaks block faster"
)
class Fastbreak : Module() {
    private val mode = register(Settings.e<FastBreakMode>("Mode", FastBreakMode.HIT_DELAY))
    private val sneakTrigger = register(Settings.booleanBuilder("Sneak Trigger").withValue(true).withVisibility { mode.value == FastBreakMode.PACKET}.build())

    private enum class FastBreakMode {
        HIT_DELAY, PACKET
    }

    private var isPacketMining = false
    private var diggingPacket = CPacketPlayerDigging()

    @EventHandler
    private val sendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (event.packet !is CPacketPlayerDigging || mode.value != FastBreakMode.PACKET) return@EventHook
        val packet = event.packet as CPacketPlayerDigging

        if (packet.action == CPacketPlayerDigging.Action.START_DESTROY_BLOCK && !isPacketMining && ((sneakTrigger.value && mc.player.isSneaking) || !sneakTrigger.value)) {
            isPacketMining = true
            diggingPacket = packet
        } else if (packet.action == CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK && isPacketMining) { /* Cancels aborting packets */
            event.cancel()
        }
    })

    override fun onUpdate() {
        if (mode.value == FastBreakMode.HIT_DELAY) {
            mc.playerController.blockHitDelay = 0
        } else if (isPacketMining) {
            val stopDiggingPacket = CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, diggingPacket.position, diggingPacket.facing)
            mc.connection!!.sendPacket(stopDiggingPacket) /* Sends a stop digging packet so the blocks will actually be mined after the server side breaking animation */
            isPacketMining = false
        }
    }
}