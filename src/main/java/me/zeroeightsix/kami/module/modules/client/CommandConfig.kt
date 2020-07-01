package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MessageSendHelper

/**
 * @author dominikaaaa
 */
@Module.Info(
        name = "CommandConfig",
        category = Module.Category.CLIENT,
        description = "Configures client chat related stuff",
        showOnArray = Module.ShowOnArray.OFF
)
class CommandConfig : Module() {
    @JvmField
    var aliasInfo: Setting<Boolean> = register(Settings.b("AliasInfo", true))
    @JvmField
    var prefixChat: Setting<Boolean> = register(Settings.b("PrefixChat", true))
    @JvmField
    var toggleMessages: Setting<Boolean> = register(Settings.b("ToggleMessages", false))
    @JvmField
    var logLevel: Setting<LogLevel> = register(Settings.e("LogLevel", LogLevel.ALL))
    @JvmField
    var customTitle: Setting<Boolean> = register(Settings.b("WindowTitle", true))

    enum class LogLevel {
        NONE, ERROR, WARN, ALL
    }

    public override fun onDisable() {
        sendDisableMessage()
    }

    private fun sendDisableMessage() {
        MessageSendHelper.sendErrorMessage("Error: The " + KamiMod.MODULE_MANAGER.getModule(this.javaClass).name + " module is only for configuring command options, disabling it doesn't do anything.")
        KamiMod.MODULE_MANAGER.getModule(this.javaClass).enable()
    }
}