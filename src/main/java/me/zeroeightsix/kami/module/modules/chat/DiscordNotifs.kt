package me.zeroeightsix.kami.module.modules.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.text.*
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.network.play.server.SPacketChat
import net.minecraftforge.fml.common.gameevent.TickEvent
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.kamiblue.event.listener.listener

internal object DiscordNotifs : Module(
    name = "DiscordNotifs",
    category = Category.CHAT,
    description = "Sends your chat to a set Discord channel"
) {
    private val timeout by setting("Timeout", true)
    private val timeoutTime by setting("Seconds", 10, 0..120, 5, { timeout })
    private val time by setting("Timestamp", true)
    private val importantPings by setting("ImportantPings", false)
    private val connectionChange by setting("ConnectionChange", true, description = "When you get disconnected or reconnected to the server")
    private val all by setting("AllMessages", false)
    private val direct by setting("DMs", true, { !all })
    private val queue by setting("QueuePosition", true, { !all })
    private val restart by setting("Restart", true, { !all }, description = "Server restart notifications")

    val url = setting("URL", "unchanged")
    val pingID = setting("PingID", "unchanged")
    val avatar = setting("Avatar", "${KamiMod.GITHUB_LINK}/assets/raw/assets/assets/icons/kamiGithub.png")

    private const val username = "${KamiMod.NAME} ${KamiMod.VERSION}"
    private val server: String get() = mc.currentServerData?.serverIP ?: "the server"
    private val timer = TickTimer(TimeUnit.SECONDS)

    /* Listeners to send the messages */
    init {
        listener<PacketEvent.Receive> {
            if (mc.player == null || it.packet !is SPacketChat) return@listener
            val message = it.packet.chatComponent.unformattedText
            if (timeout(message) && shouldSend(message)) {
                sendMessage(getPingID(message) + getMessageType(message, server) + getTime() + message)
            }
        }

        listener<ConnectionEvent.Connect> {
            if (!connectionChange) return@listener
            sendMessage(getPingID("KamiBlueMessageType1") + getTime() + getMessageType("KamiBlueMessageType1", server))
        }

        listener<ConnectionEvent.Disconnect> {
            if (!connectionChange) return@listener
            sendMessage(getPingID("KamiBlueMessageType2") + getTime() + getMessageType("KamiBlueMessageType2", server))
        }

        /* Always on status code */
        safeListener<TickEvent.ClientTickEvent> {
            if (url.value == "unchanged") {
                MessageSendHelper.sendErrorMessage(chatName + " You must first set a webhook url with the " +
                    formatValue("${CommandManager.prefix}discordnotifs") +
                    " command")
                disable()
            } else if (pingID.value == "unchanged" && importantPings) {
                MessageSendHelper.sendErrorMessage(chatName + " For Pings to work, you must set a Discord ID with the " +
                    formatValue("${CommandManager.prefix}discordnotifs") +
                    " command")
                disable()
            }
        }
    }

    private fun shouldSend(message: String): Boolean {
        return all
            || direct && MessageDetection.Direct.ANY detect message
            || restart && MessageDetection.Server.RESTART detect message
            || queue && MessageDetection.Server.QUEUE detect message
    }

    private fun getMessageType(message: String, server: String): String {
        if (direct && MessageDetection.Direct.RECEIVE detect message) return "You got a direct message!\n"
        if (direct && MessageDetection.Direct.SENT detect message) return "You sent a direct message!\n"
        if (message == "KamiBlueMessageType1") return "Connected to $server"
        return if (message == "KamiBlueMessageType2") "Disconnected from $server" else ""
    }

    private fun timeout(message: String) = !timeout
        || restart && MessageDetection.Server.RESTART detect message
        || direct && MessageDetection.Direct.ANY detect message
        || timer.tick(timeoutTime.toLong())

    /* Text formatting and misc methods */
    private fun getPingID(message: String) = if (message == "KamiBlueMessageType1"
        || message == "KamiBlueMessageType2"
        || direct && MessageDetection.Direct.ANY detect message
        || restart && MessageDetection.Server.RESTART detect message
        || importantPings && MessageDetection.Server.QUEUE_IMPORTANT detect message) formatPingID()
    else ""

    private fun formatPingID(): String {
        return if (!importantPings) "" else "<@!${pingID.value}>: "
    }

    private fun getTime() =
        if (!time) ""
        else ChatTimestamp.time

    private fun sendMessage(content: String) {
        val jsonType = "application/json; charset=utf-8".toMediaType()

        // todo json dsl
        val body = "{\"username\": \"$username\", \"content\": \"$content\", \"avatar_url\": \"${avatar.value}\"}".toRequestBody(jsonType)
        val request: Request = Request.Builder()
            .url(url.value)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        defaultScope.launch(Dispatchers.IO) {
            // todo probably make some DSL for this lol
            KamiMod.getHttpClient().newCall(request).execute().use { response ->
                val responseBody = response.body?.string().toString()
                if (responseBody.isBlank()) return@launch // Returns 204 empty normally. We want to warn log any non-empty responses.
                KamiMod.LOG.warn("DiscordNotifs OkHttp Request\n${responseBody}")
            }
        }
    }
}