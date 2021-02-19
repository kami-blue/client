package org.kamiblue.client.event

import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.PlayerControllerMP
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.network.NetHandlerPlayClient
import org.kamiblue.client.command.CommandManager
import org.kamiblue.client.util.Wrapper
import org.kamiblue.command.execute.ExecuteEvent
import org.kamiblue.command.execute.IExecuteEvent

abstract class AbstractClientEvent : Event {
    val mc = Wrapper.minecraft
    abstract val world: WorldClient?
    abstract val player: EntityPlayerSP?
    abstract val playerController: PlayerControllerMP?
    abstract val connection: NetHandlerPlayClient?
}

open class ClientEvent : AbstractClientEvent() {
    final override val world: WorldClient? = mc.world
    final override val player: EntityPlayerSP? = mc.player
    final override val playerController: PlayerControllerMP? = mc.playerController
    final override val connection: NetHandlerPlayClient? = mc.connection
}

open class SafeClientEvent internal constructor(
    override val world: WorldClient,
    override val player: EntityPlayerSP,
    override val playerController: PlayerControllerMP,
    override val connection: NetHandlerPlayClient
) : AbstractClientEvent()

class ClientExecuteEvent(
    args: Array<String>
) : ClientEvent(), IExecuteEvent by ExecuteEvent(CommandManager, args)

class SafeExecuteEvent internal constructor(
    world: WorldClient,
    player: EntityPlayerSP,
    playerController: PlayerControllerMP,
    connection: NetHandlerPlayClient,
    event: ClientExecuteEvent
) : SafeClientEvent(world, player, playerController, connection), IExecuteEvent by event