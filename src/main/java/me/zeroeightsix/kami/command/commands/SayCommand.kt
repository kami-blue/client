package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendServerMessage
import me.zeroeightsix.kami.command.CommandOld
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.util.text.MessageSendHelper
import java.lang.StringBuilder

object SayCommand : ClientCommand(
    name = "say",
    description = "Allows you to send any message, even with a prefix in it."
) {
    init {
        greedy("message") { messageArg ->
            execute {
                sendServerMessage(messageArg.value.trim())
            }
        }
    }
}