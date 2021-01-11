package me.zeroeightsix.kami.command.commands

import kotlinx.coroutines.*
import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.event.SafeExecuteEvent
import me.zeroeightsix.kami.module.modules.client.Configurations
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.defaultScope

object ConfigCommand : ClientCommand(
    name = "config",
    alias = arrayOf("cfg"),
    description = "Change config saving path or manually save and reload your config"
) {
    private var lastName = ""
    private val confirmTimer = TickTimer(TimeUnit.SECONDS)

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
                    execute("Change preset") {
                        configTypeArg.value.setPreset(nameArg.value)
                    }
                }
            }

            literal("set") {
                string("name") { nameArg ->
                    execute("Change preset") {
                        configTypeArg.value.setPreset(nameArg.value)
                    }
                }
            }

            literal("copy", "ctrl+c", "ctrtc") {
                string("name") { nameArg ->
                    execute("Copy current preset to specific preset") {
                        val name = nameArg.value
                        if (!confirm(name)) return@execute

                        configTypeArg.value.copyPreset(name)
                    }
                }
            }

            literal("delete", "del", "remove") {
                string("name") { nameArg ->
                    execute("Delete specific preset") {
                        val name = nameArg.value
                        if (!confirm(name)) return@execute

                        configTypeArg.value.deletePreset(name)
                    }
                }
            }

            literal("list") {
                execute("List all available presets") {
                    configTypeArg.value.printAllPresets()
                }
            }

            literal("server") {
                literal("create", "new", "add") {
                    executeSafe("Create a new server preset") {
                        val ip = getIpOrNull() ?: return@executeSafe

                        configTypeArg.value.newServerPreset(ip)
                    }
                }

                literal("delete", "del", "remove") {
                    executeSafe("Delete the current server preset") {
                        val ip = getIpOrNull() ?: return@executeSafe
                        val configType = configTypeArg.value

                        if (!configType.serverPresets.contains(ip)) {
                            MessageSendHelper.sendChatMessage("This server doesn't have a preset in config ${configType.displayName}")
                            return@executeSafe
                        }

                        if (!confirm(ip)) return@executeSafe

                        configType.deleteServerPreset(ip)
                    }
                }

                literal("list") {
                    execute("List all available server presets") {
                        configTypeArg.value.printAllServerPreset()
                    }
                }
            }

            execute("Print current preset name") {
                configTypeArg.value.printCurrentPreset()
            }
        }
    }

    private fun SafeExecuteEvent.getIpOrNull(): String? {
        val ip = mc.currentServerData?.serverIP

        return if (ip == null || mc.isIntegratedServerRunning) {
            MessageSendHelper.sendWarningMessage("You are not in a server!")
            null
        } else {
            ip
        }
    }

    private fun confirm(name: String): Boolean {
        return if (name != lastName || confirmTimer.tick(8L, false)) {
            MessageSendHelper.sendWarningMessage("This can't be undone, run this command again to confirm!")
            lastName = name
            confirmTimer.reset()
            false
        } else {
            true
        }
    }
}