package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper

/**
 * @author l1ving
 */
@Module.Info(
        name = "CommandConfig",
        category = Module.Category.CLIENT,
        description = "Configures client chat related stuff",
        showOnArray = Module.ShowOnArray.OFF,
        alwaysEnabled = true
)
object CommandConfig : Module() {
    val aliasInfo = register(Settings.b("AliasInfo", true))
    val prefixChat = register(Settings.b("PrefixChat", true))
    val toggleMessages = register(Settings.b("ToggleMessages", false))
    val customTitle = register(Settings.b("WindowTitle", true))
    private val autoSaving = register(Settings.b("AutoSavingSettings", true))
    private val savingFeedBack = register(Settings.booleanBuilder("SavingFeedBack").withValue(false).withVisibility { autoSaving.value })
    private val savingInterval = register(Settings.integerBuilder("Interval(m)").withValue(3).withRange(1, 10).withVisibility { autoSaving.value })

    val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.MINUTES)

    init {
        listener<SafeTickEvent> {
            if (autoSaving.value && mc.currentScreen !is DisplayGuiScreen && timer.tick(savingInterval.value.toLong())) {
                Thread {
                    Thread.currentThread().name = "Auto Saving Thread"
                    if (savingFeedBack.value) MessageSendHelper.sendChatMessage("Auto saving settings...")
                    ConfigUtils.saveConfiguration()
                }.start()
            }
        }
    }

    override fun onDisable() {
        sendDisableMessage()
    }

    private fun sendDisableMessage() {
        MessageSendHelper.sendErrorMessage("Error: The ${name.value} module is only for configuring command options, disabling it doesn't do anything.")
        enable()
    }
}