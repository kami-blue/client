package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser
import me.zeroeightsix.kami.module.MacroManager
import me.zeroeightsix.kami.module.FileInstanceManager
import me.zeroeightsix.kami.util.Macro
import me.zeroeightsix.kami.util.MessageSendHelper.*
import me.zeroeightsix.kami.util.Wrapper

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 04/05/20
 */
class MacroCommand : Command("macro", ChunkBuilder().append("command", true, EnumParser(arrayOf("key", "list"))).append("setting", false, EnumParser(arrayOf("clear", "message|command"))).build(), "m") {
    override fun call(args: Array<out String?>) {
        val rKey = args[0]
        val macro = args[1]
        val key = Wrapper.getKey(rKey)

        if (key == 0 && !rKey.equals("list", true)) {
            sendErrorMessage("Unknown key '&7$rKey&f'! Left alt is &7lmenu&f, left Control is &7lcontrol&f and ` is &7grave&f. You cannot bind the &7meta&f key.")
            return
        }

        val keyList: List<String?>? = Macro.getMacrosForKey(key)

        when {
            args[0] == null -> { /* key, error message is caught by the command handler but you don't want to continue the rest */
                return
            }
            args[0] == "list" -> {
                if (FileInstanceManager.macros.isEmpty()) {
                    sendChatMessage("You have no macros")
                    return
                }
                sendChatMessage("You have the following macros: ")
                for ((key1, value) in FileInstanceManager.macros) {
                    sendRawChatMessage(Wrapper.getKeyName(key1) + ": $value")
                }
                return
            }
            args[1] == null -> { /* message */
                if (keyList == null || keyList.isEmpty()) {
                    sendChatMessage("'&7$rKey&f' has no macros")
                    return
                }
                sendChatMessage("'&7$rKey&f' has the following macros: ")
                Macro.sendMacrosToChat(keyList.toTypedArray())
                return
            }
            args[1] == "clear" -> {
                Macro.removeMacro(key)
                MacroManager.saveMacros()
                MacroManager.loadMacros()
                sendChatMessage("Cleared macros for '&7$rKey&f'")
                return
            }
            args[2] != null -> { /* some random 3rd argument which shouldn't exist */
                sendWarningMessage("$chatLabel Your macro / command must be inside quotes, as 1 argument in the command. Example: &7" + getCommandPrefix() + label + " R \";set AutoSpawner debug toggle\"")
                return
            }
            else -> {
                Macro.addMacroToKey(key, macro)
                MacroManager.saveMacros()
                sendChatMessage("Added macro '&7$macro&f' for key '&7$rKey&f'")
            }
        }
    }
}