package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.module.ModuleManager.getModuleT
import me.zeroeightsix.kami.module.modules.hidden.FixGui
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage

/**
 * @author dominikaaaa
 *
 * Created by dominikaaaa on 24/03/20
 * Updated by Xiaro on 28/08/20
 */
class FixGuiCommand : Command("fixgui", ChunkBuilder().build()) {

    override fun call(args: Array<String>) {
        val fixGui = getModuleT(FixGui::class.java)?: return
        fixGui.enable()
        sendChatMessage(chatLabel + "Ran")
    }

    init {
        setDescription("Allows you to disable the automatic gui positioning")
    }
}