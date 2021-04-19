package org.kamiblue.client.module.modules.chat

import net.minecraftforge.client.event.ClientChatReceivedEvent
import org.kamiblue.client.KamiMod
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.text.MessageDetection
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.formatValue
import org.kamiblue.event.listener.listener
import java.io.*
import java.util.*

internal object ChatFilter : Module(
    name = "ChatFilter",
    description = "Filters custom words or phrases from the chat",
    category = Category.CHAT
) {
    private val filterOwn by setting("Filter Own", false)
    private val filterDMs by setting("Filter DMs", false)
    private var hasRunInfo by setting("Info", false, { false })

    private val chatFilter = ArrayList<Regex>()
    private val file = File(KamiMod.DIRECTORY + "chat_filter.txt")

    init {
        onEnable {
            try {
                MessageSendHelper.sendChatMessage("$chatName Trying to find '&7chat_filter.txt&f'")
                chatFilter.clear()

                file.bufferedReader().forEachLine {
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
                MessageSendHelper.sendErrorMessage("$chatName Couldn't find a file called '&7chat_filter.txt&f' inside your '&7.minecraft/kamiblue&f' folder, disabling")
                disable()
            } catch (exception: Exception) {
                MessageSendHelper.sendErrorMessage(exception.toString())
            }

            if (!hasRunInfo) {
                MessageSendHelper.sendChatMessage("$chatName Tip: this supports &lregex&r if you know how to use those. " +
                    "This also uses &lword boundaries&r meaning it will match whole words, not part of a word. " +
                    "Eg if your filter has 'hell' then 'hello' will not be filtered.")
                hasRunInfo = true
            }
        }

        listener<ClientChatReceivedEvent> {
            if (isDetected(it.message.unformattedText)) it.isCanceled = true
        }
    }

    private fun isDetected(message: String): Boolean {
        return if (!filterOwn && MessageDetection.Message.SELF detect message
            || !filterDMs && MessageDetection.Direct.ANY detect message) {
            false
        } else {
            chatFilter.all { it.containsMatchIn(message) }
        }
    }
}
