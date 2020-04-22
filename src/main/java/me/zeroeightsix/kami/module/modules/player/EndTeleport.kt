package me.zeroeightsix.kami.module.modules.player

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent.Receive
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MessageSendHelper
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.network.play.server.SPacketDisconnect
import net.minecraft.network.play.server.SPacketRespawn
import net.minecraft.util.text.TextComponentString
import java.util.*

/**
 * Created by 0x2E | PretendingToCode
 */
@Module.Info(
        name = "EndTeleport",
        category = Module.Category.PLAYER,
        description = "Allows for teleportation when going through end portals"
)
class EndTeleport : Module() {
    private val confirmed = register(Settings.b("Confirm", true))
    public override fun onEnable() {
        if (Wrapper.getMinecraft().getCurrentServerData() == null) {
            MessageSendHelper.sendWarningMessage(chatName + "This module does not work in singleplayer")
            disable()
        } else if (!confirmed.value) {
            MessageSendHelper.sendWarningMessage(chatName + "This module will kick you from the server! It is part of the exploit and cannot be avoided")
        }
    }

    @EventHandler
    var receiveListener = Listener(EventHook { event: Receive ->
        if (event.packet is SPacketRespawn) {
            if ((event.packet as SPacketRespawn).dimensionID == 1 && confirmed.value) {
                Objects.requireNonNull(Wrapper.getMinecraft().connection)!!.handleDisconnect(SPacketDisconnect(TextComponentString("Attempting teleportation exploit")))
                disable()
            }
        }
    })
}