package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.module.modules.combat.AutoEZ
import me.zeroeightsix.kami.util.text.MessageSendHelper

class AutoEZCommand : Command("autoez", ChunkBuilder().append("message").build()) {
    override fun call(args: Array<String?>) {
        if (!AutoEZ.isEnabled) {
            MessageSendHelper.sendWarningMessage("&6Warning: The AutoEZ module is not enabled!")
            MessageSendHelper.sendWarningMessage("The command will still work, but will not visibly do anything.")
        }
        if (AutoEZ.messageMode.value != AutoEZ.MessageMode.CUSTOM) {
            MessageSendHelper.sendWarningMessage("&6Warning: You don't have custom mode enabled in AutoEZ!")
            MessageSendHelper.sendWarningMessage("The command will still work, but will not visibly do anything.")
        }
        for (s in args) {
            if (s == null) continue
            AutoEZ.customText.setValue(s)
            MessageSendHelper.sendChatMessage("Set the Custom Mode to <$s>")
        }
    }

    init {
        setDescription("Allows you to customize AutoEZ's custom setting")
    }
}