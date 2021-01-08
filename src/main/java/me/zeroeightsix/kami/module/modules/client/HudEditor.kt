package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.event.events.ShutdownEvent
import me.zeroeightsix.kami.gui.hudgui.KamiHudGui
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import org.kamiblue.event.listener.listener

internal object HudEditor : Module(
    name = "HudEditor",
    description = "Edits the Hud",
    category = Category.CLIENT,
    showOnArray = false
) {
    init {
        onEnable {
            if (mc.currentScreen !is KamiHudGui) {
                ClickGUI.disable()
                mc.displayGuiScreen(KamiHudGui)
                KamiHudGui.onDisplayed()
            }
        }

        onDisable {
            if (mc.currentScreen is KamiHudGui) {
                mc.displayGuiScreen(null)
            }
        }

        listener<ShutdownEvent> {
            disable()
        }
    }
}
