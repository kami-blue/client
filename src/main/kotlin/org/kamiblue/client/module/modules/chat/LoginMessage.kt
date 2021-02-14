package org.kamiblue.client.module.modules.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.KamiMod
import org.kamiblue.client.event.events.ConnectionEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.MovementUtils.isMoving
import org.kamiblue.client.util.text.MessageDetection
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.MessageSendHelper.sendServerMessage
import org.kamiblue.client.util.threads.defaultScope
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.listener
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

internal object LoginMessage : Module(
    name = "LoginMessage",
    description = "Sends a given message(s) to public chat on login.",
    category = Category.CHAT,
    showOnArray = false,
    modulePriority = 150
) {
    private val sendAfterMoving by setting("Send After Moving", false, description = "Wait until you have moved after logging in")

    private val file = File(KamiMod.DIRECTORY + "loginmsg.txt")
    private val loginMessages = CopyOnWriteArrayList<String>()
    private var sent = false
    private var moved = false

    init {
        onEnable {
            if (file.exists()) {
                defaultScope.launch(Dispatchers.IO) {
                    try {
                        file.forEachLine {
                            if (it.isNotBlank()) loginMessages.add(it.trim())
                        }
                        MessageSendHelper.sendChatMessage("$chatName Loaded ${loginMessages.size} login messages!")
                    } catch (e: Exception) {
                        MessageSendHelper.sendErrorMessage("$chatName Failed loading login messages, $e")
                        disable()
                    }
                }
            } else {
                file.createNewFile()
                MessageSendHelper.sendErrorMessage("$chatName Login Messages file not found!" +
                    ", please add them in the &7loginmsg.txt&f under the &7.minecraft/kamiblue&f directory.")
                disable()
            }
        }

        onDisable {
            loginMessages.clear()
        }

        listener<ConnectionEvent.Disconnect> {
            sent = false
            moved = false
        }

        safeListener<TickEvent.ClientTickEvent> { event ->
            if (event.phase != TickEvent.Phase.END) return@safeListener

            if (!sent && (!sendAfterMoving || moved)) {
                for (message in loginMessages) {
                    if (MessageDetection.Command.KAMI_BLUE detect message) {
                        MessageSendHelper.sendKamiCommand(message)
                    } else {
                        sendServerMessage(message)
                    }
                }

                sent = true
            }

            if (!moved) moved = player.isMoving
        }
    }
}
