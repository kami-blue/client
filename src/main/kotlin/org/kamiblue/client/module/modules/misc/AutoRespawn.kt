package org.kamiblue.client.module.modules.misc

import net.minecraft.client.gui.GuiGameOver
import org.kamiblue.client.event.events.GuiEvent
import org.kamiblue.client.manager.managers.WaypointManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.InfoCalculator
import org.kamiblue.client.util.math.CoordinateConverter.asString
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.event.listener.listener

internal object AutoRespawn : Module(
    name = "AutoRespawn",
    description = "Automatically respawn after dying",
    category = Category.MISC
) {
    private val respawn = setting("Respawn", true)
    private val deathCoords = setting("Save Death Coords", true)
    private val antiGlitchScreen = setting("Anti Glitch Screen", true)

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