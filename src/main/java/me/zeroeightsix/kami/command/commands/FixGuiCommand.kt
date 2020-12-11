package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.module.modules.hidden.FixGui
import me.zeroeightsix.kami.util.text.MessageSendHelper


object FixGuiCommand : ClientCommand(
    name = "fixgui",
    alias = arrayOf("fixmygui"),
    description = "Allows you to disable the automatic gui positioning!"
) {
    init {
        execute {
            FixGui.enable()
            MessageSendHelper.sendChatMessage(chatLabel + "Ran")
        }
    }
}