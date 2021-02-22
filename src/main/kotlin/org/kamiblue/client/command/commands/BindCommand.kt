package org.kamiblue.client.command.commands

import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.module.ModuleManager
import org.kamiblue.client.util.KeyboardUtils
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.formatValue

object BindCommand : ClientCommand(
    name = "bind",
    description = "Bind and unbind modules"
) {
    init {
        literal("list") {
            execute("List used module binds") {
                val binds = ModuleManager.modules.asSequence()
                    .filter { it.bind.value.key in 1..255 }
                    .map { "${formatValue(it.bind)} ${it.name}" }
                    .sorted()
                    .toList()

                MessageSendHelper.sendChatMessage("Used binds: ${formatValue(binds.size)}")
                binds.forEach(MessageSendHelper::sendRawChatMessage)
            }
        }

        literal("reset", "unbind") {
            module("module") { moduleArg ->
                execute("Reset the bind of a module to nothing") {
                    val module = moduleArg.value
                    module.bind.resetValue()
                    MessageSendHelper.sendChatMessage("Reset bind for ${module.name}!")
                }
            }
        }

        module("module") { moduleArg ->
            string("bind") { bindArg ->
                execute("Bind a module to a key") {
                    val module = moduleArg.value
                    val bind = bindArg.value

                    if (bind.equals("None", true)) {
                        module.bind.resetValue()
                        MessageSendHelper.sendChatMessage("Reset bind for ${module.name}!")
                        return@execute
                    }

                    val key = KeyboardUtils.getKey(bind)

                    if (key !in 1..255) {
                        KeyboardUtils.sendUnknownKeyError(bind)
                    } else {
                        module.bind.setValue(bind)
                        MessageSendHelper.sendChatMessage("Bind for ${module.name} set to ${formatValue(module.bind)}!")
                    }
                }
            }

            execute("Get the bind of a module") {
                val module = moduleArg.value
                MessageSendHelper.sendChatMessage("${module.name} is bound to ${formatValue(module.bind)}")
            }
        }
    }
}