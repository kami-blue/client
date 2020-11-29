package me.zeroeightsix.kami.module.modules.chat

import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.TimeUtils
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageDetectionHelper
import me.zeroeightsix.kami.util.text.MessageDetectionHelper.detect
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.Regexes
import net.minecraft.network.play.server.SPacketChat

@Module.Info(
        name = "DiscordNotifs",
        category = Module.Category.CHAT,
        description = "Sends your chat to a set Discord channel"
)
object DiscordNotifs : Module() {
    private val timeout = setting("Timeout", true)
    private val timeoutTime = setting("Seconds", 10, 0..120, 5, { timeout.value })
    private val time = setting("Timestamp", true)
    private val importantPings = setting("ImportantPings", false)
    private val disconnect = setting("DisconnectMsgs", true)
    private val all = setting("AllMessages", false)
    private val queue = setting("QueuePosition", true, { !all.value })
    private val restart = setting("RestartMsgs", true, { !all.value })
    private val direct = setting("DMs", true, { !all.value })

    val url = setting("URL", "unchanged")
    val pingID = setting("PingID", "unchanged")
    val avatar = setting("Avatar", KamiMod.GITHUB_LINK + "assets/raw/assets/assets/icons/kami.png")

    private val server: String get() = mc.currentServerData?.serverIP ?: "the server"
    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.SECONDS)

    /* Listeners to send the messages */
    init {
        listener<PacketEvent.Receive> {
            if (mc.player == null || it.packet !is SPacketChat) return@listener
            val message = it.packet.getChatComponent().unformattedText
            if (timeout(message) && MessageDetectionHelper.shouldSend(all.value, restart.value, direct.value, queue.value, importantPings.value, message)) {
                sendMessage(getPingID(message) + MessageDetectionHelper.getMessageType(direct.value, message, server) + getTime() + message, avatar.value)
            }
        }

        listener<ConnectionEvent.Connect> {
            if (!disconnect.value) return@listener
            sendMessage(getPingID("KamiBlueMessageType1") + getTime() + MessageDetectionHelper.getMessageType(direct.value, "KamiBlueMessageType1", server), avatar.value)
        }

        listener<ConnectionEvent.Disconnect> {
            if (!disconnect.value) return@listener
            sendMessage(getPingID("KamiBlueMessageType2") + getTime() + MessageDetectionHelper.getMessageType(direct.value, "KamiBlueMessageType2", server), avatar.value)
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
            || (message.detect(restart.value, Regexes.RESTART)
            || MessageDetectionHelper.isDirect(direct.value, message))
            || timer.tick(timeoutTime.value.toLong())

    /* Text formatting and misc methods */
    private fun getPingID(message: String) = if (message.detect(restart.value, Regexes.RESTART)
            || MessageDetectionHelper.isDirect(direct.value, message)
            || message.detect(importantPings.value, Regexes.QUEUE_IMPORTANT)
            || message == "KamiBlueMessageType1"
            || message == "KamiBlueMessageType2") formatPingID()
    else ""

    private fun formatPingID(): String {
        return if (!importantPings.value) "" else "<@!" + pingID.value + ">: "
    }

    private fun getTime() =
            if (!time.value) ""
            else "[${TimeUtils.getTime()}] "

    private fun sendMessage(content: String, avatarUrl: String) {
        val tm = TemmieWebhook(url.value)
        val dm = DiscordMessage(KamiMod.NAME + " " + KamiMod.VERSION, content, avatarUrl)
        tm.sendMessage(dm)
    }

}