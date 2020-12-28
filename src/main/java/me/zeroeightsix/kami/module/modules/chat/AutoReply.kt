package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.text.MessageDetection
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendServerMessage
import me.zeroeightsix.kami.util.text.formatValue
import net.minecraft.network.play.server.SPacketChat
import org.kamiblue.event.listener.listener

@Module.Info(
    name = "AutoReply",
    description = "Automatically reply to direct messages",
    category = Module.Category.CHAT
)
object AutoReply : Module() {
    private val customMessage = register(Settings.b("CustomMessage", false))
    val message = register(Settings.stringBuilder("CustomText").withValue("unchanged").withVisibility { customMessage.value })

    init {
        customMessage.settingListener = Setting.SettingListeners {
            if (customMessage.value == true && message.value == "unchanged") {
                MessageSendHelper.sendChatMessage("Use the " +
                    formatValue("${CommandManager.prefix}set AutoReply CustomText <text>") +
                    " command to change this"
                )
            }
        }

        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketChat || MessageDetection.Direct.RECEIVE detect it.packet.chatComponent.unformattedText) return@listener
            if (customMessage.value) {
                sendServerMessage("/r " + message.value)
            } else {
                sendServerMessage("/r I just automatically replied, thanks to KAMI Blue's AutoReply module!")
            }
        }
    }
}