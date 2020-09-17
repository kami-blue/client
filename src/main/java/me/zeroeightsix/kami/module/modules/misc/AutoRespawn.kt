package me.zeroeightsix.kami.module.modules.misc

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.GuiScreenEvent.Displayed
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.Waypoint
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.gui.GuiGameOver

@Module.Info(
        name = "AutoRespawn",
        description = "Automatically respawn after dying",
        category = Module.Category.MISC
)
object AutoRespawn : Module() {
    private val respawn = register(Settings.b("Respawn", true))
    private val deathCoords = register(Settings.b("SaveDeathCoords", true))
    private val antiGlitchScreen = register(Settings.b("AntiGlitchScreen", true))

    @EventHandler
    private val listener = Listener(EventHook { event: Displayed ->
        if (event.screen !is GuiGameOver) return@EventHook

        if (deathCoords.value && mc.player.health <= 0) {
            Waypoint.writePlayerCoords("Death - " + InfoCalculator.getServerType())
            MessageSendHelper.sendChatMessage(String.format("You died at x %d y %d z %d",
                    mc.player.posX.toInt(),
                    mc.player.posY.toInt(),
                    mc.player.posZ.toInt())
            )
        }

        if (respawn.value || antiGlitchScreen.value && mc.player.health > 0) {
            mc.player.respawnPlayer()
            mc.displayGuiScreen(null)
        }
    })
}