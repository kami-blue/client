package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.module.modules.chat.AutoReply
import me.zeroeightsix.kami.util.text.MessageSendHelper

/**
 * @author l1ving
 * Created by l1ving on 17/02/20
 */
class AutoReplyCommand : Command("autoreply", ChunkBuilder().append("message").build(), "reply") {
    override fun call(args: Array<String>) {
        AutoReply.message.setValue(args[0])
        MessageSendHelper.sendChatMessage("Set the AutoReply message to '&7" + args[0] + "&f'")
        if (!AutoReply.customMessage.value) {
            MessageSendHelper.sendWarningMessage("&6Warning:&f You don't have '&7Custom Message&f' enabled in AutoReply!")
            MessageSendHelper.sendWarningMessage("The command will still work, but will not visibly do anything.")
        }
    }

    init {
        setDescription("Allows you to customize AutoReply's settings")
    }
}