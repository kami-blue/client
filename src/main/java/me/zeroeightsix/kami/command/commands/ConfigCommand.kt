package me.zeroeightsix.kami.command.commands

import kotlinx.coroutines.*
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Paths

object ConfigCommand : ClientCommand(
    name = "config",
    alias = arrayOf("cfg"),
    description = "Change config saving path or manually save and reload your config"
) {
    init {
        literal("reload") {
            execute("Reload configs from storage") {
                commandScope.launch(Dispatchers.IO) {
                    val loaded = ConfigUtils.loadAll()
                    if (loaded) MessageSendHelper.sendChatMessage("All configurations reloaded!")
                    else MessageSendHelper.sendErrorMessage("Failed to load config!")
                }
            }
        }

        literal("save") {
            execute("Force save configs") {
                commandScope.launch(Dispatchers.IO) {
                    val saved = ConfigUtils.saveAll()
                    if (saved) MessageSendHelper.sendChatMessage("All configurations saved!")
                    else MessageSendHelper.sendErrorMessage("Failed to load config!")
                }
            }
        }

        literal("path") {
            string("path") { pathArg ->
                execute("Switch config files") {
                    commandScope.launch(Dispatchers.IO) {
                        val newPath = pathArg.value

                        if (!ConfigUtils.isPathValid(newPath)) {
                            MessageSendHelper.sendChatMessage("&b$newPath&r is not a valid path")
                            return@launch
                        }

                        try {
                            FileWriter(File("KAMILastConfig.txt"), false).use {
                                it.write(newPath)
                                ConfigUtils.loadAll()
                                MessageSendHelper.sendChatMessage("Configuration path set to &b$newPath&r!")
                            }
                        } catch (e: IOException) {
                            MessageSendHelper.sendChatMessage("Couldn't set path: " + e.message)
                            KamiMod.LOG.warn("Couldn't set path!", e)
                        }
                    }
                }
            }

            execute("Print current config files") {
                commandScope.launch(Dispatchers.IO) {
                    val path = Paths.get(ConfigUtils.getConfigName())
                    MessageSendHelper.sendChatMessage("Path to configuration: &b" + path.toAbsolutePath().toString())
                }
            }
        }
    }
}