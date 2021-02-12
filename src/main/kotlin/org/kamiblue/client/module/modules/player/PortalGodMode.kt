package org.kamiblue.client.module.modules.player

import net.minecraft.network.play.client.CPacketConfirmTeleport
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.threads.runSafe
import org.kamiblue.event.listener.listener

internal object PortalGodMode : Module(
    name = "PortalGodMode",
    category = Category.PLAYER,
    description = "Don't take damage in portals"
) {
    private val instantTeleport by setting("Instant Teleport", true)

    private var packet: CPacketConfirmTeleport? = null

    init {
        onEnable {
            packet = null
        }

        onDisable {
            runSafe {
                if (instantTeleport) packet?.let {
                    connection.sendPacket(it)
                }
            }
        }

        listener<PacketEvent.Send> {
            if (it.packet !is CPacketConfirmTeleport) return@listener
            it.cancel()
            packet = it.packet
        }
    }
}