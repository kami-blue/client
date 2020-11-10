package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.DependantParser
import me.zeroeightsix.kami.command.syntax.parsers.DependantParser.Dependency
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser
import me.zeroeightsix.kami.setting.GenericConfig
import me.zeroeightsix.kami.setting.ModuleConfig
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import java.io.IOException

/**
 * Created by 086 on 14/10/2018.
 * Updated by Xiaro on 21/08/20
 */
class ConfigCommand : Command("config", ChunkBuilder()
        .append("mode", true, EnumParser(arrayOf("reload", "save", "path")))
        .append("path", true, DependantParser(0, Dependency(arrayOf(arrayOf("path", "path")), "")))
        .build(), "cfg") {
    override fun call(args: Array<String?>) {
        if (args[0] == null) {
            MessageSendHelper.sendChatMessage("Missing argument &bmode&r: Choose from reload, save or path")
            return
        }

        when (args[0]!!.toLowerCase()) {
            "reload" -> {
                val loaded = ConfigUtils.loadAll()
                if (loaded) MessageSendHelper.sendChatMessage("All configurations reloaded!")
                else MessageSendHelper.sendErrorMessage("Failed to load config!")
            }

            "save" -> {
                val saved = ConfigUtils.saveAll()
                if (saved) MessageSendHelper.sendChatMessage("All configurations saved!")
                else MessageSendHelper.sendErrorMessage("Failed to save config!")
            }

            "path" -> if (args[1] == null) {
                MessageSendHelper.sendChatMessage("Path to configuration: &b" + ModuleConfig.currentPath)
            } else {
                val newPath = args[1]!!
                if (!ConfigUtils.isFilenameValid(newPath)) {
                    MessageSendHelper.sendChatMessage("&b$newPath&r is not a valid path")
                }
                val prevPath = ModuleConfig.currentPath.value
                try {
                    ConfigUtils.saveConfig(ModuleConfig)
                    ModuleConfig.currentPath.value = newPath
                    ConfigUtils.saveConfig(GenericConfig)
                    ConfigUtils.loadAll()
                    MessageSendHelper.sendChatMessage("Configuration path set to &b$newPath&r!")
                } catch (e: IOException) {
                    ModuleConfig.currentPath.value = prevPath
                    ConfigUtils.saveConfig(ModuleConfig)
                    e.printStackTrace()
                    MessageSendHelper.sendChatMessage("Couldn't set path: " + e.message)
                }
            }

            else -> MessageSendHelper.sendChatMessage("Incorrect mode, please choose from: reload, save or path")
        }
    }

    init {
        setDescription("Change where your config is saved or manually save and reload your config")
    }
}