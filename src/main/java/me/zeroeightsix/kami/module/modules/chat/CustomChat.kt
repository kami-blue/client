package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.managers.MessageManager.newMessageModifier
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.text.MessageDetectionHelper
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.formatValue
import org.kamiblue.event.listener.listener
import kotlin.math.min

@Module.Info(
    name = "CustomChat",
    category = Module.Category.CHAT,
    description = "Add a custom ending to your message!",
    showOnArray = Module.ShowOnArray.OFF,
    modulePriority = 200
)
object CustomChat : Module() {
    private val textMode = register(Settings.e<TextMode>("Message", TextMode.JAPANESE))
    private val decoMode = register(Settings.e<DecoMode>("Separator", DecoMode.NONE))
    private val commands = register(Settings.b("Commands", false))
    private val spammer = register(Settings.b("Spammer", false))
    val customText = register(Settings.s("CustomText", "unchanged"))

    private enum class DecoMode {
        SEPARATOR, CLASSIC, NONE
    }

    private enum class TextMode {
        NAME, ON_TOP, WEBSITE, JAPANESE, CUSTOM
    }

    val isCustomMode get() = textMode.value == TextMode.CUSTOM
    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.SECONDS)
    private val modifier = newMessageModifier(
        filter = {
            (commands.value || !MessageDetectionHelper.isCommand(it.packet.message))
                && (spammer.value || it.source !is Spammer)
        },
        modifier = {
            val message = it.packet.message + getFull()
            message.substring(0, min(256, message.length))
        }
    )

    override fun onEnable() {
        modifier.enable()
    }

    override fun onDisable() {
        modifier.disable()
    }

    private fun getText() = when (textMode.value) {
        TextMode.NAME -> "ᴋᴀᴍɪ ʙʟᴜᴇ"
        TextMode.ON_TOP -> "ᴋᴀᴍɪ ʙʟᴜᴇ ᴏɴ ᴛᴏᴘ"
        TextMode.WEBSITE -> "ｋａｍｉｂｌｕｅ．ｏｒｇ"
        TextMode.JAPANESE -> "上にカミブルー"
        TextMode.CUSTOM -> customText.value
        else -> ""
    }

    private fun getFull() = when (decoMode.value) {
        DecoMode.NONE -> " " + getText()
        DecoMode.CLASSIC -> " \u00ab " + getText() + " \u00bb"
        DecoMode.SEPARATOR -> " | " + getText()
        else -> ""
    }

    init {
        listener<SafeTickEvent> {
            if (timer.tick(5L) && textMode.value == TextMode.CUSTOM && customText.value.equals("unchanged", ignoreCase = true)) {
                MessageSendHelper.sendErrorMessage("$chatName In order to use the custom suffix, please run the " +
                    formatValue("${CommandManager.prefix}set CustomChat CustomText \"text here\"") +
                    " command to change it")
            }
        }
    }


}
