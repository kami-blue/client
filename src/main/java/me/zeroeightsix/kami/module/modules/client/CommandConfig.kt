package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.clickgui.KamiClickGui
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.BackgroundScope
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.Display

@Module.Info(
    name = "CommandConfig",
    category = Module.Category.CLIENT,
    description = "Configures client chat related stuff",
    showOnArray = false,
    alwaysEnabled = true
)
object CommandConfig : Module() {
    val prefix = setting("Prefix", ";", { false })
    val toggleMessages = setting("ToggleMessages", false)
    val customTitle = setting("WindowTitle", true)
    private val autoSaving = setting("AutoSavingSettings", true)
    private val savingFeedBack = setting("SavingFeedBack", false, { autoSaving.value })
    private val savingInterval = setting("Interval(m)", 3, 1..10, 1, { autoSaving.value })
    val modifierEnabled = setting("ModifierEnabled", false, { false })

    private val timer = TickTimer(TimeUnit.MINUTES)
    private val prevTitle = Display.getTitle()
    private const val title = "${KamiMod.NAME} ${KamiMod.KAMI_KATAKANA} ${KamiMod.VERSION_SIMPLE}"

    init {
        listener<TickEvent.ClientTickEvent> {
            updateTitle()
        }

        BackgroundScope.launchLooping("Config Auto Saving", 60000L) {
            if (autoSaving.value && mc.currentScreen !is KamiClickGui && timer.tick(savingInterval.value.toLong())) {
                if (savingFeedBack.value) MessageSendHelper.sendChatMessage("Auto saving settings...")
                ConfigUtils.saveConfig(ModuleConfig)
            }
        }
    }

    private fun updateTitle() {
        if (customTitle.value) Display.setTitle(title)
        else Display.setTitle(prevTitle)
    }
}