package org.kamiblue.client.command.commands

import net.minecraft.util.text.TextFormatting
import org.kamiblue.client.KamiMod
import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.gui.GuiManager
import org.kamiblue.client.gui.hudgui.AbstractHudElement
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
    alias = arrayOf("setting", "settings"),
    description = "Change the setting of a certain module."
) {
    private val moduleSettingMap: Map<AbstractModule, Map<String, AbstractSetting<*>>> by AsyncCachedValue(5L, TimeUnit.SECONDS) {
        ModuleManager.modules
            .associateWith { module ->
                module.fullSettingList.associateBy {
                    it.name.formatSetting()
                }
            }
    }

    private val hudElementSettingMap: Map<AbstractHudElement, Map<String, AbstractSetting<*>>> by AsyncCachedValue(5L, TimeUnit.SECONDS) {
        GuiManager.hudElements
            .associateWith { hudElements ->
                hudElements.settingList.associateBy {
                    it.name.formatSetting()
                }
            }
    }

    init {
        hudElement("hud element") { hudElementArg ->
            string("setting") { settingArg ->
                literal("toggle") {
                    execute {
                        val hudElement = hudElementArg.value
                        val settingName = settingArg.value
                        val setting = getSetting(hudElement, settingName)

                        toggleSetting(hudElement.name, settingName, setting)
                    }
                }

                greedy("value") { valueArg ->
                    execute("Set the value of a hud element's setting") {
                        val hudElement = hudElementArg.value
                        val settingName = settingArg.value
                        val setting = getSetting(hudElement, settingName)

                        setSetting(hudElement.name, settingName, setting, valueArg.value)
                    }
                }

                execute("Show the value of a setting") {
                    val hudElement = hudElementArg.value
                    val settingName = settingArg.value
                    val setting = getSetting(hudElement, settingName)

                    printSetting(hudElement.name, settingName, setting)
                }
            }

            execute("List settings for a hud element") {
                listSetting(hudElementArg.value.name, hudElementArg.value.settingList)
            }
        }

        module("module") { moduleArg ->
            string("setting") { settingArg ->
                literal("toggle") {
                    execute {
                        val module = moduleArg.value
                        val settingName = settingArg.value
                        val setting = getSetting(module, settingName)

                        toggleSetting(module.name, settingName, setting)
                    }
                }

                greedy("value") { valueArg ->
                    execute("Set the value of a module's setting") {
                        val module = moduleArg.value
                        val settingName = settingArg.value
                        val setting = getSetting(module, settingName)

                        setSetting(module.name, settingName, setting, valueArg.value)
                    }
                }

                execute("Show the value of a setting") {
                    val module = moduleArg.value
                    val settingName = settingArg.value
                    val setting = getSetting(module, settingName)

                    printSetting(module.name, settingName, setting)
                }
            }

            execute("List settings for a module") {
                listSetting(moduleArg.value.name, moduleArg.value.fullSettingList)
            }
        }
    }

    private fun String.formatSetting(lowerCase: Boolean = true) =
        this.replace(" ", "")
            .replace("_", "")
            .run {
                if (lowerCase) this.toLowerCase(Locale.ROOT) else this
            }

    private fun getSetting(module: AbstractModule, settingName: String) =
        moduleSettingMap[module]?.get(settingName.formatSetting())

    private fun getSetting(module: AbstractHudElement, settingName: String) =
        hudElementSettingMap[module]?.get(settingName.formatSetting())

    private fun toggleSetting(name: String, settingName: String, setting: AbstractSetting<*>?) {
        if (setting == null) {
            sendUnknownSettingMessage(name, settingName)
            return
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

    private fun setSetting(name: String, settingName: String, setting: AbstractSetting<*>?, value: String) {
        if (setting == null) {
            sendUnknownSettingMessage(name, settingName)
            return
        }

        try {
            setting.setValue(value)
            MessageSendHelper.sendChatMessage("Set ${formatValue(setting.name)} to ${formatValue(setting.value)}.")
        } catch (e: Exception) {
            MessageSendHelper.sendChatMessage("Unable to set value! ${TextFormatting.RED format e.message.toString()}")
            KamiMod.LOG.info("Unable to set value!", e)
        }
    }

    private fun printSetting(name: String, settingName: String, setting: AbstractSetting<*>?) {
        if (setting == null) {
            sendUnknownSettingMessage(name, settingName)
            return
        }

        MessageSendHelper.sendChatMessage("${formatValue(settingName)} is a " +
            "${formatValue(setting.valueClass.simpleName)}. " +
            "Its current value is ${formatValue(setting)}"
        )
    }

    private fun listSetting(name: String, settingList: List<AbstractSetting<*>>) {
        MessageSendHelper.sendChatMessage("List of settings for ${formatValue(name)} ${formatValue(settingList.size)}")
        MessageSendHelper.sendRawChatMessage(settingList.joinToString("\n") {
            "    ${it.name.formatSetting(false)} ${TextFormatting.GRAY format it.value}"
        })
    }

    private fun sendUnknownSettingMessage(settingName: String, name: String) {
        MessageSendHelper.sendChatMessage("Unknown setting ${formatValue(settingName)} in ${formatValue(name)}!")
    }
}