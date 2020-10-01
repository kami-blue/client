package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.module.modules.misc.FakePlayer
import me.zeroeightsix.kami.util.text.MessageSendHelper

class FakePlayerCommand : Command("fakeplayer", null, "fp") {
    override fun call(args: Array<out String?>) {
        args[0]?.let {
            FakePlayer.playerName.value = it
            MessageSendHelper.sendChatMessage("${FakePlayer.name.value} player name has been set to $it")
        } ?: MessageSendHelper.sendErrorMessage("Invalid name!")
    }
}