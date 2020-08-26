package me.zeroeightsix.kami.module.modules.player

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.network.play.client.CPacketPlayerDigging

/**
 * @author 086
 * Updated by Xiaro on 18/07/2020.
 */
@Module.Info(
        name = "Fastbreak",
        category = Module.Category.PLAYER,
        description = "Breaks block faster and nullifies the break delay"
)
class Fastbreak : Module() {
    private val packetMine = register(Settings.b("PacketMine", true))
    private val sneakTrigger = register(Settings.booleanBuilder("SneakTrigger").withValue(true).withVisibility { packetMine.value }.build())

    @EventHandler
    private val sendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (event.packet !is CPacketPlayerDigging || !packetMine.value || !((sneakTrigger.value && mc.player.isSneaking) || !sneakTrigger.value)) return@EventHook
        val packet = event.packet as CPacketPlayerDigging

        if (packet.action == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
            /* Spams stop digging packets so the blocks will actually be mined after the server side breaking animation */
            Thread(Runnable {
                val startTime = System.currentTimeMillis()
                while (!mc.world.isAirBlock(packet.position) && System.currentTimeMillis() - startTime < 10000L) { /* Stops running if the block is mined or it took too long */
                    mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, packet.position, packet.facing))
                    Thread.sleep(200L)
                }
            }).start()
        } else if (packet.action == CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) {
            event.cancel() /* Cancels aborting packets */
        }
    })

    override fun onUpdate() {
        mc.playerController.blockHitDelay = 0
    }
}