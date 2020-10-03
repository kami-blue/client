package me.zeroeightsix.kami.module

import com.google.common.base.Converter
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.module.modules.ClickGUI
import me.zeroeightsix.kami.module.modules.client.CommandConfig
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.setting.builder.SettingBuilder
import me.zeroeightsix.kami.util.Bind
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.Minecraft
import org.lwjgl.input.Keyboard
import java.util.*

open class Module {
    /* Annotations */
    @JvmField val originalName: String = annotation.name
    @JvmField val category: Category = annotation.category
    @JvmField val description: String = annotation.description
    @JvmField val modulePriority: Int = annotation.modulePriority
    @JvmField var alwaysListening: Boolean = annotation.alwaysListening

    @JvmField var settingList = ArrayList<Setting<*>>()

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
    @JvmField val name = register(Settings.s("Name", originalName))
    @JvmField val bind = register(Settings.custom("Bind", Bind.none(), BindConverter()).build())
    private val enabled = register(Settings.booleanBuilder("Enabled").withVisibility { false }.withValue(annotation.enabledByDefault || annotation.alwaysEnabled).build())
    private val showOnArray = register(Settings.e<ShowOnArray>("Visible", annotation.showOnArray))
    /* End of settings */

    /* Properties */
    val isEnabled: Boolean get() = enabled.value || annotation.alwaysEnabled
    val isDisabled: Boolean get() = !isEnabled
    val bindName: String get() = bind.value.toString()
    val chatName: String get() = "[${name.value}]"
    val isOnArray: Boolean get() = showOnArray.value == ShowOnArray.ON
    val isProduction: Boolean get() = name.value == "clickGUI" || category != Category.EXPERIMENTAL && category != Category.HIDDEN
    /* End of properties */


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
        if (!alwaysListening) KamiMod.EVENT_BUS.subscribe(this)
    }

    fun disable() {
        if (annotation.alwaysEnabled) return
        enabled.value = false
        onDisable()
        onToggle()
        sendToggleMessage()
        if (!alwaysListening) KamiMod.EVENT_BUS.unsubscribe(this)
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

    open fun onUpdate() {}
    open fun onRender() {}
    open fun onWorldRender(event: RenderEvent) {}
    protected open fun onEnable() {}
    protected open fun onDisable() {}
    protected open fun onToggle() {}


    /* Setting registering */
    protected fun registerAll(vararg settings: Setting<*>) {
        for (setting in settings) {
            register(setting)
        }
    }

    protected fun <T> register(setting: Setting<T>): Setting<T> {
        settingList.add(setting)
        return SettingBuilder.register(setting, "modules.$originalName")
    }

    protected fun <T> register(builder: SettingBuilder<T>): Setting<T> {
        val setting = builder.build()
        settingList.add(setting)
        return SettingBuilder.register(setting, "modules.$originalName")
    }
    /* End of setting registering */

    /* Key binding */
    private class BindConverter : Converter<Bind, JsonElement>() {
        override fun doForward(bind: Bind): JsonElement {
            return JsonPrimitive(bind.toString())
        }

        override fun doBackward(jsonElement: JsonElement): Bind {
            var s = jsonElement.asString
            if (s.equals("None", ignoreCase = true)) return Bind.none()
            var ctrl = false
            var alt = false
            var shift = false
            if (s.startsWith("Ctrl+")) {
                ctrl = true
                s = s.substring(5)
            }
            if (s.startsWith("Alt+")) {
                alt = true
                s = s.substring(4)
            }
            if (s.startsWith("Shift+")) {
                shift = true
                s = s.substring(6)
            }
            var key = -1
            try {
                key = Keyboard.getKeyIndex(s.toUpperCase())
            } catch (ignored: Exception) {
            }
            return if (key == 0) Bind.none() else Bind(ctrl, alt, shift, key)
        }
    }
    /* End of key binding */

    protected companion object {
        @JvmField val mc: Minecraft = Minecraft.getMinecraft()
    }
}