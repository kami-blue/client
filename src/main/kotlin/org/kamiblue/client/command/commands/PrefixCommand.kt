package org.kamiblue.client.command.commands

import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.module.modules.client.CommandConfig
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.formatValue

object PrefixCommand : ClientCommand(
    name = "prefix",
    description = "Change command prefix"
) {
    init {
        literal("reset") {
            execute("Reset the prefix to ;") {
                CommandConfig.prefix = ";"
                MessageSendHelper.sendChatMessage("Reset prefix to [&7;&f]!")
            }
        }

        string("new prefix") { prefixArg ->
            execute("Set a new prefix") {
                if (prefixArg.value.isEmpty() || prefixArg.value == "\\") {
                    CommandConfig.prefix = ";"
                    MessageSendHelper.sendChatMessage("Reset prefix to [&7;&f]!")
                    return@execute
                }

                CommandConfig.prefix = prefixArg.value
                MessageSendHelper.sendChatMessage("Set prefix to ${formatValue(prefixArg.value)}!")
            }
        }
    }
}