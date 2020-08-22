package me.zeroeightsix.kami.module.modules.client

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.GuiScreenEvent
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.MessageSendHelper
import me.zeroeightsix.kami.util.Timer

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
    @JvmField val aliasInfo: Setting<Boolean> = register(Settings.b("AliasInfo", true))
    @JvmField val prefixChat: Setting<Boolean> = register(Settings.b("PrefixChat", true))
    @JvmField val toggleMessages: Setting<Boolean> = register(Settings.b("ToggleMessages", false))
    @JvmField val logLevel: Setting<LogLevel> = register(Settings.e("LogLevel", LogLevel.ALL))
    @JvmField val customTitle: Setting<Boolean> = register(Settings.b("WindowTitle", true))
    private val autoSaving = register(Settings.b("AutoSavingSettings", true))
    private val savingFeedBack = register(Settings.booleanBuilder("SavingFeedBack").withValue(false).withVisibility { autoSaving.value }.build())
    private val savingInterval = register(Settings.integerBuilder("Interval(m)").withValue(3).withRange(1, 10).withVisibility { autoSaving.value }.build())

    enum class LogLevel {
        NONE, ERROR, WARN, ALL
    }

    val timer = Timer(Timer.TimeUnit.MINUTES)

    override fun onUpdate() {
        if (autoSaving.value && mc.currentScreen !is DisplayGuiScreen && timer.tick(savingInterval.value.toLong())) {
            Thread{
                Thread.currentThread().name = "Auto Saving Thread"
                if (savingFeedBack.value) MessageSendHelper.sendChatMessage("Auto saving settings...")
                ConfigUtils.saveConfiguration()
            }.start()
        }
    }

    override fun onDisable() {
        sendDisableMessage()
    }

    private fun sendDisableMessage() {
        MessageSendHelper.sendErrorMessage("Error: The " + KamiMod.MODULE_MANAGER.getModule(this.javaClass).name + " module is only for configuring command options, disabling it doesn't do anything.")
        KamiMod.MODULE_MANAGER.getModule(this.javaClass).enable()
    }
}