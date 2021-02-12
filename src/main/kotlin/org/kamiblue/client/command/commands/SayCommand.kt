package org.kamiblue.client.command.commands

import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.util.text.MessageSendHelper.sendServerMessage

object SayCommand : ClientCommand(
    name = "say",
    description = "Allows you to send any message, even with a prefix in it."
) {
    init {
        greedy("message") { messageArg ->
            executeSafe {
                sendServerMessage(messageArg.value.trim())
            }
        }
    }
}