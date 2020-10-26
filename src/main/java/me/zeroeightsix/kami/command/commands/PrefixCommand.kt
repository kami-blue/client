package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.util.text.MessageSendHelper

/**
 * Created by 086 on 10/10/2018.
 */
class PrefixCommand : Command("prefix", ChunkBuilder().append("character").build()) {
    override fun call(args: Array<String?>) {
        if (args.isEmpty()) {
            MessageSendHelper.sendChatMessage("Please specify a new prefix!")
            return
        }
        when {
            args[0] == "\\" -> {
                MessageSendHelper.sendChatMessage("Error: \"\\\" is not a supported prefix")
            }
            args[0] != null -> {
                commandPrefix.setValue(args[0]!!)
                MessageSendHelper.sendChatMessage("Prefix set to &b" + commandPrefix.value)
            }
            else -> {
                MessageSendHelper.sendChatMessage("Please specify a new prefix!")
            }
        }

    }

    init {
        setDescription("Changes the prefix to your new key")
    }
}