package org.kamiblue.client.module.modules.chat

import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.MessageSendHelper.sendServerMessage
import org.kamiblue.client.util.threads.safeListener
import java.text.SimpleDateFormat
import java.util.*

internal object AutoQMain : Module(
    name = "AutoQMain",
    description = "Automatically does '/queue 2b2t-lobby'",
    category = Category.CHAT,
    showOnArray = false
) {
    private val showWarns = setting("Show Warnings", true)
    private val dimensionWarning = setting("Dimension Warning", true)
    private val delay = setting("Delay", 30, 5..120, 5)

    private val timer = TickTimer(TimeUnit.SECONDS)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (!timer.tick(delay.value.toLong())) return@safeListener

            if (mc.currentServerData == null) {
                sendMessage("&l&6Error: &r&6You are in singleplayer")
                return@safeListener
            }

            if (!mc.currentServerData!!.serverIP.equals("2b2t.org", ignoreCase = true)) {
                return@safeListener
            }

            if (player.dimension != 1 && dimensionWarning.value) {
                sendMessage("&l&6Warning: &r&6You are not in the end. Not running &b/queue main&7.")
                return@safeListener
            }

            sendQueueMain()
        }
    }

    private fun sendQueueMain() {
        val formatter = SimpleDateFormat("HH:mm:ss")
        val date = Date(System.currentTimeMillis())

        MessageSendHelper.sendChatMessage("&7Run &b/queue 2b2t-lobby&7 at " + formatter.format(date))
        sendServerMessage("/queue 2b2t-lobby")
    }

    private fun sendMessage(message: String) {
        if (showWarns.value) MessageSendHelper.sendWarningMessage("$chatName $message")
    }
}
