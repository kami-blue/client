package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.network.play.server.SPacketDisconnect
import net.minecraft.network.play.server.SPacketRespawn
import net.minecraft.util.text.TextComponentString
import org.kamiblue.event.listener.listener

object EndTeleport : Module(
    name = "EndTeleport",
    category = Category.PLAYER,
    description = "Allows for teleportation when going through end portals"
) {
    private val confirmed = setting("Confirm", false)

    override fun onEnable() {
        if (mc.getCurrentServerData() == null) {
            MessageSendHelper.sendWarningMessage("$chatName This module does not work in singleplayer")
            disable()
        } else if (!confirmed.value) {
            MessageSendHelper.sendWarningMessage("$chatName This module will kick you from the server! It is part of the exploit and cannot be avoided")
        }
    }

    init {
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketRespawn) return@listener
            if (it.packet.dimensionID == 1 && confirmed.value) {
                mc.connection!!.handleDisconnect(SPacketDisconnect(TextComponentString("Attempting teleportation exploit")))
                disable()
            }
        }
    }
}