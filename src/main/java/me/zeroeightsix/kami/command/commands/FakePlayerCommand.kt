package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.command.CommandOld
import me.zeroeightsix.kami.module.modules.misc.FakePlayer
import me.zeroeightsix.kami.util.text.MessageSendHelper

object FakePlayerCommand : ClientCommand(
    name = "fakeplayer",
    alias = arrayOf("fp"),
) {
    init {
        string("name") { nameArg ->
            execute {
                val name = nameArg.value
                FakePlayer.playerName.value = name
                MessageSendHelper.sendChatMessage("${FakePlayer.name.value} player name has been set to $name")
            }
        }
    }
}