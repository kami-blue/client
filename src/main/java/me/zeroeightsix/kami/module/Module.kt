package me.zeroeightsix.kami.module

import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.module.modules.ClickGUI
import me.zeroeightsix.kami.module.modules.client.CommandConfig
import me.zeroeightsix.kami.setting.ModuleConfig
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.Minecraft

open class Module {
    /* Annotations */
    @JvmField val originalName: String = annotation.name
    @JvmField val category: Category = annotation.category
    @JvmField val description: String = annotation.description
    @JvmField val modulePriority: Int = annotation.modulePriority
    @JvmField var alwaysListening: Boolean = annotation.alwaysListening

    val settingList get() = ModuleConfig.getGroupOrPut(this.category.categoryName).getGroupOrPut(this.originalName).getSettings()

    private val annotation: Info get() {
            if (javaClass.isAnnotationPresent(Info::class.java)) {
                return javaClass.getAnnotation(Info::class.java)
            }
            throw IllegalStateException("No Annotation on class " + this.javaClass.canonicalName + "!")
        }

    @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
            val name: String,
            val description: String,
            val category: Category,
            val modulePriority: Int = -1,
            val alwaysListening: Boolean = false,
            val showOnArray: ShowOnArray = ShowOnArray.ON,
            val alwaysEnabled: Boolean = false,
            val enabledByDefault: Boolean = false
    )

    enum class ShowOnArray {
        ON, OFF
    }

    /**
     * @see me.zeroeightsix.kami.command.commands.GenerateWebsiteCommand
     *
     * @see me.zeroeightsix.kami.module.modules.client.ActiveModules
     */
    enum class Category(val categoryName: String, val isHidden: Boolean) {
        CHAT("Chat", false),
        COMBAT("Combat", false),
        EXPERIMENTAL("Experimental", false),
        CLIENT("Client", false),
        HIDDEN("Hidden", true),
        MISC("Misc", false),
        MOVEMENT("Movement", false),
        PLAYER("Player", false),
        RENDER("Render", false);
    }
    /* End of annotations */

    /* Settings */
    @JvmField val name = setting("Name", originalName)
    @JvmField val bind = setting("Bind")
    private val enabled = setting("Enabled", annotation.enabledByDefault || annotation.alwaysEnabled, { false })
    private val showOnArray = setting("Visible", annotation.showOnArray)
    /* End of settings */

    /* Properties */
    val isEnabled: Boolean get() = enabled.value || annotation.alwaysEnabled
    val isDisabled: Boolean get() = !isEnabled
    val bindName: String get() = bind.value.toString()
    val chatName: String get() = "[${name.value}]"
    val isOnArray: Boolean get() = showOnArray.value == ShowOnArray.ON
    val isProduction: Boolean get() = name.value == "clickGUI" || category != Category.EXPERIMENTAL && category != Category.HIDDEN
    /* End of properties */


    fun resetSettings() {
        for (setting in settingList) {
            if (setting == name || setting == bind || setting == enabled || setting == showOnArray) continue
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
        enabled.value = true
        onEnable()
        onToggle()
        sendToggleMessage()
        if (!alwaysListening) {
            KamiEventBus.subscribe(this)
        }
    }

    fun disable() {
        if (annotation.alwaysEnabled) return
        enabled.value = false
        onDisable()
        onToggle()
        sendToggleMessage()
        if (!alwaysListening) {
            KamiEventBus.unsubscribe(this)
        }
    }

    private fun sendToggleMessage() {
        if (mc.currentScreen !is DisplayGuiScreen && this !is ClickGUI && CommandConfig.toggleMessages.value) {
            MessageSendHelper.sendChatMessage(name.value.toString() + if (enabled.value) " &aenabled" else " &cdisabled")
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