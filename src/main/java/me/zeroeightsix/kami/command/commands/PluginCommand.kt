package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.plugin.PluginLoader
import me.zeroeightsix.kami.plugin.PluginManager
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.formatValue
import org.apache.commons.lang3.StringUtils
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
                    val plugin = loader.load()
                    if (PluginManager.loadedPlugins.contains(plugin)) {
                        MessageSendHelper.sendWarningMessage("Plugin $name already loaded!")
                        return@execute
                    }
                    PluginManager.load(loader)
                    ConfigUtils.loadAll()

                    val stopTime = System.currentTimeMillis() - time
                    MessageSendHelper.sendChatMessage("Loaded plugin $name, took $stopTime ms!")
                }
            }
        }

        literal("reload") {
            string("plugin name") { nameArg ->
                execute {
                    val name = nameArg.value
                    val plugin = PluginManager.loadedPlugins[name]

                    if (plugin == null) {
                        MessageSendHelper.sendErrorMessage("No plugin found for name $name")
                        return@execute
                    }

                    val time = System.currentTimeMillis()
                    MessageSendHelper.sendChatMessage("Reloading plugins $name...")

                    ConfigUtils.saveAll()
                    val file = PluginManager.pluginLoaderMap[plugin]!!.file
                    PluginManager.unload(plugin)
                    PluginManager.load(PluginLoader(file))
                    ConfigUtils.loadAll()

                    val stopTime = System.currentTimeMillis() - time
                    MessageSendHelper.sendChatMessage("Reloaded plugin $name, took $stopTime ms!")
                }
            }

            execute {
                val time = System.currentTimeMillis()
                MessageSendHelper.sendChatMessage("Reloading plugins...")

                ConfigUtils.saveAll()
                PluginManager.unloadAll()
                PluginManager.loadAll(PluginManager.preLoad())
                ConfigUtils.loadAll()

                val stopTime = System.currentTimeMillis() - time
                MessageSendHelper.sendChatMessage("Reloaded plugins, took $stopTime ms!")
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
                        MessageSendHelper.sendRawChatMessage("${formatValue(index)}. " +
                            "Name: ${formatValue(plugin.name)}, " +
                            "Version: ${formatValue(plugin.version)}, " +
                            "Description: ${formatValue(plugin.description)}, " +
                            "Min KAMI Blue Version: ${formatValue(plugin.minKamiVersion)}, " +
                            "Authors: ${StringUtils.join(plugin.authors, ",")}, " +
                            "Dependencies: ${StringUtils.join(plugin.dependencies, ",")}"
                        )
                    }
                }
            }
        }
    }
}