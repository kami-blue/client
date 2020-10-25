package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser
import me.zeroeightsix.kami.command.syntax.parsers.ModuleParser
import me.zeroeightsix.kami.module.ModuleManager.getModule
import me.zeroeightsix.kami.setting.ModuleConfig
import me.zeroeightsix.kami.setting.impl.primitive.EnumSetting
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendStringChatMessage

class SetCommand : Command("set", ChunkBuilder()
        .append("module", true, ModuleParser())
        .append("setting", true)
        .append("set", true, EnumParser(arrayOf("value", "toggle")))
        .build()) {
    override fun call(args: Array<String?>) {
        if (args[0] == null) {
            sendChatMessage("Please specify a module!")
            return
        }

        val module = getModule(args[0])

        if (module == null) {
            sendChatMessage("Unknown module &b" + args[0] + "&r!")
            return
        }

        if (args[1] == null) {
            val settings = module.fullSettingList.joinToString()
            if (settings.isEmpty()) sendChatMessage("Module &b" + module.name.value + "&r has no settings.") else {
                sendStringChatMessage(arrayOf(
                        "Please specify a setting! Choose one of the following:", settings
                ))
            }
            return
        }

        val setting = ModuleConfig.getGroup(module.category.categoryName)?.getGroup(module.originalName)?.getSetting(args[1]!!)
        if (setting == null) {
            sendChatMessage("Unknown setting &b" + args[1] + "&r in &b" + module.name.value + "&r!")
            return
        }

        var arg2 = args[2]
        if (arg2 == null) {
            sendChatMessage("&b" + setting.name + "&r is a &3" + setting.value.javaClass.simpleName + "&r. Its current value is &3" + setting.toString())
            return
        }

        try {
            if (setting is EnumSetting) arg2 = arg2.toUpperCase()
            setting.setValue(arg2)
            sendChatMessage("Set &b" + setting.name + "&r to &3" + setting.toString() + "&r.")
        } catch (e: Exception) {
            sendChatMessage("Unable to set value! &6" + e.message)
        }
    }

    init {
        setDescription("Change the setting of a certain module")
    }
}