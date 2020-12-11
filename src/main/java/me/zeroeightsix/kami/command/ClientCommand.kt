package me.zeroeightsix.kami.command

import org.kamiblue.command.CommandBuilder
import org.kamiblue.command.ExecuteEvent

abstract class ClientCommand(
    name: String,
    alias: Array<out String> = emptyArray(),
    description: String = "No description",
) : CommandBuilder<ExecuteEvent>(name, alias, description) {

    val chatLabel = "[$name]"

}