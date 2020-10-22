package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.module.modules.chat.ChatTimestamp
import me.zeroeightsix.kami.util.text.MessageSendHelper

/**
 * @author l1ving
 */
class FakeMessageCommand : Command("fakemsg", ChunkBuilder().append("message").build()) {
    override fun call(args: Array<out String?>) {
        when {
            args[1] != null -> {
                MessageSendHelper.sendErrorMessage(chatLabel + "You must send your entire message inside \"double quotes\". Use &7&&f to add colours.")
                return
            }
        }
        MessageSendHelper.sendRawChatMessage(getTime() + args[0]?.replace('&', KamiMod.color))
    }

    private fun getTime(): String {
        return if (ChatTimestamp.isEnabled) ChatTimestamp.formattedTime else ""
    }
}