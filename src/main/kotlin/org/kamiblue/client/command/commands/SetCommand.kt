package org.kamiblue.client.command.commands

import net.minecraft.util.text.TextFormatting
import org.kamiblue.client.KamiMod
import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.module.AbstractModule
import org.kamiblue.client.module.ModuleManager
import org.kamiblue.client.setting.settings.AbstractSetting
import org.kamiblue.client.setting.settings.impl.primitive.BooleanSetting
import org.kamiblue.client.setting.settings.impl.primitive.EnumSetting
import org.kamiblue.client.util.AsyncCachedValue
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.format
import org.kamiblue.client.util.text.formatValue
import java.util.*

object SetCommand : ClientCommand(
    name = "set",
    alias = arrayOf("settings"),
    description = "Change the setting of a certain module."
) {
    private val settingMap: Map<AbstractModule, Map<String, AbstractSetting<*>>> by AsyncCachedValue(5L, TimeUnit.SECONDS) {
        ModuleManager.modules.asSequence()
            .associateWith { module ->
                module.fullSettingList.associateBy {
                    it.name.formatSetting()
                }
            }
    }

    init {
        module("module") { moduleArg ->
            string("setting") { settingArg ->
                literal("toggle") {
                    execute {
                        val module = moduleArg.value
                        val settingName = settingArg.value
                        val setting = getSetting(module, settingName)

                        if (setting == null) {
                            sendUnknownSettingMessage(module, settingName)
                            return@execute
                        }

                        when (setting) {
                            is BooleanSetting -> {
                                setting.value = !setting.value
                            }

                            is EnumSetting -> {
                                setting.nextValue()
                            }

                            else -> {
                                MessageSendHelper.sendChatMessage("Unable to toggle value for ${formatValue(setting.name)}")
                            }
                        }

                        MessageSendHelper.sendChatMessage("Set ${formatValue(setting.name)} to ${formatValue(setting.value)}.")
                    }
                }

                greedy("value") { valueArg ->
                    execute("Set the value of a module's setting") {
                        val module = moduleArg.value
                        val settingName = settingArg.value
                        val setting = getSetting(module, settingName)

                        if (setting == null) {
                            sendUnknownSettingMessage(module, settingName)
                            return@execute
                        }

                        try {
                            val value = valueArg.value

                            setting.setValue(value)
                            MessageSendHelper.sendChatMessage("Set ${formatValue(setting.name)} to ${formatValue(value)}.")
                        } catch (e: Exception) {
                            MessageSendHelper.sendChatMessage("Unable to set value! ${TextFormatting.RED format e.message.toString()}")
                            KamiMod.LOG.info("Unable to set value!", e)
                        }
                    }
                }

                execute("Show the value of a setting") {
                    val module = moduleArg.value
                    val settingName = settingArg.value
                    val setting = getSetting(module, settingName)

                    if (setting == null) {
                        sendUnknownSettingMessage(module, settingName)
                        return@execute
                    }

                    MessageSendHelper.sendChatMessage("${formatValue(settingName)} is a " +
                        "${formatValue(setting.valueClass.simpleName)}. " +
                        "Its current value is ${formatValue(setting)}"
                    )
                }
            }

            execute("List settings for a module") {
                val module = moduleArg.value
                val settingList = module.fullSettingList

                MessageSendHelper.sendChatMessage("List of settings for ${formatValue(module.name)} ${formatValue(settingList.size)}")
                MessageSendHelper.sendRawChatMessage(settingList.joinToString("\n") {
                    "    ${it.name.formatSetting(false)} ${TextFormatting.GRAY format it.value}"
                })
            }
        }
    }

    private fun String.formatSetting(lowerCase: Boolean = true): String {
        this.replace(" ", "")
            .replace("_", "")
            .apply {
                return if (lowerCase) this.toLowerCase(Locale.ROOT) else this
            }
    }

    private fun getSetting(module: AbstractModule, settingName: String) =
        settingMap[module]?.get(settingName.formatSetting())

    private fun sendUnknownSettingMessage(module: AbstractModule, settingName: String) {
        MessageSendHelper.sendChatMessage("Unknown setting ${formatValue(settingName)} in ${formatValue(module.name)}!")
    }
}