package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.ModuleParser
import me.zeroeightsix.kami.module.ModuleManager.ModuleNotFoundException
import me.zeroeightsix.kami.module.ModuleManager.getModule
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage

class RenameModuleCommand : Command("renamemodule", ChunkBuilder().append("module", true, ModuleParser()).append("name").build()) {
    override fun call(args: Array<String?>) {
        val moduleName = args[0]

        try {
            val module = getModule(args[0])

            if (args.isEmpty() || moduleName == null || module == null) {
                sendChatMessage("Please specify a module!")
                return
            }

            val name = if (args.size == 1) module.originalName else moduleName
            if (!name.matches("[a-zA-Z]+".toRegex())) {
                sendChatMessage("Name must be alphabetic!")
                return
            }
            sendChatMessage("&b" + module.name.value + "&r renamed to &b" + name)
            module.name.setValue(name)
        } catch (x: ModuleNotFoundException) {
            sendChatMessage("Unknown module '" + args[0] + "'!")
        }
    }

    init {
        setDescription("Rename a module to something else")
    }
}