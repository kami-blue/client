package me.zeroeightsix.kami.module.modules.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.text.MessageDetection
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendServerMessage
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.io.File
import java.net.URL
import java.util.*
import kotlin.random.Random

@Module.Info(
    name = "Spammer",
    description = "Spams text from a file on a set delay into the chat",
    category = Module.Category.CHAT,
    modulePriority = 100
)
object Spammer : Module() {
    private val modeSetting = setting("Order", Mode.RANDOM_ORDER)
    private val delay = setting("Delay(s)", 10,1..100, 1)
    private val loadRemote = setting("LoadFromURL", false)
    private val remoteURL = setting("RemoteURL", "Unchanged")

    private val file = File(KamiMod.DIRECTORY + "spammer.txt")
    private val spammer = Collections.synchronizedList(ArrayList<String>())
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

    override fun onEnable() {
        spammer.clear()

        if (loadRemote.value) {
            val url = urlValue ?: return

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

    init {
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
