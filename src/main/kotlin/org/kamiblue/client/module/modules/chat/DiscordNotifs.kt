package org.kamiblue.client.module.modules.chat

import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.network.play.server.SPacketChat
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.apache.commons.io.IOUtils
import org.kamiblue.client.KamiMod
import org.kamiblue.client.command.CommandManager
import org.kamiblue.client.event.events.ConnectionEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.text.*
import org.kamiblue.client.util.threads.defaultScope
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.commons.utils.ConnectionUtils
import org.kamiblue.event.listener.listener

internal object DiscordNotifs : Module(
    name = "DiscordNotifs",
    category = Category.CHAT,
    description = "Sends your chat to a set Discord channel"
) {
    private val timeout by setting("Timeout", true)
    private val timeoutTime by setting("Seconds", 10, 0..120, 5, { timeout })
    private val time by setting("Timestamp", true)
    private val importantPings by setting("Important Pings", false)
    private val connectionChange by setting("Connection Change", true, description = "When you get disconnected or reconnected to the server")
    private val all by setting("All Messages", false)
    private val direct by setting("DMs", true, { !all })
    private val queue by setting("Queue Position", true, { !all })
    private val restart by setting("Restart", true, { !all }, description = "Server restart notifications")

    val url = setting("URL", "unchanged")
    val pingID = setting("Ping ID", "unchanged")
    val avatar = setting("Avatar", "${KamiMod.GITHUB_LINK}/assets/raw/assets/assets/icons/kamiGithub.png")

    private const val username = "${KamiMod.NAME} ${KamiMod.VERSION}"
    private val server: String get() = mc.currentServerData?.serverIP ?: "the server"
    private val timer = TickTimer(TimeUnit.SECONDS)

    /* Listeners to send the messages */
    init {
        safeListener<PacketEvent.Receive> {
            if (it.packet !is SPacketChat) return@safeListener
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
        defaultScope.launch(Dispatchers.IO) {
            ConnectionUtils.runConnection(
                url.value,
                { connection ->
                    val bytes = JsonObject().run {
                        addProperty("username", username)
                        addProperty("content", content)
                        addProperty("avatar_url", avatar.value)
                        toString().toByteArray(Charsets.UTF_8)
                    }

                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    connection.setRequestProperty("Accept", "application/json")
                    connection.setRequestProperty("User-Agent", "")

                    connection.requestMethod = "POST"
                    connection.outputStream.use {
                        it.write(bytes)
                    }

                    val response = connection.inputStream.use {
                        IOUtils.toString(it, Charsets.UTF_8)
                    }

                    if (response.isNotEmpty()) {
                        KamiMod.LOG.info("Unexpected response from DiscordNotifs http request: $response")
                    }
                },
                {
                    KamiMod.LOG.warn("Error while sending webhook", it)
                },
            )
        }
    }

}