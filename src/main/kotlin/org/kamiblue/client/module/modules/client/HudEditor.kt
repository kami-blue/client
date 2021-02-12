package org.kamiblue.client.module.modules.client

import org.kamiblue.client.event.events.ShutdownEvent
import org.kamiblue.client.gui.hudgui.KamiHudGui
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
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
