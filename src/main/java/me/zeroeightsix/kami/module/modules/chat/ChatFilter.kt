package me.zeroeightsix.kami.module.modules.chat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MessageDetectionHelper
import me.zeroeightsix.kami.util.MessageSendHelper
import net.minecraftforge.client.event.ClientChatReceivedEvent
import java.io.*
import java.util.*
import java.util.regex.Pattern

/**
 * @author dominikaaaa
 * Updated by suretic on 13/05/20
 * Updated by domikaaaa on 23/05/20
 */
@Module.Info(
        name = "ChatFilter",
        description = "Filters custom words or phrases from the chat",
        category = Module.Category.CHAT,
        showOnArray = Module.ShowOnArray.OFF
)
class ChatFilter : Module() {
    private val filterOwn = register(Settings.b("Filter Own", false))
    private val filterDMs = register(Settings.b("Filter DMs", false))
    private val hasRunInfo = register(Settings.booleanBuilder("Info").withValue(false).withVisibility { v: Boolean? -> false }.build())

    @EventHandler
    var listener = Listener(EventHook { event: ClientChatReceivedEvent ->
        if (mc.player == null) return@EventHook
        if (isDetected(event.message.unformattedText)) {
            event.isCanceled = true
        }
    })

    private fun isDetected(message: String): Boolean {
        val OWN_MESSAGE = "^<" + mc.player.name + "> "
        return if (!filterOwn.value && customMatch(OWN_MESSAGE, message) || MessageDetectionHelper.isDirect(filterDMs.value, message) || MessageDetectionHelper.isDirectOther(filterDMs.value, message)) {
            false
        } else {
            isMatched(chatFilter, message)
        }
    }

    private fun isMatched(patterns: ArrayList<Pattern>, message: String): Boolean {
        for (pattern in patterns) {
            if (pattern.matcher(message).find()) {
                return true
            }
        }
        return false
    }

    private fun customMatch(filter: String, message: String): Boolean {
        return Pattern.compile(filter, Pattern.CASE_INSENSITIVE).matcher(message).find()
    }

    public override fun onEnable() {
        val bufferedReader: BufferedReader
        try {
            MessageSendHelper.sendChatMessage("$chatName Trying to find '&7chat_filter.txt&f'")
            bufferedReader = BufferedReader(InputStreamReader(FileInputStream("chat_filter.txt"), "UTF-8"))
            var line: String
            chatFilter.clear()
            while (bufferedReader.readLine().also { line = it } != null) {
                while (customMatch("[ ]$", line)) { /* remove trailing spaces */
                    line = line.substring(0, line.length - 1)
                }
                while (customMatch("^[ ]", line)) {
                    line = line.substring(1) /* remove beginning spaces */
                }
                if (line.length <= 0) return
                chatFilter.add(Pattern.compile("\\b$line\\b", Pattern.CASE_INSENSITIVE))
            }
            bufferedReader.close()
        } catch (exception: FileNotFoundException) {
            MessageSendHelper.sendErrorMessage("$chatName Couldn't find a file called '&7chat_filter.txt&f' inside your '&7.minecraft&f' folder, disabling")
            disable()
        } catch (exception: IOException) {
            MessageSendHelper.sendErrorMessage(exception.toString())
        }
        if (isDisabled) return
        MessageSendHelper.sendChatMessage("$chatName Found '&7chat_filter.txt&f'!")
        if (!hasRunInfo.value) {
            MessageSendHelper.sendChatMessage("$chatName Tip: this supports &lregex&r if you know how to use those. This also uses &lword boundaries&r meaning it will match whole words, not part of a word. Eg if your filter has 'hell' then 'hello' will not be filtered.")
            hasRunInfo.value = true
        }
    }

    companion object {
        private val chatFilter = ArrayList<Pattern>()
    }
}