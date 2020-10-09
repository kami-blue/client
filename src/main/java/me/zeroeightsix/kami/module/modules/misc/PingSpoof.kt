package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.network.play.client.CPacketKeepAlive
import net.minecraft.network.play.server.SPacketKeepAlive
import java.util.*

@Module.Info(
        name = "PingSpoof",
        category = Module.Category.MISC,
        description = "Cancels or adds delay to your ping packets"
)
object PingSpoof : Module() {
    private val cancel = register(Settings.b("Cancel", false))// most servers will kick/time you out for this
    private val delay = register(Settings.integerBuilder("Delay").withValue(100).withRange(0, 2000).withStep(25).withVisibility { !cancel.value }.build())

    init {
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketKeepAlive || mc.player == null) return@listener
            it.cancel()
            if (!cancel.value) {
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        mc.connection?.sendPacket(CPacketKeepAlive(it.packet.id))
                    }
                }, delay.value.toLong())
            }
        }
    }
}
