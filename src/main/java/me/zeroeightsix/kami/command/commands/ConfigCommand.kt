package me.zeroeightsix.kami.command.commands

import kotlinx.coroutines.*
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.setting.GenericConfig
import me.zeroeightsix.kami.setting.ModuleConfig
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.formatValue
import me.zeroeightsix.kami.util.threads.defaultScope
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
                defaultScope.launch(Dispatchers.IO) {
                    val loaded = ConfigUtils.loadAll()
                    if (loaded) MessageSendHelper.sendChatMessage("All configurations reloaded!")
                    else MessageSendHelper.sendErrorMessage("Failed to load config!")
                }
            }
        }

        literal("save") {
            execute("Force save configs") {
                defaultScope.launch(Dispatchers.IO) {
                    val saved = ConfigUtils.saveAll()
                    if (saved) MessageSendHelper.sendChatMessage("All configurations saved!")
                    else MessageSendHelper.sendErrorMessage("Failed to load config!")
                }
            }
        }

        literal("path") {
            string("path") { pathArg ->
                execute("Switch config files") {
                    defaultScope.launch(Dispatchers.IO) {
                        val newPath = pathArg.value

                        if (!ConfigUtils.isPathValid(newPath)) {
                            MessageSendHelper.sendChatMessage("&b$newPath&r is not a valid path")
                            return@launch
                        }
                        val prevPath = ModuleConfig.currentPath

                        try {
                            ConfigUtils.saveConfig(ModuleConfig)
                            ModuleConfig.currentPath = newPath
                            ConfigUtils.saveConfig(GenericConfig)
                            ConfigUtils.loadAll()
                            MessageSendHelper.sendChatMessage("Configuration path set to &b$newPath&r!")
                        } catch (e: IOException) {
                            MessageSendHelper.sendChatMessage("Couldn't set path: " + e.message)
                            KamiMod.LOG.warn("Couldn't set path!", e)
                            ModuleConfig.currentPath = prevPath
                            ConfigUtils.saveConfig(ModuleConfig)
                        }
                    }
                }
            }

            execute("Print current config files") {
                defaultScope.launch(Dispatchers.IO) {
                    val path = Paths.get(ModuleConfig.currentPath).toAbsolutePath()
                    MessageSendHelper.sendChatMessage("Path to configuration: ${formatValue(path)}")
                }
            }
        }
    }
}