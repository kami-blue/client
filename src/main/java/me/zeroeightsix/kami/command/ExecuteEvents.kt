package me.zeroeightsix.kami.command

import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.PlayerControllerMP
import net.minecraft.client.multiplayer.WorldClient
import org.kamiblue.command.ExecuteEvent
import org.kamiblue.command.IExecuteEvent

abstract class AbstractClientEvent {
    val mc = Wrapper.minecraft
    abstract val world: WorldClient?
    abstract val player: EntityPlayerSP?
    abstract val playerController: PlayerControllerMP?
}

open class ClientEvent : AbstractClientEvent() {
    final override val world: WorldClient? = mc.world
    final override val player: EntityPlayerSP? = mc.player
    final override val playerController: PlayerControllerMP? = mc.playerController
}

open class SafeClientEvent(
    override val world: WorldClient,
    override val player: EntityPlayerSP,
    override val playerController: PlayerControllerMP
) : AbstractClientEvent()

class ClientExecuteEvent(
    args: Array<String>
) : ClientEvent(), IExecuteEvent by ExecuteEvent(CommandManager, args)

class SafeExecuteEvent(
    args: Array<String>,
    world: WorldClient,
    player: EntityPlayerSP,
    playerController: PlayerControllerMP
) : SafeClientEvent(world, player, playerController), IExecuteEvent by ExecuteEvent(CommandManager, args)

fun ClientEvent.toSafe() =
    if (world != null && player != null && playerController != null) SafeClientEvent(world, player, playerController)
    else null

fun ClientExecuteEvent.toSafe() =
    if (world != null && player != null && playerController != null) SafeExecuteEvent(args, world, player, playerController)
    else null