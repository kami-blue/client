package me.zeroeightsix.kami.module

import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.module.modules.client.ClickGUI
import me.zeroeightsix.kami.module.modules.client.CommandConfig
import me.zeroeightsix.kami.setting.ModuleConfig
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.setting.settings.AbstractSetting
import me.zeroeightsix.kami.util.Bind
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.runSafe
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
    private val enabled = setting("Enabled", false, { false })
    private val visible = setting("Visible", showOnArray)
    private val default = setting("Default", false, { settingList.isNotEmpty() })
    /* End of settings */

    /* Properties */
    val isEnabled: Boolean get() = enabled.value || alwaysEnabled
    val isDisabled: Boolean get() = !isEnabled
    val chatName: String get() = "[${name}]"
    val isVisible: Boolean get() = visible.value
    /* End of properties */

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
            if (this@Module !is ClickGUI && CommandConfig.toggleMessages.value) {
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