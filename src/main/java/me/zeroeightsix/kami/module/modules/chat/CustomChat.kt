package me.zeroeightsix.kami.module.modules.chat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MessageSendHelper
import net.minecraft.network.play.client.CPacketChatMessage

/**
 * Created by 086 on 8/04/2018.
 * Updated by dominikaaaa on 12/03/20
 * Updated by Humboldt123 on 16/7/20
 */
@Module.Info(
        name = "CustomChat",
        category = Module.Category.CHAT,
        description = "Add a custom ending to your message!",
        showOnArray = Module.ShowOnArray.OFF
)
class CustomChat : Module() {
    @JvmField
    var textMode: Setting<TextMode> = register(Settings.e("Message", TextMode.JAPANESE))
    private val decoMode = register(Settings.e<DecoMode>("Separator", DecoMode.NONE))
    private val commands = register(Settings.b("Commands", false))
    @JvmField
    var customText: Setting<String> = register(Settings.s("CustomText", "unchanged"))

    private enum class DecoMode {
        SEPARATOR, CLASSIC, NONE
    }

    enum class TextMode {
        NAME, ON_TOP, WEBSITE, JAPANESE, CUSTOM
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

    @EventHandler
    private val listener = Listener(EventHook { event: PacketEvent.Send ->
        if (event.packet is CPacketChatMessage) {
            var s = (event.packet as CPacketChatMessage).getMessage()
            if (!commands.value && isCommand(s)) return@EventHook
            s += getFull(decoMode.value)

            if (s.length >= 256) s = s.substring(0, 256)
            (event.packet as CPacketChatMessage).message = s
        }
    })

    private fun isCommand(s: String): Boolean {
        for (value in cmdCheck) {
            if (s.startsWith(value)) return true
        }
        return false
    }

    override fun onUpdate() {
        if (startTime == 0L) startTime = System.currentTimeMillis()
        if (startTime + 5000 <= System.currentTimeMillis()) { // 5 seconds in milliseconds
            if (textMode.value == TextMode.CUSTOM && customText.value.equals("unchanged", ignoreCase = true) && mc.player != null) {
                MessageSendHelper.sendWarningMessage("$chatName Warning: In order to use the custom " + name + ", please run the &7" + Command.getCommandPrefix() + "customchat&r command to change it")
            }
            startTime = System.currentTimeMillis()
        }
    }

    companion object {
        @JvmField
        var cmdCheck = arrayOf("/", ",", ".", "-", ";", "?", "*", "^", "&", "%", "#", "$", Command.getCommandPrefix(), ChatEncryption.delimiterValue.value)
        private var startTime: Long = 0
    }
}
