package org.kamiblue.client.module.modules.misc

import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import net.minecraft.network.play.client.CPacketKeepAlive
import net.minecraft.network.play.server.SPacketKeepAlive
import org.kamiblue.event.listener.listener
import java.util.*

internal object PingSpoof : Module(
    name = "PingSpoof",
    category = Category.MISC,
    description = "Cancels or adds delay to your ping packets"
) {
    private val cancel = setting("Cancel", false) // most servers will kick/time you out for this
    private val delay = setting("Delay", 100, 0..2000, 25, { !cancel.value })

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
