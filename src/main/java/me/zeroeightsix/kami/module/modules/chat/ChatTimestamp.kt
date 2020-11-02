@file:Suppress("DEPRECATION")

package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.TimeUtils
import me.zeroeightsix.kami.util.color.ColorTextFormatting
import me.zeroeightsix.kami.util.color.ColorTextFormatting.ColorCode
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.client.event.ClientChatReceivedEvent

@Module.Info(
        name = "ChatTimestamp",
        category = Module.Category.CHAT,
        description = "Shows the time a message was sent beside the message",
        showOnArray = false
)
object ChatTimestamp : Module() {
    private val firstColor = setting("FirstColour", ColorCode.GRAY)
    private val secondColor = setting("SecondColour", ColorCode.GRAY)
    private val timeTypeSetting = setting("TimeFormat", TimeUtils.TimeType.HHMM)
    private val timeUnitSetting = setting("TimeUnit", TimeUtils.TimeUnit.H24)
    private val doLocale = setting("ShowAM/PM", true, { timeUnitSetting.value == TimeUtils.TimeUnit.H12 })

    init {
        listener<ClientChatReceivedEvent> {
            if (mc.player == null) return@listener
            val prefix = TextComponentString(formattedTime)
            it.message = prefix.appendSibling(it.message)
        }
    }

    val formattedTime: String
        get() = "<" + TimeUtils.getFinalTime(setToText(secondColor.value), setToText(firstColor.value), timeUnitSetting.value, timeTypeSetting.value, doLocale.value) + TextFormatting.RESET + "> "

    private fun setToText(colourCode: ColorCode): TextFormatting {
        return ColorTextFormatting.toTextMap[colourCode]!!
    }
}