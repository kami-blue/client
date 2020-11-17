package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageDetectionHelper.isDirect
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage
import net.minecraftforge.client.event.ClientChatReceivedEvent
import java.io.*
import java.util.*
import java.util.regex.Pattern


@Module.Info(
        name = "ChatFilter",
        description = "Filters custom words or phrases from the chat",
        category = Module.Category.CHAT
)
object ChatFilter : Module() {
    private val filterOwn = register(Settings.b("FilterOwn", false))
    private val filterDMs = register(Settings.b("FilterDMs", false))
    private val hasRunInfo = register(Settings.booleanBuilder("Info").withValue(false).withVisibility { false })

    private val chatFilter = ArrayList<Pattern>()

    init {
        listener<ClientChatReceivedEvent> {
            if (isDetected(it.message.unformattedText)) it.isCanceled = true
        }
    }

    private fun isDetected(message: String): Boolean {
        val ownMsg = "^<" + mc.player.name + "> "
        return if (!filterOwn.value && customMatch(ownMsg, message) || isDirect(filterDMs.value, message)) {
            false
        } else {
            isMatched(message)
        }
    }

    private fun isMatched(message: String): Boolean {
        for (pattern in chatFilter) {
            if (pattern.matcher(message).find()) {
                return true
            }
        }
        return false
    }

    private fun customMatch(filter: String, message: String): Boolean {
        return Pattern.compile(filter, Pattern.CASE_INSENSITIVE).matcher(message).find()
    }

    override fun onEnable() {
        val bufferedReader: BufferedReader
        try {
            sendChatMessage("$chatName Trying to find '&7chat_filter.txt&f'")
            bufferedReader = BufferedReader(InputStreamReader(FileInputStream("chat_filter.txt"), "UTF-8"))
            var line = bufferedReader.readLine()
            chatFilter.clear()
            while (line != null) {
                while (customMatch("[ ]$", line)) { /* remove trailing spaces */
                    line = line.substring(0, line.length - 1)
                }
                while (customMatch("^[ ]", line)) {
                    line = line.substring(1) /* remove beginning spaces */
                }
                if (line.isEmpty()) return
                chatFilter.add(Pattern.compile("\\b$line\\b", Pattern.CASE_INSENSITIVE))
                line = bufferedReader.readLine()
            }
            bufferedReader.close()
            sendChatMessage("$chatName Found '&7chat_filter.txt&f'!")
        } catch (exception: FileNotFoundException) {
            sendErrorMessage("$chatName Couldn't find a file called '&7chat_filter.txt&f' inside your '&7.minecraft&f' folder, disabling")
            disable()
        } catch (exception: IOException) {
            sendErrorMessage(exception.toString())
        }

        if (!hasRunInfo.value) {
            sendChatMessage("$chatName Tip: this supports &lregex&r if you know how to use those. " +
                    "This also uses &lword boundaries&r meaning it will match whole words, not part of a word. " +
                    "Eg if your filter has 'hell' then 'hello' will not be filtered.")
            hasRunInfo.value = true
        }
    }
}