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
        description = "Configures PrefixChat and Alias options",
        showOnArray = Module.ShowOnArray.OFF
)
class CommandConfig : Module() {
    @JvmField
    var aliasInfo: Setting<Boolean> = register(Settings.b("Alias Info", true))
    @JvmField
    var prefixChat: Setting<Boolean> = register(Settings.b("PrefixChat", true))
    @JvmField
    var customTitle: Setting<Boolean> = register(Settings.b("Window Title", true))

    public override fun onDisable() {
        sendDisableMessage()
    }

    private fun sendDisableMessage() {
        MessageSendHelper.sendErrorMessage("Error: The " + KamiMod.MODULE_MANAGER.getModule(this.javaClass).name + " module is only for configuring command options, disabling it doesn't do anything.")
        KamiMod.MODULE_MANAGER.getModule(this.javaClass).enable()
    }
}