package org.kamiblue.client.command.commands

import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.manager.managers.MacroManager
import org.kamiblue.client.util.KeyboardUtils
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.formatValue

object MacroCommand : ClientCommand(
    name = "macro",
    alias = arrayOf("m"),
    description = "Manage your command / message macros"
) {
    init {
        literal("list") {
            string("key") { keyArg ->
                execute("List macros for a key") {
                    val input = keyArg.value
                    val key = KeyboardUtils.getKey(input)

                    if (key !in 1..255) {
                        KeyboardUtils.sendUnknownKeyError(input)
                        return@execute
                    }

                    val macros = MacroManager.macros[key]
                    val formattedName = formatValue(KeyboardUtils.getDisplayName(key) ?: "Unknown")

                    if (macros.isNullOrEmpty()) {
                        MessageSendHelper.sendChatMessage("&cYou have no macros for the key $formattedName")
                    } else {
                        MessageSendHelper.sendChatMessage("You have has the following macros for $formattedName: ")
                        for (macro in macros) {
                            MessageSendHelper.sendRawChatMessage("$formattedName $macro")
                        }
                    }
                }
            }

            execute("List all macros") {
                if (MacroManager.isEmpty) {
                    MessageSendHelper.sendChatMessage("&cYou have no macros")
                } else {
                    MessageSendHelper.sendChatMessage("You have the following macros: ")
                    for ((key, value) in MacroManager.macros) {
                        val formattedName = formatValue(KeyboardUtils.getDisplayName(key) ?: "Unknown")
                        MessageSendHelper.sendRawChatMessage("$formattedName $value")
                    }
                }
            }
        }

        literal("clear") {
            string("key") { keyArg ->
                execute("Clear macros for a key") {
                    val input = keyArg.value
                    val key = KeyboardUtils.getKey(input)

                    if (key !in 1..255) {
                        KeyboardUtils.sendUnknownKeyError(input)
                        return@execute
                    }

                    val formattedName = formatValue(KeyboardUtils.getDisplayName(key) ?: "Unknown")

                    MacroManager.removeMacro(key)
                    MacroManager.saveMacros()
                    MacroManager.loadMacros()
                    MessageSendHelper.sendChatMessage("Cleared macros for $formattedName")
                }
            }
        }

        string("key") { keyArg ->
            greedy("command / message") { macroArg ->
                execute("Set a command / message for a key") {
                    val input = keyArg.value
                    val key = KeyboardUtils.getKey(input)

                    if (key !in 1..255) {
                        KeyboardUtils.sendUnknownKeyError(input)
                        return@execute
                    }

                    val macro = macroArg.value
                    val formattedName = formatValue(KeyboardUtils.getDisplayName(key) ?: "Unknown")

                    MacroManager.addMacroToKey(key, macro)
                    MacroManager.saveMacros()
                    MessageSendHelper.sendChatMessage("Added macro ${formatValue(macro)} for key $formattedName")
                }
            }
        }
    }
}