package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.gui.hudgui.KamiHudGui
import me.zeroeightsix.kami.module.Module

@Module.Info(
        name = "HudGUI",
        description = "Opens the Hud GUI",
        category = Module.Category.CLIENT,
        showOnArray = false,
        alwaysListening = true
)
object HudGUI : Module() {

    override fun onEnable() {
        if (mc.currentScreen !is KamiHudGui) {
            ClickGUI.disable()
            mc.displayGuiScreen(KamiHudGui)
            KamiEventBus.subscribe(KamiHudGui)
            KamiHudGui.onDisplayed()
        }
    }

    override fun onDisable() {
        if (mc.currentScreen is KamiHudGui) {
            mc.displayGuiScreen(null)
            KamiEventBus.unsubscribe(KamiHudGui)
        }
    }

}
