package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.gui.clickgui.KamiClickGui
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.Display

@Module.Info(
        name = "CommandConfig",
        category = Module.Category.CLIENT,
        description = "Configures client chat related stuff",
        showOnArray = false,
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

    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.MINUTES)
    private val prevTitle = Display.getTitle()
    private const val title = "${KamiMod.NAME} ${KamiMod.KAMI_KATAKANA} ${KamiMod.VERSION_SIMPLE}"

    init {
        listener<SafeTickEvent> {
            if (autoSaving.value && mc.currentScreen !is KamiClickGui && timer.tick(savingInterval.value.toLong())) {
                if (savingFeedBack.value) MessageSendHelper.sendChatMessage("Auto saving settings...")
                ConfigUtils.saveConfig(ModuleConfig)
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
        MessageSendHelper.sendErrorMessage("Error: The $name module is only for configuring command options, disabling it doesn't do anything.")
        enable()
    }

    private fun updateTitle() {
        if (customTitle.value) Display.setTitle(title)
        else Display.setTitle(prevTitle)
    }
}