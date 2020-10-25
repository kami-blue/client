package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper

@Module.Info(
        name = "CommandConfig",
        category = Module.Category.CLIENT,
        description = "Configures client chat related stuff",
        showOnArray = Module.ShowOnArray.OFF,
        alwaysEnabled = true
)
object CommandConfig : Module() {
    val aliasInfo = setting("AliasInfo", true)
    val prefixChat = setting("PrefixChat", true)
    val toggleMessages = setting("ToggleMessages", false)
    val customTitle = setting("WindowTitle", true)
    private val autoSaving = setting("AutoSavingSettings", true)
    private val savingFeedBack = setting("SavingFeedBack", false, { autoSaving.value })
    private val savingInterval = setting("Interval(m)", 3, 1..10, 1, { autoSaving.value })
    val commandPrefix = setting("CommandPrefix", ";", { false })
    val modifierEnabled = setting("ModifierEnabled", false, { false })

    val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.MINUTES)

    init {
        listener<SafeTickEvent> {
            if (autoSaving.value && mc.currentScreen !is DisplayGuiScreen && timer.tick(savingInterval.value.toLong())) {
                Thread {
                    Thread.currentThread().name = "Auto Saving Thread"
                    if (savingFeedBack.value) MessageSendHelper.sendChatMessage("Auto saving settings...")
                    ConfigUtils.saveConfig(ModuleConfig)
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