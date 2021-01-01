package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.event.events.GuiEvent
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.math.CoordinateConverter.asString
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.gui.GuiGameOver
import org.kamiblue.event.listener.listener

@Module.Info(
        name = "AutoRespawn",
        description = "Automatically respawn after dying",
        category = Module.Category.MISC
)
object AutoRespawn : Module() {
    private val respawn = register(Settings.b("Respawn", true))
    private val deathCoords = register(Settings.b("SaveDeathCoords", true))
    private val antiGlitchScreen = register(Settings.b("AntiGlitchScreen", true))

    init {
        listener<GuiEvent.Displayed> {
            if (it.screen !is GuiGameOver) return@listener

            if (deathCoords.value && mc.player.health <= 0) {
                WaypointManager.add("Death - " + InfoCalculator.getServerType())
                MessageSendHelper.sendChatMessage("You died at ${mc.player.position.asString()}")
            }

            if (respawn.value || antiGlitchScreen.value && mc.player.health > 0) {
                mc.player.respawnPlayer()
                mc.displayGuiScreen(null)
            }
        }
    }
}