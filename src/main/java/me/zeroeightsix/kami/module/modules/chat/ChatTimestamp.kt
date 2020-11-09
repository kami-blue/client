package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.TimeUtils
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.client.event.ClientChatReceivedEvent

@Module.Info(
        name = "ChatTimestamp",
        category = Module.Category.CHAT,
        description = "Shows the time a message was sent beside the message",
        showOnArray = false
)
object ChatTimestamp : Module() {

    init {
        listener<ClientChatReceivedEvent> {
            if (mc.player == null) return@listener
            val prefix = TextComponentString(formattedTime)
            it.message = prefix.appendSibling(it.message)
        }
    }

    val formattedTime: String
        get() = "<${TimeUtils.getTime()}>"
}