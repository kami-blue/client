package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.TimeUtils
import me.zeroeightsix.kami.util.color.EnumTextColor
import me.zeroeightsix.kami.util.text.format
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.client.event.ClientChatReceivedEvent
import org.kamiblue.event.listener.listener

@Module.Info(
    name = "ChatTimestamp",
    category = Module.Category.CHAT,
    description = "Shows the time a message was sent beside the message",
    showOnArray = false
)
object ChatTimestamp : Module() {
    private val color = setting("Color", EnumTextColor.GRAY)
    private val timeFormat = setting("TimeFormat", TimeUtils.TimeFormat.HHMM)
    private val timeUnit = setting("TimeUnit", TimeUtils.TimeUnit.H12)

    init {
        listener<ClientChatReceivedEvent> {
            if (mc.player == null) return@listener
            it.message = TextComponentString(formattedTime).appendSibling(it.message)
        }
    }

    val formattedTime: String
        get() = "<${color.value format TimeUtils.getTime(timeFormat.value, timeUnit.value)}>"
}