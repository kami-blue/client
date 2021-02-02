package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.setting.settings.impl.primitive.BooleanSetting
import me.zeroeightsix.kami.setting.settings.impl.primitive.EnumSetting
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.format
import me.zeroeightsix.kami.util.text.formatValue
import net.minecraft.util.text.TextFormatting

object SetCommand : ClientCommand(
    name = "set",
    alias = arrayOf("settings"),
    description = "Change the setting of a certain module."
) {
    init {
        module("module") { moduleArg ->
            string("setting") { settingArg ->
                literal("toggle") {
                    execute {
                        val module = moduleArg.value
                        val settingName = settingArg.value
                        val setting = module.fullSettingList.find { it.name.equals(settingName, true) }

                        if (setting == null) {
                            sendUnknownSettingMessage(module.name, settingName)
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
                        val setting = module.fullSettingList.find { it.name.equals(settingName, true) }

                        if (setting == null) {
                            sendUnknownSettingMessage(module.name, settingName)
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
                    val setting = module.fullSettingList.find { it.name.equals(settingName, true) }

                    if (setting == null) {
                        sendUnknownSettingMessage(module.name, settingName)
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

                MessageSendHelper.sendChatMessage("List of settings for ${formatValue(module.name)}: " +
                    formatValue(settingList.size)
                )
                MessageSendHelper.sendRawChatMessage(settingList.joinToString { it.name })
            }
        }
    }

    private fun sendUnknownSettingMessage(moduleName: String, settingName: String) {
        MessageSendHelper.sendChatMessage("Unknown setting ${formatValue(settingName)} in ${formatValue(moduleName)}!")
    }
}