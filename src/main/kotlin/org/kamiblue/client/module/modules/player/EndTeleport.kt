package org.kamiblue.client.module.modules.player

import net.minecraft.network.play.server.SPacketDisconnect
import net.minecraft.network.play.server.SPacketRespawn
import net.minecraft.util.text.TextComponentString
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.safeListener

internal object EndTeleport : Module(
    name = "EndTeleport",
    category = Category.PLAYER,
    description = "Allows for teleportation when going through end portals"
) {
    private val confirmed by setting("Confirm", false)

    init {
        onEnable {
            if (mc.currentServerData == null) {
                MessageSendHelper.sendWarningMessage("$chatName This module does not work in singleplayer")
                disable()
            } else if (!confirmed) {
                MessageSendHelper.sendWarningMessage("$chatName This module will kick you from the server! It is part of the exploit and cannot be avoided")
            }
        }

        safeListener<PacketEvent.Receive> {
            if (it.packet !is SPacketRespawn) return@safeListener
            if (it.packet.dimensionID == 1 && confirmed) {
                connection.handleDisconnect(SPacketDisconnect(TextComponentString("Attempting teleportation exploit")))
                disable()
            }
        }
    }
}