package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.onMainThread
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

                                MessageSendHelper.sendChatMessage("Set ${TextFormatting.AQUA}${setting.name}${TextFormatting.RESET}" +
                                    " to ${TextFormatting.DARK_AQUA}${value}${TextFormatting.RESET}.")
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

                    val string = "${TextFormatting.AQUA}$settingName${TextFormatting.RESET} " +
                        "is a ${TextFormatting.DARK_AQUA}${setting.valueClass.simpleName}${TextFormatting.RESET}. " +
                        "Its current value is ${TextFormatting.DARK_AQUA}$setting"
                    MessageSendHelper.sendChatMessage(string)
                }
            }

            executeAsync("List settings for a module") {
                val module = moduleArg.value
                val settingsString = module.fullSettingList.joinToString()
                val string = "List of settings for ${TextFormatting.AQUA}${module.name.value}${TextFormatting.RESET}:\n" +
                    settingsString
                MessageSendHelper.sendChatMessage(string)
            }
        }
    }

    private fun sendUnknownSettingMessage(moduleName: String, settingName: String) {
        val string = "Unknown setting ${TextFormatting.AQUA}$settingName${TextFormatting.RESET} " +
            "in ${TextFormatting.AQUA}$moduleName${TextFormatting.RESET}!"
        MessageSendHelper.sendChatMessage(string)
    }
}