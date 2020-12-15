package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import net.minecraft.entity.Entity

object VanishCommand : ClientCommand(
    name = "vanish",
    description = "Allows you to vanish using an entity."
) {
    private var vehicle: Entity? = null

    init {
        executeSafe {
            if (player.ridingEntity != null && vehicle == null) {
                vehicle = mc.player.ridingEntity?.also {
                    player.dismountRidingEntity()
                    world.removeEntityFromWorld(it.entityId)
                    sendChatMessage("Vehicle " + it.name + " removed.")
                }
            } else {
                vehicle?.let {
                    vehicle!!.isDead = false
                    mc.world.addEntityToWorld(it.entityId, it)
                    mc.player.startRiding(it, true)
                    sendChatMessage("Vehicle " + vehicle!!.name + " created.")
                    vehicle = null
                } ?: sendChatMessage("No Vehicle.")
            }
        }
    }
}