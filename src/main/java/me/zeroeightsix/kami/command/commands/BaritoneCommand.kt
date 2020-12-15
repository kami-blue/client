package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.command.CommandOld
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser
import me.zeroeightsix.kami.util.text.MessageSendHelper

object BaritoneCommand : ClientCommand(
    name = "Baritone",
    alias = arrayOf("b")
) {
    init {
        greedy("arguments") { args ->
            execute {
                val newArgs = CommandManager.tryParseArgument(args.value)?: return@execute
                MessageSendHelper.sendBaritoneCommand(*newArgs)
            }
        }
    }

}