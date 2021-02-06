package org.kamiblue.client.command.commands

import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.module.modules.chat.ChatTimestamp
import org.kamiblue.client.util.text.MessageSendHelper

object FakeMessageCommand : ClientCommand(
    name = "fakemsg",
    alias = arrayOf("fm", "fakemsg"),
    description = "Send a client side fake message, use & with formatting codes."
) {
    init {
        greedy("message") { messageArg ->
            execute("Use & for color formatting") {
                MessageSendHelper.sendRawChatMessage(getTime() + messageArg.value.replace('&', '§'))
            }
        }
    }

    private fun getTime() = if (ChatTimestamp.isEnabled) ChatTimestamp.formattedTime else ""
}