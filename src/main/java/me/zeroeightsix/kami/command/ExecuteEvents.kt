package me.zeroeightsix.kami.command

import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.PlayerControllerMP
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.world.World
import org.kamiblue.command.ExecuteEvent

class ClientExecuteEvent(args: Array<String>) : ExecuteEvent(CommandManager, args) {
    val world = Wrapper.world
    val player = Wrapper.player
    val playerController: PlayerControllerMP? = Wrapper.minecraft.playerController
}

class SafeExecuteEvent(
    args: Array<String>,
    val world: WorldClient,
    val player: EntityPlayerSP,
    val playerController: PlayerControllerMP
) : ExecuteEvent(CommandManager, args)