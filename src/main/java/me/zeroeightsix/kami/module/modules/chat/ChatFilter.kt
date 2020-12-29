package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.text.MessageDetection
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.formatValue
import net.minecraftforge.client.event.ClientChatReceivedEvent
import org.kamiblue.event.listener.listener
import java.io.*
import java.util.*

@Module.Info(
        name = "ChatFilter",
        description = "Filters custom words or phrases from the chat",
        category = Module.Category.CHAT
)
object ChatFilter : Module() {
    private val filterOwn = register(Settings.b("FilterOwn", false))
    private val filterDMs = register(Settings.b("FilterDMs", false))
    private val hasRunInfo = register(Settings.booleanBuilder("Info").withValue(false).withVisibility { false })

    private val chatFilter = ArrayList<Regex>()

    init {
        listener<ClientChatReceivedEvent> {
            if (isDetected(it.message.unformattedText)) it.isCanceled = true
        }
    }

    private fun isDetected(message: String): Boolean {
        return if (!filterOwn.value && MessageDetection.Message.SELF detect message
            || !filterDMs.value && MessageDetection.Direct.ANY detect message) {
            false
        } else {
            chatFilter.all { it.containsMatchIn(message) }
        }
    }

    override fun onEnable() {
        try {
            MessageSendHelper.sendChatMessage("$chatName Trying to find '&7chat_filter.txt&f'")
            chatFilter.clear()

           File("chat_filter.txt").bufferedReader().forEachLine {
               val string = it.trim()
               if (string.isEmpty()) return@forEachLine

               try {
                   val regex = "\\b$string\\b".toRegex(RegexOption.IGNORE_CASE)
                   chatFilter.add(regex)
               } catch (e: Exception) {
                   MessageSendHelper.sendErrorMessage("$chatName Failed to compile line ${formatValue(it)}, ${e.message}")
               }
           }

            MessageSendHelper.sendChatMessage("$chatName Loaded '&7chat_filter.txt&f'!")
        } catch (exception: FileNotFoundException) {
            MessageSendHelper.sendErrorMessage("$chatName Couldn't find a file called '&7chat_filter.txt&f' inside your '&7.minecraft&f' folder, disabling")
            disable()
        } catch (exception: Exception) {
            MessageSendHelper.sendErrorMessage(exception.toString())
        }

        if (!hasRunInfo.value) {
            MessageSendHelper.sendChatMessage("$chatName Tip: this supports &lregex&r if you know how to use those. " +
                    "This also uses &lword boundaries&r meaning it will match whole words, not part of a word. " +
                    "Eg if your filter has 'hell' then 'hello' will not be filtered.")
            hasRunInfo.value = true
        }
    }
}