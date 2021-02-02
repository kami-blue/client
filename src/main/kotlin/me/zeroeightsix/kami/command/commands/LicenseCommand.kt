package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.util.text.MessageSendHelper

object LicenseCommand : ClientCommand(
    name = "license",
    description = "Information about KAMI Blue's license"
) {
    init {
        execute {
            MessageSendHelper.sendChatMessage("You can view KAMI Blue's &7client&f License (LGPLv3) at &9https://kamiblue.org/license")
        }
    }
}