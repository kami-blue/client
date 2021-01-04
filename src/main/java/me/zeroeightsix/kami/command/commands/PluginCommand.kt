package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.plugin.Plugin
import me.zeroeightsix.kami.plugin.PluginError
import me.zeroeightsix.kami.plugin.PluginLoader
import me.zeroeightsix.kami.plugin.PluginManager
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.formatValue
import java.io.File

object PluginCommand : ClientCommand(
    name = "plugin",
    description = "Manage plugins"
) {
    init {
        literal("load") {
            string("jar name") { nameArg ->
                execute {
                    val name = nameArg.value
                    val file = File("${PluginManager.pluginPath}$name")

                    if (!file.exists() || !file.extension.equals("jar", true)) {
                        MessageSendHelper.sendErrorMessage("$name is not a valid jar file name!")
                    }

                    val time = System.currentTimeMillis()
                    MessageSendHelper.sendChatMessage("Loading plugin $name...")

                    ConfigUtils.saveAll()
                    val loader = PluginLoader(file)

                    if (PluginManager.loadedPlugins.containsName(loader.info.name)) {
                        MessageSendHelper.sendWarningMessage("Plugin $name is already loaded!")
                        return@execute
                    }

                    PluginManager.load(loader)
                    ConfigUtils.loadAll()

                    val stopTime = System.currentTimeMillis() - time
                    MessageSendHelper.sendChatMessage("Loaded plugin $name, took $stopTime ms!")

                    PluginError.displayErrors()
                }
            }
        }

        literal("reload") {
            string("plugin name") { nameArg ->
                execute {
                    val name = nameArg.value
                    val plugin = PluginManager.loadedPlugins[name]

                    if (plugin == null) {
                        MessageSendHelper.sendErrorMessage("No plugins called $name were found")
                        return@execute
                    }

                    val time = System.currentTimeMillis()
                    MessageSendHelper.sendChatMessage("Reloading plugins for $name...")

                    ConfigUtils.saveAll()

                    val file = PluginManager.pluginLoaderMap[plugin]!!.file
                    PluginManager.unload(plugin)
                    PluginManager.load(PluginLoader(file))
                    ConfigUtils.loadAll()

                    val stopTime = System.currentTimeMillis() - time
                    MessageSendHelper.sendChatMessage("Reloaded plugin $name, took $stopTime ms!")

                    PluginError.displayErrors()
                }
            }

            execute {
                val time = System.currentTimeMillis()
                MessageSendHelper.sendChatMessage("Reloading plugins...")

                ConfigUtils.saveAll()
                PluginManager.unloadAll()
                PluginManager.loadAll(PluginManager.getLoaders())
                ConfigUtils.loadAll()

                val stopTime = System.currentTimeMillis() - time
                MessageSendHelper.sendChatMessage("Reloaded plugins, took $stopTime ms!")

                PluginError.displayErrors()
            }
        }

        literal("unload") {
            string("plugin name") { nameArg ->
                execute {
                    val name = nameArg.value
                    val plugin = PluginManager.loadedPlugins[name]

                    if (plugin == null) {
                        MessageSendHelper.sendErrorMessage("No plugin found for name $name")
                        return@execute
                    }

                    val time = System.currentTimeMillis()
                    MessageSendHelper.sendChatMessage("Unloading plugin $name...")

                    ConfigUtils.saveAll()
                    PluginManager.unload(plugin)
                    ConfigUtils.loadAll()

                    val stopTime = System.currentTimeMillis() - time
                    MessageSendHelper.sendChatMessage("Unloaded plugin $name, took $stopTime ms!")
                }
            }

            execute {
                val time = System.currentTimeMillis()
                MessageSendHelper.sendChatMessage("Unloading plugins...")

                ConfigUtils.saveAll()
                PluginManager.unloadAll()
                ConfigUtils.loadAll()

                val stopTime = System.currentTimeMillis() - time
                MessageSendHelper.sendChatMessage("Unloaded plugins, took $stopTime ms!")
            }
        }

        literal("list") {
            execute {
                MessageSendHelper.sendChatMessage("Loaded plugins: ${formatValue(PluginManager.loadedPlugins.size)}")
                if (PluginManager.loadedPlugins.isEmpty()) {
                    MessageSendHelper.sendRawChatMessage("No plugin loaded")
                } else {
                    for ((index, plugin) in PluginManager.loadedPlugins.withIndex()) {
                        MessageSendHelper.sendRawChatMessage("${formatValue(index)}. ${formatValue(plugin.name)}")
                    }
                }
            }
        }

        literal("info") {
            int("index") { indexArg ->
                execute {
                    val index = indexArg.value
                    val plugin = PluginManager.loadedPlugins.toList().getOrNull(index)
                        ?: run {
                            MessageSendHelper.sendChatMessage("No plugin found for index: ${formatValue(index)}")
                            return@execute
                        }
                    val loader = PluginManager.pluginLoaderMap[plugin]!!

                    sendPluginInfo(plugin, loader)
                }
            }

            string( "plugin name") { nameArg ->
                execute {
                    val name = nameArg.value
                    val plugin = PluginManager.loadedPlugins[name]
                        ?: run {
                            MessageSendHelper.sendChatMessage("No plugin found for name: ${formatValue(name)}")
                            return@execute
                        }
                    val loader = PluginManager.pluginLoaderMap[plugin]!!

                    sendPluginInfo(plugin, loader)
                }
            }
        }
    }

    private fun sendPluginInfo(plugin: Plugin, loader: PluginLoader) {
        MessageSendHelper.sendChatMessage("Info for plugin: $loader")
        MessageSendHelper.sendRawChatMessage(plugin.toString())
    }

}