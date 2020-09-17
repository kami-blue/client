package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.module.modules.chat.ChatEncryption
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendWarningMessage

class ChatEncryptionCommand : Command("chatencryption", ChunkBuilder().append("delimiter").build(), "delimiter") {
    override fun call(args: Array<String?>) {
        if (!ChatEncryption.isEnabled) {
            sendWarningMessage("&6Warning: The ChatEncryption module is not enabled!")
            sendWarningMessage("The command will still work, but will not visibly do anything.")
        }
        for (s in args) {
            if (s == null) {
                sendChatMessage("Delimiter is currently: &7${ChatEncryption.delimiterValue.value}")
                continue
            }
            if (s.length > 1) {
                sendErrorMessage("Delimiter can only be 1 character long")
                return
            }
            ChatEncryption.delimiterValue.value = s
            sendChatMessage("Set the delimiter to <$s>")
        }
    }

    init {
        setDescription("Allows you to customize ChatEncryption's delimiter")
    }
}