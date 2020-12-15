package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.util.text.TextFormatting

object HelpCommand : ClientCommand(
    name = "help",
    description = "FAQ and command list"
) {
    init {
        literal("commands", "cmds") {
            execute("List available commands") {
                val commands = CommandManager
                    .getCommands()
                    .sortedBy { it.name }

                MessageSendHelper.sendChatMessage("Available commands: (${TextFormatting.GRAY}${commands.size}${TextFormatting.RESET})")
                commands.forEach {
                    MessageSendHelper.sendRawChatMessage("  ${it.name}\n    ${TextFormatting.GRAY}${it.description}")
                }
            }
        }

        string("command") { commandArg ->
            execute("List help for a command") {
                val cmd = CommandManager.getCommandOrNull(commandArg.value) ?: run {
                    MessageSendHelper.sendErrorMessage("Could not find command '${TextFormatting.GRAY}${commandArg.value}${TextFormatting.RESET}'!")
                    return@execute
                }

                MessageSendHelper.sendChatMessage("Help for command [${cmd.name}]\n" + cmd.printArgHelp())
            }
        }

        execute("Print FAQ") {
            MessageSendHelper.sendChatMessage("General FAQ:\n" +
                "How do I use Baritone? - [${TextFormatting.GRAY}${prefix}b${TextFormatting.RESET}]\n" +
                "How do I change ${TextFormatting.GRAY};${TextFormatting.RESET} to something else? - [${TextFormatting.GRAY}${prefix}prefix${TextFormatting.RESET}]\n" +
                "How do I get a Cape? - Donate, or contribute to one of our projects.\n" +
                "Other questions? - Get support at ${TextFormatting.BLUE}${KamiMod.WEBSITE_LINK}/discord"
            )

        }
    }
}