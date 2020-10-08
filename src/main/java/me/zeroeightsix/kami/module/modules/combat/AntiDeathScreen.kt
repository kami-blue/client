package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.GuiScreenEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.client.gui.GuiGameOver

@Module.Info(
        name = "AntiDeathScreen",
        description = "Fixes random death screen glitches",
        category = Module.Category.COMBAT
)
object AntiDeathScreen : Module() {
    init {
        listener<GuiScreenEvent.Displayed> {
            if (it.screen !is GuiGameOver) return@listener
            if (mc.player.health > 0) {
                mc.player.respawnPlayer()
                mc.displayGuiScreen(null)
            }
        }
    }
}