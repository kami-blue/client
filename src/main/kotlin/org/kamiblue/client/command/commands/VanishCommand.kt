package org.kamiblue.client.command.commands

import net.minecraft.entity.Entity
import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.util.text.MessageSendHelper.sendChatMessage
import org.kamiblue.client.util.text.formatValue

object VanishCommand : ClientCommand(
    name = "vanish",
    description = "Allows you to vanish using an entity."
) {
    private var vehicle: Entity? = null

    init {
        executeSafe {
            if (player.ridingEntity != null && vehicle == null) {
                vehicle = player.ridingEntity?.also {
                    player.dismountRidingEntity()
                    world.removeEntityFromWorld(it.entityId)
                    sendChatMessage("Vehicle " + formatValue(it.name) + " removed")
                }
            } else {
                vehicle?.let {
                    it.isDead = false
                    world.addEntityToWorld(it.entityId, it)
                    player.startRiding(it, true)
                    sendChatMessage("Vehicle " + formatValue(it.name) + " created")
                    vehicle = null
                } ?: sendChatMessage("Not riding any vehicles")
            }
        }
    }
}