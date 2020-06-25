package me.zeroeightsix.kami.module.modules.player

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zero.alpine.listener.EventHook
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.network.play.client.CPacketPlayerDigging
import me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage

/**
 * @author 086
 * Updated by Xiaro on 24/06/2020.
 */
@Module.Info(
        name = "Fastbreak",
        category = Module.Category.PLAYER,
        description = "Breaks block faster"
)
class Fastbreak : Module() {
    private val packetMine = register(Settings.b("Packet Mine", false))
    private val sneakTrigger = register(Settings.booleanBuilder("Sneak Trigger").withValue(true).withVisibility {packetMine.value}.build())

    private var diggingPacket = CPacketPlayerDigging()

    @EventHandler
    private val sendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (event.packet !is CPacketPlayerDigging || !packetMine.value) return@EventHook
        val packet = event.packet as CPacketPlayerDigging

        if (packet.action == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
            diggingPacket = packet
        } else if (packet.action == CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK && packet.position == diggingPacket.position && ((sneakTrigger.value && mc.player.isSneaking) || !sneakTrigger.value)) {
            val stopDiggingPacket = CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, packet.position, packet.facing)
            event.cancel() /* Cancels aborting packets */
            mc.connection!!.sendPacket(stopDiggingPacket) /* Sends a stop digging packet so the blocks will actually be mined after the server side breaking animation */
        }
    })

    override fun onUpdate() {
            mc.playerController.blockHitDelay = 0
    }
}