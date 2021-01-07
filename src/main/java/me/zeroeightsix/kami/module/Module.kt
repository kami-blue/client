package me.zeroeightsix.kami.module

import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.module.modules.client.ClickGUI
import me.zeroeightsix.kami.module.modules.client.CommandConfig
import me.zeroeightsix.kami.setting.ModuleConfig
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.setting.settings.AbstractSetting
import me.zeroeightsix.kami.util.Bind
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.Minecraft
import org.kamiblue.commons.interfaces.Alias
import org.kamiblue.commons.interfaces.DisplayEnum
import org.kamiblue.commons.interfaces.Nameable

open class Module(
    override val name: String,
    override val alias: Array<String> = emptyArray(),
    val category: Category,
    val description: String,
    val modulePriority: Int = -1,
    var alwaysListening: Boolean = false,
    val showOnArray: Boolean = true,
    val alwaysEnabled: Boolean = false,
    val enabledByDefault: Boolean = false
) : Nameable, Alias {

    /* Settings */
    val fullSettingList: List<AbstractSetting<*>> get() = ModuleConfig.getGroupOrPut(name).getSettings()
    val settingList: List<AbstractSetting<*>> get() = fullSettingList.filter { it != bind && it != enabled && it != visible && it != default }

    val bind = setting("Bind", Bind(), { !alwaysEnabled })
    private val enabled = setting("Enabled", enabledByDefault || alwaysEnabled, { false })
    private val visible = setting("Visible", showOnArray)
    private val default = setting("Default", false, { settingList.isNotEmpty() })
    /* End of settings */

    /* Properties */
    val isEnabled: Boolean get() = enabled.value || alwaysEnabled
    val isDisabled: Boolean get() = !isEnabled
    val chatName: String get() = "[${name}]"
    val isVisible: Boolean get() = visible.value
    /* End of properties */


    fun resetSettings() {
        for (setting in settingList) {
            setting.resetValue()
        }
    }

    fun toggle() {
        setEnabled(!isEnabled)
    }

    fun setEnabled(state: Boolean) {
        if (isEnabled != state) if (state) enable() else disable()
    }

    fun enable() {
        if (!enabled.value) sendToggleMessage()

        enabled.value = true
        onEnable()
        onToggle()
        if (!alwaysListening) {
            KamiEventBus.subscribe(this)
        }
    }

    fun disable() {
        if (alwaysEnabled) return
        if (enabled.value) sendToggleMessage()

        enabled.value = false
        onDisable()
        onToggle()
        if (!alwaysListening) {
            KamiEventBus.unsubscribe(this)
        }
    }

    private fun sendToggleMessage() {
        if (this !is ClickGUI && CommandConfig.toggleMessages.value) {
            MessageSendHelper.sendChatMessage(name + if (enabled.value) " &cdisabled" else " &aenabled")
        }
    }


    open fun destroy() {
        // Cleanup method in case this module wants to do something when the client closes down
    }

    open fun isActive(): Boolean {
        return isEnabled || alwaysListening
    }

    open fun getHudInfo(): String {
        return ""
    }

    protected open fun onEnable() {
        // override to run code when the module is enabled
    }

    protected open fun onDisable() {
        // override to run code when the module is disabled
    }

    protected open fun onToggle() {
        // override to run code when the module is enabled or disabled
    }

    init {
        default.valueListeners.add { _, it ->
            if (it) {
                settingList.forEach { it.resetValue() }
                default.value = false
                MessageSendHelper.sendChatMessage("$chatName Set to defaults!")
            }
        }
    }

    enum class Category(override val displayName: String) : DisplayEnum {
        CHAT("Chat"),
        CLIENT("Client"),
        COMBAT("Combat"),
        MISC("Misc"),
        MOVEMENT("Movement"),
        PLAYER("Player"),
        RENDER("Render");

        override fun toString() = displayName
    }

    protected companion object {
        @JvmField val mc: Minecraft = Minecraft.getMinecraft()
    }
}