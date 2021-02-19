package org.kamiblue.client.module

import net.minecraft.client.Minecraft
import org.kamiblue.client.event.KamiEventBus
import org.kamiblue.client.module.modules.client.ClickGUI
import org.kamiblue.client.module.modules.client.CommandConfig
import org.kamiblue.client.setting.configs.NameableConfig
import org.kamiblue.client.setting.settings.AbstractSetting
import org.kamiblue.client.setting.settings.SettingRegister
import org.kamiblue.client.setting.settings.impl.other.BindSetting
import org.kamiblue.client.setting.settings.impl.primitive.BooleanSetting
import org.kamiblue.client.util.Bind
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.runSafe
import org.kamiblue.commons.interfaces.Alias
import org.kamiblue.commons.interfaces.Nameable

@Suppress("UNCHECKED_CAST")
abstract class AbstractModule(
    override val name: String,
    override val alias: Array<String> = emptyArray(),
    val category: Category,
    val description: String,
    val modulePriority: Int = -1,
    var alwaysListening: Boolean = false,
    val showOnArray: Boolean = true,
    val alwaysEnabled: Boolean = false,
    val enabledByDefault: Boolean = false,
    private val config: NameableConfig<out Nameable>
) : Nameable, Alias, SettingRegister<Nameable> by config as NameableConfig<Nameable> {

    val bind = BindSetting("Bind", Bind(), { !alwaysEnabled }).also(::addSetting)
    private val enabled = BooleanSetting("Enabled", false, { false }).also(::addSetting)
    private val visible = BooleanSetting("Visible", showOnArray).also(::addSetting)
    private val default = BooleanSetting("Default", false, { settingList.isNotEmpty() }).also(::addSetting)

    val fullSettingList get() = config.getSettings(this)
    val settingList: List<AbstractSetting<*>> get() = fullSettingList.filter { it != bind && it != enabled && it != visible && it != default }

    val isEnabled: Boolean get() = enabled.value || alwaysEnabled
    val isDisabled: Boolean get() = !isEnabled
    val chatName: String get() = "[${name}]"
    val isVisible: Boolean get() = visible.value

    private fun addSetting(setting: AbstractSetting<*>) {
        config.getGroupOrPut(name).addSetting(setting)
    }

    internal fun postInit() {
        enabled.value = enabledByDefault || alwaysEnabled
        if (alwaysListening) KamiEventBus.subscribe(this)
    }

    fun toggle() {
        enabled.value = !enabled.value
    }

    fun enable() {
        enabled.value = true
    }

    fun disable() {
        enabled.value = false
    }

    private fun sendToggleMessage() {
        runSafe {
            if (this@AbstractModule !is ClickGUI && CommandConfig.toggleMessages.value) {
                MessageSendHelper.sendChatMessage(name + if (enabled.value) " &cdisabled" else " &aenabled")
            }
        }
    }

    open fun isActive(): Boolean {
        return isEnabled || alwaysListening
    }

    open fun getHudInfo(): String {
        return ""
    }

    protected fun onEnable(block: (Boolean) -> Unit) {
        enabled.valueListeners.add { _, input ->
            if (input) {
                block(input)
            }
        }
    }

    protected fun onDisable(block: (Boolean) -> Unit) {
        enabled.valueListeners.add { _, input ->
            if (!input) {
                block(input)
            }
        }
    }

    protected fun onToggle(block: (Boolean) -> Unit) {
        enabled.valueListeners.add { _, input ->
            block(input)
        }
    }

    init {
        enabled.consumers.add { prev, input ->
            val enabled = alwaysEnabled || input

            if (prev != input && !alwaysEnabled) {
                sendToggleMessage()
            }

            if (enabled || alwaysListening) {
                KamiEventBus.subscribe(this)
            } else {
                KamiEventBus.unsubscribe(this)
            }

            enabled
        }

        default.valueListeners.add { _, it ->
            if (it) {
                settingList.forEach { it.resetValue() }
                default.value = false
                MessageSendHelper.sendChatMessage("$chatName Set to defaults!")
            }
        }
    }

    protected companion object {
        val mc: Minecraft = Minecraft.getMinecraft()
    }
}