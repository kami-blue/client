package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.util.text.MessageSendHelper

object HelpCommand : ClientCommand(
    name = "help",
    description = "FAQ and command list"
) {
    init {
        literal("commands", "cmds") {
            execute("List available commands") {
                val commands = CommandManager
                    .getCommands()
                    .sortedWith(compareBy { it.name })

                MessageSendHelper.sendChatMessage("Available commands: (&7${commands.size}&f)")
                commands.forEach {
                    MessageSendHelper.sendRawChatMessage("  ${it.name}\n    &7${it.description}")
                }
            }
        }

        string("command") { commandArg ->
            execute("List help for a command") {
                val cmd = CommandManager.getCommandOrNull(commandArg.value) ?: run {
                    MessageSendHelper.sendErrorMessage("Could not find command '&7${commandArg.value}&f'!")
                    return@execute
                }

                MessageSendHelper.sendChatMessage("Help for command [${cmd.name}]\n" + cmd.printArgHelp())
            }
        }

        execute("Print FAQ") {
            MessageSendHelper.sendChatMessage("General FAQ:\n" +
                "How do I use Baritone? - [&7${prefix}b&f]\n" +
                "How do I change &7;&f to something else? - [&7${prefix}prefix&f]\n" +
                "How do I get a Cape? - Donate, or contribute to one of our projects.\n" +
                "Other questions? - Get support at &9${KamiMod.WEBSITE_LINK}/discord"
            )

        }
    }
}