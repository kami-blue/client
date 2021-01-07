package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.MovementUtils.isMoving
import me.zeroeightsix.kami.util.text.MessageDetection
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendServerMessage
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import java.io.File
import java.io.FileReader

object LoginMessage : Module(
    name = "LoginMessage",
    description = "Sends a given message to public chat on login.",
    category = Category.CHAT,
    showOnArray = false,
    modulePriority = 150
) {
    private val sendAfterMoving by setting("SendAfterMoving", false)

    private val file = File(KamiMod.DIRECTORY + "loginmsg.txt")
    private var loginMessage: String? = null
    private var sent = false
    private var moved = false

    init {
        onEnable {
            if (file.exists()) {
                val fileReader = FileReader(file)
                try {
                    fileReader.readLines().getOrNull(0)?.let {
                        if (it.isNotBlank()) loginMessage = it.trim()
                    }
                    MessageSendHelper.sendChatMessage("$chatName Loaded login message!")
                } catch (e: Exception) {
                    MessageSendHelper.sendErrorMessage("$chatName Failed loading login message, $e")
                    disable()
                }
                fileReader.close()
            } else {
                file.createNewFile()
                MessageSendHelper.sendErrorMessage("$chatName Login Message file is empty!" +
                    ", please add them in the &7loginmsg.txt&f under the &7.minecraft/kamiblue&f directory.")
                disable()
            }
        }

        listener<ConnectionEvent.Disconnect> {
            sent = false
            moved = false
        }

        safeListener<TickEvent.ClientTickEvent> { event ->
            if (event.phase != TickEvent.Phase.END) return@safeListener

            if (!sent && (!sendAfterMoving || moved)) {
                loginMessage?.let {
                    if (MessageDetection.Command.KAMI_BLUE detect it) {
                        MessageSendHelper.sendKamiCommand(it)
                    } else {
                        sendServerMessage(it)
                    }
                    sent = true
                }
            }

            if (!moved) moved = player.isMoving
        }
    }
}
