package me.zeroeightsix.kami.module.modules.chat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.ColourTextFormatting
import me.zeroeightsix.kami.util.ColourTextFormatting.ColourCode
import me.zeroeightsix.kami.util.TimeUtil
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.client.event.ClientChatReceivedEvent

/**
 * @author dominikaaaa
 * Updated by dominikaaaa on 19/04/20
 */
@Module.Info(
        name = "ChatTimestamp",
        category = Module.Category.CHAT,
        description = "Shows the time a message was sent beside the message",
        showOnArray = Module.ShowOnArray.OFF
)
class ChatTimestamp : Module() {
    private val firstColour = register(Settings.e<ColourCode>("FirstColour", ColourCode.GRAY))
    private val secondColour = register(Settings.e<ColourCode>("SecondColour", ColourCode.GRAY))
    private val timeTypeSetting = register(Settings.e<TimeUtil.TimeType>("TimeFormat", TimeUtil.TimeType.HHMM))
    private val timeUnitSetting = register(Settings.e<TimeUtil.TimeUnit>("TimeUnit", TimeUtil.TimeUnit.H24))
    private val doLocale = register(Settings.booleanBuilder("ShowAM/PM").withValue(true).withVisibility { timeUnitSetting.value == TimeUtil.TimeUnit.H12 }.build())

    @EventHandler
    private val listener = Listener(EventHook { event: ClientChatReceivedEvent ->
        if (mc.player == null) return@EventHook
        val prefix = TextComponentString(
                formattedTime
        )
        event.message = prefix.appendSibling(event.message)
    })

    val formattedTime: String
        get() = "<" + TimeUtil.getFinalTime(setToText(secondColour.value), setToText(firstColour.value), timeUnitSetting.value, timeTypeSetting.value, doLocale.value) + TextFormatting.RESET + "> "

    private fun setToText(colourCode: ColourCode): TextFormatting? {
        return ColourTextFormatting.toTextMap[colourCode]
    }
}