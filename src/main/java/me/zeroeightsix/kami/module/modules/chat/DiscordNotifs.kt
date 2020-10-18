package me.zeroeightsix.kami.module.modules.chat

import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.client.InfoOverlay
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimeUtils.getFinalTime
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageDetectionHelper
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.network.play.server.SPacketChat

@Module.Info(
        name = "DiscordNotifs",
        category = Module.Category.CHAT,
        description = "Sends your chat to a set Discord channel"
)
object DiscordNotifs : Module() {
    private val timeout = register(Settings.b("Timeout", true))
    private val timeoutTime = register(Settings.integerBuilder("Seconds").withValue(10).withRange(0, 120).withStep(5).withVisibility { timeout.value })
    private val time = register(Settings.b("Timestamp", true))
    private val importantPings = register(Settings.b("ImportantPings", false))
    private val disconnect = register(Settings.b("DisconnectMsgs", true))
    private val all = register(Settings.b("AllMessages", false))
    private val queue = register(Settings.booleanBuilder("QueuePosition").withValue(true).withVisibility { !all.value })
    private val restart = register(Settings.booleanBuilder("RestartMsgs").withValue(true).withVisibility { !all.value })
    private val direct = register(Settings.booleanBuilder("ReceivedDMs").withValue(true).withVisibility { !all.value })
    private val directSent = register(Settings.booleanBuilder("SendDMs").withValue(true).withVisibility { !all.value })

    val url = register(Settings.s("URL", "unchanged"))
    val pingID = register(Settings.s("PingID", "unchanged"))
    val avatar = register(Settings.s("Avatar", KamiMod.GITHUB_LINK + "assets/raw/assets/assets/icons/kami.png"))

    private val server: String get() = mc.currentServerData?.serverIP ?: "the server"
    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.SECONDS)

    /* Listeners to send the messages */
    init {
        listener<PacketEvent.Receive> {
            if (mc.player == null || it.packet !is SPacketChat) return@listener
            val message = it.packet.getChatComponent().unformattedText
            if (timeout(message) && MessageDetectionHelper.shouldSend(all.value, restart.value, direct.value, directSent.value, queue.value, importantPings.value, message)) {
                sendMessage(getPingID(message) + MessageDetectionHelper.getMessageType(direct.value, directSent.value, message, server) + getTime() + message, avatar.value)
            }
        }

        listener<ConnectionEvent.Connect> {
            if (!disconnect.value) return@listener
            sendMessage(getPingID("KamiBlueMessageType1") + getTime() + MessageDetectionHelper.getMessageType(direct.value, directSent.value, "KamiBlueMessageType1", server), avatar.value)
        }

        listener<ConnectionEvent.Disconnect> {
            if (!disconnect.value) return@listener
            sendMessage(getPingID("KamiBlueMessageType2") + getTime() + MessageDetectionHelper.getMessageType(direct.value, directSent.value, "KamiBlueMessageType2", server), avatar.value)
        }

        /* Always on status code */
        listener<SafeTickEvent> {
            if (url.value == "unchanged") {
                MessageSendHelper.sendErrorMessage(chatName + " You must first set a webhook url with the '&7" + Command.getCommandPrefix() + "discordnotifs&r' command")
                disable()
            } else if (pingID.value == "unchanged" && importantPings.value) {
                MessageSendHelper.sendErrorMessage(chatName + " For Pings to work, you must set a Discord ID with the '&7" + Command.getCommandPrefix() + "discordnotifs&r' command")
                disable()
            }
        }
    }

    private fun timeout(message: String) = !timeout.value
            || (MessageDetectionHelper.isRestart(restart.value, message)
            || MessageDetectionHelper.isDirect(direct.value, message)
            || MessageDetectionHelper.isDirectOther(directSent.value, message))
            || timer.tick(timeoutTime.value.toLong())

    /* Text formatting and misc methods */
    private fun getPingID(message: String) = if (MessageDetectionHelper.isRestart(restart.value, message)
            || MessageDetectionHelper.isDirect(direct.value, message)
            || MessageDetectionHelper.isDirectOther(directSent.value, message)
            || MessageDetectionHelper.isImportantQueue(importantPings.value, message)
            || message == "KamiBlueMessageType1"
            || message == "KamiBlueMessageType2") formatPingID()
    else ""

    private fun formatPingID(): String {
        return if (!importantPings.value) "" else "<@!" + pingID.value + ">: "
    }

    private fun getTime(): String {
        return if (!time.value) ""
        else "[" + getFinalTime(InfoOverlay.timeUnitSetting.value, InfoOverlay.timeTypeSetting.value, InfoOverlay.doLocale.value) + "] "
    }

    private fun sendMessage(content: String, avatarUrl: String) {
        val tm = TemmieWebhook(url.value)
        val dm = DiscordMessage(KamiMod.MODNAME + " " + KamiMod.VER_FULL_BETA, content, avatarUrl)
        tm.sendMessage(dm)
    }

}