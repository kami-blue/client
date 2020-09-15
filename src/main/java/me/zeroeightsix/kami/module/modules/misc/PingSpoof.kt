package me.zeroeightsix.kami.module.modules.misc

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.network.play.client.CPacketKeepAlive
import net.minecraft.network.play.server.SPacketKeepAlive
import java.util.*

@Module.Info(
        name = "PingSpoof",
        category = Module.Category.MISC,
        description = "Cancels or adds delay to your ping packets"
)
class PingSpoof : Module() {
    private val cancel = register(Settings.b("Cancel", false))// most servers will kick/time you out for this
    private val delay = register(Settings.integerBuilder("Delay").withValue(100).withRange(0, 2000).withVisibility { !cancel.value }.build())

    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (event.packet is SPacketKeepAlive) {
            event.cancel()
            if (!cancel.value) {
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        mc.connection.let { it!!.sendPacket(CPacketKeepAlive(event.packet.id)) }
                    }
                }, delay.value.toLong())
            }
        }
    })
}
