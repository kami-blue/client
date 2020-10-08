package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.network.play.client.CPacketChatMessage


@Module.Info(
        name = "CustomChat",
        category = Module.Category.CHAT,
        description = "Add a custom ending to your message!",
        showOnArray = Module.ShowOnArray.OFF
)
object CustomChat : Module() {
    val textMode: Setting<TextMode> = register(Settings.e("Message", TextMode.JAPANESE))
    private val decoMode = register(Settings.e<DecoMode>("Separator", DecoMode.NONE))
    private val commands = register(Settings.b("Commands", false))
    val customText: Setting<String> = register(Settings.s("CustomText", "unchanged"))

    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.SECONDS)

    private enum class DecoMode {
        SEPARATOR, CLASSIC, NONE
    }

    enum class TextMode {
        NAME, ON_TOP, WEBSITE, JAPANESE, CUSTOM
    }

    init {
        listener<PacketEvent.Send> {
            if (mc.player == null || it.packet !is CPacketChatMessage) return@listener
            var s = it.packet.getMessage()
            if (!commands.value && isCommand(s)) return@listener
            s += getFull(decoMode.value)

            if (s.length >= 256) s = s.substring(0, 256)
            it.packet.message = s
        }
    }

    override fun onUpdate(event: SafeTickEvent) {
        if (timer.tick(5L) && textMode.value == TextMode.CUSTOM && customText.value.equals("unchanged", ignoreCase = true)) {
            MessageSendHelper.sendWarningMessage("$chatName Warning: In order to use the custom " + name + ", please run the &7" + Command.getCommandPrefix() + "customchat&r command to change it")
        }
    }

    private fun getText(t: TextMode): String {
        return when (t) {
            TextMode.NAME -> "\u1d0b\u1d00\u1d0d\u026a \u0299\u029f\u1d1c\u1d07"
            TextMode.ON_TOP -> "\u1d0b\u1d00\u1d0d\u026a \u0299\u029f\u1d1c\u1d07 \u1d0f\u0274 \u1d1b\u1d0f\u1d18"
            TextMode.WEBSITE -> "\uff4b\uff41\uff4d\uff49\uff42\uff4c\uff55\uff45\uff0e\uff4f\uff52\uff47"
            TextMode.JAPANESE -> "\u4e0a\u306b\u30ab\u30df\u30d6\u30eb\u30fc"
            TextMode.CUSTOM -> customText.value
        }
    }

    private fun getFull(d: DecoMode): String {
        return when (d) {
            DecoMode.NONE -> " " + getText(textMode.value)
            DecoMode.CLASSIC -> " \u00ab " + getText(textMode.value) + " \u00bb"
            DecoMode.SEPARATOR -> " " + KamiMod.separator + " " + getText(textMode.value)
        }
    }

    private fun isCommand(s: String): Boolean {
        for (value in cmdCheck) {
            if (s.startsWith(value)) return true
        }
        return false
    }

    val cmdCheck: Array<String>
        get() = arrayOf("/", ",", ".", "-", ";", "?", "*", "^", "&", "%", "#", "$",
                Command.getCommandPrefix(),
                ChatEncryption.delimiterValue.value)

}
