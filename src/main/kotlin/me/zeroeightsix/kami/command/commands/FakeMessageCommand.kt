package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.module.modules.chat.ChatTimestamp
import me.zeroeightsix.kami.util.text.MessageSendHelper

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