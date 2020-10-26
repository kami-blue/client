package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.module.modules.chat.CustomChat
import me.zeroeightsix.kami.util.text.MessageSendHelper

class CustomChatCommand : Command("customchat", ChunkBuilder().append("ending").build(), "chat") {
    override fun call(args: Array<String?>) {
        if (!CustomChat.isEnabled) {
            MessageSendHelper.sendWarningMessage("&6Warning: The CustomChat module is not enabled!")
            MessageSendHelper.sendWarningMessage("The command will still work, but will not visibly do anything.")
        }
        if (CustomChat.isCustomMode) {
            MessageSendHelper.sendWarningMessage("&6Warning: You don't have custom mode enabled in CustomChat!")
            MessageSendHelper.sendWarningMessage("The command will still work, but will not visibly do anything.")
        }
        for (s in args) {
            if (s == null) continue
            CustomChat.customText.setValue(s)
            MessageSendHelper.sendChatMessage("Set the Custom Text Mode to <$s>")
        }
    }

    init {
        setDescription("Allows you to customize CustomChat's custom setting")
    }
}