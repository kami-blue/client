package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendServerMessage
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.text.SimpleDateFormat
import java.util.*

@Module.Info(
    name = "AutoQMain",
    description = "Automatically does '/queue 2b2t-lobby'",
    category = Module.Category.CHAT,
    showOnArray = false
)
object AutoQMain : Module() {
    private val showWarns = setting("ShowWarnings", true)
    private val dimensionWarning = setting("DimensionWarning", true)
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
