package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser
import me.zeroeightsix.kami.manager.managers.MacroManager
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.text.MessageSendHelper

class MacroCommand : Command("macro", ChunkBuilder().append("command", true, EnumParser(arrayOf("key", "list"))).append("setting", false, EnumParser(arrayOf("clear", "message|command"))).build(), "m") {
    override fun call(args: Array<out String?>) {
        val rKey = args[0] ?: return
        val keycode = Wrapper.getKey(rKey)

        if (keycode == 0 && !rKey.equals("list", true)) {
            MessageSendHelper.sendErrorMessage("Unknown key '&7$rKey&f'! Left alt is &7lmenu&f, left Control is &7lcontrol&f and ` is &7grave&f. You cannot bind the &7meta&f key.")
            return
        }

        when {
            args[0] == null -> { /* key, error message is caught by the command handler but you don't want to continue the rest */
                return
            }

            args[0].equals("list", ignoreCase = true) -> {
                if (MacroManager.isEmpty) {
                    MessageSendHelper.sendChatMessage("You have no macros")
                } else {
                    MessageSendHelper.sendChatMessage("You have the following macros: ")
                    for ((key, value) in MacroManager.macros) {
                        MessageSendHelper.sendRawChatMessage(Wrapper.getKeyName(key) + ": $value")
                    }
                }
            }

            args[1] == null -> { /* message */
                val keyList = MacroManager.getMacros(keycode)
                if (keyList == null || keyList.isEmpty()) {
                    MessageSendHelper.sendChatMessage("'&7$rKey&f' has no macros")
                    return
                }
                val message = "'&7$rKey&f' has the following macros:\n" +
                    keyList.joinToString("\n")
                MessageSendHelper.sendChatMessage(message)
            }

            args[1].equals("clear", ignoreCase = true) -> {
                MacroManager.removeMacro(keycode)
                MacroManager.saveMacros()
                MacroManager.loadMacros()
                MessageSendHelper.sendChatMessage("Cleared macros for '&7$rKey&f'")
            }

            args[2] != null -> { /* some random 3rd argument which shouldn't exist */
                MessageSendHelper.sendWarningMessage("$chatLabel Your macro / command must be inside quotes, as 1 argument in the command. Example: &7" +
                    getCommandPrefix() + label + " R \";set AutoSpawner debug toggle\"")
            }

            else -> {
                val macro = args[1]!!
                MacroManager.addMacroToKey(keycode, macro)
                MacroManager.saveMacros()
                MessageSendHelper.sendChatMessage("Added macro '&7$macro&f' for key '&7$rKey&f'")
            }
        }
    }
}