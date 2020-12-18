package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.command.CommandManager.colorFormatValue
import me.zeroeightsix.kami.util.text.MessageSendHelper

object PrefixCommand : ClientCommand(
    name = "prefix",
    description = "Change command prefix"
) {
    init {
        literal("reset") {
            execute("Reset the prefix to ;") {
                CommandManager.prefix.value = ";"
                MessageSendHelper.sendChatMessage("Reset prefix to [&7;&f]!")
            }
        }

        string("new prefix") { prefixArg ->
            execute("Set a new prefix") {
                if (prefixArg.value.isEmpty() || prefixArg.value == "\\") {
                    CommandManager.prefix.value = ";"
                    MessageSendHelper.sendChatMessage("Reset prefix to [&7;&f]!")
                    return@execute
                }

                CommandManager.prefix.value = prefixArg.value
                MessageSendHelper.sendChatMessage("Set prefix to ${prefixArg.value.colorFormatValue}!")
            }
        }
    }
}