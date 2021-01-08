package me.zeroeightsix.kami.command.commands

import kotlinx.coroutines.*
import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.module.modules.client.Configurations
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.defaultScope

object ConfigCommand : ClientCommand(
    name = "config",
    alias = arrayOf("cfg"),
    description = "Change config saving path or manually save and reload your config"
) {
    init {
        literal("all") {
            literal("reload") {
                execute("Reload all configs") {
                    defaultScope.launch(Dispatchers.IO) {
                        val loaded = ConfigUtils.loadAll()
                        if (loaded) MessageSendHelper.sendChatMessage("All configurations reloaded!")
                        else MessageSendHelper.sendErrorMessage("Failed to load config!")
                    }
                }
            }

            literal("save") {
                execute("Save all configs") {
                    defaultScope.launch(Dispatchers.IO) {
                        val saved = ConfigUtils.saveAll()
                        if (saved) MessageSendHelper.sendChatMessage("All configurations saved!")
                        else MessageSendHelper.sendErrorMessage("Failed to load config!")
                    }
                }
            }
        }

        enum<Configurations.ConfigType>("config type") { configTypeArg ->
            literal("reload") {
                execute("Reload a config") {
                    configTypeArg.value.reload()
                }
            }

            literal("save") {
                execute("Save a config") {
                    configTypeArg.value.save()
                }
            }

            literal("set") {
                string("name") { nameArg ->
                    execute("Change preset for a config") {
                        configTypeArg.value.preset(nameArg.value)
                    }
                }
            }

            literal("list") {
                execute("List all available presets for a config") {
                    configTypeArg.value.printAllPresets()
                }
            }

            literal("server") {
                literal("add", "new", "create") {
                    executeSafe("Create a new server preset for a config") {
                        val ip = mc.currentServerData?.serverIP

                        if (ip == null || mc.isIntegratedServerRunning) {
                            MessageSendHelper.sendWarningMessage("You are not in a server!")
                            return@executeSafe
                        }

                        configTypeArg.value.newServerPreset(ip)
                    }
                }

                literal("list") {
                    execute("List all available server presets for a config") {
                        configTypeArg.value.printAllServerPreset()
                    }
                }
            }

            execute("Print current preset name") {
                configTypeArg.value.printCurrentPreset()
            }
        }
    }
}