package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.event.events.ShutdownEvent
import me.zeroeightsix.kami.gui.hudgui.KamiHudGui
import me.zeroeightsix.kami.module.Module
import org.kamiblue.event.listener.listener

@Module.Info(
    name = "HudEditor",
    description = "Edits the Hud",
    category = Module.Category.CLIENT,
    showOnArray = false
)
object HudEditor : Module() {
    override fun onEnable() {
        if (mc.currentScreen !is KamiHudGui) {
            ClickGUI.disable()
            mc.displayGuiScreen(KamiHudGui)
            KamiHudGui.onDisplayed()
        }
    }

    override fun onDisable() {
        if (mc.currentScreen is KamiHudGui) {
            mc.displayGuiScreen(null)
        }
    }

    init {
        listener<ShutdownEvent> {
            disable()
        }
    }
}
