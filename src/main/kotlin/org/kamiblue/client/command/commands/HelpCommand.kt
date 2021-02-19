package org.kamiblue.client.command.commands

import net.minecraft.util.text.TextFormatting
import org.kamiblue.client.KamiMod
import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.command.CommandManager
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.formatValue

object HelpCommand : ClientCommand(
    name = "help",
    description = "FAQ and command list"
) {
    init {
        literal("commands", "cmds", "list") {
            execute("List available commands") {
                val commands = CommandManager
                    .getCommands()
                    .sortedBy { it.name }

                MessageSendHelper.sendChatMessage("Available commands: ${formatValue(commands.size)}")
                commands.forEach {
                    MessageSendHelper.sendRawChatMessage("$prefix${it.name}\n" +
                        "${TextFormatting.GRAY}${it.description}\n"
                    )
                }
            }
        }

        string("command") { commandArg ->
            execute("List help for a command") {
                val cmd = CommandManager.getCommandOrNull(commandArg.value) ?: run {
                    MessageSendHelper.sendErrorMessage("Could not find command ${formatValue(commandArg.value)}!")
                    return@execute
                }

                MessageSendHelper.sendChatMessage("Help for command ${formatValue("$prefix${cmd.name}")}\n"
                    + cmd.printArgHelp()
                )
            }
        }

        execute("Print FAQ") {
            MessageSendHelper.sendChatMessage("General FAQ:\n" +
                "How do I see all commands? - ${formatValue("$prefix${name} commands")}\n" +
                "How do I use Baritone? - ${formatValue("${prefix}b")}\n" +
                "How do I change ${TextFormatting.GRAY};${TextFormatting.RESET} to something else? - ${formatValue("${prefix}prefix")}\n" +
                "How do I get a Cape? - Donate, or contribute to one of our projects.\n" +
                "Other questions? - Get support at ${TextFormatting.BLUE}${KamiMod.WEBSITE_LINK}/discord"
            )
        }
    }
}