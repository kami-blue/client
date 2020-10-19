package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendServerMessage
import net.minecraft.client.gui.GuiChat
import java.io.*
import kotlin.random.Random

@Module.Info(
        name = "Spammer",
        description = "Spams text from a file on a set delay into the chat",
        category = Module.Category.CHAT
)
object Spammer : Module() {
    private val modeSetting = register(Settings.e<Mode>("Order", Mode.RANDOM_ORDER))
    private val timeoutTime = register(Settings.integerBuilder("Timeout(s)").withRange(1, 240).withValue(10).withStep(5))

    private var spammer = ArrayList<String>()
    private var currentLine = 0
    private var startTime = 0L
    private var isChatOpen = false

    private enum class Mode { IN_ORDER, RANDOM_ORDER }

    override fun onEnable() {
        val bufferedReader: BufferedReader
        try {
            sendChatMessage("$chatName Trying to find '&7spammer.txt&f'")
            bufferedReader = BufferedReader(InputStreamReader(FileInputStream("spammer.txt"), "UTF-8"))
            spammer.clear()
            var str = bufferedReader.readLine()
            while (str != null) {
                spammer.add(str)
                str = bufferedReader.readLine()
            }
            bufferedReader.close()
        } catch (exception: FileNotFoundException) {
            sendErrorMessage("$chatName Couldn't find a file called '&7spammer.txt&f' inside your '&7.minecraft&f' folder, disabling")
            disable()
            return
        } catch (exception: IOException) {
            sendErrorMessage(exception.toString())
            return
        }
        sendChatMessage("$chatName Found '&7spammer.txt&f'!")
        startTime = System.currentTimeMillis()
    }

    init {
        listener<SafeTickEvent> {
            sendMsg()
        }
    }

    private fun sendMsg() {
        if (startTime + (timeoutTime.value * 1000) <= System.currentTimeMillis()) { // 1 timeout = 1 second = 1000 ms
            when {
                mc.currentScreen is GuiChat -> { /* Delays the spammer msg if the chat gui is open */
                    startTime = System.currentTimeMillis() - (timeoutTime.value * 1000)
                    isChatOpen = true
                    return
                }
                isChatOpen -> { /* Adds extra delay after the chat gui is closed */
                    startTime += 3000
                    isChatOpen = false
                }
                else -> {
                    startTime = System.currentTimeMillis()
                    if (modeSetting.value == Mode.IN_ORDER) {
                        sendServerMessage(getOrdered(spammer))
                    } else {
                        sendServerMessage(getRandom(spammer, currentLine))
                    }
                }
            }
        }
    }

    private fun getOrdered(array: ArrayList<String>): String {
        if (currentLine >= array.size) currentLine = 0
        currentLine++
        return array[currentLine - 1]
    }

    private fun getRandom(array: ArrayList<String>, LastLine: Int): String {
        while (array.size != 1 && currentLine == LastLine) {
            currentLine = Random.nextInt(array.size)
        } /* Avoids sending the same message */
        return array[currentLine]
    }
}
