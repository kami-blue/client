package me.zeroeightsix.kami.module

import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.module.modules.client.ClickGUI
import me.zeroeightsix.kami.module.modules.client.CommandConfig
import me.zeroeightsix.kami.setting.ModuleConfig
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.Minecraft

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
     *
     * @see me.zeroeightsix.kami.module.modules.client.ActiveModules
     */
    enum class Category(val categoryName: String, val isHidden: Boolean) {
        CHAT("Chat", false),
        COMBAT("Combat", false),
        CLIENT("Client", false),
        HIDDEN("Hidden", true),
        MISC("Misc", false),
        MOVEMENT("Movement", false),
        PLAYER("Player", false),
        RENDER("Render", false);
    }
    /* End of annotations */

    /* Settings */

    val fullSettingList get() = ModuleConfig.getGroupOrPut(this.category.categoryName).getGroupOrPut(this.name).getSettings()
    val settingList get() = fullSettingList.filter { it != bind || it != enabled || it != visible }
    val bind = setting("Bind", { !annotation.alwaysEnabled })
    private val enabled = setting("Enabled", annotation.enabledByDefault || annotation.alwaysEnabled, { false })
    private val visible = setting("Visible", annotation.showOnArray)
    /* End of settings */

    /* Properties */
    val isEnabled: Boolean get() = enabled.value || annotation.alwaysEnabled
    val isDisabled: Boolean get() = !isEnabled
    val bindName: String get() = bind.value.toString()
    val chatName: String get() = "[${name}]"
    val isVisible: Boolean get() = visible.value
    val isProduction: Boolean get() = category != Category.HIDDEN
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
        if (annotation.alwaysEnabled || isEnabled) return
        enabled.value = true
        onEnable()
        onToggle()
        sendToggleMessage()
        if (!alwaysListening) {
            KamiEventBus.subscribe(this)
        }
    }

    fun disable() {
        if (annotation.alwaysEnabled || isDisabled) return
        enabled.value = false
        onDisable()
        onToggle()
        sendToggleMessage()
        if (!alwaysListening) {
            KamiEventBus.unsubscribe(this)
        }
    }

    private fun sendToggleMessage() {
        if (this !is ClickGUI && CommandConfig.toggleMessages.value) {
            MessageSendHelper.sendChatMessage(name + if (enabled.value) " &aenabled" else " &cdisabled")
        }
    }


    /**
     * Cleanup method in case this module wants to do something when the client closes down
     */
    open fun destroy() {}
    open fun isActive(): Boolean {
        return isEnabled || alwaysListening
    }

    open fun getHudInfo(): String? {
        return null
    }

    protected open fun onEnable() {}
    protected open fun onDisable() {}
    protected open fun onToggle() {}

    protected companion object {
        @JvmField val mc: Minecraft = Minecraft.getMinecraft()
    }
}