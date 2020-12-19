package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.Display

@Module.Info(
    name = "CommandConfig",
    category = Module.Category.CLIENT,
    description = "Configures client chat related stuff",
    showOnArray = Module.ShowOnArray.OFF,
    alwaysEnabled = true
)
object CommandConfig : Module() {
    val prefix: Setting<String> = Settings.s("commandPrefix", ";")
    val toggleMessages = register(Settings.b("ToggleMessages", false))
    private val customTitle = register(Settings.b("WindowTitle", true))
    private val autoSaving = register(Settings.b("AutoSavingSettings", true))
    private val savingFeedBack = register(Settings.booleanBuilder("SavingFeedBack").withValue(false).withVisibility { autoSaving.value })
    private val savingInterval = register(Settings.integerBuilder("Interval(m)").withValue(3).withRange(1, 10).withVisibility { autoSaving.value })

    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.MINUTES)
    private val prevTitle = Display.getTitle()
    private const val title = "${KamiMod.NAME} ${KamiMod.KAMI_KATAKANA} ${KamiMod.VERSION_SIMPLE}"

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

        listener<TickEvent.ClientTickEvent> {
            updateTitle()
        }
    }

    override fun onDisable() {
        sendDisableMessage()
    }

    private fun sendDisableMessage() {
        MessageSendHelper.sendErrorMessage("Error: The ${name.value} module is only for configuring command options, disabling it doesn't do anything.")
        enable()
    }

    private fun updateTitle() {
        if (customTitle.value) Display.setTitle(title)
        else Display.setTitle(prevTitle)
    }
}