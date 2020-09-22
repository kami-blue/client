package me.zeroeightsix.kami.module.modules.chat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimeUtils
import me.zeroeightsix.kami.util.color.ColorTextFormatting
import me.zeroeightsix.kami.util.color.ColorTextFormatting.ColourCode
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.client.event.ClientChatReceivedEvent


@Module.Info(
        name = "ChatTimestamp",
        category = Module.Category.CHAT,
        description = "Shows the time a message was sent beside the message",
        showOnArray = Module.ShowOnArray.OFF
)
object ChatTimestamp : Module() {
    private val firstColour = register(Settings.e<ColourCode>("FirstColour", ColourCode.GRAY))
    private val secondColour = register(Settings.e<ColourCode>("SecondColour", ColourCode.GRAY))
    private val timeTypeSetting = register(Settings.e<TimeUtils.TimeType>("TimeFormat", TimeUtils.TimeType.HHMM))
    private val timeUnitSetting = register(Settings.e<TimeUtils.TimeUnit>("TimeUnit", TimeUtils.TimeUnit.H24))
    private val doLocale = register(Settings.booleanBuilder("ShowAM/PM").withValue(true).withVisibility { timeUnitSetting.value == TimeUtils.TimeUnit.H12 }.build())

    @EventHandler
    private val listener = Listener(EventHook { event: ClientChatReceivedEvent ->
        if (mc.player == null) return@EventHook
        val prefix = TextComponentString(
                formattedTime
        )
        event.message = prefix.appendSibling(event.message)
    })

    val formattedTime: String
        get() = "<" + TimeUtils.getFinalTime(setToText(secondColour.value), setToText(firstColour.value), timeUnitSetting.value, timeTypeSetting.value, doLocale.value) + TextFormatting.RESET + "> "

    private fun setToText(colourCode: ColourCode): TextFormatting {
        return ColorTextFormatting.toTextMap[colourCode]!!
    }
}