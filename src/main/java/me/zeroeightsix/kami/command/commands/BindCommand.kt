package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.module.modules.client.CommandConfig
import me.zeroeightsix.kami.util.KeyboardUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.format
import me.zeroeightsix.kami.util.text.formatValue
import net.minecraft.util.text.TextFormatting

object BindCommand : ClientCommand(
    name = "bind",
    description = "Bind and unbind modules"
) {
    init {
        literal("list") {
            execute("List used module binds") {
                val modules = ModuleManager.getModules().filter { it.bind.value.key > 0 }.sortedBy { it.bind.toString() }

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

        literal("modifiers") {
            boolean("enabled") { modifiersArg ->
                execute("Disallow binds while holding a modifier") {
                    val modifiers = modifiersArg.value

                    CommandConfig.modifierEnabled.value = modifiers
                    MessageSendHelper.sendChatMessage(
                        "Modifiers ${if (modifiers) " ${TextFormatting.GREEN format "enabled"}" else " ${TextFormatting.RED format "disabled"}"}"
                    )
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