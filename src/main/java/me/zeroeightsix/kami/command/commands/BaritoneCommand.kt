package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.util.text.MessageSendHelper

object BaritoneCommand : ClientCommand(
    name = "baritone",
    alias = arrayOf("b")
) {
    init {
        greedy("arguments") { args ->
            executeSafe {
                val newArgs = CommandManager.tryParseArgument(args.value) ?: return@executeSafe
                MessageSendHelper.sendBaritoneCommand(*newArgs)
            }
        }
    }

}