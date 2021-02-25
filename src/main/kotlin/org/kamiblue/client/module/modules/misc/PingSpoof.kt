package org.kamiblue.client.module.modules.misc

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.network.play.client.CPacketKeepAlive
import net.minecraft.network.play.server.SPacketKeepAlive
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.threads.defaultScope
import org.kamiblue.client.util.threads.onMainThreadSafe
import org.kamiblue.event.listener.listener

internal object PingSpoof : Module(
    name = "PingSpoof",
    category = Category.MISC,
    description = "Cancels or adds delay to your ping packets"
) {
    private val delay = setting("Delay", 100, 0..2000, 25)

    init {
        listener<PacketEvent.Receive> {
            if (it.packet is SPacketKeepAlive) {
                it.cancel()
                defaultScope.launch {
                    delay(delay.value.toLong())
                    onMainThreadSafe {
                        connection.sendPacket(CPacketKeepAlive(it.packet.id))
                    }
                }
            }
        }
    }
}
