package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser
import me.zeroeightsix.kami.module.modules.combat.VisualRange
import me.zeroeightsix.kami.util.text.MessageSendHelper

class VisualRangeCommand : Command("visualrange", ChunkBuilder()
        .append("mode", true, EnumParser(arrayOf("join","leave")))
        .append("message")
        .build(),"vr") {

    override fun call(args: Array<String>) {
        if (VisualRange == null)
            MessageSendHelper.sendErrorMessage("&cThe VisualRange Join Option Not Activated. Make sure to enable it for the message to appear.")
        if (args [0] == null) {
            MessageSendHelper.sendErrorMessage("&c Specify Join or Leave")
            return
        }
        if (args [1] == null) {
            MessageSendHelper.sendErrorMessage("&c Input Your Custom Message")
            return
        }
        if (args != null && args[0] != null) {
            when (args[0]!!.toLowerCase()) {
                "join" -> {
                    if (args[2] == null) {
                        VisualRange.VisualJoinMessage.value = args[1]
                        MessageSendHelper.sendChatMessage("Set the VisualRange join message to '&7" + args[1] + "&f'")
                        return
                    }
                }
                "leave" -> {
                    if (args[2] == null) {
                        VisualRange.VisualLeaveMessage.value = args[1]
                        MessageSendHelper.sendChatMessage("Set the VisualRange leave message to '&7" + args[1] + "&f'")
                        return
                    }
                }
                else -> MessageSendHelper.sendErrorMessage("Please enter a valid argument!")
            }
        }
    }
    init {
        setDescription("Allows you to customize VisualRange's message")
    }
}







