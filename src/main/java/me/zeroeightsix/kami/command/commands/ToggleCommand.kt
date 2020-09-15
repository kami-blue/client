package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.ModuleParser
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.module.ModuleManager.ModuleNotFoundException
import me.zeroeightsix.kami.module.ModuleManager.getModule
import me.zeroeightsix.kami.module.modules.ClickGUI
import me.zeroeightsix.kami.module.modules.client.CommandConfig
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage

/**
 * Created by 086 on 17/11/2017.
 */
class ToggleCommand : Command("toggle", ChunkBuilder()
        .append("module", true, ModuleParser())
        .build(), "t") {

    override fun call(args: Array<String>) {
        if (args.isEmpty()) {
            sendChatMessage("Please specify a module!")
            return
        }

        try {
            getModule(args[0])?.let {
                it.toggle()
                if (it !is ClickGUI && !ModuleManager.getModuleT(CommandConfig::class.java)!!.toggleMessages.value) {
                    sendChatMessage(it.name.value + if (it.isEnabled) " &aenabled" else " &cdisabled")
                }
            }
        } catch (x: ModuleNotFoundException) {
            sendChatMessage("Unknown module '" + args[0] + "'")
        }
    }

    init {
        setDescription("Quickly toggle a module on and off")
    }
}