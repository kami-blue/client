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
import org.kamiblue.commons.interfaces.DisplayEnum

open class Module {
    /* Annotations */
    private val annotation =
        javaClass.annotations.firstOrNull { it is Info } as? Info
            ?: throw IllegalStateException("No Annotation on class " + this.javaClass.canonicalName + "!")

    val name = annotation.name
    val alias = arrayOf(name, *annotation.alias)
    val category = annotation.category
    val description = annotation.description
    val modulePriority = annotation.modulePriority
    var alwaysListening = annotation.alwaysListening

    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val name: String,
        val alias: Array<String> = [],
        val description: String,
        val category: Category,
        val modulePriority: Int = -1,
        val alwaysListening: Boolean = false,
        val showOnArray: Boolean = true,
        val alwaysEnabled: Boolean = false,
        val enabledByDefault: Boolean = false
    )

    /**
     * @see me.zeroeightsix.kami.command.commands.GenerateWebsiteCommand
     */
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
    /* End of annotations */

    /* Settings */
    val fullSettingList: List<AbstractSetting<*>> get() = ModuleConfig.getGroupOrPut(name).getSettings()
    val settingList: List<AbstractSetting<*>> get() = fullSettingList.filter { it != bind && it != enabled && it != visible && it != default }

    val bind = setting("Bind", Bind(), { !annotation.alwaysEnabled })
    private val enabled = setting("Enabled", false, { false })
    private val visible = setting("Visible", annotation.showOnArray)
    private val default = setting("Default", false, { settingList.isNotEmpty() })
    /* End of settings */

    /* Properties */
    val isEnabled: Boolean get() = enabled.value || annotation.alwaysEnabled
    val isDisabled: Boolean get() = !isEnabled
    val chatName: String get() = "[${name}]"
    val isVisible: Boolean get() = visible.value
    /* End of properties */

    internal fun postInit() {
        enabled.value = annotation.enabledByDefault || annotation.alwaysEnabled
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
            val enabled = annotation.alwaysEnabled || input

            if (prev != input && !annotation.alwaysEnabled) {
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
        @JvmField val mc: Minecraft = Minecraft.getMinecraft()
    }
}