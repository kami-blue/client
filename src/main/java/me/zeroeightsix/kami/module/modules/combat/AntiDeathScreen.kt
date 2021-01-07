package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.GuiEvent
import me.zeroeightsix.kami.module.Module
import net.minecraft.client.gui.GuiGameOver
import org.kamiblue.event.listener.listener

object AntiDeathScreen : Module(
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