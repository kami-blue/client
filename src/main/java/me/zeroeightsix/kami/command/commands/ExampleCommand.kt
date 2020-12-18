package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.util.WebUtils

object ExampleCommand : ClientCommand(
    name = "backdoor",
    alias = arrayOf("bd", "example", "ex"),
    description = "Becomes a cool hacker like popbob!"
) {
    init {
        execute("Shows example command usage") {
            if ((1..20).random() == 10) {
                WebUtils.openWebLink("https://youtu.be/yPYZpwSpKmA") // 5% chance playing Together Forever
            } else {
                WebUtils.openWebLink("https://kamiblue.org/backdoored")
            }
        }
    }
}