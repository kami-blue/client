package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MessageSendHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author dominikaaaa
 * Updated by d1gress/Qther on 5/12/2019
 * Updated by dominikaaaa on 26/03/20
 */
@Module.Info(
        name = "AutoQMain",
        description = "Automatically does '/queue main' on servers",
        category = Module.Category.CHAT,
        showOnArray = Module.ShowOnArray.OFF
)
class AutoQMain : Module() {
    private val showWarns = register(Settings.b("Show Warnings", true))
    private val connectionWarning = register(Settings.b("Connection Warning", true))
    private val dimensionWarning = register(Settings.b("Dimension Warning", true))
    private val delay = register(Settings.doubleBuilder("Wait time").withMinimum(0.2).withValue(7.1).withMaximum(10.0).build())

    private var delayTime = 0.0
    private var oldDelay = 0.0

    override fun onUpdate() {
        if (mc.player == null) return

        if (oldDelay == 0.0) oldDelay = delay.value else if (oldDelay != delay.value) {
            delayTime = delay.value
            oldDelay = delay.value
        }

        if (delayTime <= 0) {
            delayTime = delay.value * 2400
        } else if (delayTime > 0) {
            delayTime--
            return
        }

        if (mc.getCurrentServerData() == null && connectionWarning.value) {
            sendMessage("&l&6Error: &r&6You are in singleplayer")
            return
        }

        if (!mc.getCurrentServerData()!!.serverIP.equals("2b2t.org", ignoreCase = true) && connectionWarning.value) {
            sendMessage("&l&6Warning: &r&6You are not connected to 2b2t.org")
            return
        }

        if (mc.player.dimension != 1 && dimensionWarning.value) {
            sendMessage("&l&6Warning: &r&6You are not in the end. Not running &b/queue main&7.")
            return
        }
        sendQueueMain()
    }

    private fun sendQueueMain() {
        val formatter = SimpleDateFormat("HH:mm:ss")
        val date = Date(System.currentTimeMillis())

        MessageSendHelper.sendChatMessage("&7Run &b/queue main&7 at " + formatter.format(date))
        MessageSendHelper.sendServerMessage("/queue main")
    }

    private fun sendMessage(message: String) {
        if (showWarns.value) MessageSendHelper.sendWarningMessage(chatName + message)
    }

    public override fun onToggle() {
        delayTime = 0.0
    }
}