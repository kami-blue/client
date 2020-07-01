package me.zeroeightsix.kami.module.modules.chat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.MessageDetectionHelper
import me.zeroeightsix.kami.util.MessageSendHelper
import net.minecraft.network.play.server.SPacketChat

/*
 * @author dominikaaaa
 * Updated by dominikaaaa on 07/05/20
 */
@Module.Info(
        name = "AutoTPA",
        description = "Automatically accept or decline /TPAs",
        category = Module.Category.CHAT
)
class AutoTPA : Module() {
    private val friends = register(Settings.b("AlwaysAcceptFriends", true))
    private val mode = register(Settings.e<Mode>("Response", Mode.DENY))

    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (event.packet is SPacketChat && MessageDetectionHelper.isTPA(true, (event.packet as SPacketChat).getChatComponent().unformattedText)) {
            /* I tested that getting the first word is compatible with chat timestamp, and it as, as this is Receive and chat timestamp is after Receive */
            val name = (event.packet as SPacketChat).getChatComponent().unformattedText.split(" ").toTypedArray()[0]

            when (mode.value) {
                Mode.ACCEPT -> MessageSendHelper.sendServerMessage("/tpaccept $name")
                Mode.DENY -> {
                    if (friends.value && Friends.isFriend(name)) {
                        MessageSendHelper.sendServerMessage("/tpaccept $name")
                    } else {
                        MessageSendHelper.sendServerMessage("/tpdeny $name")
                    }
                }
            }
        }
    })

    enum class Mode {
        ACCEPT, DENY
    }
}