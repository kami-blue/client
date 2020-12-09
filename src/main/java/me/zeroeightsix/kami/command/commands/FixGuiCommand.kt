package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.CommandOld
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.module.modules.hidden.FixGui
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage

/**
 * @author l1ving
 *
 * Created by l1ving on 24/03/20
 * Updated by Xiaro on 28/08/20
 */
class FixGuiCommand : CommandOld("fixgui", ChunkBuilder().build()) {

    override fun call(args: Array<String>) {
        FixGui.enable()
        sendChatMessage(chatLabel + "Ran")
    }

    init {
        setDescription("Allows you to disable the automatic gui positioning")
    }
}