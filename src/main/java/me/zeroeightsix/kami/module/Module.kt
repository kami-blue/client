package me.zeroeightsix.kami.module

import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.module.modules.client.ClickGUI
import me.zeroeightsix.kami.module.modules.client.CommandConfig
import me.zeroeightsix.kami.setting.ModuleConfig
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.setting.Translatable
import me.zeroeightsix.kami.setting.settings.AbstractSetting
import me.zeroeightsix.kami.setting.settings.impl.other.BindSetting
import me.zeroeightsix.kami.setting.settings.impl.primitive.BooleanSetting
import me.zeroeightsix.kami.util.Bind
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.runSafe
import me.zeroeightsix.kami.util.translation.TranslationKey
import me.zeroeightsix.kami.util.translation.TranslationKeyBlank
import net.minecraft.client.Minecraft
import org.kamiblue.commons.interfaces.Alias
import org.kamiblue.commons.interfaces.DisplayEnum
import org.kamiblue.commons.interfaces.Nameable

open class Module(
    val alias: Array<String> = emptyArray(),
    val category: Category,
    val modulePriority: Int = -1,
    var alwaysListening: Boolean = false,
    val showOnArray: Boolean = true,
    val alwaysEnabled: Boolean = false,
    val enabledByDefault: Boolean = false
): Translatable() {

    val name : TranslationKey = getTranslationKey("ModuleName", this.javaClass.simpleName)
    val description : TranslationKey = getTranslationKey("ModuleDescription")

    /* Settings */
    val fullSettingList: List<AbstractSetting<*>> get() = ModuleConfig.getGroupOrPut(name.defaultValue).getSettings()
    val settingList: List<AbstractSetting<*>> get() = fullSettingList.filter { it != bind && it != enabled && it != visible && it != default }
    //I know that this is horrible, but otherwise they are under every single module in the translation, you don't need to translate these 160 times.
    val bind = setting(BindSetting(getTranslationKey("Modules.module.Bind", "bind"), Bind(), { !alwaysEnabled }, description = TranslationKeyBlank()))
    private val enabled = setting(BooleanSetting(getTranslationKey("Modules.module.Enabled", "enabled"), false, { !alwaysEnabled }, description = TranslationKeyBlank()))
    private val visible = setting(BooleanSetting(getTranslationKey("Modules.module.Visible", "visible"), showOnArray, description = TranslationKeyBlank()))
    private val default = setting(BooleanSetting(getTranslationKey("Modules.module.Default", "default"), showOnArray, {settingList.isNotEmpty()}, description = TranslationKeyBlank()))
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
                MessageSendHelper.sendChatMessage(name.value + if (enabled.value) " &cdisabled" else " &aenabled")
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

    enum class Category( val displayName: TranslationKey) {
        CHAT(TranslationKey("module.Chat")),
        CLIENT(TranslationKey("module.Client")),
        COMBAT(TranslationKey("module.Combat")),
        MISC(TranslationKey("module.Misc")),
        MOVEMENT(TranslationKey("module.Movement")),
        PLAYER(TranslationKey("module.Player")),
        RENDER(TranslationKey("module.Render"));

        override fun toString() = displayName.value
    }

    protected companion object {
        @JvmField val mc: Minecraft = Minecraft.getMinecraft()
    }
}