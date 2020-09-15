package me.zeroeightsix.kami.module.modules.chat

import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.module.modules.client.InfoOverlay
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimeUtils.getFinalTime
import me.zeroeightsix.kami.util.text.MessageDetectionHelper.getMessageType
import me.zeroeightsix.kami.util.text.MessageDetectionHelper.isDirect
import me.zeroeightsix.kami.util.text.MessageDetectionHelper.isDirectOther
import me.zeroeightsix.kami.util.text.MessageDetectionHelper.isImportantQueue
import me.zeroeightsix.kami.util.text.MessageDetectionHelper.isRestart
import me.zeroeightsix.kami.util.text.MessageDetectionHelper.shouldSend
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.network.play.server.SPacketChat

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 26/03/20
 * Updated by dominikaaaa on 28/03/20
 * Updated by Xiaro on 10/09/20
 */
@Module.Info(
        name = "DiscordNotifs",
        category = Module.Category.CHAT,
        description = "Sends your chat to a set Discord channel"
)
class DiscordNotifs : Module() {
    private val timeout = register(Settings.b("Timeout", true))
    private val timeoutTime = register(Settings.integerBuilder("Seconds").withValue(10).withRange(0, 120).withVisibility { timeout.value }.build())
    private val time = register(Settings.b("Timestamp", true))
    private val importantPings = register(Settings.b("ImportantPings", false))
    private val disconnect = register(Settings.b("DisconnectMsgs", true))
    private val all = register(Settings.b("AllMessages", false))
    private val queue = register(Settings.booleanBuilder("QueuePosition").withValue(true).withVisibility { !all.value }.build())
    private val restart = register(Settings.booleanBuilder("RestartMsgs").withValue(true).withVisibility { !all.value }.build())
    private val direct = register(Settings.booleanBuilder("ReceivedDMs").withValue(true).withVisibility { !all.value }.build())
    private val directSent = register(Settings.booleanBuilder("SendDMs").withValue(true).withVisibility { !all.value }.build())

    @JvmField
    val url = register(Settings.s("URL", "unchanged"))

    @JvmField
    val pingID = register(Settings.s("PingID", "unchanged"))

    @JvmField
    val avatar = register(Settings.s("Avatar", KamiMod.GITHUB_LINK + "assets/raw/assets/assets/icons/kami.png"))

    private var cServer: ServerData? = null

    /* Getters for messages */
    private var startTime: Long = 0
    private val server: String get() = if (cServer == null) "the server" else cServer!!.serverIP

    /* Listeners to send the messages */
    @EventHandler
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        if (mc.player == null || event.packet !is SPacketChat) return@EventHook
        val message = event.packet.getChatComponent().unformattedText
        if (timeout(message) && shouldSend(all.value, restart.value, direct.value, directSent.value, queue.value, importantPings.value, message)) {
            sendMessage(getPingID(message) + getMessageType(direct.value, directSent.value, message, server) + getTime() + message, avatar.value)
        }
    })

    @EventHandler
    private val connectListener = Listener(EventHook { event: ConnectionEvent.Connect ->
        if (!disconnect.value) return@EventHook
        sendMessage(getPingID("KamiBlueMessageType1") + getTime() + getMessageType(direct.value, directSent.value, "KamiBlueMessageType1", server), avatar.value)
    })

    @EventHandler
    private val disconnectListener = Listener(EventHook { event: ConnectionEvent.Disconnect ->
        if (!disconnect.value) return@EventHook
        sendMessage(getPingID("KamiBlueMessageType2") + getTime() + getMessageType(direct.value, directSent.value, "KamiBlueMessageType2", server), avatar.value)
    })

    /* Always on status code */
    override fun onUpdate() {
        if (isDisabled) return
        if (url.value == "unchanged") {
            sendErrorMessage(chatName + " You must first set a webhook url with the '&7" + Command.getCommandPrefix() + "discordnotifs&r' command")
            disable()
        } else if (pingID.value == "unchanged" && importantPings.value) {
            sendErrorMessage(chatName + " For Pings to work, you must set a Discord ID with the '&7" + Command.getCommandPrefix() + "discordnotifs&r' command")
            disable()
        }
    }

    private fun timeout(message: String): Boolean {
        if (!timeout.value) return true else if (isRestart(restart.value, message) || isDirect(direct.value, message) || isDirectOther(directSent.value, message)) return true
        if (startTime == 0L) startTime = System.currentTimeMillis()
        if (startTime + timeoutTime.value * 1000 <= System.currentTimeMillis()) { // 1 timeout = 1 second = 1000 ms
            startTime = System.currentTimeMillis()
            return true
        }
        return false
    }

    /* Text formatting and misc methods */
    private fun getPingID(message: String): String {
        return if (isRestart(restart.value, message) || isDirect(direct.value, message) || isDirectOther(directSent.value, message) || isImportantQueue(importantPings.value, message)) formatPingID() else if (message == "KamiBlueMessageType1" || message == "KamiBlueMessageType2") formatPingID() else ""
    }

    private fun formatPingID(): String {
        return if (!importantPings.value) "" else "<@!" + pingID.value + ">: "
    }

    private fun getTime(): String {
        if (!time.value) return ""
        val info = ModuleManager.getModuleT(InfoOverlay::class.java)
        return "[" + getFinalTime(info!!.timeUnitSetting.value, info.timeTypeSetting.value, info.doLocale.value) + "] "
    }

    private fun sendMessage(content: String, avatarUrl: String) {
        val tm = TemmieWebhook(url.value)
        val dm = DiscordMessage(KamiMod.MODNAME + " " + KamiMod.VER_FULL_BETA, content, avatarUrl)
        tm.sendMessage(dm)
    }

}