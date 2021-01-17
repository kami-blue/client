package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.TimeUtils
import me.zeroeightsix.kami.util.color.EnumTextColor
import me.zeroeightsix.kami.util.text.format
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.client.event.ClientChatReceivedEvent
import org.kamiblue.commons.interfaces.DisplayEnum
import org.kamiblue.event.listener.listener

internal object ChatTimestamp : Module(
    name = "ChatTimestamp",
    category = Category.CHAT,
    description = "Shows the time a message was sent beside the message",
    showOnArray = false
) {
    private val color by setting("Color", EnumTextColor.GRAY)
    private val separator by setting("Separator", Separator.ARROWS)
    private val timeFormat by setting("TimeFormat", TimeUtils.TimeFormat.HHMM)
    private val timeUnit by setting("TimeUnit", TimeUtils.TimeUnit.H12)

    init {
        listener<ClientChatReceivedEvent> {
            if (mc.player == null) return@listener
            it.message = TextComponentString(formattedTime).appendSibling(it.message)
        }
    }

    val formattedTime: String
        get() = "${separator.left}${color format TimeUtils.getTime(timeFormat, timeUnit)}${separator.right} "

    val time: String
        get() = "${separator.left}${TimeUtils.getTime(timeFormat, timeUnit)}${separator.right}"

    @Suppress("unused")
    private enum class Separator(override val displayName: String, val left: String, val right: String) : DisplayEnum {
        ARROWS("< >", "<", ">"),
        SQUARE_BRACKETS("[ ]", "[", "]"),
        CURLY_BRACKETS("{ }", "{", "}"),
        ROUND_BRACKETS("( )", "(", ")"),
        NONE("None", "", "")
    }
}