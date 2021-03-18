package org.kamiblue.client.command.commands

import net.minecraft.util.text.TextFormatting
import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.module.ModuleManager
import org.kamiblue.client.module.modules.client.CommandConfig
import org.kamiblue.client.util.KeyboardUtils
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.format
import org.kamiblue.client.util.text.formatValue

object BindCommand : ClientCommand(
    name = "bind",
    description = "Bind and unbind modules"
) {
    init {
        literal("list") {
            execute("List used module binds") {
                val modules = ModuleManager.modules.filter { it.bind.value.key > 0 }.sortedBy { it.bind.toString() }

                MessageSendHelper.sendChatMessage("Used binds: ${formatValue(modules.size)}")
                modules.forEach {
                    MessageSendHelper.sendRawChatMessage("${formatValue(it.bind)} ${it.name}")
                }
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

                    if (bind.equals("none", true)) {
                        module.bind.resetValue()
                        MessageSendHelper.sendChatMessage("Reset bind for ${module.name}!")
                        return@execute
                    }


                    val key = KeyboardUtils.getKey(bind)
                    if (key <= 0) {
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