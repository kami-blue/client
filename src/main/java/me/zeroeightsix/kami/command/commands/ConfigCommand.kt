package me.zeroeightsix.kami.command.commands

import kotlinx.coroutines.*
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.module.modules.client.Configurations
import me.zeroeightsix.kami.setting.ConfigManager
import me.zeroeightsix.kami.setting.GenericConfig
import me.zeroeightsix.kami.setting.GuiConfig
import me.zeroeightsix.kami.setting.ModuleConfig
import me.zeroeightsix.kami.setting.configs.IConfig
import me.zeroeightsix.kami.setting.settings.impl.primitive.StringSetting
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.formatValue
import me.zeroeightsix.kami.util.threads.defaultScope
import org.kamiblue.commons.interfaces.DisplayEnum
import java.io.IOException
import java.nio.file.Paths

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

        enum<ConfigType>("config type") { configTypeArg ->
            literal("reload") {
                execute("Reload specific config") {
                    configTypeArg.value.reload()
                }
            }

            literal("save") {
                execute("Save specific config") {
                    configTypeArg.value.save()
                }
            }

            literal("preset") {
                string("name") { nameArg ->
                    execute("Change preset for specific config") {
                        configTypeArg.value.preset(nameArg.value)
                    }
                }

                execute("Print current preset name") {
                    configTypeArg.value.printPreset()
                }
            }
        }
    }

    @Suppress("UNUSED")
    private enum class ConfigType(override val displayName: String, val config: IConfig, val setting: StringSetting) : DisplayEnum {
        GUI("GUI", GuiConfig, Configurations.guiPresetSetting),
        MODULES("Modules", ModuleConfig, Configurations.modulePresetSetting);

        fun reload() {
            defaultScope.launch(Dispatchers.IO) {
                var loaded = ConfigManager.load(GenericConfig)
                loaded = ConfigManager.load(config) || loaded

                if (loaded) MessageSendHelper.sendChatMessage("${formatValue(config.name)} config reloaded!")
                else MessageSendHelper.sendErrorMessage("Failed to load ${formatValue(config.name)} config!")
            }
        }

        fun save() {
            defaultScope.launch(Dispatchers.IO) {
                var saved = ConfigManager.save(GenericConfig)
                saved = ConfigManager.save(config) || saved

                if (saved) MessageSendHelper.sendChatMessage("${formatValue(config.name)} config saved!")
                else MessageSendHelper.sendErrorMessage("Failed to load ${formatValue(config.name)} config!")
            }
        }

        fun preset(name: String) {
            defaultScope.launch(Dispatchers.IO) {
                val nameWithoutExtension = name.removeSuffix(".json")
                val nameWithExtension = "$nameWithoutExtension.json"

                if (!ConfigUtils.isPathValid(nameWithExtension)) {
                    MessageSendHelper.sendChatMessage("${formatValue(nameWithoutExtension)} is not a valid preset name")
                    return@launch
                }

                val prevPath = setting.value

                try {
                    ConfigManager.save(config)
                    setting.value = nameWithoutExtension
                    ConfigManager.save(GenericConfig)
                    ConfigManager.load(config)

                    MessageSendHelper.sendChatMessage("Preset set to ${formatValue(nameWithoutExtension)}!")
                } catch (e: IOException) {
                    MessageSendHelper.sendChatMessage("Couldn't set preset: ${e.message}")
                    KamiMod.LOG.warn("Couldn't set path!", e)

                    setting.value = prevPath
                    ConfigManager.save(GenericConfig)
                }
            }
        }

        fun printPreset() {
            defaultScope.launch(Dispatchers.IO) {
                val path = Paths.get("${setting.value}.json").toAbsolutePath()
                MessageSendHelper.sendChatMessage("Path to config: ${formatValue(path)}")
            }
        }
    }
}