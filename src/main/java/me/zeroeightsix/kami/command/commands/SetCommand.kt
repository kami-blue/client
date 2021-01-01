package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.util.onMainThread
import me.zeroeightsix.kami.util.text.MessageSendHelper
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
                greedy("value") { valueArg ->
                    executeAsync("Set the value of a module's setting") {
                        val module = moduleArg.value
                        val settingName = settingArg.value
                        val setting = module.fullSettingList.find { it.name.equals(settingName, true) }

                        if (setting == null) {
                            sendUnknownSettingMessage(module.name.value, settingName)
                            return@executeAsync
                        }

                        try {
                            var value = valueArg.value
                            if (setting.javaClass.simpleName == "EnumSetting") {
                                value = value.toUpperCase()
                            }

                            onMainThread {
                                setting.setValueFromString(value, setting.valueClass.simpleName == "Boolean")

                                MessageSendHelper.sendChatMessage("Set ${formatValue(setting.name)} to ${formatValue(value)}.")
                            }

                        } catch (e: Exception) {
                            MessageSendHelper.sendChatMessage("Unable to set value! ${TextFormatting.GOLD}${e.message}")
                            KamiMod.LOG.info("Unable to set value!", e)
                        }
                    }
                }

                executeAsync("Show the value of a setting") {
                    val module = moduleArg.value
                    val settingName = settingArg.value
                    val setting = module.fullSettingList.find { it.name.equals(settingName, true) }

                    if (setting == null) {
                        sendUnknownSettingMessage(module.name.value, settingName)
                        return@executeAsync
                    }

                    MessageSendHelper.sendChatMessage("${formatValue(settingName)} is a " +
                        "${formatValue(setting.valueClass.simpleName)}. " +
                        "Its current value is ${formatValue(setting)}"
                    )
                }
            }

            executeAsync("List settings for a module") {
                val module = moduleArg.value
                val settingList = module.fullSettingList
                MessageSendHelper.sendChatMessage("List of settings for ${formatValue(module.name.value)}: " +
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