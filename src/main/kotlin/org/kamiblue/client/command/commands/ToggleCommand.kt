package org.kamiblue.client.command.commands

import net.minecraft.util.text.TextFormatting
import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.module.modules.client.ClickGUI
import org.kamiblue.client.module.modules.client.CommandConfig
import org.kamiblue.client.util.text.MessageSendHelper.sendChatMessage

object ToggleCommand : ClientCommand(
    name = "toggle",
    alias = arrayOf("switch", "t"),
    description = "Toggle a module on and off!"
) {
    init {
        module("module") { moduleArg ->
            execute {
                val module = moduleArg.value
                module.toggle()
                if (module !is ClickGUI && !CommandConfig.toggleMessages) {
                    sendChatMessage(module.name +
                        if (module.isEnabled) " ${TextFormatting.GREEN}enabled"
                        else " ${TextFormatting.RED}disabled"
                    )
                }
            }
        }
    }
}