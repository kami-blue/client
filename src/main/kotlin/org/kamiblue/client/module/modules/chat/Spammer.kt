package org.kamiblue.client.module.modules.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.KamiMod
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.text.MessageDetection
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.MessageSendHelper.sendServerMessage
import org.kamiblue.client.util.threads.defaultScope
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.commons.extension.synchronized
import java.io.File
import java.net.URL
import java.util.*
import kotlin.random.Random

internal object Spammer : Module(
    name = "Spammer",
    description = "Spams text from a file on a set delay into the chat",
    category = Category.CHAT,
    modulePriority = 100
) {
    private val modeSetting = setting("Order", Mode.RANDOM_ORDER)
    private val delay = setting("Delay", 10, 1..180, 1, description = "Delay between messages, in seconds")
    private val loadRemote = setting("Load From URL", false)
    private val remoteURL = setting("Remote URL", "Unchanged", { loadRemote.value })

    private val file = File(KamiMod.DIRECTORY + "spammer.txt")
    private val spammer = ArrayList<String>().synchronized()
    private val timer = TickTimer(TimeUnit.SECONDS)
    private var currentLine = 0

    private enum class Mode {
        IN_ORDER, RANDOM_ORDER
    }

    private val urlValue
        get() = if (remoteURL.value != "Unchanged") {
            remoteURL.value
        } else {
            MessageSendHelper.sendErrorMessage("Change the RemoteURL setting in the ClickGUI!")
            disable()
            null
        }

    init {
        onEnable {
            spammer.clear()

            if (loadRemote.value) {
                val url = urlValue ?: return@onEnable

                defaultScope.launch(Dispatchers.IO) {
                    try {
                        val text = URL(url).readText()
                        spammer.addAll(text.split("\n"))

                        MessageSendHelper.sendChatMessage("$chatName Loaded remote spammer messages!")
                    } catch (e: Exception) {
                        MessageSendHelper.sendErrorMessage("$chatName Failed loading remote spammer, $e")
                        disable()
                    }
                }

            } else {
                defaultScope.launch(Dispatchers.IO) {
                    if (file.exists()) {
                        try {
                            file.forEachLine { if (it.isNotBlank()) spammer.add(it.trim()) }
                            MessageSendHelper.sendChatMessage("$chatName Loaded spammer messages!")
                        } catch (e: Exception) {
                            MessageSendHelper.sendErrorMessage("$chatName Failed loading spammer, $e")
                            disable()
                        }
                    } else {
                        file.createNewFile()
                        MessageSendHelper.sendErrorMessage("$chatName Spammer file is empty!" +
                            ", please add them in the &7spammer.txt&f under the &7.minecraft/kamiblue&f directory.")
                        disable()
                    }
                }
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START || spammer.isEmpty() || !timer.tick(delay.value.toLong())) return@safeListener
            val message = if (modeSetting.value == Mode.IN_ORDER) getOrdered() else getRandom()
            if (MessageDetection.Command.KAMI_BLUE detect message) {
                MessageSendHelper.sendKamiCommand(message)
            } else {
                sendServerMessage(message)
            }
        }
    }

    private fun getOrdered(): String {
        currentLine %= spammer.size
        return spammer[currentLine++]
    }

    private fun getRandom(): String {
        val prevLine = currentLine
        // Avoids sending the same message
        while (spammer.size != 1 && currentLine == prevLine) {
            currentLine = Random.nextInt(spammer.size)
        }
        return spammer[currentLine]
    }
}
