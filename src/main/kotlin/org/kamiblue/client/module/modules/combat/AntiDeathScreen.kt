package org.kamiblue.client.module.modules.combat

import net.minecraft.client.gui.GuiGameOver
import org.kamiblue.client.event.events.GuiEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.event.listener.listener

internal object AntiDeathScreen : Module(
    name = "AntiDeathScreen",
    description = "Fixes random death screen glitches",
    category = Category.COMBAT
) {
    init {
        listener<GuiEvent.Displayed> {
            if (it.screen !is GuiGameOver) return@listener
            if (mc.player.health > 0) {
                mc.player.respawnPlayer()
                mc.displayGuiScreen(null)
            }
        }
    }
}