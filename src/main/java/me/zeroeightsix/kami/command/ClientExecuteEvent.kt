package me.zeroeightsix.kami.command

import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.multiplayer.PlayerControllerMP
import org.kamiblue.command.ExecuteEvent

class ClientExecuteEvent(args: Array<String>) : ExecuteEvent(CommandManager, args) {

    val world = Wrapper.world
    val player = Wrapper.player
    val playerController: PlayerControllerMP? = Wrapper.minecraft.playerController

}