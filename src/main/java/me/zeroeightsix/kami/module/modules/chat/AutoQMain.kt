package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import org.kamiblue.event.listener.listener
import java.text.SimpleDateFormat
import java.util.*

@Module.Info(
        name = "AutoQMain",
        description = "Automatically does '/queue 2b2t-lobby'",
        category = Module.Category.CHAT,
        showOnArray = Module.ShowOnArray.OFF
)
object AutoQMain : Module() {
    private val showWarns = register(Settings.b("ShowWarnings", true))
    private val dimensionWarning = register(Settings.b("DimensionWarning", true))
    private val delay = register(Settings.integerBuilder("Delay").withValue(30).withRange(5, 120).withStep(5))

    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.SECONDS)

    init {
        listener<SafeTickEvent> {
            if (!timer.tick(delay.value.toLong())) return@listener

            if (mc.getCurrentServerData() == null) {
                sendMessage("&l&6Error: &r&6You are in singleplayer")
                return@listener
            }

            if (!mc.getCurrentServerData()!!.serverIP.equals("2b2t.org", ignoreCase = true)) {
                return@listener
            }

            if (mc.player.dimension != 1 && dimensionWarning.value) {
                sendMessage("&l&6Warning: &r&6You are not in the end. Not running &b/queue main&7.")
                return@listener
            }

            sendQueueMain()
        }
    }

    private fun sendQueueMain() {
        val formatter = SimpleDateFormat("HH:mm:ss")
        val date = Date(System.currentTimeMillis())

        MessageSendHelper.sendChatMessage("&7Run &b/queue 2b2t-lobby&7 at " + formatter.format(date))
        MessageSendHelper.sendServerMessage("/queue 2b2t-lobby")
    }

    private fun sendMessage(message: String) {
        if (showWarns.value) MessageSendHelper.sendWarningMessage("$chatName $message")
    }
}
