package org.kamiblue.client.event.events

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MovementInput

class InputUpdateEvent(val player: EntityPlayer, val movementInput: MovementInput)