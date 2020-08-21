package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.DependantParser
import me.zeroeightsix.kami.command.syntax.parsers.DependantParser.Dependency
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.MessageSendHelper
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

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
                Thread{
                    val loaded = ConfigUtils.loadAll()
                    if (loaded) MessageSendHelper.sendChatMessage("Configuration, macros and waypoints reloaded!")
                    else MessageSendHelper.sendErrorMessage("Failed to load config!")
                }.start()
            }

            "save" -> {
                Thread {
                    val saved = ConfigUtils.saveAll()
                    if (saved) MessageSendHelper.sendChatMessage("Configuration, macros and waypoints saved!")
                    else MessageSendHelper.sendErrorMessage("Failed to load config!")
                }.start()
            }


            "path" -> if (args[1] == null) {
                val file = Paths.get(KamiMod.getConfigName())
                MessageSendHelper.sendChatMessage("Path to configuration: &b" + file.toAbsolutePath().toString())
            } else {
                val newPath = args[1]!!
                if (!KamiMod.isFilenameValid(newPath)) {
                    MessageSendHelper.sendChatMessage("&b$newPath&r is not a valid path")
                }
                try {
                    Files.newBufferedWriter(Paths.get("KAMILastConfig.txt")).use { writer ->
                        writer.write(newPath)
                        ConfigUtils.loadAll()
                        MessageSendHelper.sendChatMessage("Configuration path set to &b$newPath&r!")
                    }
                } catch (e: IOException) {
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