package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.GuiScreenEvent.Displayed
import me.zeroeightsix.kami.module.Module
import net.minecraft.client.gui.GuiGameOver

@Module.Info(
        name = "AntiDeathScreen",
        description = "Fixes random death screen glitches",
        category = Module.Category.COMBAT
)
object AntiDeathScreen : Module() {
    @EventHandler
    private val listener = Listener(EventHook { event: Displayed ->
        if (event.screen !is GuiGameOver) return@EventHook
        if (mc.player.health > 0) {
            mc.player.respawnPlayer()
            mc.displayGuiScreen(null)
        }
    })
}